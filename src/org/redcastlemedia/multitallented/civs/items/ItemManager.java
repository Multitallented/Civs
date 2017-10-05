package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.io.File;
import java.util.*;

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
        loopThroughTypeFiles(typeFolder, null);
    }
    private void loopThroughTypeFiles(File file, List<CivItem> parentList) {
        try {
            if (file.isDirectory()) {
                for (File pFile : file.listFiles()) {
                    List<CivItem> currParentList = new ArrayList<>();

                    loopThroughTypeFiles(pFile, currParentList);
                    FolderType folderType = new FolderType(new ArrayList<String>(),
                            pFile.getName().replace("invisible", ""),
                            CVItem.createCVItemFromString("CHEST"),
                            0,
                            null,
                            currParentList,
                            pFile.getName().contains("invisible"));
                    if (parentList != null) {
                        parentList.add(folderType);
                    }
                }
            } else {
                try {
                    FileConfiguration typeConfig = new YamlConfiguration();
                    typeConfig.load(file);
                    String type = typeConfig.getString("type","region");
                    CivItem civItem = null;
                    if (type.equals("region")) {
                        civItem = loadRegionType(typeConfig);
                    } else if (type.equals("spell")) {
                        civItem = loadSpellType(typeConfig);
                    } else if (type.equals("class")) {
                        civItem = loadClassType(typeConfig);
                    }
                    if (civItem != null && parentList != null) {
                        parentList.add(civItem);
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
    public CivItem loadClassType(FileConfiguration configuration) {
        //TODO load classestype properly
        CVItem icon = CVItem.createCVItemFromString(configuration.getString("icon", "CHEST"));
        String name = configuration.getString("name");
        CivItem civItem = new CivItem(
                configuration.getStringList("reqs"),
                false,
                CivItem.ItemType.SPELL,
                name,
                icon.getMat(),
                icon.getDamage(),
                1,0, -1,
                configuration.getDouble("price", 0),
                configuration.getString("permission"));
        itemTypes.put(name, civItem);
        return civItem;
    }

    public CivItem loadSpellType(FileConfiguration config) {
        //TODO load spelltype properly
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", "CHEST"));
        String name = config.getString("name");
        CivItem civItem = new CivItem(
                config.getStringList("reqs"),
                false,
                CivItem.ItemType.SPELL,
                name,
                icon.getMat(),
                icon.getDamage(),
                1, 0, -1,
                config.getDouble("price", 0),
                config.getString("permission"));
        itemTypes.put(name, civItem);
        return civItem;
    }

    public RegionType loadRegionType(FileConfiguration config) {
        String name = config.getString("name");
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", "CHEST"));
        List<List<CVItem>> reqs = new ArrayList<>();
        for (String req : config.getStringList("build-reqs")) {
            reqs.add(CVItem.createListFromString(req));
        }
        HashSet<String> effects = new HashSet<>();
        effects.addAll(config.getStringList("effects"));
        int buildRadius = config.getInt("build-radius", 5);
        int buildRadiusX = config.getInt("build-radius-x", buildRadius);
        int buildRadiusY = config.getInt("build-radius-y", buildRadius);
        int buildRadiusZ = config.getInt("build-radius-z", buildRadius);
        int effectRadius = config.getInt("effect-radius", buildRadius);
        String rebuild = config.getString("rebuild");
        RegionType regionType = new RegionType(
                name,
                icon,
                config.getStringList("pre-reqs"),
                config.getInt("qty", 1),
                config.getInt("min", 0),
                config.getInt("max", -1),
                config.getDouble("price", 0),
                config.getString("permission"),
                reqs,
                effects,
                buildRadius,
                buildRadiusX,
                buildRadiusY,
                buildRadiusZ,
                effectRadius,
                rebuild,
                config.getString("description", "Please enter a description"));
        itemTypes.put(name.toLowerCase(), regionType);
        return regionType;
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

    public List<CivItem> getShopItems(Civilian civilian, CivItem parent) {
        List<CivItem> shopItems = getAllItemsWithParent(parent);

        return shopItems;
    }
    private List<CivItem> getAllItemsWithParent(CivItem parent) {
        List<CivItem> returnList = new ArrayList<>();
        HashSet<CivItem> checkList = new HashSet<>();
        if (parent == null) {
            for (CivItem civItem : itemTypes.values()) {
                if (civItem.getItemType() == CivItem.ItemType.FOLDER) {
                    checkList.addAll(((FolderType) civItem).getChildren());
                } else if (civItem.getItemType() == CivItem.ItemType.CLASS) {
                    //TODO implement class parents
                }
            }
            for (CivItem civItem : itemTypes.values()) {
                if (checkList.contains(civItem)) {
                    continue;
                }
                returnList.add(civItem);
            }
            return returnList;
        } else {
            if (parent.getItemType().equals(CivItem.ItemType.FOLDER)) {
                return ((FolderType) parent).getChildren();
            } else if (parent.getItemType().equals(CivItem.ItemType.CLASS)) {
                //TODO implement class parents
                return returnList;
            } else {
                return returnList;
            }
        }
    }
}
