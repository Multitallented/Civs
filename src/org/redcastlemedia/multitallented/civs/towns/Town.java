package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.items.ItemManager;

import java.util.*;

import lombok.Getter;
import lombok.Setter;

public class Town {
    @Getter
    @Setter
    private String type;

    private int maxPower;
    private int power;
    private Location location;
    private String name;
    private HashMap<UUID, String> people;
    private int housing;
    private ArrayList<Bounty> bounties = new ArrayList<>();
    private List<String> allyInvites = new ArrayList<>();
    private List<Location> childLocations = new ArrayList<>();
    @Getter @Setter
    private HashMap<String, String> effects = new HashMap<>();
    private long lastDisable;
    private final int Y_LEVEL = 80;
    private int villagers;

    @Getter
    @Setter
    private double bankAccount;

    @Getter
    @Setter
    private double taxes;

    @Getter
    @Setter
    private String governmentType;

    @Getter
    @Setter
    private String colonialTown;

    @Getter
    @Setter
    private long lastVote = 0;

    @Getter
    @Setter
    private HashMap<UUID, HashMap<UUID, Integer>> votes = new HashMap<>();

    @Getter
    @Setter
    private boolean govTypeChangedToday;

    @Getter
    @Setter
    private long lastActive;
    @Getter @Setter
    private HashMap<UUID, Integer> idiocracyScore = new HashMap<>();

    @Getter
    private HashSet<UUID> revolt = new HashSet<>();

    public Town(String name, String type, Location location, HashMap<UUID, String> people, int power, int maxPower,
                int housing, int villagers, long lastDisable) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.people = people;
        this.power = power;
        this.maxPower = maxPower;
        this.housing = housing;
        this.villagers = villagers;
        this.lastDisable = lastDisable;
        TownType townType = (TownType) ItemManager.getInstance().getItemType(type);
        governmentType = townType.getDefaultGovType();
    }

    public long getLastDisable() {
        return lastDisable;
    }
    public void setLastDisable(long lastDisable) {
        this.lastDisable = lastDisable;
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
    public void setPeople(UUID uuid, String role) {
        people.put(uuid, role);
    }
    public HashMap<UUID, String> getPeople() {
        HashSet<Alliance> allies = new HashSet<>(AllianceManager.getInstance().getAlliances(this));
        if (allies.isEmpty()) {
            return people;
        }
        HashMap<UUID, String> newPeople = new HashMap<>(people);
        for (Alliance alliance : allies) {
            HashSet<String> removeMembers = new HashSet<>();
            for (String townName : alliance.getMembers()) {
                if (townName.equals(name)) {
                    continue;
                }
                Town town = TownManager.getInstance().getTown(townName);
                if (town == null) {
                    removeMembers.add(townName);
                    continue;
                }
                for (UUID uuid : town.getRawPeople().keySet()) {
                    if (!newPeople.containsKey(uuid) &&
                            !town.getRawPeople().get(uuid).contains("ally")) {
                        newPeople.put(uuid, "allyforeign");
                    }
                }
            }
            boolean needsSaving = false;
            for (String townName : removeMembers) {
                alliance.getMembers().remove(townName);
                needsSaving = true;
            }
            if (needsSaving) {
                AllianceManager.getInstance().saveAlliance(alliance);
            }
        }
        return newPeople;
    }
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
        int memberCount = 0;
        for (String role : people.values()) {
            if (!role.contains("ally")) {
                memberCount++;
            }
        }
        return memberCount + getVillagers();
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
        final Material material = ConfigManager.getInstance().getTownRingMat();
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
                            world.getBlockAt(xp, yL, zp).setType(material);
                            world.getBlockAt(xn, yL, zp).setType(material);
                            world.getBlockAt(xp, yL, zn).setType(material);
                            world.getBlockAt(xn, yL, zn).setType(material);

                        }
                        x++;
                    }
                }, 0, 4L);
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
                            world.getBlockAt(xp, yL, zp).setType(material);
                            world.getBlockAt(xn, yL, zp).setType(material);
                            world.getBlockAt(xp, yL, zn).setType(material);
                            world.getBlockAt(xn, yL, zn).setType(material);

                        }
                        z++;
                    }
                }, 2, 4L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                Bukkit.getServer().getScheduler().cancelTask(threadID);
                Bukkit.getServer().getScheduler().cancelTask(threadID1);
                x =0;
                z=0;
            }
        }, 4 * radius + 2);
    }

    public void destroyRing(boolean destroyAll, boolean useGravel) {
        if (Civs.getInstance() == null) {
            return;
        }
        if (!ConfigManager.getInstance().isTownRingsCrumbleToGravel()) {
            useGravel = false;
        }
        final boolean finalUseGravel = useGravel;
        removeOuterRing(useGravel);

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
                            removeRing(loc, srType.getBuildRadius(), finalUseGravel);
                        }

                    }, delay);
            delay += townType.getBuildRadius() * 2;
        }
    }

    private void removeOuterRing(boolean useGravel) {
        TownType townType = (TownType) ItemManager.getInstance().getItemType(type);
        final int radius = townType.getBuildRadius();
        removeRing(location, radius, useGravel);
    }

    private void removeRing(final Location l, final int radius, boolean setGravel) {
        final World world = l.getWorld();
        if (world == null) {
            return;
        }
        x = 0;
        z = 0;
        int baseY = l.getWorld().getHighestBlockAt(l).getY();
        baseY = baseY < 64 ? 64 : baseY;
        baseY = baseY + Y_LEVEL > l.getWorld().getMaxHeight() ? l.getWorld().getMaxHeight() - 1 : baseY + Y_LEVEL;
        final int yL = baseY;
        final Material material = ConfigManager.getInstance().getTownRingMat();
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
                            if (setGravel) {
                                Block block1 = world.getBlockAt(xp, yL, zp);
                                if (block1.getType() == material) {
                                    block1.setType(Material.GRAVEL);
                                }
                                Block block2 = world.getBlockAt(xn, yL, zp);
                                if (block2.getType() == material) {
                                    block2.setType(Material.GRAVEL);
                                }
                                Block block3 = world.getBlockAt(xp, yL, zn);
                                if (block3.getType() == material) {
                                    block3.setType(Material.GRAVEL);
                                }
                                Block block4 = world.getBlockAt(xn, yL, zn);
                                if (block4.getType() == material) {
                                    block4.setType(Material.GRAVEL);
                                }
                            } else {
                                Block block1 = world.getBlockAt(xp, yL, zp);
                                if (block1.getType() == material) {
                                    block1.setType(Material.AIR);
                                }
                                Block block2 = world.getBlockAt(xn, yL, zp);
                                if (block2.getType() == material) {
                                    block2.setType(Material.AIR);
                                }
                                Block block3 = world.getBlockAt(xp, yL, zn);
                                if (block3.getType() == material) {
                                    block3.setType(Material.AIR);
                                }
                                Block block4 = world.getBlockAt(xn, yL, zn);
                                if (block4.getType() == material) {
                                    block4.setType(Material.AIR);
                                }
                            }

                        }
                        x++;
                    }
                }, 2, 4L);
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
                            if (setGravel) {
                                world.getBlockAt(xp, yL, zp).setType(Material.GRAVEL);
                                world.getBlockAt(xn, yL, zp).setType(Material.GRAVEL);
                                world.getBlockAt(xp, yL, zn).setType(Material.GRAVEL);
                                world.getBlockAt(xn, yL, zn).setType(Material.GRAVEL);
                            } else {
                                world.getBlockAt(xp, yL, zp).setType(Material.AIR);
                                world.getBlockAt(xn, yL, zp).setType(Material.AIR);
                                world.getBlockAt(xp, yL, zn).setType(Material.AIR);
                                world.getBlockAt(xn, yL, zn).setType(Material.AIR);
                            }


                        }
                        z++;
                    }
                }, 0, 4L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                Bukkit.getServer().getScheduler().cancelTask(threadID);
                Bukkit.getServer().getScheduler().cancelTask(threadID1);
                x =0;
                z=0;
            }
        }, 4 * radius + 2);
    }
}
