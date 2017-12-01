package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.UUID;

public class Town {
    private final String type;


    private int maxPower;
    private int power;
    private Location location;
    private String name;
    private HashMap<UUID, String> people;

    public Town(String name, String type, Location location, HashMap<UUID, String> people, int power, int maxPower) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.people = people;
        this.power = power;
        this.maxPower = maxPower;
    }

    public String getType() {
        return type;
    }
    public Location getLocation() {
        return location;
    }
    public String getName() { return name; }
    public HashMap<UUID, String> getPeople() { return people; }
    public int getMaxPower() {
        return maxPower;
    }
    public int getPower() {
        return power;
    }
    public void setMaxPower(int maxPower) {
        this.maxPower = maxPower;
    }
    public void setPower(int power) {
        this.power = power;
    }

    public int countPeople(String role) {
        if (role == null) {
            return people.size();
        }
        int count = 0;
        for (String currentRole : people.values()) {
            if (currentRole.contains(role) || currentRole.contains("owner")) {
                count++;
            }
        }
        return count;
    }
}
