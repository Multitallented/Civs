package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;
import org.redcastlemedia.multitallented.civs.spells.effects.Effect;
import org.redcastlemedia.multitallented.civs.spells.targets.Target;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;

public class Spell {
    private final Player caster;
    private String type;
    private int level;
    private HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables;

    public Spell(String type, Player caster, int level) {
        this.type = type;
        this.caster = caster;
        this.level = level;
        this.abilityVariables = new HashMap<>();
    }

    public String getType() {
        return type;
    }
    public HashMap<String, HashMap<Object, HashMap<String, Double>>> getAbilityVariables() {
        return abilityVariables;
    }

    public boolean useAbility() {
        SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(type);
        FileConfiguration config = spellType.getConfig();
        HashMap<String, Set<?>> mappedTargets = new HashMap<>();
        HashSet<LivingEntity> tempSet = new HashSet<>();
        tempSet.add(caster);
        mappedTargets.put("self", tempSet);
//        HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables = new HashMap<String, HashMap<Object, HashMap<String, Double>>>();


        ConfigurationSection conditionsConfig = config.getConfigurationSection("conditions");
        if (conditionsConfig != null) {
            for (String key : conditionsConfig.getKeys(false)) {
                String conditionName = "";
                if (!key.contains("^")) {
                    conditionName += key;
                } else {
                    conditionName = key.split("\\^")[0];
                }
//				System.out.println("Condition: " + key + "/" + conditionName);
                String costValueString = conditionsConfig.getString(key, "not-a-string");
//                AbilityComponent component = rpgen.getAbilityManager().getAbilityComponent(conditionName, key);
                boolean invert = key.endsWith("^not");


                boolean isSection = costValueString.equals("not-a-string") || costValueString.contains("MemorySection");
                String targetKey = "self";
                if (isSection) {
                    String tempTarget = conditionsConfig.getConfigurationSection(key).getString("target", "not-a-string");
                    if (!tempTarget.equals("not-a-string")) {
                        targetKey = tempTarget;
                    }
                }
                Set<?> targetSet = mappedTargets.get(targetKey);
                if (targetSet == null || targetSet.isEmpty()) {
                    continue;
                }
                for (Object target : targetSet) {
                    Effect component;
                    if (!costValueString.equals("not-a-string") && !costValueString.contains("MemorySection")) {
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
                        System.out.println(conditionName + " failed requirement");
                        return false;
                    }
                }
//				System.out.println(conditionName + " passed requirement");
            }
        }

        ConfigurationSection targetSections = config.getConfigurationSection("targets");
        if (targetSections != null) {
            for (String key : targetSections.getKeys(false)) {
                ConfigurationSection targetSection = targetSections.getConfigurationSection("targets." + key);
                String targetName = targetSection.getString("type", "nearby");
                Target target = SpellType.getTarget(targetName, key, targetSection, level, caster, this);
                try {
                    mappedTargets.put(key, target.getTargets());
                } catch (NullPointerException npe) {
                    Civs.logger.severe("Failed to find target " + targetName + " in spell " + type);
                    return false;
                }
                if (targetSection.getBoolean("cancel-if-empty", false) && mappedTargets.get(key).isEmpty()) {
                    return false;
                }
            }
        }
        /*for (String key : this.targetSchema.keySet()) {
            String targetName = this.targetSchema.get(key).getString("type", "nearby");
            Target abilityTarget = SpellType.getTarget(targetName, key);
            if (abilityTarget == null) {
                continue;
            }
            mappedTargets.put(key, abilityTarget.getTargets(caster, this.targetSchema.get(key), level, abilityVariables));
            if (this.targetSchema.get(key).getBoolean("cancel-if-empty", false) && mappedTargets.get(key).isEmpty()) {
                return false;
            }
        }*/

        return useAbility(mappedTargets, false, new HashMap<String, ConfigurationSection>());
    }

    public boolean useAbilityFromListener(Player caster, int level, ConfigurationSection useSection, Object newTarget) {
        HashMap<String, Set<?>> mappedTargets = new HashMap<>();
        HashSet<LivingEntity> tempSet = new HashSet<>();
        tempSet.add(caster);
        mappedTargets.put("self", tempSet);
        HashSet<Object> tempTarget = new HashSet<>();
        tempTarget.add(newTarget);
        mappedTargets.put("target", tempTarget);
        SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(type);

        ConfigurationSection targetSections = spellType.getConfig().getConfigurationSection("targets");
        for (String key : targetSections.getKeys(false)) {
            ConfigurationSection targetSection = targetSections.getConfigurationSection(key);
            String targetName = targetSection.getString("type", "nearby");
            Target abilityTarget = SpellType.getTarget(targetName, key, targetSection, level, caster, this);
            if (abilityTarget == null) {
                continue;
            }
            mappedTargets.put(key, abilityTarget.getTargets());
            if (targetSection.getBoolean("cancel-if-empty", false) && mappedTargets.get(key).isEmpty()) {
                return false;
            }
        }

        useAbility(mappedTargets, true, new HashMap<String, ConfigurationSection>());

        return useSection.getBoolean("cancel", false);
    }

    public boolean useAbility(HashMap<String, Set<?>> incomingTargets,
                              boolean delayed,
                              Map<String, ConfigurationSection> durationTargets) {
        SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(type);
        HashMap<String, Set<?>> mappedTargets = new HashMap<>(incomingTargets);
        if (durationTargets != null) {
            for (String key : durationTargets.keySet()) {
                String targetName = durationTargets.get(key).getString("type", "nearby");
                Target target = SpellType.getTarget(targetName, key, durationTargets.get(key), level, caster, this);
//                Target abilityTarget = rpgen.getAbilityManager().getAbilityTarget(targetName);
                if (target == null) {
                    continue;
                }
                mappedTargets.put(key, target.getTargets());
                if (durationTargets.get(key).getBoolean("cancel-if-empty", false) && mappedTargets.get(key).isEmpty()) {
                    return false;
                }
            }
        }

        HashMap<String, ConfigurationSection> components = spellType.getComponents();
        HashSet<String> fulfilledRequirements = new HashSet<>();
        Collection<String> abilityKeys = components.keySet();
        List<String> sorted = new ArrayList<>();
        sorted.addAll(abilityKeys);
        Collections.sort(sorted);
        for (String componentName : sorted) {
            String compName = "";
            if (!componentName.contains("\\.")) {
                compName += componentName;
            } else {
                compName = componentName.split("\\.")[0];
            }
            ConfigurationSection currentComponent = components.get(compName);
            if (currentComponent == null) {
                Civs.logger.severe("Unable to find componentSection " + compName + " for spell " + type);
                continue;
            }

            //filter targets
            ConfigurationSection filterSection = currentComponent.getConfigurationSection("filters");
            if (filterSection != null) {
                for (String key : filterSection.getKeys(false)) {
                    String filterName = "";
                    if (!key.contains("^")) {
                        filterName += key;
                    } else {
                        filterName = key.split("\\^")[0];
                    }
                    String filterValueString = filterSection.getString(filterName, "not-a-string");
                    Effect component;
                    boolean invert = key.endsWith("^not");

                    boolean isSection = filterValueString.equals("not-a-string") || filterValueString.contains("MemorySection");
                    String targetKey = "self";
                    if (isSection) {
                        String tempTarget = filterSection.getConfigurationSection(key).getString("target", "not-a-string");
                        if (!tempTarget.equals("not-a-string")) {
                            targetKey = tempTarget;
                        }
                    }
                    Set<?> targetSet = mappedTargets.get(targetKey);
                    if (targetSet == null || targetSet.isEmpty()) {
                        continue;
                    }
                    HashSet<Object> removeMe = new HashSet<>();
                    for (Object target : targetSet) {
                        if (!filterValueString.equals("not-a-string") && !filterValueString.contains("MemorySection")) {
                            component = SpellType.getEffect(filterName, key, filterValueString, level, target, caster, this);
                        } else {
                            ConfigurationSection currentConfigSection = filterSection.getConfigurationSection(key);
                            component = SpellType.getEffect(filterName, key, currentConfigSection, level, target, caster, this);
                        }
                        boolean meetsRequirement = component.meetsRequirement();
                        if ((!meetsRequirement && !invert) || (meetsRequirement && invert)) {
                            removeMe.add(target);
                        }
                    }
                    for (Object removeTarget : removeMe) {
                        targetSet.remove(removeTarget);
                    }
                }
            }

            //variables
            ConfigurationSection varSection = currentComponent.getConfigurationSection("variables");
            if (varSection != null) {
                varLoop: for (String key : varSection.getKeys(false)) {
                    String varName = "";
                    if (!key.contains("^")) {
                        varName += key;
                    } else {
                        varName = key.split("\\^")[0];
                    }
                    String varValueString = varSection.getString(key, "not-a-string");
                    Effect component;
                    boolean isSection = varValueString.equals("not-a-string") || varValueString.contains("MemorySection");
                    String targetKey = "self";
                    if (isSection) {
                        String tempTarget = varSection.getConfigurationSection(key).getString("target", "not-a-string");
                        if (!tempTarget.equals("not-a-string")) {
                            targetKey = tempTarget;
                        }
                    }
                    Set<?> targetSet = mappedTargets.get(targetKey);
                    if (targetSet == null || targetSet.isEmpty()) {
                        continue;
                    }

                    HashMap<Object, HashMap<String, Double>> currentComponentVars = new HashMap<Object, HashMap<String, Double>>();
                    for (Object target : targetSet) {
                        if (!varValueString.equals("not-a-string") && !varValueString.contains("MemorySection")) {
                            component = SpellType.getEffect(varName, key, varValueString, level, target, caster, this);
                        } else {
                            ConfigurationSection currentConfigSection = filterSection.getConfigurationSection(key);
                            component = SpellType.getEffect(varName, key, currentConfigSection, level, target, caster, this);
                        }
                        HashMap<String, Double> currentVars = component.getVariables(target, caster, level, this);
                        currentComponentVars.put(target, currentVars);
                    }
                    abilityVariables.put(varName, currentComponentVars);
                }
            }

            //costs
            ConfigurationSection costSection = currentComponent.getConfigurationSection("costs");
            boolean costsMet = true;
            if (costSection != null) {
                costLoop: for (String key : costSection.getKeys(false)) {
                    String costName = "";
                    if (!key.contains("^")) {
                        costName += key;
                    } else {
                        costName = key.split("\\^")[0];
                    }
//					System.out.println("Keys: " + key + ":" + costName);
                    boolean invert = key.endsWith("^not");
                    String costValueString = costSection.getString(key, "not-a-string");
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
                    boolean isString = !costValueString.equals("not-a-string") && !costValueString.contains("MemorySection");
                    String targetKey = isString ? "self" : costSection.getConfigurationSection(key).getString("target", "self");
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
//						System.out.println(key + ": " + meetsRequirement);
                        if ((!meetsRequirement && !invert) || (meetsRequirement && invert)) {
                            costsMet = false;
                            break costLoop;
                        }
                    }
                }

                if (costsMet) {
                    fulfilledRequirements.add(componentName);
                }
            } else {
                fulfilledRequirements.add(componentName);
            }

            //yield
            ConfigurationSection yieldSection = currentComponent.getConfigurationSection("yield");

            if (yieldSection == null || !costsMet) {
                continue;
            }
            for (String key : yieldSection.getKeys(false)) {
                String yieldName = "";
                if (!key.contains("^")) {
                    yieldName += key;
                } else {
                    yieldName = key.split("\\^")[0];
                }

                if (yieldName.equals("damage-listener")) {
                    final ConfigurationSection damageListenerSection = yieldSection.getConfigurationSection(key);
                    final ConfigurationSection damageListenerConfig = spellType.getConfig().getConfigurationSection("listeners." + key);
                    if (damageListenerConfig == null) {
                        continue;
                    }

                    long delay = (long) Math.round(getLevelAdjustedValue("" + damageListenerSection.getLong("delay", 0), level, null, this));
                    long duration = (long) Math.round(getLevelAdjustedValue("" + damageListenerSection.getLong("duration", 0), level, null, this));
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
                        durationId = Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                SpellListener.getInstance().removeDamageListener(finalCaster);
                                finalChampion.getStates().remove(finalName + "." + finalKey);
                            }
                        }, delay + duration);
                    }
                    if (delayId < -1) {
                        delayId = Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                SpellListener.getInstance().addDamageListener(finalCaster, finalLevel, damageListenerConfig, spell);

                            }
                        }, delay);
                    } else {
                        SpellListener.getInstance().addDamageListener(caster, level, damageListenerConfig, spell);
                        HashMap<String, Object> listenerVars = new HashMap<String, Object>();

                        CivState state = new CivState(this, finalYieldName, durationId, -1, damageListenerConfig);
                        finalChampion.getStates().put(finalName + "." + finalKey, state);
                    }
                    continue;
                }

                if (yieldName.equals("duration") && !delayed) {
                    final ConfigurationSection durationSection = yieldSection.getConfigurationSection(key);
                    long delay = (long) Math.round(getLevelAdjustedValue("" + durationSection.getLong("delay", 0), level, null, this));
                    long duration = (long) Math.round(getLevelAdjustedValue("" + durationSection.getLong("duration", 0), level, null, this));
                    long period = (long) Math.round(getLevelAdjustedValue("" + durationSection.getLong("period", 0), level, null, this));
                    int durationId = -1;
                    int periodId = -1;
                    final Player finalCaster = caster;
                    final HashMap<String, Set<?>> finalMappedTargets = mappedTargets;
                    final String finalName = type;
                    final String finalKey = key;
                    final HashMap<String, ConfigurationSection> durationAbilities = new HashMap<String, ConfigurationSection>();
                    for (String durationKey : durationSection.getConfigurationSection("section").getKeys(false)) {
                        durationAbilities.put(durationKey, durationSection.getConfigurationSection("section." + durationKey));
                    }
                    final HashMap<String, ConfigurationSection> mappedDurationTargets = new HashMap<String, ConfigurationSection>();
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
                                useAbility(finalMappedTargets, true, durationAbilities);
                            }
                        }, delay, period);
                    }
                    if (delay > 0 && period < 1) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                useAbility(finalMappedTargets, true, durationAbilities);
                            }
                        }, delay + duration);
                    } else {
                        useAbility(mappedTargets, true, durationAbilities);
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
                    civilian.getStates().put(type + "." + key, new CivState(this, key, durationId, periodId));

                    continue;
                }

                String yieldValueString = yieldSection.getString(key, "not-a-string");
                Effect component;
                boolean isString = !yieldValueString.equals("not-a-string") && !yieldValueString.contains("MemorySection");


                //String targetKey = component.getTargetName();
                String targetKey = isString ? "self" : yieldSection.getConfigurationSection(key).getString("target", "self");
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
                    component.apply();
                }
            }
        }

        if (fulfilledRequirements.isEmpty()) {
            return false;
        }

        if (!delayed) {
            //TODO localize this
            String message = ChatColor.BLUE + Civs.getPrefix() + " " + caster.getDisplayName() + ChatColor.WHITE + " used " + ChatColor.RED + type;
            caster.sendMessage(message);
            for (String key : mappedTargets.keySet()) {
                if (key.equals("self")) {
                    continue;
                }
                for (Object obj : mappedTargets.get(key)) {
                    if (!(obj instanceof Player)) {
                        continue;
                    }
                    ((Player) obj).sendMessage(message);
                }
            }
        }
        return true;
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

        }
        return 0;
    }
    private static String replaceAllVariables(String input, int level, Object target, Spell spell) {
        HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables = spell.getAbilityVariables();
        input = input.replace("$level$", "" + level);
        input = input.replace("$rand$", "" + Math.random());
        String[] inputParts = input.split("\\$");
        if (spell != null && target != null) {
            input = "";
            for (int i = 0; i < inputParts.length; i++) {
                if (inputParts[i].contains("#")) {
                    HashMap<String, HashMap<Object, HashMap<String, Double>>> variables = new HashMap<>(abilityVariables);
                    HashMap<Object, HashMap<String, Double>> targetVars = variables.get(inputParts[i].split("#")[0]);
                    if (targetVars == null) {
                        continue;
                    }
                    HashMap<String, Double> componentVars = targetVars.get(target);
                    if (componentVars == null) {
                        continue;
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
}
