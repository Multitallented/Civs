package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;
import org.redcastlemedia.multitallented.civs.spells.effects.Effect;
import org.redcastlemedia.multitallented.civs.spells.targets.Target;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;

import lombok.Getter;

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

    public boolean useAbilityFromListener(Player caster, ConfigurationSection useSection, Object newTarget) {
        HashMap<String, Set<?>> mappedTargets = new HashMap<>();
        HashSet<LivingEntity> tempSet = new HashSet<>();
        tempSet.add(caster);
        mappedTargets.put(SpellConstants.SELF, tempSet);
        HashSet<Object> tempTarget = new HashSet<>();
        tempTarget.add(newTarget);
        mappedTargets.put(SpellConstants.TARGET, tempTarget);

        useAbility(mappedTargets, true, useSection);

        return useSection.getBoolean("cancel", false);
    }

    public boolean useAbility(Map<String, Set<?>> incomingTargets,
                              boolean delayed,
                              ConfigurationSection abilitySection) {
        SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(type);
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
            produceYield(delayed, spellType, mappedTargets, yieldSection);
        }

        if (fulfilledRequirements.isEmpty()) {
            return false;
        }

        if (!delayed) {
            caster.sendMessage(getSpellCastMessage(caster));
            for (String key : mappedTargets.keySet()) {
                if (key.equals(SpellConstants.SELF)) {
                    continue;
                }
                for (Object obj : mappedTargets.get(key)) {
                    if (!(obj instanceof Player)) {
                        continue;
                    }
                    Player cPlayer = (Player) obj;
                    cPlayer.sendMessage(getSpellCastMessage(cPlayer));
                }
            }
        }
        return true;
    }

    public String getSpellCastMessage(Player player) {
        String localSpellName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                type + LocaleConstants.NAME_SUFFIX);
        return Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                "spell-cast").replace("$1", caster.getDisplayName())
                .replace("$2", localSpellName);
    }

    private void produceYield(boolean delayed, SpellType spellType, HashMap<String, Set<?>> mappedTargets, ConfigurationSection yieldSection) {
        for (String key : yieldSection.getKeys(false)) {
            String yieldName = "";
            if (!key.contains("^")) {
                yieldName += key;
            } else {
                yieldName = key.split("\\^")[0];
            }

            if (createDamageListenerYield(spellType, yieldSection, key, yieldName)) {
                continue;
            }

            if (createDurationYield(delayed, mappedTargets, yieldSection, key, yieldName)) {
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

    private boolean createDamageListenerYield(SpellType spellType, ConfigurationSection yieldSection, String key, String yieldName) {
        if (!yieldName.equalsIgnoreCase("damage-listener")) {
            return false;
        }
        final ConfigurationSection damageListenerSection = yieldSection.getConfigurationSection(key);
        final ConfigurationSection damageListenerConfig = spellType.getConfig().getConfigurationSection("listeners." + key);
        if (damageListenerConfig == null) {
            return true;
        }

        long delay = Math.round(getLevelAdjustedValue("" + damageListenerSection.getLong("delay", 0), level, null, this));
        long duration = Math.round(getLevelAdjustedValue("" + damageListenerSection.getLong("duration", 0), level, null, this));
        final Player finalCaster = caster;
        final int finalLevel = level;
        int delayId = -1;
        int durationId = -1;
        final Civilian finalChampion = CivilianManager.getInstance().getCivilian(finalCaster.getUniqueId());
        final String finalName = type;
        final String finalKey = key;
        final Spell spell = this;
        final String finalYieldName = yieldName;
        if (duration > 0) {
            durationId = Bukkit.getScheduler().runTaskLater(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    SpellListener.getInstance().removeDamageListener(finalCaster);
                    finalChampion.getStates().remove(finalName + "." + finalKey);
                }
            }, delay + duration).getTaskId();
        }
        if (delayId < -1) {
            Bukkit.getScheduler().runTaskLater(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    SpellListener.getInstance().addDamageListener(finalCaster, finalLevel, damageListenerConfig, spell);

                }
            }, delay);
        } else {
            SpellListener.getInstance().addDamageListener(caster, level, damageListenerConfig, spell);
            HashMap<String, Object> listenerVars = new HashMap<>();

            CivState state = new CivState(this, finalYieldName, durationId, -1, damageListenerConfig, listenerVars);
            finalChampion.getStates().put(finalName + "." + finalKey, state);
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
        long delay = Math.round(getLevelAdjustedValue("" + durationSection.getLong("delay", 0), level, null, this));
        long duration = Math.round(getLevelAdjustedValue("" + durationSection.getLong("duration", 0), level, null, this));
        long period = Math.round(getLevelAdjustedValue("" + durationSection.getLong("period", 0), level, null, this));
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
            periodId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    useAbility(finalMappedTargets, true, durationSectionEffects);
                }
            }, delay, period);
        }
        if (delay > 0 && period < 1) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    useAbility(finalMappedTargets, true, durationSectionEffects);
                }
            }, delay + duration);
        } else {
            useAbility(mappedTargets, true, durationSectionEffects);
        }

        final int finalPeriodId = periodId;
        if (duration > 0) {
            durationId = Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    removeAbility(finalMappedTargets, durationAbilities);
                    Bukkit.getScheduler().cancelTask(finalPeriodId);
                    Civilian champion = CivilianManager.getInstance().getCivilian(finalCaster.getUniqueId());
                    CivState state = champion.getStates().get(finalName + "." + finalKey);
                    if (state != null) {
                        state.remove(finalCaster);
                        champion.getStates().remove(finalName + "." + finalKey);
                    }
                }
            }, delay + duration);
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
            //component = getAbilityComponent(costName, level);

            //String targetKey = component.getTargetName();
            boolean isString = !costValueString.equals(SpellConstants.NOT_A_STRING) && !costValueString.contains("MemorySection");
            String targetKey = isString ? SpellConstants.SELF : costSection.getConfigurationSection(key).getString(SpellConstants.TARGET, SpellConstants.SELF);
            Set<?> targetSet = mappedTargets.get(targetKey);
            if (targetSet == null || targetSet.isEmpty()) {
                costsMet = invert;
                break;
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
            ConfigurationSection configurationSection = null;
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
                input += inputParts[i];
            }
        }
        return input;
    }

    public static void addSelfToTargetMapping(Map<String, Set<?>> mappedTargets, Player self) {
        Set<Object> targetSet = new HashSet<>();
        targetSet.add(self);
        mappedTargets.put(SpellConstants.SELF, targetSet);
    }
}
