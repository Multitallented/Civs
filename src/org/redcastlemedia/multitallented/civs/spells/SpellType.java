package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.spells.conditions.Condition;
import org.redcastlemedia.multitallented.civs.spells.effects.Effect;
import org.redcastlemedia.multitallented.civs.spells.targets.AreaTarget;
import org.redcastlemedia.multitallented.civs.spells.targets.Target;
import org.redcastlemedia.multitallented.civs.spells.targets.TargetScheme;
import org.redcastlemedia.multitallented.civs.spells.targets.VectorTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
                     List<String> description,
                     List<String> groups,
                     HashSet<Target> targets,
                     ArrayList<HashMap<Condition, String>> preCastConditions,
                     ArrayList<HashMap<Effect, String>> preCastEffects,
                     ArrayList<HashMap<Condition, String>> postCastConditions,
                     ArrayList<HashMap<Effect, String>> postCastEffects) {
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
        this.targets = targets;
        this.preCastConditions = preCastConditions;
        this.preCastEffects = preCastEffects;
        this.postCastConditions = postCastConditions;
        this.postCastEffects = postCastEffects;
    }

    private final HashSet<Target> targets;
    private final ArrayList<HashMap<Condition, String>> preCastConditions;
    private final ArrayList<HashMap<Effect, String>> preCastEffects;
    private final ArrayList<HashMap<Condition, String>> postCastConditions;
    private final ArrayList<HashMap<Effect, String>> postCastEffects;

    public ArrayList<HashMap<Condition, String>> getPreCastConditions() {
        return preCastConditions;
    }
    public ArrayList<HashMap<Condition, String>> getPostCastConditions() {
        return postCastConditions;
    }
    public ArrayList<HashMap<Effect, String>> getPreCastEffects() {
        return preCastEffects;
    }
    public ArrayList<HashMap<Effect, String>> getPostCastEffects() {
        return postCastEffects;
    }

    public static Target getTarget(String type, ConfigurationSection section) {
        if (type.equals("vector")) {
            return new VectorTarget(section);
        } else if (type.equals("area")) {
            return new AreaTarget(section);
        }
        return null;
    }
    public static Condition getCondition(String type, ConfigurationSection section) {
        return null;
    }
    public static Effect getEffect(String type, ConfigurationSection section) {
        return null;
    }

    public void useSkill(Civilian civilian) {
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

//        Spell cs = new Spell(caster, this, targetMap, types);
//        cs.checkConditions();
    }
}
