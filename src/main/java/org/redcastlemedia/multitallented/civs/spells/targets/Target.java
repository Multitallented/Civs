package org.redcastlemedia.multitallented.civs.spells.targets;


import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Multitallented
 */
public abstract class Target extends SpellComponent {
    private final ConfigurationSection config;

    public Target(Spell spell,
                  String key,
                  Entity origin,
                  int level,
                  ConfigurationSection config) {
        super(spell, key, null, origin, level);
        this.config=config;
    }
    public ConfigurationSection getConfig() {
        return config;
    }

    public Location applyTargetSettings(Location currentLocation) {
        //jitter
        int level = getLevel();
        HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables = getSpell().getAbilityVariables();
        String jitter = config.getString("jitter");
        if (jitter != null) {
            String jX = jitter.split(";")[0];
            String jY = jitter.split(";")[1];
            String jZ = jitter.split(";")[2];
            double jitterX = Spell.getLevelAdjustedValue(jX, level, null, null);
            double jitterY = Spell.getLevelAdjustedValue(jY, level, null, null);
            double jitterZ = Spell.getLevelAdjustedValue(jZ, level, null, null);

            if (jitterX != 0) {
                jitterX = Math.random() * jitterX * 2 - jitterX;
            }
            if (jitterY != 0) {
                jitterY = Math.random() * jitterY * 2 - jitterY;
            }
            if (jitterZ != 0) {
                jitterZ = Math.random() * jitterZ * 2 - jitterZ;
            }
            currentLocation = currentLocation.add(jitterX, jitterY, jitterZ);
        }
        //offset
        String offset = config.getString("offset");
        if (offset != null) {
            double offsetX = Spell.getLevelAdjustedValue(offset.split(";")[0], level, null, null);
            double offsetY = Spell.getLevelAdjustedValue(offset.split(";")[1], level, null, null);
            double offsetZ = Spell.getLevelAdjustedValue(offset.split(";")[2], level, null, null);
            currentLocation = currentLocation.add(offsetX, offsetY, offsetZ);
        }
        return currentLocation;
    }

    public abstract Set<?> getTargets();
}