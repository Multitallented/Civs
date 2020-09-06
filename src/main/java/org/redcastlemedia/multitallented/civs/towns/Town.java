package org.redcastlemedia.multitallented.civs.towns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.Constants;

import lombok.Getter;
import lombok.Setter;

public class Town {
    @Getter @Setter
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
    private int villagers;

    @Getter @Setter
    private double bankAccount;

    @Getter @Setter
    private double taxes;

    @Getter @Setter
    private String governmentType;

    @Getter @Setter
    private String colonialTown;

    @Getter @Setter
    private long lastVote = 0;

    @Getter @Setter
    private HashMap<UUID, HashMap<UUID, Integer>> votes = new HashMap<>();

    @Getter @Setter
    private boolean govTypeChangedToday;

    @Getter @Setter
    private long lastActive;

    @Getter @Setter
    private boolean devolvedToday;

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

    public String getSummary(Player player) {
        String localTownType = LocaleManager.getInstance().getTranslation(player,
                type + LocaleConstants.NAME_SUFFIX);
        StringBuilder ownerString = new StringBuilder();
        for (Map.Entry<UUID, String> entry : people.entrySet()) {
            if (entry.getValue() == null || !entry.getValue().contains(Constants.OWNER)) {
                continue;
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            if (offlinePlayer.getName() != null) {
                ownerString.append(offlinePlayer.getName()).append(", ");
            }
            ownerString = new StringBuilder(ownerString.substring(0, ownerString.length() - 2));
        }

        return LocaleManager.getInstance().getTranslation(player, "town-summary")
                .replace("$1", localTownType).replace("$2", ownerString.toString())
                .replace("$3", "" + power).replace("$4", "" + maxPower)
                .replace("$5", "" + getPopulation()).replace("$6", "" + getHousing());
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
            if (currentRole.contains(role) || currentRole.contains(Constants.OWNER)) {
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

    public double getWorth() {
        double value = 0;
        TownType townType = (TownType) ItemManager.getInstance().getItemType(type);
        UUID uuid = getFirstOwner();
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        value += townType.getPrice(civilian);
        while (townType.getChild() != null && !townType.getChild().isEmpty()) {
            townType = (TownType) ItemManager.getInstance().getItemType(townType.getChild());
            value += townType.getPrice(civilian);
        }
        for (Region region : TownManager.getInstance().getContainingRegions(name)) {
            Set<UUID> owners = region.getOwners();
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            double price = regionType.getRawPrice();
            if (!owners.isEmpty()) {
                UUID regionOwnerUuid = owners.iterator().next();
                Civilian regionCivilian = CivilianManager.getInstance().getCivilian(regionOwnerUuid);
                price = regionType.getPrice(regionCivilian);
            }
            value += price;
        }

        return value;
    }

    public void destroyRing(boolean destroyAll, boolean useGravel) {
        RingBuilder ringBuilder = new RingBuilder(this);
        ringBuilder.destroyRing(destroyAll, useGravel);
    }

    public void createRing() {
        RingBuilder ringBuilder = new RingBuilder(this);
        ringBuilder.createRing();
    }

    public double getHardship() {
        double price = 0;
        for (UUID uuid : people.keySet()) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
            price += civilian.getHardship();
        }
        return price;
    }

    public UUID getFirstOwner() {
        UUID ownerUuid = null;
        for (Map.Entry<UUID, String> entry : people.entrySet()) {
            if (entry.getValue().contains(Constants.OWNER)) {
                ownerUuid = entry.getKey();
                break;
            }
        }
        return ownerUuid;
    }

    public double getPrice() {
        TownType townType = (TownType) ItemManager.getInstance().getItemType(type);
        double townPrice = townType.getRawPrice();
        UUID ownerUuid = getFirstOwner();
        if (ownerUuid != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(ownerUuid);
            townPrice = townType.getPrice(civilian);
        }
        return townPrice;
    }
}
