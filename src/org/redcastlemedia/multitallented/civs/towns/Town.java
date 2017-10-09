package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Location;

import java.util.Set;
import java.util.UUID;

public class Town {
    private final String type;
    private Location location;
    private String name;
    private Set<UUID> owners;

    public Town(String name, String type, Location location, Set<UUID> owners) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.owners = owners;
    }

    public String getType() {
        return type;
    }
    public Location getLocation() {
        return location;
    }
    public String getName() { return name; }
    public Set<UUID> getOwners() { return owners; }
}
