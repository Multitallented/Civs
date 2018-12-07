package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
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
                configuration.getInt("max-mana", 100),
                configuration.getBoolean("is-in-shop", true));

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
                config,
                config.getBoolean("is-in-shop", true));
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
        HashMap<String, String> effects = new HashMap<>();
        List<String> configEffects = config.getStringList("effects");
        for (String effectString : configEffects) {
            if (effectString.contains(":")) {
                String[] effectSplit = effectString.split(":");
                effects.put(effectSplit[0], effectSplit[1]);
            } else {
                effects.put(effectString, null);
            }
        }
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
                config.getString("child"),
                config.getBoolean("is-in-shop", true));
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
                double exp = config.getDouble("upkeep." + key + ".exp", 0);
                RegionUpkeep regionUpkeep = new RegionUpkeep(reagents, inputs, outputs, payout, exp);
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
        Set<Biome> biomes = new HashSet<>();
        if (config.isSet("biomes")) {
            for (String s : config.getStringList("biomes")) {
                biomes.add(Biome.valueOf(s));
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
                biomes,
                description,
                config.getLong("period", 0),
                config.getString("period", "false").equals("daily"),
                config.getStringList("groups"),
                config.getBoolean("is-in-shop", true));
        itemTypes.put(name.toLowerCase(), regionType);
        return regionType;
    }

    public HashMap<String, Integer> loadCivItems(FileConfiguration civConfig, UUID uuid) {
        HashMap<String, Integer> items = new HashMap<>();
        ConfigurationSection configurationSection = civConfig.getConfigurationSection("items");
        if (configurationSection == null) {
            return items;
        }
        for (String key : configurationSection.getKeys(false)) {
            CivItem currentItem = getItemType(key);
            if (currentItem == null) {
                continue;
            }
            items.put(key, civConfig.getInt("items." + key));
        }
        return items;
    }

    public CivItem getItemType(String name) {
        return itemTypes.get(name.replace("Civs ", "").toLowerCase());
    }

    public HashMap<String, Integer> getNewItems() {
        HashMap<String, Integer> newItems = new HashMap<>();
        for (CivItem civItem : itemTypes.values()) {
            if (civItem.getCivReqs().isEmpty() && civItem.getCivQty() > 0) {
                newItems.put(civItem.getProcessedName(), civItem.getQty());
            } else if (civItem.getCivReqs().isEmpty() && civItem.getCivMin() > 0) {
                newItems.put(civItem.getProcessedName(), civItem.getQty());
            }
        }
        return newItems;
    }

    public List<CivItem> getShopItems(Civilian civilian, CivItem parent) {
        return getAllItemsWithParent(civilian, parent, true);
    }
    private List<CivItem> getAllItemsWithParent(Civilian civilian, CivItem parent, boolean isShop) {
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
        boolean isCivAdmin = Civs.perm != null &&
                Civs.perm.has(Bukkit.getPlayer(civilian.getUuid()), "civs.admin");
        for (CivItem item : returnList) {
            if (!hasItemUnlocked(civilian, item) ||
                    (isShop && !item.getInShop() && !isCivAdmin)) {
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
                //perm=civs.admin
                if (req.startsWith("perm=")) {
                    String permission = req.replace("perm=", "");
                    if (Civs.perm != null &&
                            Civs.perm.has(player, permission)) {
                        continue outer;
                    } else {
                        continue;
                    }
                //member=settlement:town:...
                } else if (req.startsWith("member=")) {
                    Set<String> townTypes = new HashSet<>();
                    String[] townTypeStrings = req.replace("member=", "").split(":");
                    for (int i = 0; i < townTypeStrings.length; i++) {
                        townTypes.add(townTypeStrings[i]);
                    }
                    for (Town town : TownManager.getInstance().getTowns()) {
                        if (townTypes.contains(town.getType()) &&
                                town.getPeople().containsKey(civilian.getUuid())) {
                            continue outer;
                        }
                    }
                    continue;
                //population=15
                } else if (req.startsWith("population=")) {
                    int pop = Integer.parseInt(req.replace("population=", ""));
                    for (Town town : TownManager.getInstance().getTowns()) {
                        if (!town.getPeople().containsKey(civilian.getUuid()) ||
                                !town.getPeople().get(civilian.getUuid()).contains("owner")) {
                            continue;
                        }
                        if (pop <= town.countPeopleWithRole("member")) {
                            continue outer;
                        }
                    }
                    continue;
                }
                String[] splitReq = req.split(":");
                CivItem reqItem = itemManager.getItemType(splitReq[0]);
                if (reqItem == null) {
                    continue;
                }
                //house:???
                if (splitReq.length < 2) {
                    if (civilian.getCountStashItems(splitReq[0]) > 0 ||
                            civilian.getCountNonStashItems(splitReq[0]) > 0) {
                        continue outer;
                    } else {
                        continue;
                    }
                }
                String[] reqParams = splitReq[1].split("=");
                //settlement:built=1
                if (reqParams[0].equals("built") && reqItem.getItemType().equals(CivItem.ItemType.REGION)) {
                    if (civilian.getCountNonStashItems(splitReq[0]) >= Integer.parseInt(reqParams[1])) {
                        continue outer;
                    } else {
                        continue;
                    }
                //bash:level=4
                } else if (reqParams[0].equals("level")) {
                    if (civilian.getExp().get(reqItem) == null) {
                        continue;
                    }
                    int level = civilian.getLevel(reqItem);
                    if (level >= Integer.parseInt(reqParams[1])) {
                        continue outer;
                    } else {
                        continue;
                    }
                //house:has=2
                } else if (reqParams[0].equals("has")) {
                    if (civilian.getCountStashItems(splitReq[0]) >= Integer.parseInt(reqParams[1]) ||
                            civilian.getCountNonStashItems(splitReq[0]) > Integer.parseInt(reqParams[1])) {
                        continue outer;
                    } else {
                        continue;
                    }
                //hamlet:population=15
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
                    continue;
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
            if (CivItem.ItemType.REGION == civItem.getItemType()) {
                count += civilian.getCountRegions(civItem.getProcessedName());
            }
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
            civilian.getStashItems().put(civItem.getProcessedName(), civItem.getQty());
        }
        CivilianManager.getInstance().saveCivilian(civilian);
    }
}
