package org.redcastlemedia.multitallented.civs.spells.targets;


import org.bukkit.configuration.ConfigurationSection;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;

import java.util.HashSet;

/**
 *
 * @author Multitallented
 */
public abstract class Target {
    final ConfigurationSection config;
    private final HashSet<String> targetTypes = new HashSet<>();
    public final String NAME;

    public Target(ConfigurationSection config) {
        this.config = config;
        NAME = config.getString("name", "unnamed");
        try {
            targetTypes.addAll(config.getStringList("target-types"));
        } catch (Exception e) {

        }
    }

    public abstract TargetScheme getTargets(Civilian civilian);
}