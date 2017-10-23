package org.redcastlemedia.multitallented.civs.spells.targets;


import org.bukkit.Location;

import java.util.HashSet;

public class TargetScheme {
    public final HashSet<Object> targets;
    public final Location origin;
    public TargetScheme(HashSet<Object> targets, Location origin) {
        this.targets = targets;
        this.origin = origin;
    }
}