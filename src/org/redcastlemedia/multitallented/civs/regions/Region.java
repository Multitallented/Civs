package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.UUID;

public class Region {

    private final String type;
    private final HashSet<UUID> members;
    private final HashSet<UUID> owners;
    private final Location location;

    public Region(String type, HashSet<UUID> owners, HashSet<UUID> members, Location location) {
        this.type = type;
        this.owners = owners;
        this.members = members;
        this.location = location;
    }
    public String getType() {
        return type;
    }
    public HashSet<UUID> getOwners() {
        return owners;
    }
    public HashSet<UUID> getMembers() {
        return members;
    }
    public Location getLocation() {
        return location;
    }
    public int getXNRadius() { return 5; } //TODO fix this
}
