package org.redcastlemedia.multitallented.civs.spells.targets;


import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;

/**
 *
 * @author Multitallented
 */
public abstract class Target {
    private final FileConfiguration config;
    private final HashSet<String> targetTypes = new HashSet<>();
    public final String NAME;

    public Target(FileConfiguration config) {
        this.config = config;
        for (String s : config.getConfigurationSection("pattern").getKeys(false)) {
            targetTypes.add(config.getString("pattern." + s));
        }
        NAME = config.getString("name");
    }

    public abstract HashSet<Object> getTargets();
}