package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.items.ItemManager;

import java.util.*;

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
    private ArrayList<Bounty> bounties = new ArrayList<>();
    private List<String> allyInvites = new ArrayList<>();
    private List<Location> childLocations = new ArrayList<>();
    private final int Y_LEVEL = 80;
    private int villagers;

    public Town(String name, String type, Location location, HashMap<UUID, String> people, int power, int maxPower,
                int housing, int population, int villagers) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.people = people;
        this.power = power;
        this.maxPower = maxPower;
        this.housing = housing;
        this.population = population;
        this.villagers = villagers;
        this.allies = new HashSet<>();
    }

    public int getVillagers() {
        return villagers;
    }

    public void setVillagers(int villagers) {
        this.villagers = villagers;
    }

    public String getType() {
        return type;
    }
    public Location getLocation() {
        return location;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getAllyInvites() {
        return allyInvites;
    }

    public List<Location> getChildLocations() {
        return childLocations;
    }
    public void setChildLocations(List<Location> childLocations) {
        this.childLocations = childLocations;
    }

    public HashMap<UUID, String> getRawPeople() {
        return people;
    }
    public HashMap<UUID, String> getPeople() {
        if (allies.isEmpty()) {
            return people;
        }
        HashMap<UUID, String> newPeople = (HashMap<UUID, String>) people.clone();
        for (String name : allies) {
            Town town = TownManager.getInstance().getTown(name);
            for (UUID uuid : town.getRawPeople().keySet()) {
                if (!newPeople.containsKey(uuid)) {
                    newPeople.put(uuid, "ally");
                }
            }
        }
        return newPeople;
    }
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
    protected void setPower(int power) {
        this.power = power;
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

    public ArrayList<Bounty> getBounties() {
        return bounties;
    }
    public void setBounties(ArrayList<Bounty> bounties) {
        this.bounties = bounties;
    }
    public void sortBounties() {
        if (bounties.size() < 2) {
            return;
        }
        Collections.sort(bounties, new Comparator<Bounty>() {
            @Override
            public int compare(Bounty o1, Bounty o2) {
                if (o1.getAmount() == o2.getAmount()) {
                    return 0;
                }
                return o1.getAmount() > o2.getAmount() ? 1 : -1;
            }
        });
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

    private int x = 0;
    private int z = 0;
    public void createRing() {

        //Check if super-region has the effect
        TownType townType = (TownType) ItemManager.getInstance().getItemType(type);
        if (townType == null) {
            return;
        }

        final Location l = location;
        int baseY = l.getWorld().getHighestBlockAt(l).getY();
        baseY = baseY < 64 ? 64 : baseY;
        baseY = baseY + Y_LEVEL > l.getWorld().getMaxHeight() ? l.getWorld().getMaxHeight() - 1 : baseY + Y_LEVEL;
        final int yL = baseY;
        final int radius = townType.getBuildRadius();
        final World world = l.getWorld();
        x = 0;
        z = 0;
        final int threadID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {

                        if (x <= radius) {
                            int xp = (int) l.getX() + x;
                            int xn = (int) l.getX() - x;
                            int asdf = (int) Math.sqrt(radius*radius - (x * x));
                            int zp = asdf + (int) l.getZ();
                            int zn = (int) l.getZ() - asdf;
                            world.getBlockAt(xp, yL, zp).setType(Material.GLOWSTONE);
                            world.getBlockAt(xn, yL, zp).setType(Material.GLOWSTONE);
                            world.getBlockAt(xp, yL, zn).setType(Material.GLOWSTONE);
                            world.getBlockAt(xn, yL, zn).setType(Material.GLOWSTONE);

                        }
                        x++;
                    }
                }, 0, 2L);
        final int threadID1 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {

                        if (z <= radius) {
                            int zp = (int) l.getZ() + z;
                            int zn = (int) l.getZ() - z;
                            int asdf = (int) Math.sqrt(radius*radius - (z * z));
                            int xp = asdf + (int) l.getX();
                            int xn = (int) l.getX() - asdf;
                            world.getBlockAt(xp, yL, zp).setType(Material.GLOWSTONE);
                            world.getBlockAt(xn, yL, zp).setType(Material.GLOWSTONE);
                            world.getBlockAt(xp, yL, zn).setType(Material.GLOWSTONE);
                            world.getBlockAt(xn, yL, zn).setType(Material.GLOWSTONE);

                        }
                        z++;
                    }
                }, 0, 2L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                Bukkit.getServer().getScheduler().cancelTask(threadID);
                Bukkit.getServer().getScheduler().cancelTask(threadID1);
                x =0;
                z=0;
            }
        }, 2 * radius);
    }

    public void destroyRing(boolean destroyAll) {
        removeOuterRing();

        if (!destroyAll) {
            return;
        }

        List<Location> childLocations = getChildLocations();
        if (childLocations == null || childLocations.isEmpty()) {
            return;
        }
        TownType townType = (TownType) ItemManager.getInstance().getItemType(type);
        long delay = (long) townType.getBuildRadius() * 2;
        for (Location l : childLocations) {

            final Location loc = l;
            if (townType.getChild() == null) {
                return;
            }

            townType = (TownType) ItemManager.getInstance().getItemType(townType.getChild());
            final TownType srType = townType;
            if (townType == null) {
                return;
            }

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(),
                    new Runnable() {

                        @Override
                        public void run() {
                            removeRing(loc, srType.getBuildRadius());
                        }

                    }, delay);
            delay += townType.getBuildRadius() * 2;
        }
    }

    private void removeOuterRing() {
        TownType townType = (TownType) ItemManager.getInstance().getItemType(type);
        final int radius = townType.getBuildRadius();
        removeRing(location, radius);
    }

    private void removeRing(final Location l, final int radius) {
        final World world = l.getWorld();
        x = 0;
        z = 0;
        int baseY = l.getWorld().getHighestBlockAt(l).getY();
        baseY = baseY < 64 ? 64 : baseY;
        baseY = baseY + Y_LEVEL > l.getWorld().getMaxHeight() ? l.getWorld().getMaxHeight() - 1 : baseY + Y_LEVEL;
        final int yL = baseY;
        final int threadID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {

                        if (x <= radius) {
                            int xp = (int) l.getX() + x;
                            int xn = (int) l.getX() - x;
                            int asdf = (int) Math.sqrt(radius*radius - (x * x));
                            int zp = asdf + (int) l.getZ();
                            int zn = (int) l.getZ() - asdf;
                            world.getBlockAt(xp, yL, zp).setType(Material.GRAVEL);
                            world.getBlockAt(xn, yL, zp).setType(Material.GRAVEL);
                            world.getBlockAt(xp, yL, zn).setType(Material.GRAVEL);
                            world.getBlockAt(xn, yL, zn).setType(Material.GRAVEL);

                        }
                        x++;
                    }
                }, 0, 2L);
        final int threadID1 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(),
                new Runnable() {
                    @Override
                    public void run() {

                        if (z <= radius) {
                            int zp = (int) l.getZ() + z;
                            int zn = (int) l.getZ() - z;
                            int asdf = (int) Math.sqrt(radius*radius - (z * z));
                            int xp = asdf + (int) l.getX();
                            int xn = (int) l.getX() - asdf;
                            world.getBlockAt(xp, yL, zp).setType(Material.GRAVEL);
                            world.getBlockAt(xn, yL, zp).setType(Material.GRAVEL);
                            world.getBlockAt(xp, yL, zn).setType(Material.GRAVEL);
                            world.getBlockAt(xn, yL, zn).setType(Material.GRAVEL);

                        }
                        z++;
                    }
                }, 0, 2L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                Bukkit.getServer().getScheduler().cancelTask(threadID);
                Bukkit.getServer().getScheduler().cancelTask(threadID1);
                x =0;
                z=0;
            }
        }, 2 * radius);
    }
}
