package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.*;

public class TownManager {

    private static TownManager townManager = null;
    private HashMap<String, Town> towns = new HashMap<>();
    private List<Town> sortedTowns = new ArrayList<>();
    private HashMap<UUID, Town> invites = new HashMap<>();

    public TownManager() {
        townManager = this;
    }

    public void loadAllTowns() {
        File townFolder = new File(Civs.getInstance().getDataFolder(), "towns");
        if (!townFolder.exists()) {
            townFolder.mkdir();
        }
        try {
            for (File file : townFolder.listFiles()) {
                FileConfiguration config = new YamlConfiguration();
                try {
                    config.load(file);

                    loadTown(config, file.getName().replace(".yml", ""));
                } catch (Exception e) {
                    Civs.logger.warning("Unable to read from towns/" + file.getName());
                }
            }
        } catch (NullPointerException npe) {
            Civs.logger.severe("Unable to read from town folder!");
        }
    }

    public List<Town> getTowns() { return sortedTowns; }
    public Town getTown(String name) {
        return towns.get(name);
    }

    public Town getTownAt(Location location) {
        ItemManager itemManager = ItemManager.getInstance();
        for (Town town : sortedTowns) {
            TownType townType = (TownType) itemManager.getItemType(town.getType());
            int radius = townType.getBuildRadius();
            int radiusY = townType.getBuildRadiusY();
            Location townLocation = town.getLocation();

            if (townLocation.getWorld() == null) {
                continue;
            }
            if (!townLocation.getWorld().equals(location.getWorld())) {
                continue;
            }

            if (townLocation.getX() - radius >= location.getX()) {
                break;
            }

            if (townLocation.getX() + radius >= location.getX() &&
                    townLocation.getZ() + radius >= location.getZ() &&
                    townLocation.getZ() - radius <= location.getZ() &&
                    townLocation.getY() - radiusY <= location.getY() &&
                    townLocation.getY() + radiusY >= location.getY()) {
                return town;
            }

        }
        return null;
    }

    public void checkCriticalRequirements(Region region) {
        Town town = getTownAt(region.getLocation());
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        RegionManager regionManager = RegionManager.getInstance();
        if (town == null) {
            return;
        }
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        if (!townType.getCriticalReqs().contains(region.getType().toLowerCase())) {
            boolean containsReq = false;
            for (String currentReq : regionType.getGroups()) {
                if (townType.getCriticalReqs().contains(currentReq)) {
                    containsReq = true;
                    break;
                }
            }
            if (!containsReq) {
                return;
            }
        }
        boolean hasReq = false;
        outer: for (Region containedRegion :
                regionManager.getContainingRegions(town.getLocation(), townType.getBuildRadius())) {
            if (region.equals(containedRegion)) {
                continue;
            }
            if (containedRegion.getType().equalsIgnoreCase(region.getType())) {
                hasReq = true;
                break;
            }
            RegionType containedType = (RegionType) ItemManager.getInstance().getItemType(containedRegion.getType());
            for (String currentReq : containedType.getGroups()) {
                if (regionType.getGroups().contains(currentReq)) {
                    hasReq = true;
                    break outer;
                }
            }
        }
        if (!hasReq) {
            removeTown(town, true);
        }
    }

    public List<Town> checkIntersect(Location location, TownType townType) {
        Location[] locationCheck = new Location[9];
        locationCheck[0] = location;
        List<Town> towns = new ArrayList<>();
        locationCheck[1] = new Location(location.getWorld(),
                location.getX() + townType.getBuildRadius(),
                Math.min(location.getY() + townType.getBuildRadiusY(), location.getWorld().getMaxHeight()),
                location.getZ() + townType.getBuildRadius());
        locationCheck[2] = new Location(location.getWorld(),
                location.getX() - townType.getBuildRadius(),
                Math.min(location.getY() + townType.getBuildRadiusY(), location.getWorld().getMaxHeight()),
                location.getZ() + townType.getBuildRadius());
        locationCheck[3] = new Location(location.getWorld(),
                location.getX() + townType.getBuildRadius(),
                Math.min(location.getY() + townType.getBuildRadiusY(), location.getWorld().getMaxHeight()),
                location.getZ() - townType.getBuildRadius());
        locationCheck[4] = new Location(location.getWorld(),
                location.getX() - townType.getBuildRadius(),
                Math.min(location.getY() + townType.getBuildRadiusY(), location.getWorld().getMaxHeight()),
                location.getZ() - townType.getBuildRadius());
        locationCheck[5] = new Location(location.getWorld(),
                location.getX() + townType.getBuildRadius(),
                Math.max(location.getY() - townType.getBuildRadiusY(), 0),
                location.getZ() + townType.getBuildRadius());
        locationCheck[6] = new Location(location.getWorld(),
                location.getX() - townType.getBuildRadius(),
                Math.max(location.getY() - townType.getBuildRadiusY(), 0),
                location.getZ() + townType.getBuildRadius());
        locationCheck[7] = new Location(location.getWorld(),
                location.getX() + townType.getBuildRadius(),
                Math.max(location.getY() - townType.getBuildRadiusY(), 0),
                location.getZ() - townType.getBuildRadius());
        locationCheck[8] = new Location(location.getWorld(),
                location.getX() - townType.getBuildRadius(),
                Math.max(location.getY() - townType.getBuildRadiusY(), 0),
                location.getZ() - townType.getBuildRadius());
        for (Location currentLocation : locationCheck) {
            Town town = getTownAt(currentLocation);
            if (town != null && !towns.contains(town)) {
                towns.add(town);
            }
        }
        return towns;
    }

    private void loadTown(FileConfiguration config, String name) {

        HashMap<UUID, String> people = new HashMap<>();
        for (String key : config.getConfigurationSection("people").getKeys(false)) {
            people.put(UUID.fromString(key), config.getString("people." + key));
        }
        int maxPower = config.getInt("max-power", 500);
        int power = config.getInt("power", maxPower);
        int housing = config.getInt("housing", 0);
        int villagers = config.getInt("villagers", 0);
        long lastDisable = config.getLong("last-disable", -1);
        GovernmentType governmentType = GovernmentType.valueOf(config.getString("gov-type", "DICTATORSHIP"));
        Town town = new Town(name,
                config.getString("type"),
                Region.idToLocation(config.getString("location")),
                people,
                power,
                maxPower,
                housing,
                villagers,
                lastDisable);
        town.setGovernmentType(governmentType);
        if (config.isSet("bounties")) {
            town.setBounties(Util.readBountyList(config));
        }
        if (config.isSet("child-locations")) {
            List<Location> locationList = new ArrayList<>();
            for (String locationString : config.getStringList("child-locations")) {
                locationList.add(Region.idToLocation(locationString));
            }
            town.setChildLocations(locationList);
        }
        addTown(town);
    }
    public void addTown(Town town) {
        towns.put(town.getName(), town);
        sortedTowns.add(town);
        if (sortedTowns.size() > 1) {
            Collections.sort(sortedTowns, new Comparator<Town>() {

                @Override
                public int compare(Town o1, Town o2) {
                    ItemManager itemManager = ItemManager.getInstance();
                    TownType townType1 = (TownType) itemManager.getItemType(o1.getType());
                    TownType townType2 = (TownType) itemManager.getItemType(o2.getType());
                    if (o1.getLocation().getX() - townType1.getBuildRadius() >
                            o2.getLocation().getX() - townType2.getBuildRadius()) {
                        return 1;
                    } else if (o1.getLocation().getX() - townType1.getBuildRadius() <
                            o2.getLocation().getX() - townType2.getBuildRadius()) {
                        return -1;
                    }
                    return 0;
                }
            });
        }
    }
    public void removeTown(Town town, boolean broadcast) {
        removeTown(town, broadcast, true);
    }
    public void removeTown(Town town, boolean broadcast, boolean destroyRing) {
        if (broadcast) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Civilian civ = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civ.getLocale(),
                        "town-destroyed").replace("$1", town.getName()));
            }
        }
        towns.remove(town.getName());
        sortedTowns.remove(town);
        if (Civs.getInstance() == null) {
            return;
        }
        if (destroyRing && ConfigManager.getInstance().getTownRings()) {
            town.destroyRing(true, broadcast);
        }
        removeTownFile(town.getName());
    }

    public void setTownPower(Town town, int power) {
        if (power > town.getMaxPower()) {
            town.setPower(town.getMaxPower());
        } else {
            town.setPower(power);
        }
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        if (town.getPower() < 1 && ConfigManager.getInstance().getDestroyTownsAtZero() &&
                townType.getChild() == null) {
            TownManager.getInstance().removeTown(town, true);
        } else {
            if (town.getPower() < 1) {
                if (townType.getChild() != null) {
                    devolveTown(town, townType);
                } else {
                    hasGrace(town, true);
                }
            } else {
                TownManager.getInstance().saveTown(town);
            }
        }
    }

    private void devolveTown(Town town, TownType townType) {
        if (townType.getChild() == null) {
            return;
        }
        town.destroyRing(false, true);
        TownType childTownType = (TownType) ItemManager.getInstance().getItemType(townType.getChild());
        town.setType(childTownType.getProcessedName());
        town.setPower(childTownType.getPower());
        town.setMaxPower(childTownType.getMaxPower());
        TownManager.getInstance().saveTown(town);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            player.sendMessage(ChatColor.RED + ChatColor.stripColor(Civs.getPrefix()) +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "devolve-town")
                    .replace("$1", town.getName())
                    .replace("$2", childTownType.getProcessedName()));
        }
    }

    private void removeTownFile(String townName) {
        File townFolder = new File(Civs.getInstance().getDataFolder(), "towns");
        if (!townFolder.exists()) {
            townFolder.mkdir();
        }
        File townFile = new File(townFolder, townName + ".yml");
        townFile.delete();
    }

    public boolean hasGrace(Town town, boolean disable) {
        long grace = getRemainingGracePeriod(town);
        if (grace < 0 && disable) {
            long lastDisable = (ConfigManager.getInstance().getTownGracePeriod() * 1000) + System.currentTimeMillis();
            town.setLastDisable(lastDisable);
            TownManager.getInstance().saveTown(town);
            return true;
        }
        if (!disable) {
            if (grace > -1) {
                town.setLastDisable(-1);
                TownManager.getInstance().saveTown(town);
            }
            return true;
        }
        return grace != 0;
    }

    public long getRemainingGracePeriod(Town town) {
        if (town == null) {
            return 0;
        }
        if (town.getLastDisable() < 1) {
            return -1;
        }
        return Math.max(0, town.getLastDisable() - System.currentTimeMillis());
    }

    public void addInvite(UUID uuid, Town town) {
        invites.put(uuid, town);
    }
    public void clearInvite(UUID uuid) {
        invites.remove(uuid);
    }
    public Town getInviteTown(UUID uuid) {
        return invites.get(uuid);
    }
    public boolean acceptInvite(UUID uuid) {
        if (!invites.containsKey(uuid)) {
            return false;
        }
        Town town = invites.get(uuid);
        town.setPeople(uuid, "member");
        saveTown(town);
        invites.remove(uuid);
        return true;
    }

    public Set<Region> getContainingRegions(String townName) {
        Town town = getTown(townName);
        if (town == null) {
            return new HashSet<>();
        }

        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        return RegionManager.getInstance().getRegionsXYZ(town.getLocation(), townType.getBuildRadius(),
                townType.getBuildRadiusY(), townType.getBuildRadius(), false);
    }

    public void saveTown(Town town) {
        if (Civs.getInstance() == null) {
            return;
        }
        File townFolder = new File(Civs.getInstance().getDataFolder(), "towns");
        if (!townFolder.exists()) {
            townFolder.mkdir();
        }
        File townFile = new File(townFolder, town.getName() + ".yml");
        try {
            if (!townFile.exists()) {
                townFile.createNewFile();
            }
            FileConfiguration config = new YamlConfiguration();
            config.load(townFile);


            config.set("name", town.getName());
            config.set("type", town.getType());
            config.set("location", Region.locationToString(town.getLocation()));
            for (UUID key : town.getPeople().keySet()) {
                config.set("people." + key, town.getPeople().get(key));
            }
            List<String> locationList = new ArrayList<>();
            for (Location lo : town.getChildLocations()) {
                locationList.add(Region.locationToString(lo));
            }
            config.set("child-locations", locationList);
            config.set("housing", town.getHousing());
            config.set("population", town.getPopulation());
            config.set("villagers", town.getVillagers());
            config.set("last-disable", town.getLastDisable());
            config.set("power", town.getPower());
            config.set("max-power", town.getMaxPower());
            config.set("gov-type", town.getGovernmentType());

            if (town.getBounties() != null && !town.getBounties().isEmpty()) {
                for (int i = 0; i < town.getBounties().size(); i++) {
                    if (town.getBounties().get(i).getIssuer() != null) {
                        config.set("bounties." + i + ".issuer", town.getBounties().get(i).getIssuer().toString());
                    }
                    config.set("bounties." + i + ".amount", town.getBounties().get(i).getAmount());
                }
            } else {
                config.set("bounties", null);
            }

            //TODO save all town properties
            config.save(townFile);
        } catch (Exception e) {
            e.printStackTrace();
            Civs.logger.severe("Unable to save town " + town.getName() + ".yml");
        }
    }

    public static TownManager getInstance() {
        if (townManager == null) {
            new TownManager();
        }
        return townManager;
    }

    public Set<Town> findCommonTowns(Civilian damagerCiv, Civilian dyingCiv) {
        HashSet<Town> commonTowns = new HashSet<>();
        for (Town town : sortedTowns) {
            if (town.getPeople().containsKey(damagerCiv.getUuid()) &&
                    town.getPeople().containsKey(dyingCiv.getUuid())) {
                commonTowns.add(town);
            }
        }
        return commonTowns;
    }

    public Town isOwnerOfATown(Civilian civilian) {
        for (Town town : sortedTowns) {
            if (!town.getRawPeople().containsKey(civilian.getUuid()) ||
                    !town.getRawPeople().get(civilian.getUuid()).equals("owner")) {
                continue;
            }
            return town;
        }
        return null;
    }

    public boolean townNameExists(String name) {
        for (String townName : towns.keySet()) {
            if (name.toLowerCase().equalsIgnoreCase(townName)) {
                return true;
            }
        }
        return false;
    }
}
