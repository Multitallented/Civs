package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.spells.conditions.Condition;
import org.redcastlemedia.multitallented.civs.spells.effects.Effect;
import org.redcastlemedia.multitallented.civs.spells.targets.Target;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.*;

public class Spell {
    private final Player caster;
    private String type;

    public Spell(String type, Player caster) {
        this.type = type;
        this.caster = caster;
    }

    public boolean useAbility(Player caster, int level) {
        FileConfiguration config = ((SpellType) ItemManager.getInstance().getItemType(type)).getConfig();
        HashMap<String, Set<?>> mappedTargets = new HashMap<>();
        HashSet<LivingEntity> tempSet = new HashSet<>();
        tempSet.add(caster);
        mappedTargets.put("self", tempSet);
        HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables = new HashMap<String, HashMap<Object, HashMap<String, Double>>>();


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
                Condition component = SpellType.getCondition(conditionName, key);
//                AbilityComponent component = rpgen.getAbilityManager().getAbilityComponent(conditionName, key);
                boolean invert = key.endsWith("^not");

                if (!costValueString.equals("not-a-string") && !costValueString.contains("MemorySection")) {
                    component.setData(costValueString, level, abilityVariables);
                } else {
                    ConfigurationSection currentConfigSection = conditionsConfig.getConfigurationSection(key);
                    component.setData(currentConfigSection, level, abilityVariables);
                }
                String targetKey = component.getTargetName();
                Set<?> targetSet = mappedTargets.get(targetKey);
                if (targetSet == null || targetSet.isEmpty()) {
                    continue;
                }
                for (Object target : targetSet) {
                    boolean meetsRequirement = component.meetsRequirement(target, caster, level, this);
                    if ((!meetsRequirement && !invert) || (meetsRequirement && invert)) {
                        System.out.println(conditionName + " failed requirement");
                        return false;
                    }
                }
//				System.out.println(conditionName + " passed requirement");
            }
        }

        for (String key : this.targetSchema.keySet()) {
            String targetName = this.targetSchema.get(key).getString("type", "nearby");
            Target abilityTarget = SpellType.getTarget(targetName, key);
            if (abilityTarget == null) {
                continue;
            }
            mappedTargets.put(key, abilityTarget.getTargets(caster, this.targetSchema.get(key), level, abilityVariables));
            if (this.targetSchema.get(key).getBoolean("cancel-if-empty", false) && mappedTargets.get(key).isEmpty()) {
                return false;
            }
        }

        return useAbility(caster, level, mappedTargets, false, components, new HashMap<String, ConfigurationSection>());
    }

    public static boolean useAbility(Player caster, int level, ConfigurationSection useSection, Object newTarget) {
        //TODO finish this stub of ability listener
		/*HashMap<String, Set<?>> mappedTargets = new HashMap<>();
		HashSet<LivingEntity> tempSet = new HashSet<>();
		tempSet.add(caster);
		mappedTargets.put("self", tempSet);
		HashSet<Object> tempTarget = new HashSet<Object>();
		tempTarget.add(newTarget);
		mappedTargets.put("target", tempTarget);

		for (String key : this.targetSchema.keySet()) {
			String targetName = this.targetSchema.get(key).getString("type", "nearby");
			AbilityTarget abilityTarget = rpgen.getAbilityManager().getAbilityTarget(targetName);
			if (abilityTarget == null) {
				continue;
			}
			mappedTargets.put(key, abilityTarget.getTargets(caster, this.targetSchema.get(key), level));
			if (this.targetSchema.get(key).getBoolean("cancel-if-empty", false) && mappedTargets.get(key).isEmpty()) {
				return false;
			}
		}

		useAbility(caster, level, mappedTargets, true, components, new HashMap<String, ConfigurationSection>());*/

        return useSection.getBoolean("cancel", false);
    }

    public boolean useAbility(Player caster, int level,
                              HashMap<String, Set<?>> incomingTargets,
                              boolean delayed,
                              Map<String, ConfigurationSection> abilitySections,
                              Map<String, ConfigurationSection> durationTargets) {
        HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables = new HashMap<>();
        return useAbility(caster, level, incomingTargets, delayed, abilitySections, durationTargets, abilityVariables);
    }

    public boolean useAbility(Player caster, int level,
                              HashMap<String, Set<?>> incomingTargets,
                              boolean delayed,
                              Map<String, ConfigurationSection> abilitySections,
                              Map<String, ConfigurationSection> durationTargets,
                              HashMap<String, HashMap<Object, HashMap<String, Double>>> tempAbilityVariables) {
        HashMap<String, Set<?>> mappedTargets = new HashMap<String, Set<?>>(incomingTargets);
        final HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables = tempAbilityVariables;
        if (durationTargets != null) {
            for (String key : durationTargets.keySet()) {
                String targetName = durationTargets.get(key).getString("type", "nearby");
                AbilityTarget abilityTarget = rpgen.getAbilityManager().getAbilityTarget(targetName);
                if (abilityTarget == null) {
                    continue;
                }
                mappedTargets.put(key, abilityTarget.getTargets(caster, durationTargets.get(key), level, abilityVariables));
                if (durationTargets.get(key).getBoolean("cancel-if-empty", false) && mappedTargets.get(key).isEmpty()) {
                    return false;
                }
            }
        }


        HashSet<String> fullfilledRequirements = new HashSet<String>();
        Collection<String> abilityKeys = abilitySections.keySet();
        List<String> sorted = new ArrayList<String>();
        sorted.addAll(abilityKeys);
        Collections.sort(sorted);
        for (String componentName : sorted) {
            String compName = "";
            if (!componentName.contains("\\.")) {
                compName += componentName;
            } else {
                compName = componentName.split("\\.")[0];
            }
            ConfigurationSection currentComponent = abilitySections.get(compName);
            if (currentComponent == null) {
                RPGen.LOGGER.severe(RPGen.getPrefix() + " unable to find componentSection " + compName + " for " + name);
                continue;
            }

            //filter targets
            ConfigurationSection filterSection = currentComponent.getConfigurationSection("filters");
            if (filterSection != null) {
                filterLoop: for (String key : filterSection.getKeys(false)) {
                    String filterName = "";
                    if (!key.contains("^")) {
                        filterName += key;
                    } else {
                        filterName = key.split("\\^")[0];
                    }
                    String filterValueString = filterSection.getString(filterName, "not-a-string");
                    AbilityComponent component;
                    boolean invert = key.endsWith("^not");

                    component = rpgen.getAbilityManager().getAbilityComponent(filterName, key);
                    if (!filterValueString.equals("not-a-string") && !filterValueString.contains("MemorySection")) {
                        component.setData(filterValueString, level, abilityVariables);
                    } else {
                        ConfigurationSection currentConfigSection = filterSection.getConfigurationSection(key);
                        component.setData(currentConfigSection, level, abilityVariables);
                    }
                    String targetKey = component.getTargetName();
                    Set<?> targetSet = mappedTargets.get(targetKey);
                    if (targetSet == null || targetSet.isEmpty()) {
                        continue;
                    }
                    HashSet<Object> removeMe = new HashSet<>();
                    for (Object target : targetSet) {
                        boolean meetsRequirement = component.meetsRequirement(target, caster, level, this);
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
                    AbilityComponent component = rpgen.getAbilityManager().getAbilityComponent(varName, key);
                    if (component == null) {
                        rpgen.getLogger().severe("Null component " + varName);
                        continue;
                    }
                    if (!varValueString.equals("not-a-string") && !varValueString.contains("MemorySection")) {
                        component.setData(varValueString, level, abilityVariables);
                    } else {
                        ConfigurationSection currentConfigSection = varSection.getConfigurationSection(key);
                        component.setData(currentConfigSection, level, abilityVariables);
                    }
                    String targetKey = component.getTargetName();
                    Set<?> targetSet = mappedTargets.get(targetKey);
                    if (targetSet == null || targetSet.isEmpty()) {
                        continue;
                    }

                    HashMap<Object, HashMap<String, Double>> currentComponentVars = new HashMap<Object, HashMap<String, Double>>();
                    for (Object target : targetSet) {
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
                    AbilityComponent component;

                    if (costName.equals("inherit")) {
                        boolean inheritFullfilled = fullfilledRequirements.contains(costValueString);
                        if ((!invert && !inheritFullfilled) || (invert && inheritFullfilled)) {
                            rpgen.getLogger().info(key + " cost not met");
                            costsMet = false;
                            break;
                        }
                        continue;
                    }
                    component = rpgen.getAbilityManager().getAbilityComponent(costName, key);
                    if (component == null) {
                        rpgen.getLogger().severe("Null component " + costName);
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
                            component.setData(costValueString, level, target, this, abilityVariables);
                        } else {
                            ConfigurationSection currentConfigSection = costSection.getConfigurationSection(key);
                            component.setData(currentConfigSection, level, target, this, abilityVariables);
                        }

                        boolean meetsRequirement = component.meetsRequirement(target, caster, level, this);
//						System.out.println(key + ": " + meetsRequirement);
                        if ((!meetsRequirement && !invert) || (meetsRequirement && invert)) {
                            costsMet = false;
                            break costLoop;
                        }
                    }
                }

                if (costsMet) {
                    fullfilledRequirements.add(componentName);
                }
            } else {
                fullfilledRequirements.add(componentName);
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
                    final ConfigurationSection damageListenerConfig = config.getConfigurationSection("listeners." + key);
                    if (damageListenerConfig == null) {
                        continue;
                    }

                    long delay = (long) Math.round(Ability.getLevelAdjustedValue(abilityVariables, "" + damageListenerSection.getLong("delay", 0), level, null, this));
                    long duration = (long) Math.round(Ability.getLevelAdjustedValue(abilityVariables, "" + damageListenerSection.getLong("duration", 0), level, null, this));
                    final Player finalCaster = caster;
                    final int finalLevel = level;
                    int delayId = -1;
                    int durationId = -1;
                    final Champion finalChampion = rpgen.getChampionManager().getChampion(finalCaster.getName());
                    final String finalName = this.name;
                    final String finalKey = key;
                    final String finalYieldName = yieldName;
                    if (duration > 0) {
                        durationId = Bukkit.getScheduler().scheduleSyncDelayedTask(rpgen, new Runnable() {
                            @Override
                            public void run() {
                                rpgen.getAbilityListener().removeDamageListener(finalCaster);
                                finalChampion.getStates().remove(finalName + "." + finalKey);
                            }
                        }, delay + duration);
                    }
                    if (delayId < -1) {
                        delayId = Bukkit.getScheduler().scheduleSyncDelayedTask(rpgen, new Runnable() {
                            @Override
                            public void run() {
                                rpgen.getAbilityListener().addDamageListener(finalCaster, finalLevel, damageListenerConfig);

                            }
                        }, delay);
                    } else {
                        rpgen.getAbilityListener().addDamageListener(caster, level, damageListenerConfig);
                        HashMap<String, Object> listenerVars = new HashMap<String, Object>();

                        AbilityState state = new AbilityState(finalName, finalYieldName, durationId, -1, listenerVars, damageListenerConfig, null, abilityVariables);
                        finalChampion.getStates().put(finalName + "." + finalKey, state);
                    }
                    continue;
                }

                if (yieldName.equals("duration") && !delayed) {
                    final ConfigurationSection durationSection = yieldSection.getConfigurationSection(key);
                    long delay = (long) Math.round(Ability.getLevelAdjustedValue(abilityVariables, "" + durationSection.getLong("delay", 0), level, null, this));
                    long duration = (long) Math.round(Ability.getLevelAdjustedValue(abilityVariables, "" + durationSection.getLong("duration", 0), level, null, this));
                    long period = (long) Math.round(Ability.getLevelAdjustedValue(abilityVariables, "" + durationSection.getLong("period", 0), level, null, this));
                    int durationId = -1;
                    int periodId = -1;
                    final Player finalCaster = caster;
                    final int finalLevel = level;
                    final HashMap<String, Set<?>> finalMappedTargets = mappedTargets;
                    final String finalName = this.name;
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
                        periodId = Bukkit.getScheduler().scheduleSyncRepeatingTask(rpgen, new Runnable() {
                            @Override
                            public void run() {
                                useAbility(finalCaster, finalLevel, finalMappedTargets, true, durationAbilities, mappedDurationTargets, abilityVariables);
                            }
                        }, delay, period);
                    }
                    if (delay > 0 && period < 1) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(rpgen, new Runnable() {
                            @Override
                            public void run() {
                                useAbility(finalCaster, finalLevel, finalMappedTargets, true, durationAbilities, mappedDurationTargets, abilityVariables);
                            }
                        }, delay + duration);
                    } else {
                        useAbility(caster, level, mappedTargets, true, durationAbilities, mappedDurationTargets, abilityVariables);
                    }

                    final int finalPeriodId = periodId;
                    if (duration > 0) {
                        durationId = Bukkit.getScheduler().scheduleSyncDelayedTask(rpgen, new Runnable() {
                            @Override
                            public void run() {
                                removeAbility(finalCaster, finalLevel, finalMappedTargets, durationAbilities, mappedDurationTargets);
                                Bukkit.getScheduler().cancelTask(finalPeriodId);
                                Champion champion = rpgen.getChampionManager().getChampion(finalCaster.getName());
                                AbilityState state = champion.getStates().get(finalName + "." + finalKey);
                                if (state != null) {
                                    state.remove(rpgen, finalCaster);
                                    champion.getStates().remove(finalName + "." + finalKey);
                                }
                            }
                        }, delay + duration);
                    }

                    Champion champion = rpgen.getChampionManager().getChampion(caster.getName());
                    champion.getStates().put(this.name + "." + key, new AbilityState(this.name, key, durationId, periodId, null, null, null, abilityVariables));

                    continue;
                }

                String yieldValueString = yieldSection.getString(key, "not-a-string");
                AbilityComponent component;
                component = rpgen.getAbilityManager().getAbilityComponent(yieldName, key);
//				component = getAbilityComponent(yieldName, level);
                if (component == null) {
                    rpgen.getLogger().severe("Null component " + yieldName);
                    continue;
                }
                boolean isString = !yieldValueString.equals("not-a-string") && !yieldValueString.contains("MemorySection");


                //String targetKey = component.getTargetName();
                String targetKey = isString ? "self" : yieldSection.getConfigurationSection(key).getString("target", "self");
                Set<?> targetSet = mappedTargets.get(targetKey);

                if (targetSet == null || targetSet.isEmpty()) {
                    continue;
                }
                for (Object target : targetSet) {
                    if (isString) {
                        component.setData(yieldValueString, level, target, this, abilityVariables);
                    } else {
                        component.setData(yieldSection.getConfigurationSection(key), level, target, this, abilityVariables);
                    }
                    component.apply(target, caster, level, this, abilityVariables);
                }
            }
        }

        if (fullfilledRequirements.isEmpty()) {
            return false;
        }

        if (!delayed) {
            String message = ChatColor.BLUE + RPGen.getPrefix() + " " + caster.getDisplayName() + ChatColor.WHITE + " used " + ChatColor.RED + name;
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

    public static double getLevelAdjustedValue(HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables, String configString, int level, Object target, Spell spell) {
        if (configString.equals("0")) {
            return 0;
        }
        //System.out.println(configString);
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            Object engineEval = engine.eval(replaceAllVariables(abilityVariables, configString, level, target, spell));
            if (engineEval instanceof Integer) {
                //System.out.println("type Integer: " + (Integer) engineEval);
                return (Integer) engineEval;
            } else if (engineEval instanceof Double) {
                //System.out.println("type Double: " + (Double) engineEval);
                return (Double) engineEval;
            } else {
                //System.out.println("weird type 0");
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            //System.out.println("failed Eval Double: " + Double.parseDouble(configString));
            return Double.parseDouble(configString);
        } catch (Exception e) {

        }
        //System.out.println("failed Eval and parse: 0");
        return 0;
    }
    private static String replaceAllVariables(HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables, String input, int level, Object target, Spell spell) {
        input = input.replace("$level$", "" + level);
        input = input.replace("$rand$", "" + Math.random());
        String[] inputParts = input.split("\\$");
        if (spell != null && target != null) {
            input = "";
            for (int i = 0; i < inputParts.length; i++) {
                if (inputParts[i].contains("#")) {
                    HashMap<String, HashMap<Object, HashMap<String, Double>>> variables = new HashMap<String, HashMap<Object, HashMap<String, Double>>>(abilityVariables);
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
        //System.out.println(input);
        return input;
    }
}
