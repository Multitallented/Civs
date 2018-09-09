package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionUpkeep;
import org.redcastlemedia.multitallented.civs.spells.SpellType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

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
        try {
            for (File file : typeFolder.listFiles()) {
                loopThroughTypeFiles(file, null);
            }
        } catch (NullPointerException e) {
            Civs.logger.severe("Unable to read from item-types folder");
            e.printStackTrace();
        }
    }
    private void loopThroughTypeFiles(File file, List<CivItem> parentList) throws NullPointerException {
        try {
            if (file.isDirectory() && !file.getName().contains(".yml")) {
                List<CivItem> currParentList = new ArrayList<>();
                for (File pFile : file.listFiles()) {

                    loopThroughTypeFiles(pFile, currParentList);
                }
                String folderName = file.getName().replace("-invisible", "");
                FolderType folderType = new FolderType(new ArrayList<String>(),
                        folderName,
                        ConfigManager.getInstance().getFolderIcon(folderName.toLowerCase()),
                        0,
                        null,
                        currParentList,
                        !file.getName().contains("invisible"));
                itemTypes.put(folderName.toLowerCase(), folderType);
                if (parentList != null) {
                    parentList.add(folderType);
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
                    } else if (type.equals("town")) {
                        civItem = loadTownType(typeConfig, file.getName().replace(".yml", ""));
                    }
                    if (civItem != null && parentList != null) {
                        parentList.add(civItem);
                    }
                } catch (Exception e) {
                    Civs.logger.severe("Unable to read from " + file.getName());
                    e.printStackTrace();
                }
            }
        } catch (NullPointerException npe) {
            Civs.logger.warning("No region types found in " + file.getName());
            return;
        }
    }
    public CivItem loadClassType(FileConfiguration configuration) throws NullPointerException {
        //TODO load classestype properly
        CVItem icon = CVItem.createCVItemFromString(configuration.getString("icon", "CHEST"));
        String name = configuration.getString("name");
        ConfigurationSection configurationSection = configuration.getConfigurationSection("description");
        HashMap<String, String> description = new HashMap<>();
        if (configurationSection != null) {
            for (String key : configurationSection.getKeys(false)) {
                description.put(key, configurationSection.getString(key));
            }
        }
        ClassType civItem = new ClassType(
                configuration.getStringList("reqs"),
                name,
                icon,
                configuration.getDouble("price", 0),
                configuration.getString("permission"),
                configuration.getStringList("children"),
                description,
                configuration.getStringList("groups"),
                configuration.getInt("mana-per-second", 1),
                configuration.getInt("max-mana", 100));

        itemTypes.put(name, civItem);
        return civItem;
    }

    public CivItem loadSpellType(FileConfiguration config) throws NullPointerException {
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", "CHEST"));
        String name = config.getString("name");
        ConfigurationSection configurationSection = config.getConfigurationSection("description");
        HashMap<String, String> description = new HashMap<>();
        if (configurationSection != null) {
            for (String key : configurationSection.getKeys(false)) {
                description.put(key, configurationSection.getString(key));
            }
        }
        SpellType spellType = new SpellType(
                config.getStringList("reqs"),
                name,
                icon.getMat(),
                config.getInt("qty", 0),
                config.getInt("min", 0),
                config.getInt("max", -1),
                config.getDouble("price", 0),
                config.getString("permission"),
                description,
                config.getStringList("groups"),
                config);
        itemTypes.put(name.toLowerCase(), spellType);
        return spellType;
    }

    private HashMap<String, Integer> convertListToMap(List<String> inputList) {
        HashMap<String, Integer> returnMap = new HashMap<>();
        for (String currentString : inputList) {
            String[] splitString = currentString.split(":");
            if (splitString.length != 2) {
                returnMap.put(splitString[0], 1);
            } else {
                returnMap.put(splitString[0], Integer.parseInt(splitString[1]));
            }
        }
        return returnMap;
    }

    public TownType loadTownType(FileConfiguration config, String name) throws NullPointerException {
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", "STONE"));
        HashSet<String> effects = new HashSet<>();
        effects.addAll(config.getStringList("effects"));
        int buildRadius = config.getInt("build-radius", 20);
        ConfigurationSection configurationSection = config.getConfigurationSection("description");
        HashMap<String, String> description = new HashMap<>();
        if (configurationSection != null) {
            for (String key : configurationSection.getKeys(false)) {
                description.put(key, configurationSection.getString(key));
            }
        }
        TownType townType = new TownType(
                name,
                icon,
                config.getStringList("pre-reqs"),
                config.getInt("qty", 0),
                config.getInt("min",0),
                config.getInt("max", -1),
                config.getDouble("price", 0),
                config.getString("permission"),
                convertListToMap(config.getStringList("build-reqs")),
                convertListToMap(config.getStringList("limits")),
                effects,
                buildRadius,
                config.getInt("build-radius-y", buildRadius),
                config.getStringList("critical-build-reqs"),
                description,
                config.getInt("power", 200),
                config.getInt("max-power", 1000),
                config.getStringList("groups"),
                config.getString("child"));
        itemTypes.put(Util.getValidFileName(name).toLowerCase(), townType);
        return townType;
    }

    public RegionType loadRegionType(FileConfiguration config) throws NullPointerException {
        String name = config.getString("name");
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", "CHEST"));
        List<List<CVItem>> reqs = new ArrayList<>();
        for (String req : config.getStringList("build-reqs")) {
            reqs.add(CVItem.createListFromString(req));
        }
        List<RegionUpkeep> upkeeps = new ArrayList<>();
        ConfigurationSection upkeepSection = config.getConfigurationSection("upkeep");
        if (upkeepSection != null) {
            for (String key : upkeepSection.getKeys(false)) {
                List<List<CVItem>> reagents = new ArrayList<>();
                for (String reagent : config.getStringList("upkeep." + key + ".reagents")) {
                    reagents.add(CVItem.createListFromString(reagent));
                }
                List<List<CVItem>> inputs = new ArrayList<>();
                for (String input : config.getStringList("upkeep." + key + ".input")) {
                    inputs.add(CVItem.createListFromString(input));
                }
                List<List<CVItem>> outputs = new ArrayList<>();
                for (String output : config.getStringList("upkeep." + key + ".output")) {
                    outputs.add(CVItem.createListFromString(output));
                }
                double payout = config.getDouble("upkeep." + key + ".payout", 0);
                RegionUpkeep regionUpkeep = new RegionUpkeep(reagents, inputs, outputs, payout);
                regionUpkeep.setPowerReagent(config.getInt("upkeep." + key + ".power-reagent", 0));
                regionUpkeep.setPowerInput(config.getInt("upkeep." + key + ".power-input", 0));
                regionUpkeep.setPowerOutput(config.getInt("upkeep." + key + ".power-output", 0));
                upkeeps.add(regionUpkeep);
            }
        }
        HashSet<String> townSet;
        if (config.isSet("towns")) {
            townSet = new HashSet<>(config.getStringList("towns"));
        } else {
            townSet = null;
        }
        HashMap<String, String> effects = new HashMap<>();
        for (String s : config.getStringList("effects")) {
            String[] effectSplit = s.split(":");
            if (effectSplit.length > 1) {
                effects.put(effectSplit[0], effectSplit[1]);
            } else {
                effects.put(s, null);
            }
        }
        int buildRadius = config.getInt("build-radius", 5);
        int buildRadiusX = config.getInt("build-radius-x", buildRadius);
        int buildRadiusY = config.getInt("build-radius-y", buildRadius);
        int buildRadiusZ = config.getInt("build-radius-z", buildRadius);
        int effectRadius = config.getInt("effect-radius", buildRadius);
        String rebuild = config.getString("rebuild", null);
        ConfigurationSection configurationSection = config.getConfigurationSection("description");
        HashMap<String, String> description = new HashMap<>();
        if (configurationSection != null) {
            for (String key : configurationSection.getKeys(false)) {
                description.put(key, configurationSection.getString(key));
            }
        }
        RegionType regionType = new RegionType(
                name,
                icon,
                config.getStringList("pre-reqs"),
                config.getInt("qty", 0),
                config.getInt("min", 0),
                config.getInt("max", -1),
                config.getDouble("price", 0),
                config.getString("permission"),
                reqs,
                upkeeps,
                effects,
                buildRadius,
                buildRadiusX,
                buildRadiusY,
                buildRadiusZ,
                effectRadius,
                rebuild,
                townSet,
                description,
                config.getLong("period", 0),
                config.getString("period", "false").equals("daily"),
                config.getStringList("groups"));
        itemTypes.put(name.toLowerCase(), regionType);
        return regionType;
    }

    public ArrayList<CivItem> loadCivItems(FileConfiguration civConfig, UUID uuid) {
        ArrayList<CivItem> items = new ArrayList<>();
        ConfigurationSection configurationSection = civConfig.getConfigurationSection("items");
        if (configurationSection == null) {
            return items;
        }
        for (String key : configurationSection.getKeys(false)) {
            CivItem currentItem = getItemType(key);
            if (currentItem == null) {
                continue;
            }
            currentItem.setQty(civConfig.getInt("items." + key));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(uuid.toString());
            currentItem.setLore(lore);
            items.add(currentItem);
        }
        return items;
    }

    public CivItem getItemType(String name) {
        return itemTypes.get(name.replace("Civs ", "").toLowerCase());
    }

    public ArrayList<CivItem> getNewItems() {
        ArrayList<CivItem> newItems = new ArrayList<>();
        for (CivItem civItem : itemTypes.values()) {
            if (civItem.getCivReqs().isEmpty() && civItem.getCivQty() > 0) {
                CVItem newItem = civItem.clone();
                newItem.setQty(civItem.getCivQty());
                newItems.add(civItem);
            } else if (civItem.getCivReqs().isEmpty() && civItem.getCivMin() > 0) {
                CVItem newItem = civItem.clone();
                newItem.setQty(civItem.getCivMin());
                newItems.add(civItem);
            }
        }
        return newItems;
    }

    public List<CivItem> getShopItems(Civilian civilian, CivItem parent) {
        return getAllItemsWithParent(civilian, parent);
    }
    private List<CivItem> getAllItemsWithParent(Civilian civilian, CivItem parent) {
        List<CivItem> returnList = new ArrayList<>();
        HashSet<CivItem> checkList = new HashSet<>();
        if (parent == null) {
            for (CivItem civItem : itemTypes.values()) {
                if (civItem.getItemType() == CivItem.ItemType.FOLDER) {
                    checkList.addAll(((FolderType) civItem).getChildren());
                } else if (civItem.getItemType() == CivItem.ItemType.CLASS) {
                    for (String key : ((ClassType) civItem).getChildren()) {
                        if (getItemType(key) != null) {
                            checkList.add(getItemType(key));
                        }
                    }
                }
            }
            for (CivItem civItem : itemTypes.values()) {
                if (checkList.contains(civItem)) {
                    continue;
                }
                returnList.add(civItem);
            }
        } else {
            if (parent.getItemType().equals(CivItem.ItemType.FOLDER)) {
                returnList.addAll(((FolderType) parent).getChildren());
            } else if (parent.getItemType().equals(CivItem.ItemType.CLASS)) {
                for (String key : ((ClassType) parent).getChildren()) {
                    if (getItemType(key) != null) {
                        checkList.add(getItemType(key));
                    }
                }
            }
        }
        checkList.clear();
        for (CivItem item : returnList) {
            if (!hasItemUnlocked(civilian, item)) {
                checkList.add(item);
            }
        }
        returnList.removeAll(checkList);
        return returnList;
    }

    boolean hasItemUnlocked(Civilian civilian, CivItem civItem) {
        if (civItem.getCivReqs().isEmpty()) {
            return true;
        }
        Player player = Bukkit.getPlayer(civilian.getUuid());
        outer: for (String reqString : civItem.getCivReqs()) {
            for (String req : reqString.split("\\|")) {
                if (req.startsWith("perm")) {
                    String permission = req.replace("perm=", "");
                    if (Civs.perm != null &&
                            Civs.perm.has(player, permission)) {
                        continue outer;
                    } else {
                        break;
                    }
                }
                String[] splitReq = req.split(":");
                CivItem reqItem = itemManager.getItemType(splitReq[0]);
                if (reqItem == null) {
                    continue;
                }
                if (splitReq.length < 2) {
                    if (civilian.getCountStashItems(splitReq[0]) > 0 ||
                            civilian.getCountNonStashItems(splitReq[0]) > 0) {
                        continue outer;
                    } else {
                        break;
                    }
                }
                String[] reqParams = splitReq[1].split("=");
                if (reqParams[0].equals("built") && reqItem.getItemType().equals(CivItem.ItemType.REGION)) {
                    if (civilian.getCountNonStashItems(splitReq[0]) >= Integer.parseInt(reqParams[1])) {
                        continue outer;
                    } else {
                        break;
                    }
                } else if (reqParams[0].equals("level")) {
                    if (civilian.getExp().get(reqItem) == null) {
                        continue;
                    }
                    int level = civilian.getLevel(reqItem);
                    if (level >= Integer.parseInt(reqParams[1])) {
                        continue outer;
                    } else {
                        break;
                    }
                } else if (reqParams[0].equals("has")) {
                    if (civilian.getCountStashItems(splitReq[0]) >= Integer.parseInt(reqParams[1]) ||
                            civilian.getCountNonStashItems(splitReq[0]) > Integer.parseInt(reqParams[1])) {
                        continue outer;
                    } else {
                        break;
                    }
                } else if (reqParams[0].equals("population")) {
                    int requirement = Integer.parseInt(reqParams[1]);
                    for (Town town : TownManager.getInstance().getTowns()) {
                        if (!town.getType().equalsIgnoreCase(splitReq[0]) ||
                                !town.getPeople().containsKey(civilian.getUuid()) ||
                                !town.getPeople().get(civilian.getUuid()).contains("owner")) {
                            continue;
                        }
                        if (requirement <= town.countPeopleWithRole("member")) {
                            continue outer;
                        }
                    }
                    break;
                }
            }
            return false;
        }
        return true;
    }

    public void addMinItems(Civilian civilian) {
        ArrayList<CivItem> addItems = new ArrayList<>();
        for (CivItem civItem : itemTypes.values()) {
            if (civItem.getCivMin() < 1) {
                continue;
            }
            int count = civilian.getCountStashItems(civItem.getProcessedName());
            if (count >= civItem.getCivMin()) {
                continue;
            }
            if (hasItemUnlocked(civilian, civItem)) {
                int add = 0;
                while(count + add < civItem.getCivMin()) {
                    addItems.add(civItem);
                    add++;
                }
            }
        }
        for (CivItem civItem : addItems) {
            civilian.getStashItems().add(civItem);
        }
        CivilianManager.getInstance().saveCivilian(civilian);
    }
}
