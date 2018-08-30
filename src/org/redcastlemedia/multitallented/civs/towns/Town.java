package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Town {
    private final String type;


    private int maxPower;
    private int power;
    private Location location;
    private String name;
    private HashMap<UUID, String> people;
    private int housing;
    private int population;
    private HashSet<String> allies;

    public Town(String name, String type, Location location, HashMap<UUID, String> people, int power, int maxPower,
                int housing, int population) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.people = people;
        this.power = power;
        this.maxPower = maxPower;
        this.housing = housing;
        this.population = population;
        this.allies = new HashSet<>();
    }

    public String getType() {
        return type;
    }
    public Location getLocation() {
        return location;
    }
    public String getName() { return name; }
    public HashMap<UUID, String> getPeople() { return people; }
    public HashSet<String> getAllies() { return allies; }
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
        if (power > this.maxPower) {
            this.power = maxPower;
        } else {
            this.power = power;
        }
    }
    public void setAllies(HashSet<String> allies) { this.allies = allies; }

    public int countPeopleWithRole(String role) {
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

    public int getHousing() {
        return this.housing;
    }
    public void setHousing(int housing) {
        this.housing = housing;
    }

    public int getPopulation() {
        return population;
    }
    public void setPopulation(int population) {
        this.population = population;
    }
}
