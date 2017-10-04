package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

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
                        loadSpellType(typeConfig);
                    }
                } catch (Exception e) {
                    Civs.logger.severe("Unable to read from " + file.getName());
                }
            }
        } catch (NullPointerException npe) {
            Civs.logger.warning("No region types found in " + file.getName());
            return;
        }
    }
    public void loadSpellType(FileConfiguration config) {
        //TODO load spells
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", "CHEST"));
        CivItem civItem = new CivItem(
                config.getStringList("reqs"),
                false,
                CivItem.ItemType.SPELL,
                config.getString("name"),
                icon.getMat(),
                icon.getDamage(),
                config.getInt("qty", 1),
                config.getInt("min", 0),
                config.getInt("max", -1));
        itemTypes.put(config.getString("name"), civItem);
    }

    public void loadRegionType(FileConfiguration config) {
        String name = config.getString("name");
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", "CHEST"));
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
                icon,
                config.getStringList("pre-reqs"),
                config.getInt("qty", 1),
                config.getInt("min", 0),
                config.getInt("max", -1),
                reqs,
                effects,
                buildRadius,
                buildRadiusX,
                buildRadiusY,
                buildRadiusZ,
                effectRadius,
                rebuild));
    }

    public ArrayList<CivItem> loadCivItems(FileConfiguration civConfig, UUID uuid) {
        ArrayList<CivItem> items = new ArrayList<>();
        for (String key : civConfig.getConfigurationSection("items").getKeys(false)) {
            CivItem currentItem = getItemType(civConfig.getString("items." + key + ".name"));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(uuid.toString());
            currentItem.setLore(lore);
            items.add(currentItem);
        }
        return items;
    }

    public CivItem getItemType(String name) {
        return itemTypes.get(name);
    }

    public ArrayList<CivItem> getNewItems() {
        ArrayList<CivItem> newItems = new ArrayList<>();
        for (CivItem civItem : itemTypes.values()) {
            if (civItem.getCivReqs().isEmpty()) {
                newItems.add(civItem);
            }
        }
        return newItems;
    }
}
