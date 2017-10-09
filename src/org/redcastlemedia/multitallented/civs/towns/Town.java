package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Location;

public class Town {
    private final String type;
    private Location location;
    private String name;

    public Town(String name, String type, Location location) {
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public String getType() {
        return type;
    }
    public Location getLocation() {
        return location;
    }
    public String getName() { return name; }
}
