package org.redcastlemedia.multitallented.civs.spells.targets;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;

import java.util.HashSet;

public class VectorTarget extends Target {
    public VectorTarget(ConfigurationSection config) {
        super(config);
    }

    @Override
    public TargetScheme getTargets(Civilian civilian) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        //TODO Get all targets in a line
        HashSet<Object> targets = new HashSet<>();
        return new TargetScheme(targets, player.getLocation());
    }
}
