package org.redcastlemedia.multitallented.civs.spells.targets;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;

import java.util.HashSet;

public class AreaTarget extends Target {
    public AreaTarget(ConfigurationSection config) {
        super(config);
    }

    @Override
    public TargetScheme getTargets(Civilian civilian) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        HashSet<Object> targets = new HashSet<>();
        //TODO get all entities near the target
        return new TargetScheme(targets, player.getLocation());
    }
}
