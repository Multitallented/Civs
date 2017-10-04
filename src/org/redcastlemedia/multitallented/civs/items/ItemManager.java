package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class ItemManager {
    private static ItemManager itemManager;
    private HashMap<String, CivItem> itemTypes = new HashMap<>();

    public ItemManager() {
        itemManager = this;
        loadAllItemTypes();
    }

    public static ItemManager getInstance() {
        if (itemManager == null) {
            itemManager = new ItemManager();
        }
        return itemManager;
    }

    private void loadAllItemTypes() {
        Civs civs = Civs.getInstance();
        if (civs == null) {
            return;
        }
        File typeFolder = new File(civs.getDataFolder(), "item-types");
        if (!typeFolder.exists()) {
            typeFolder.mkdir();
        }
        loopThroughTypeFiles(typeFolder);
    }
    private void loopThroughTypeFiles(File file) {
        try {
            if (file.isDirectory()) {
                for (File pFile : file.listFiles()) {
                    loopThroughTypeFiles(pFile);
                }
            } else {
                try {
                    FileConfiguration typeConfig = new YamlConfiguration();
                    typeConfig.load(file);
                    String type = typeConfig.getString("type","region");
                    if (type.equals("region")) {
                        loadRegionType(typeConfig);
                    } else if (type.equals("spell")) {
                        //TODO load spells
                    }
                } catch (Exception e) {
                    Civs.logger.severe(Civs.getPrefix() + "Unable to read from " + file.getName());
                }
            }
        } catch (NullPointerException npe) {
            Civs.logger.warning(Civs.getPrefix() + "No region types found in " + file.getName());
            return;
        }
    }

    public void loadRegionType(FileConfiguration config) {
        String name = config.getString("name");
        HashSet<CVItem> reqs = new HashSet<>();
        for (String req : config.getStringList("requirements")) {
            reqs.add(CVItem.createCVItemFromString(req));
        }
        HashSet<String> effects = new HashSet<>();
        effects.addAll(config.getStringList("effects"));
        int buildRadius = config.getInt("build-radius", 5);
        int buildRadiusX = config.getInt("build-radius-x", buildRadius);
        int buildRadiusY = config.getInt("build-radius-y", buildRadius);
        int buildRadiusZ = config.getInt("build-radius-z", buildRadius);
        int effectRadius = config.getInt("effect-radius", buildRadius);
        String rebuild = config.getString("rebuild");
        itemTypes.put(name.toLowerCase(), new RegionType(
                name,
                reqs,
                effects,
                buildRadius,
                buildRadiusX,
                buildRadiusY,
                buildRadiusZ,
                effectRadius,
                rebuild));
    }

    public CivItem getItemType(String name) {
        return itemTypes.get(name);
    }
}
