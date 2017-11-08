package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.spells.effects.DamageEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.Effect;
import org.redcastlemedia.multitallented.civs.spells.targets.AreaTarget;
import org.redcastlemedia.multitallented.civs.spells.targets.Target;
import org.redcastlemedia.multitallented.civs.spells.targets.VectorTarget;

import java.util.HashMap;
import java.util.List;

public class SpellType extends CivItem {


    public SpellType(List<String> reqs,
                     String name,
                     Material material,
                     int damage,
                     int qty,
                     int min,
                     int max,
                     double price,
                     String permission,
                     HashMap<String, String> description,
                     List<String> groups,
                     FileConfiguration config) {
        super(reqs,
                false,
                ItemType.SPELL,
                name,
                material,
                damage,
                qty,
                min,
                max,
                price,
                permission,
                description,
                groups);
        this.config = config;
        this.components = new HashMap<>();
        ConfigurationSection componentSection = config.getConfigurationSection("components");
        if (componentSection == null) {
            Civs.logger.severe("Failed to load spell type " + name + " no components");
            return;
        }
        for (String key : componentSection.getKeys(false)) {
            ConfigurationSection currentSection = componentSection.getConfigurationSection(key);
            if (currentSection != null) {
                components.put(key, currentSection);
            }
        }
    }

    private final FileConfiguration config;
    private final HashMap<String, ConfigurationSection> components;

    public FileConfiguration getConfig() {
        return config;
    }
    public HashMap<String, ConfigurationSection> getComponents() {
        return components;
    }

    /*public static Target getTarget(String type,
                                   String key,
                                   String config,
                                   int level,
                                   Player caster,
                                   Spell spell,
                                   HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables) {
        if (type.equals("vector")) {
            return new VectorTarget(spell, key, caster, level, abilityVariables, config);
        }
        return null;
    }*/
    public static Target getTarget(String type,
                                   String key,
                                   ConfigurationSection config,
                                   int level,
                                   Player caster,
                                   Spell spell) {
        if (type.equals("vector")) {
            return new VectorTarget(spell, key, caster, level, config);
        }
        return null;
    }

    public static Effect getEffect(String type,
                                   String key,
                                   String config,
                                   int level,
                                   Object target,
                                   Player caster,
                                   Spell spell) {
        if (type.equals("damage")) {
            return new DamageEffect(spell, key, target, caster, level, config);
        }
        return null;
    }
    public static Effect getEffect(String type,
                                   String key,
                                   ConfigurationSection config,
                                   int level,
                                   Object target,
                                   Player caster,
                                   Spell spell) {
        if (type.equals("damage")) {
            return new DamageEffect(spell, key, target, caster, level, config);
        }
        return null;
    }

    /*public void useSkill(Civilian civilian) {
        Player caster = Bukkit.getPlayer(civilian.getUuid());
        if (caster == null) {
            return;
        }

        //This will populate the target map by recursively
        //acquiring targets and end-points beginning with origin
        //self. If there is an origin that does not match the
        //name of a target scheme, then the skill will fail
        HashMap<String, HashSet<Object>> targetMap = new HashMap<>();
        HashSet<Target> targetsClone = (HashSet<Target>) targets.clone();
        HashMap<Target, Location> processedTargets = new HashMap<>();
        do {
            HashSet<Target> removeLater = new HashSet<>();
            for (Target tar : targetsClone) {
//                if (!tar.getNode().containsKey("origin")) {
//                    removeLater.add(tar);
//                }
                String originName = "self";
                try {
//                    originName = (String) tar.getNode().get("origin");
                } catch (Exception e) {
                    removeLater.add(tar);
                }
                if (!originName.equals("self")) {
                    for (Target targ : processedTargets.keySet()) {
                        if (!targ.NAME.equals(originName)) {
                            continue;
                        }
                        TargetScheme ts = tar.getTargets(civilian);
                        targetMap.put(originName, ts.targets);
                        removeLater.add(tar);
                        processedTargets.put(tar, ts.origin);
                        break;
                    }
                } else {
                    TargetScheme ts = tar.getTargets(civilian);
                    targetMap.put(originName, ts.targets);
                    removeLater.add(tar);
                    processedTargets.put(tar, ts.origin);
                }
            }
            for (Target tar : removeLater) {
                targetsClone.remove(tar);
            }
            if (removeLater.isEmpty() && !targetsClone.isEmpty()) {
                Civs.logger.severe("Failed to cast " + getProcessedName() + " for " + caster.getName());
                Civs.logger.severe("Improper target origin configuration.");
                return;
            }
        } while (!targetsClone.isEmpty());
        HashSet<Object> tempSet = new HashSet<>();
        tempSet.add(civilian);
        targetMap.put("self", tempSet);

        Spell cs = new Spell(getProcessedName(), caster);
        cs.checkConditions();
    }*/
}
