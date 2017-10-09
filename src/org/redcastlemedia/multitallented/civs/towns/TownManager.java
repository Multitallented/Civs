package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;

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

            if (townLocation.getX() - radius > location.getX()) {
                break;
            }

            if (townLocation.getX() + radius > location.getX() &&
                    townLocation.getZ() + radius > location.getZ() &&
                    townLocation.getZ() - radius < location.getZ() &&
                    townLocation.getY() - radiusY < location.getY() &&
                    townLocation.getY() + radiusY > location.getY()) {
                return town;
            }

        }
        return null;
    }

    private void loadTown(FileConfiguration config, String name) {

        Set<UUID> owners = new HashSet<>();
        for (String s : config.getStringList("owners")) {
            owners.add(UUID.fromString(s));
        }
        Town town = new Town(name,
                config.getString("type"),
                Region.idToLocation(config.getString("location")),
                owners);
        addTown(town);
    }
    void addTown(Town town) {
        towns.put(town.getName().toLowerCase(), town);
        sortedTowns.add(town);
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
    public void addInvite(UUID uuid, Town town) {
        invites.put(uuid, town);
    }
    public void clearInvite(UUID uuid) {
        invites.remove(uuid);
    }

    public void saveTown(Town town) {
        if (Civs.getInstance() == null) {
            return;
        }
        File townFolder = new File(Civs.getInstance().getDataFolder(), "towns");
        if (!townFolder.exists()) {
            townFolder.mkdir();
        }
        File townFile = new File(townFolder, town.getName().toLowerCase() + ".yml");
        try {
            if (!townFile.exists()) {
                townFile.createNewFile();
            }
            FileConfiguration config = new YamlConfiguration();
            config.load(townFile);

            List<String> tempOwners = new ArrayList<>();
            for (UUID uuid : town.getOwners()) {
                tempOwners.add(uuid.toString());
            }

            config.set("name", town.getName());
            config.set("type", town.getType());
            config.set("location", Region.locationToString(town.getLocation()));
            config.set("owners", tempOwners);

            //TODO save all town properties
            config.save(townFile);
        } catch (Exception e) {
            Civs.logger.severe("Unable to save town " + town.getName().toLowerCase() + ".yml");
        }
    }

    public static TownManager getInstance() {
        if (townManager == null) {
            new TownManager();
        }
        return townManager;
    }
}
