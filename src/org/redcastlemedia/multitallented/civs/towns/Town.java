package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.UUID;

public class Town {
    private final String type;
    private Location location;
    private String name;
    private HashMap<UUID, String> people;

    public Town(String name, String type, Location location, HashMap<UUID, String> people) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.people = people;
    }

    public String getType() {
        return type;
    }
    public Location getLocation() {
        return location;
    }
    public String getName() { return name; }
    public HashMap<UUID, String> getPeople() { return people; }
}
