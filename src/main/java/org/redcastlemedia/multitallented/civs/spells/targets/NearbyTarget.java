package org.redcastlemedia.multitallented.civs.spells.targets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.redcastlemedia.multitallented.civs.spells.Spell;

public class NearbyTarget extends Target {
    public NearbyTarget(Spell spell,
                        String key,
                        Entity origin,
                        int level,
                        ConfigurationSection config) {
        super(spell, key, origin, level, config);
    }

    public Set<?> getTargets() {
        int level = getLevel();
        Spell spell = getSpell();
        Set<LivingEntity> returnSet = new HashSet<>();
        if (!(getOrigin() instanceof LivingEntity)) {
            return returnSet;
        }
        LivingEntity player = (LivingEntity) getOrigin();
        ConfigurationSection config = getConfig();
        int range = (int) Math.round(Spell.getLevelAdjustedValue(config.getString("range","15"), level, null, spell));
        int radius = (int) Math.round(Spell.getLevelAdjustedValue(config.getString("radius", "5"), level, null, spell));
        int maxTargets = (int) Math.round(Spell.getLevelAdjustedValue(config.getString("max-targets", "-1"), level, null, spell));
        Collection<Entity> nearbyEntities;
        if (range < 1) {
            nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        } else {
            HashSet<Material> materialHashSet = new HashSet<>();
            Location center = player.getTargetBlock(materialHashSet, range).getLocation();
            center = applyTargetSettings(center);
            ArmorStand removeMe = (ArmorStand) center.getWorld().spawnEntity(center, EntityType.ARMOR_STAND);
            removeMe.setVisible(false);
            nearbyEntities = removeMe.getNearbyEntities(radius, radius, radius);
            removeMe.remove();
        }

        for (Entity target : nearbyEntities) {
            if (maxTargets > 0 && returnSet.size() >= maxTargets) {
                break;
            }

            if (target != player &&
                    target instanceof LivingEntity) {

                returnSet.add((LivingEntity) target);
            }
        }
        filterOutUntargetables(returnSet);
        return returnSet;
    }

}
