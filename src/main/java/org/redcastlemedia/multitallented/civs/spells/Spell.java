package org.redcastlemedia.multitallented.civs.spells;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;
import org.redcastlemedia.multitallented.civs.spells.effects.Effect;
import org.redcastlemedia.multitallented.civs.spells.targets.Target;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Spell {
    private final Player caster;
    @Getter
    private final String type;
    private final int level;
    @Getter
    private final Map<String, Map<Object, Map<String, Double>>> abilityVariables;

    public Spell(String type, Player caster, int level) {
        this.type = type;
        this.caster = caster;
        this.level = level;
        this.abilityVariables = new HashMap<>();
    }

    public boolean useAbility() {
        SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(type);
        FileConfiguration config = spellType.getConfig();
        HashMap<String, Set<?>> mappedTargets = new HashMap<>();
        HashSet<LivingEntity> tempSet = new HashSet<>();
        tempSet.add(caster);
        mappedTargets.put(SpellConstants.SELF, tempSet);

        return useAbility(mappedTargets, false, config);
    }

    private boolean isFailingOrMapTargets(ConfigurationSection config, Map<String, Set<?>> mappedTargets) {
        ConfigurationSection targetSections = config.getConfigurationSection("targets");
        if (targetSections == null) {
            return false;
        }
        for (String key : targetSections.getKeys(false)) {
            ConfigurationSection targetSection = targetSections.getConfigurationSection(key);
            String targetName = targetSection.getString("type", "nearby");
            Target target = SpellType.getTarget(targetName, key, targetSection, level, caster, this);
            try {
                mappedTargets.put(key, target.getTargets());
            } catch (NullPointerException npe) {
                Civs.logger.severe("Failed to find target " + targetName + " in spell " + type);
                return true;
            }
            if (targetSection.getBoolean("cancel-if-empty", false) && mappedTargets.get(key).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isFailingConditions(ConfigurationSection config, HashMap<String, Set<?>> mappedTargets) {
        ConfigurationSection conditionsConfig = config.getConfigurationSection("conditions");
        if (conditionsConfig == null) {
            return false;
        }
        for (String key : conditionsConfig.getKeys(false)) {
            String conditionName = "";
            if (!key.contains("^")) {
                conditionName += key;
            } else {
                conditionName = key.split("\\^")[0];
            }
            String costValueString = conditionsConfig.getString(key, SpellConstants.NOT_A_STRING);
            boolean invert = key.endsWith("^not");
            boolean isSection = costValueString.equals(SpellConstants.NOT_A_STRING) || costValueString.contains("MemorySection");
            String targetKey = SpellConstants.SELF;
            if (isSection) {
                String tempTarget = conditionsConfig.getConfigurationSection(key).getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
                if (!tempTarget.equals(SpellConstants.NOT_A_STRING)) {
                    targetKey = tempTarget;
                }
            }
            Set<?> targetSet = mappedTargets.get(targetKey);
            if (targetSet == null || targetSet.isEmpty()) {
                continue;
            }
            for (Object target : targetSet) {
                Effect component;
                if (!costValueString.equals(SpellConstants.NOT_A_STRING) && !costValueString.contains("MemorySection")) {
                    component = SpellType.getEffect(conditionName, key, costValueString, level, target, caster, this);
                } else {
                    ConfigurationSection currentConfigSection = conditionsConfig.getConfigurationSection(key);
                    component = SpellType.getEffect(conditionName, key, currentConfigSection, level, target, caster, this);
                }
                boolean meetsRequirement;
                try {
                    meetsRequirement = component.meetsRequirement();
                } catch (NullPointerException npe) {
                    Civs.logger.severe("Failed to find component " + conditionName + " in spell " + type);
                    meetsRequirement = false;
                }
                if ((!meetsRequirement && !invert) || (meetsRequirement && invert)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean useAbilityFromListener(ConfigurationSection useSection, Object newTarget, String key, Map<String, Set<?>> mappedTargets) {
        HashSet<Object> tempTarget = new HashSet<>();
        tempTarget.add(newTarget);
        mappedTargets.put(key, tempTarget);

        boolean ableToUseAbility = useAbility(mappedTargets, true, useSection);

        return !ableToUseAbility || useSection.getBoolean("cancel", false);
    }

    public boolean useAbility(Map<String, Set<?>> incomingTargets,
                              boolean delayed,
                              ConfigurationSection abilitySection) {
        HashMap<String, Set<?>> mappedTargets = new HashMap<>(incomingTargets);

        if (isFailingConditions(abilitySection, mappedTargets)) {
            return false;
        }

        if (isFailingOrMapTargets(abilitySection, mappedTargets)) {
            return false;
        }
        ConfigurationSection componentSection = abilitySection.getConfigurationSection("components");
        if (componentSection == null) {
            return false;
        }
        HashSet<String> fulfilledRequirements = new HashSet<>();
        for (String componentName : componentSection.getKeys(false)) {

            ConfigurationSection currentComponent = componentSection.getConfigurationSection(componentName);
            if (currentComponent == null) {
                continue;
            }
            filterTargets(mappedTargets, currentComponent);

            createVariables(mappedTargets, currentComponent);

            boolean costsMet = isCostsMet(mappedTargets, fulfilledRequirements, componentName, currentComponent);

            //yield
            ConfigurationSection yieldSection = currentComponent.getConfigurationSection("yield");

            if (yieldSection == null || !costsMet) {
                continue;
            }
            produceYield(delayed, mappedTargets, yieldSection);
        }

        if (fulfilledRequirements.isEmpty()) {
            return false;
        }

        sendCastMessage(delayed, mappedTargets);
        return true;
    }

    private void sendCastMessage(boolean delayed, HashMap<String, Set<?>> mappedTargets) {
        if (!delayed) {
            caster.sendMessage(getSpellCastMessage(caster));
            for (Map.Entry<String, Set<?>> entry : mappedTargets.entrySet()) {
                if (entry.getKey().equals(SpellConstants.SELF)) {
                    continue;
                }
                for (Object obj : entry.getValue()) {
                    if (!(obj instanceof Player)) {
                        continue;
                    }
                    Player cPlayer = (Player) obj;
                    cPlayer.sendMessage(getSpellCastMessage(cPlayer));
                }
            }
        }
    }

    public String getSpellCastMessage(Player player) {
        CivItem civItem = ItemManager.getInstance().getItemType(type);
        String localSpellName = civItem.getDisplayName(player);
        return Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                "spell-cast").replace("$1", caster.getDisplayName())
                .replace("$2", localSpellName);
    }

    private void produceYield(boolean delayed, HashMap<String, Set<?>> mappedTargets, ConfigurationSection yieldSection) {
        for (String key : yieldSection.getKeys(false)) {
            String yieldName = "";
            if (!key.contains("^")) {
                yieldName += key;
            } else {
                yieldName = key.split("\\^")[0];
            }

            if (createDamageListener(yieldSection, key, yieldName, mappedTargets)) {
                continue;
            }

            if (createDurationYield(delayed, mappedTargets, yieldSection, key, yieldName)) {
                continue;
            }

            if (createProjectileListener(yieldName, key, yieldSection, mappedTargets)) {
                continue;
            }

            String yieldValueString = yieldSection.getString(key, SpellConstants.NOT_A_STRING);
            Effect component;
            boolean isString = !yieldValueString.equals(SpellConstants.NOT_A_STRING) && !yieldValueString.contains("MemorySection");


            String targetKey = isString ? SpellConstants.SELF : yieldSection.getConfigurationSection(key).getString(SpellConstants.TARGET, SpellConstants.SELF);
            Set<?> targetSet = mappedTargets.get(targetKey);

            if (targetSet == null || targetSet.isEmpty()) {
                continue;
            }
            for (Object target : targetSet) {
                if (isString) {
                    component = SpellType.getEffect(yieldName, key, yieldValueString, level, target, caster, this);
                } else {
                    component = SpellType.getEffect(yieldName, key, yieldSection.getConfigurationSection(key), level, target, caster, this);
                }
                if (component != null) {
                    component.apply();
                }
            }
        }
    }

    private boolean createProjectileListener(String yieldName, String key, ConfigurationSection yieldSection, HashMap<String, Set<?>> mappedTargets) {
        if (!yieldName.equalsIgnoreCase("projectile")) {
            return false;
        }
        final ConfigurationSection projectileSection = yieldSection.getConfigurationSection(key);
        EntityType entityType = EntityType.valueOf(projectileSection.getString("projectile", "ARROW"));
        double speed = getLevelAdjustedValue("" + projectileSection.getString("speed", "0.5"), level, caster, this);
        double spread = getLevelAdjustedValue("" + projectileSection.getString("spread", "20"), level, caster, this);
        int amount = (int) Math.round(getLevelAdjustedValue("" + projectileSection.getString("amount", "1"), level, caster, this));

        String targetKey = projectileSection.getString(SpellConstants.TARGET, SpellConstants.SELF);
        for (Object target : mappedTargets.get(targetKey)) {
            if (target instanceof LivingEntity) {
                launchProjectiles((LivingEntity) target, (Class<Projectile>) entityType.getEntityClass(),
                        speed, amount, spread, projectileSection, key, mappedTargets);
            }
        }

        return true;
    }

    private void launchProjectiles(LivingEntity livingEntity, Class<Projectile> projectileType, double speed, int amount, double spread,
                                   ConfigurationSection projectileSection, String key, Map<String, Set<?>> mappedTargets) {
        final double PHI = -livingEntity.getLocation().getPitch();
        final double THETA = -livingEntity.getLocation().getYaw();
        for (int i = 0; i < amount; i++) {
            double phi = (PHI + (Math.random() - 0.5) * spread)
                    / 180 * Math.PI;
            double theta = (THETA + (Math.random() - 0.5) * spread)
                    / 180 * Math.PI;
            Projectile pr = livingEntity.launchProjectile(
                    projectileType,
                    new Vector(Math.cos(phi) * Math.sin(theta),
                            Math.sin(phi), Math.cos(phi)
                            * Math.cos(theta)));
            if (pr instanceof Arrow) {
                Arrow arrow = (Arrow) pr;
                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            }
            pr.setVelocity(livingEntity.getLocation().getDirection()
                    .multiply(5));
            Vector direction = livingEntity.getLocation().getDirection()
                    .normalize();
            pr.setVelocity(direction.multiply(speed));
            pr.setShooter(livingEntity);
            if (projectileSection.isSet("section")) {
                SpellListener.getInstance().addProjectileListener(pr, level, projectileSection.getConfigurationSection("section"), this, key, caster, mappedTargets);
            }
        }
    }

    private boolean createDamageListener(ConfigurationSection yieldSection, String key, String yieldName, HashMap<String, Set<?>> mappedTargets) {
        if (!yieldName.equalsIgnoreCase("damage-listener")) {
            return false;
        }
        final ConfigurationSection damageListenerSection = yieldSection.getConfigurationSection(key);

        long delay = Math.round(getLevelAdjustedValue("" + damageListenerSection.getLong("delay", 0), level, caster, this));
        long ticks = Math.round(getLevelAdjustedValue("" + damageListenerSection.getLong("ticks", 0), level, caster, this));
        final Player finalCaster = caster;
        final int finalLevel = level;
        int delayId = -1;
        int durationId = -1;
        final Civilian finalChampion = CivilianManager.getInstance().getCivilian(finalCaster.getUniqueId());
        final String finalName = type;
        final String finalKey = key;
        final Spell spell = this;
        final String finalYieldName = yieldName;

        String targetKey = damageListenerSection.getString(SpellConstants.TARGET, SpellConstants.SELF);
        Set<?> targetSet = mappedTargets.get(targetKey);
        for (Object target : targetSet) {
            if (!(target instanceof LivingEntity)) {
                continue;
            }
            if (ticks > 0) {
                durationId = Bukkit.getScheduler().runTaskLater(Civs.getInstance(), () -> {
                    SpellListener.getInstance().removeDamageListener((LivingEntity) target);
                    finalChampion.getStates().remove(finalName + "." + finalKey);
                }, delay + ticks).getTaskId();
            }
            if (delayId < -1) {
                Bukkit.getScheduler().runTaskLater(Civs.getInstance(), () -> SpellListener.getInstance().addDamageListener((LivingEntity) target, finalLevel,
                        damageListenerSection.getConfigurationSection("section"), spell, finalCaster, key, mappedTargets), delay);
            } else {
                SpellListener.getInstance().addDamageListener((LivingEntity) target, level,
                        damageListenerSection.getConfigurationSection("section"), spell, caster, key, mappedTargets);
                HashMap<String, Object> listenerVars = new HashMap<>();

                CivState state = new CivState(this, finalYieldName, durationId, -1,
                        damageListenerSection.getConfigurationSection("section"), listenerVars);
                finalChampion.getStates().put(finalName + "." + finalKey, state);
            }
        }

        return true;
    }

    private boolean createDurationYield(boolean delayed, HashMap<String, Set<?>> mappedTargets, ConfigurationSection yieldSection, String key, String yieldName) {
        if (!yieldName.equalsIgnoreCase("duration")) {
            return false;
        }
        if (delayed) {
            return true;
        }
        final ConfigurationSection durationSection = yieldSection.getConfigurationSection(key);
        long delay = Math.round(getLevelAdjustedValue("" + durationSection.getLong("delay", 0), level, caster, this));
        long ticks = Math.round(getLevelAdjustedValue("" + durationSection.getLong("ticks", 0), level, caster, this));
        long period = Math.round(getLevelAdjustedValue("" + durationSection.getLong("period", 0), level, caster, this));
        int durationId = -1;
        int periodId = -1;
        final Player finalCaster = caster;
        final HashMap<String, Set<?>> finalMappedTargets = mappedTargets;
        final String finalName = type;
        final String finalKey = key;
        final HashMap<String, ConfigurationSection> durationAbilities = new HashMap<>();
        final ConfigurationSection durationSectionEffects = durationSection.getConfigurationSection("section");
        final HashMap<String, ConfigurationSection> mappedDurationTargets = new HashMap<>();
        ConfigurationSection newTargets = durationSection.getConfigurationSection("targets");
        if (newTargets != null) {
            for (String durationKey : durationSection.getConfigurationSection("targets").getKeys(false)) {
                mappedDurationTargets.put(durationKey, durationSection.getConfigurationSection("targets." + durationKey));
            }
        }
        if (period > 0) {
            periodId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(), () -> useAbility(finalMappedTargets, true, durationSectionEffects), delay, period);
        }
        if (delay > 0 && period < 1) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), () -> useAbility(finalMappedTargets, true, durationSectionEffects), delay + ticks);
        } else {
            useAbility(mappedTargets, true, durationSectionEffects);
        }

        final int finalPeriodId = periodId;
        if (ticks > 0) {
            durationId = Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), () -> {
                removeAbility(finalMappedTargets, durationAbilities);
                Bukkit.getScheduler().cancelTask(finalPeriodId);
                Civilian champion = CivilianManager.getInstance().getCivilian(finalCaster.getUniqueId());
                CivState state = champion.getStates().get(finalName + "." + finalKey);
                if (state != null) {
                    state.remove(finalCaster);
                    champion.getStates().remove(finalName + "." + finalKey);
                }
            }, delay + ticks);
        }

        Civilian civilian = CivilianManager.getInstance().getCivilian(caster.getUniqueId());
        civilian.getStates().put(type + "." + key, new CivState(this, key, durationId, periodId, new HashMap<>()));

        return true;
    }

    protected boolean isCostsMet(HashMap<String, Set<?>> mappedTargets, HashSet<String> fulfilledRequirements, String componentName, ConfigurationSection currentComponent) {
        ConfigurationSection costSection = currentComponent.getConfigurationSection("costs");
        boolean costsMet = true;
        if (costSection == null) {
            fulfilledRequirements.add(componentName);
            return true;
        }
        costLoop: for (String key : costSection.getKeys(false)) {
            String costName = "";
            if (!key.contains("^")) {
                costName += key;
            } else {
                costName = key.split("\\^")[0];
            }
            boolean invert = key.endsWith("^not");
            String costValueString = costSection.getString(key, SpellConstants.NOT_A_STRING);
            Effect component;

            if (costName.equals("inherit")) {
                boolean inheritFullfilled = fulfilledRequirements.contains(costValueString);
                if ((!invert && !inheritFullfilled) || (invert && inheritFullfilled)) {
                    Civs.logger.info(key + " cost not met");
                    costsMet = false;
                    break;
                }
                continue;
            }
            boolean isString = !costValueString.equals(SpellConstants.NOT_A_STRING) && !costValueString.contains("MemorySection");
            String targetKey = isString ? SpellConstants.SELF : costSection.getConfigurationSection(key).getString(SpellConstants.TARGET, SpellConstants.SELF);
            Set<?> targetSet = mappedTargets.get(targetKey);
            if (targetSet == null || targetSet.isEmpty()) {
                costsMet = invert;
                break;
            }
            if (costName.equals("value")) {
                for (Object target : targetSet) {
                    String value;
                    if (isString) {
                        value = replaceAllVariables(costValueString, level, target, this);
                    } else {
                        ConfigurationSection currentConfigSection = costSection.getConfigurationSection(key);
                        value = replaceAllVariables(currentConfigSection.getString("value", ""), level, target, this);
                    }

                    boolean meetsRequirement = !value.isEmpty() && !value.equals("0") && !value.equals("false");
                    if ((!meetsRequirement && !invert) || (meetsRequirement && invert)) {
                        costsMet = false;
                        break costLoop;
                    }
                }
                continue;
            }
            for (Object target : targetSet) {
                if (isString) {
                    component = SpellType.getEffect(costName, key, costValueString, level, target, caster, this);
                } else {
                    ConfigurationSection currentConfigSection = costSection.getConfigurationSection(key);
                    component = SpellType.getEffect(costName, key, currentConfigSection, level, target,  caster,this);
                }

                boolean meetsRequirement = component.meetsRequirement();
                if ((!meetsRequirement && !invert) || (meetsRequirement && invert)) {
                    costsMet = false;
                    break costLoop;
                }
            }
        }

        if (costsMet) {
            fulfilledRequirements.add(componentName);
        }
        return costsMet;
    }

    protected void createVariables(HashMap<String, Set<?>> mappedTargets, ConfigurationSection currentComponent) {
        ConfigurationSection varSection = currentComponent.getConfigurationSection("variables");
        if (varSection == null) {
            return;
        }
        for (String key : varSection.getKeys(false)) {
            String varName = "";
            if (!key.contains("^")) {
                varName += key;
            } else {
                varName = key.split("\\^")[0];
            }
            String varValueString = varSection.getString(key, SpellConstants.NOT_A_STRING);
            Effect component;
            boolean isSection = varValueString.equals(SpellConstants.NOT_A_STRING) || varValueString.contains("MemorySection");
            String targetKey = SpellConstants.SELF;
            ConfigurationSection configurationSection;
            if (isSection) {
                configurationSection = varSection.getConfigurationSection(key);
                String tempTarget = configurationSection.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
                if (!tempTarget.equals(SpellConstants.NOT_A_STRING)) {
                    targetKey = tempTarget;
                }
            }
            Set<?> targetSet = mappedTargets.get(targetKey);
            if (targetSet == null || targetSet.isEmpty()) {
                continue;
            }

            Map<Object, Map<String, Double>> currentComponentVars = new HashMap<>();
            for (Object target : targetSet) {
                if (!varValueString.equals(SpellConstants.NOT_A_STRING) && !varValueString.contains("MemorySection")) {
                    component = SpellType.getEffect(varName, key, varValueString, level, target, caster, this);
                } else {
                    ConfigurationSection currentConfigSection = varSection.getConfigurationSection(key);
                    component = SpellType.getEffect(varName, key, currentConfigSection, level, target, caster, this);
                }
                HashMap<String, Double> currentVars;
                if (component != null) {
                    currentVars = component.getVariables(target, caster, level, this);
                } else {
                    currentVars = new HashMap<>();
                    if (!isSection) {
                        currentVars.put("var", getLevelAdjustedValue(varValueString, level, target, this));
                    }
                }
                currentComponentVars.put(target, currentVars);
            }
            abilityVariables.put(key, currentComponentVars);
        }
    }

    private void filterTargets(HashMap<String, Set<?>> mappedTargets, ConfigurationSection currentComponent) {
        ConfigurationSection filterSection = currentComponent.getConfigurationSection("filters");
        if (filterSection == null) {
            return;
        }
        for (String key : filterSection.getKeys(false)) {
            String filterName = "";
            if (!key.contains("^")) {
                filterName += key;
            } else {
                filterName = key.split("\\^")[0];
            }
            String filterValueString = filterSection.getString(filterName, SpellConstants.NOT_A_STRING);
            Effect component;
            boolean invert = key.endsWith("^not");

            boolean isSection = filterValueString.equals(SpellConstants.NOT_A_STRING) || filterValueString.contains("MemorySection");
            String targetKey = SpellConstants.SELF;
            if (isSection) {
                String tempTarget = filterSection.getConfigurationSection(key).getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
                if (!tempTarget.equals(SpellConstants.NOT_A_STRING)) {
                    targetKey = tempTarget;
                }
            }
            Set<?> targetSet = mappedTargets.get(targetKey);
            if (targetSet == null || targetSet.isEmpty()) {
                continue;
            }
            for (Object target : new HashSet<>(targetSet)) {
                if (!filterValueString.equals(SpellConstants.NOT_A_STRING) && !filterValueString.contains("MemorySection")) {
                    component = SpellType.getEffect(filterName, key, filterValueString, level, target, caster, this);
                } else {
                    ConfigurationSection currentConfigSection = filterSection.getConfigurationSection(key);
                    component = SpellType.getEffect(filterName, key, currentConfigSection, level, target, caster, this);
                }
                boolean meetsRequirement = component.meetsRequirement();
                if ((!meetsRequirement && !invert) || (meetsRequirement && invert)) {
                    targetSet.remove(target);
                }
            }
        }
    }

    public boolean removeAbility(HashMap<String, Set<?>> mappedTargets,
                                 Map<String, ConfigurationSection> componentMap) {
        //TODO do I really need this?
        return true;
    }

    public static double getLevelAdjustedValue(String configString, int level, Object target, Spell spell) {
        if (configString.equals("0")) {
            return 0;
        }
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            Object engineEval = engine.eval(replaceAllVariables(configString, level, target, spell));
            if (engineEval instanceof Integer) {
                return (Integer) engineEval;
            } else if (engineEval instanceof Double) {
                return (Double) engineEval;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            return Double.parseDouble(configString);
        } catch (Exception e) {
            return 0;
        }
    }
    private static String replaceAllVariables(String input, int level, Object target, Spell spell) {
        input = input.replace("$level$", "" + level);
        input = input.replace("$rand$", "" + Math.random());
        String[] inputParts = input.split("\\$");
        if (spell != null && target != null) {
            input = "";
            Map<String, Map<Object, Map<String, Double>>> abilityVariables = spell.getAbilityVariables();
            StringBuilder inputBuilder = new StringBuilder(input);
            for (int i = 0; i < inputParts.length; i++) {
                if (inputParts[i].contains("#")) {
                    Map<String, Map<Object, Map<String, Double>>> variables = new HashMap<>(abilityVariables);
                    Map<Object, Map<String, Double>> targetVars = variables.get(inputParts[i].split("#")[0]);
                    if (targetVars == null) {
                        continue;
                    }
                    Map<String, Double> componentVars = targetVars.get(target);
                    if (componentVars == null) {
                        if (targetVars.isEmpty()) {
                            continue;
                        } else {
                            componentVars = targetVars.values().iterator().next();
                        }
                    }
                    Double var = componentVars.get(inputParts[i].split("#")[1]);
                    if (var == null) {
                        continue;
                    }
                    inputParts[i] = "" + var;
                    if (inputParts[i] == null || inputParts[i].equals("")) {
                        inputParts[i] = "0";
                    }
                }
                inputBuilder.append(inputParts[i]);
            }
            input = inputBuilder.toString();
        }
        return input;
    }

    public static void addSelfToTargetMapping(Map<String, Set<?>> mappedTargets, Player self) {
        Set<Object> targetSet = new HashSet<>();
        targetSet.add(self);
        mappedTargets.put(SpellConstants.SELF, targetSet);
    }
}
