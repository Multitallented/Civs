package org.redcastlemedia.multitallented.civs.spells.targets;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.Vector3D;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class VectorTarget extends Target {
    public VectorTarget(Spell spell,
                        String key,
                        Entity origin,
                        int level,
                        ConfigurationSection config) {
        super(spell, key, origin, level, config);
    }

    @Override
    public Set<?> getTargets() {
        Set<LivingEntity> returnSet = new HashSet<LivingEntity>();
        int level = getLevel();
        ConfigurationSection config = getConfig();
        Entity origin = getOrigin();
        if (!(origin instanceof LivingEntity)) {
            return returnSet;
        }
        LivingEntity player = (LivingEntity) origin;
        int range = (int) Math.round(Spell.getLevelAdjustedValue(config.getString("range","15"), level, null, null));
        boolean pen = config.getBoolean("penetration", true);
        boolean allowMultiple = config.getBoolean("allow-multiple", false);

        Location observerPos = player.getEyeLocation();
        Vector3D observerDir = new Vector3D(observerPos.getDirection());

        Vector3D observerStart = new Vector3D(observerPos);
        Vector3D observerEnd = observerStart.add(observerDir.multiply(range));

        double closestDistance = 999999999;
        HashSet<Material> materialHashSet = new HashSet<>();
        Location wallLocation = player.getTargetBlock(materialHashSet, range).getLocation();
        double wallDistance = player.getLocation().distanceSquared(wallLocation);
        // Get nearby entities
        for (Entity target : player.getNearbyEntities(range, range, range)) {
            // Bounding box of the given player
            Vector3D targetPos = new Vector3D(target.getLocation());
            Vector3D minimum = targetPos.add(-0.5, 0, -0.5);
            Vector3D maximum = targetPos.add(0.5, 1.67, 0.5);

            if (target != player &&
                    Vector3D.hasIntersection(observerStart, observerEnd, minimum, maximum) &&
                    target instanceof LivingEntity) {

                if (!pen && player.getLocation().distanceSquared(target.getLocation()) > wallDistance) {
                    continue;
                }

                if (!allowMultiple) {
                    double currentDistance = observerPos.distanceSquared(target.getLocation());
                    if (closestDistance < currentDistance) {
                        continue;
                    } else {
                        closestDistance = currentDistance;
                        returnSet.clear();
                    }
                }

                returnSet.add((LivingEntity) target);
            }
        }

        return returnSet;
    }
}
