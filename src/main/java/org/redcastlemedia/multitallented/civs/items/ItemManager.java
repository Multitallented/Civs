package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
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
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.FallbackConfigUtil;
import org.redcastlemedia.multitallented.civs.util.Util;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.ResourcesScanner;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGHER)
public class ItemManager {
    private static ItemManager itemManager;
    private HashMap<String, CivItem> itemTypes = new HashMap<>();

    public static ItemManager getInstance() {
        if (itemManager == null) {
            itemManager = new ItemManager();
            itemManager.loadAllItemTypes();
        }
        return itemManager;
    }

    public void reload() {
        itemTypes.clear();
        loadAllItemTypes();
    }

    private void loadAllItemTypes() {
        final String ITEM_TYPES_FOLDER_NAME = Constants.ITEM_TYPES;
        String resourcePath = "resources." + ConfigManager.getInstance().getDefaultConfigSet() + "." + ITEM_TYPES_FOLDER_NAME;
        Reflections reflections = new Reflections(resourcePath, new ResourcesScanner());
        try {
            Set<String> resourcePaths = reflections.getResources(Pattern.compile(".*\\.yml"));
            for (String fileName : resourcePaths) {
                loopThroughResources("/" + fileName);
            }
        } catch (ReflectionsException reflectionsException) {
            Civs.logger.log(Level.WARNING, "No resources found for item-types");
        }
        File itemTypesFolder = new File(Civs.dataLocation, ITEM_TYPES_FOLDER_NAME);
        if (itemTypesFolder.exists()) {
            for (File file : itemTypesFolder.listFiles()) {
                String itemName = file.getName().replace(".yml", "").toLowerCase();
                if (itemTypes.containsKey(itemName) &&
                        itemTypes.get(itemName).getItemType() != CivItem.ItemType.FOLDER) {
                    continue;
                }
                loopThroughTypeFiles(file, null);
            }
        }
    }

    private void loopThroughResources(String path) {
        String relativePath = path.replace("/resources/" + ConfigManager.getInstance().getDefaultConfigSet(), "");
        String[] pathSplit = relativePath.split("/");
        String currentFileName = pathSplit[pathSplit.length - 1];
        try {
            FolderType folderType = null;
            for (String currentFolder : pathSplit) {
                if (currentFolder.isEmpty() || Constants.ITEM_TYPES.equals(currentFolder) ||
                        currentFolder.equals(currentFileName)) {
                    continue;
                }
                if (!ItemManager.getInstance().itemTypes.containsKey(currentFolder.replace(Constants.INVISIBLE, "").toLowerCase())) {
                    boolean isVisible = relativePath.substring(0, relativePath.lastIndexOf(currentFolder) + currentFolder.length())
                            .contains(Constants.INVISIBLE);
                    FolderType currentFolderType = createFolder(currentFolder.toLowerCase(), !isVisible);
                    if (folderType != null) {
                        folderType.getChildren().add(currentFolderType);
                    }
                    folderType = currentFolderType;
                } else {
                    folderType = (FolderType) ItemManager.getInstance().getItemType(currentFolder.toLowerCase());
                }
            }

            File file = new File(Civs.dataLocation, relativePath);
            FileConfiguration typeConfig = FallbackConfigUtil.getConfigFullPath(file, path);
            if (!typeConfig.getBoolean("enabled", true)) {
                return;
            }
            String type = typeConfig.getString("type",Constants.REGION);
            CivItem civItem = null;
            String itemName = currentFileName.replace(".yml", "").toLowerCase();
            if (Constants.REGION.equals(type)) {
                civItem = loadRegionType(typeConfig, itemName);
            } else if ("spell".equals(type)) {
                civItem = loadSpellType(typeConfig, itemName);
            } else if ("class".equals(type)) {
                civItem = loadClassType(typeConfig, itemName);
            } else if ("town".equals(type)) {
                civItem = loadTownType(typeConfig, itemName);
            }
            if (folderType != null) {
                folderType.getChildren().add(civItem);
            }
        } catch (Exception e) {
            Civs.logger.log(Level.SEVERE, "Unable to read from {0}", currentFileName);
            Civs.logger.log(Level.SEVERE, "Exception during file read", e);
        }
    }

    private FolderType createFolder(String currentFileName, boolean invisible) {
        String folderName = currentFileName.replace(Constants.INVISIBLE, "");
        FolderType folderType = new FolderType(new ArrayList<>(),
                folderName,
                ConfigManager.getInstance().getFolderIcon(folderName.toLowerCase()),
                0,
                null,
                new ArrayList<>(),
                invisible,
                1);
        itemTypes.put(folderName.toLowerCase(), folderType);
        return folderType;
    }

    private void loopThroughTypeFiles(File file, List<CivItem> parentList) {
        try {
            if (file.isDirectory() && !file.getName().contains(".yml")) {
                List<CivItem> currParentList;
                if (ItemManager.getInstance().getItemType(file.getName().toLowerCase()) != null) {
                    FolderType folderType = (FolderType) ItemManager.getInstance().getItemType(file.getName().toLowerCase());
                    currParentList = folderType.getChildren();
                } else {
                    currParentList = new ArrayList<>();
                }

                for (File pFile : file.listFiles()) {
                    loopThroughTypeFiles(pFile, currParentList);
                }
                if (itemTypes.containsKey(file.getName().toLowerCase())) {
                    return;
                }
                String folderName = file.getName().replace(Constants.INVISIBLE, "");
                FolderType folderType = new FolderType(new ArrayList<>(),
                        folderName,
                        ConfigManager.getInstance().getFolderIcon(folderName.toLowerCase()),
                        0,
                        null,
                        currParentList,
                        !file.getName().contains("invisible"),
                        1);
                itemTypes.put(folderName.toLowerCase(), folderType);
                if (parentList != null) {
                    parentList.add(folderType);
                }
            } else {
                String name = file.getName().replace(".yml", "").toLowerCase();
                if (itemTypes.containsKey(name)) {
                    return;
                }
                try {
                    FileConfiguration typeConfig = new YamlConfiguration();
                    typeConfig.load(file);
                    if (!typeConfig.getBoolean("enabled", true)) {
                        return;
                    }
                    String type = typeConfig.getString("type","region");
                    CivItem civItem = null;
                    String itemName = file.getName().replace(".yml", "").toLowerCase();
                    if (type.equals("region")) {
                        civItem = loadRegionType(typeConfig, itemName);
                    } else if (type.equals("spell")) {
                        civItem = loadSpellType(typeConfig, itemName);
                    } else if (type.equals("class")) {
                        civItem = loadClassType(typeConfig, itemName);
                    } else if (type.equals("town")) {
                        civItem = loadTownType(typeConfig, itemName);
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
    public CivItem loadClassType(FileConfiguration config, String name) {
        //TODO load classestype properly
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", Material.CHEST.name()));
        ClassType civItem = new ClassType(
                config.getStringList("reqs"),
                name,
                icon,
                CVItem.createCVItemFromString(config.getString("shop-icon", config.getString("icon", Material.CHEST.name()))),
                config.getDouble("price", 0),
                config.getString("permission"),
                config.getStringList("children"),
                config.getStringList("groups"),
                config.getInt("mana-per-second", 1),
                config.getInt("max-mana", 100),
                config.getBoolean("is-in-shop", true),
                config.getInt("level", 1));

        itemTypes.put(name, civItem);
        return civItem;
    }

    public CivItem loadSpellType(FileConfiguration config, String name) {
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", Material.CHEST.name()));
        SpellType spellType = new SpellType(
                config.getStringList("reqs"),
                name,
                icon.getMat(),
                CVItem.createCVItemFromString(config.getString("shop-icon", config.getString("icon", Material.CHEST.name()))),
                config.getInt("qty", 0),
                config.getInt("min", 0),
                config.getInt("max", -1),
                config.getDouble("price", 0),
                config.getString("permission"),
                config.getStringList("groups"),
                config,
                config.getBoolean("is-in-shop", true),
                config.getInt("level", 1));
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
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", Material.STONE.name()));
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
        TownType townType = new TownType(
                name,
                icon,
                CVItem.createCVItemFromString(config.getString("shop-icon", config.getString("icon", Material.CHEST.name()))),
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
                config.getInt("power", 200),
                config.getInt("max-power", 1000),
                config.getStringList("groups"),
                config.getString("child"),
                config.getInt("child-population", 0),
                config.getBoolean("is-in-shop", true),
                config.getInt("level", 1));
        townType.setDefaultGovType(config.getString("gov-type", ConfigManager.getInstance().getDefaultGovernmentType()));
        itemTypes.put(Util.getValidFileName(name).toLowerCase(), townType);
        return townType;
    }

    public RegionType loadRegionType(FileConfiguration config, String name) {
        CVItem icon = CVItem.createCVItemFromString(config.getString("icon", Material.CHEST.name()));
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
                String perm = config.getString("upkeep." + key + ".perm", "");
                RegionUpkeep regionUpkeep = new RegionUpkeep(reagents, inputs, outputs, payout, exp, perm);
                regionUpkeep.setPowerReagent(config.getInt("upkeep." + key + ".power-reagent", 0));
                regionUpkeep.setPowerInput(config.getInt("upkeep." + key + ".power-input", 0));
                regionUpkeep.setPowerOutput(config.getInt("upkeep." + key + ".power-output", 0));
                if (config.isSet("upkeep." + key + ".command")) {
                    regionUpkeep.setCommand(config.getString("upkeep." + key + ".command"));
                }
                upkeeps.add(regionUpkeep);
            }
        }
        HashSet<String> townSet;
        if (config.isSet("towns")) {
            townSet = new HashSet<>(config.getStringList("towns"));
        } else {
            townSet = new HashSet<>();
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
        List<String> rebuild = config.getStringList("rebuild");
        if (rebuild.isEmpty()) {
            rebuild = new ArrayList<>();
            String onlyRebuild = config.getString("rebuild");
            if (onlyRebuild != null && !onlyRebuild.isEmpty()) {
                rebuild.add(onlyRebuild);
            }
        }
        Set<Biome> biomes = new HashSet<>();
        if (config.isSet("biomes")) {
            for (String s : config.getStringList("biomes")) {
                biomes.add(Biome.valueOf(s));
            }
        }
        HashSet<String> worlds = new HashSet<>();
        if (config.isSet("worlds")) {
            worlds.addAll(config.getStringList("worlds"));
        }
        RegionType regionType = new RegionType(
                name,
                icon,
                CVItem.createCVItemFromString(config.getString("shop-icon", config.getString("icon", Material.CHEST.name()))),
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
                config.getLong("period", 0),
                config.getString("period", "false").equals("daily"),
                config.getStringList("groups"),
                config.getBoolean("is-in-shop", true),
                config.getBoolean("rebuild-required", false),
                config.getInt("level",1),
                worlds);
        if (config.isSet("commands-on-creation")) {
            regionType.getCommandsOnCreation().addAll(config.getStringList("commands-on-creation"));
        }
        if (config.isSet("commands-on-destruction")) {
            regionType.getCommandsOnCreation().addAll(config.getStringList("commands-on-destruction"));
        }
        if (config.isSet("dynmap-marker")) {
            regionType.setDynmapMarkerKey(config.getString("dynmap-marker"));
        }
        itemTypes.put(name.toLowerCase(), regionType);
        return regionType;
    }

    public Map<String, Integer> loadCivItems(FileConfiguration civConfig) {
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
        String processedName = ChatColor.stripColor(name);
        return itemTypes.get(processedName.replace(
                ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "").toLowerCase());
    }

    public List<CivItem> getItemGroup(String groupName) {
        ArrayList<CivItem> returnList = new ArrayList<>();

        for (CivItem item : this.itemTypes.values()) {
            if (item.getGroups().contains(groupName)) {
                returnList.add(item);
            }
        }

        return returnList;
    }

    public Map<String, Integer> getNewItems(Civilian civilian) {
        HashMap<String, Integer> newItems = new HashMap<>();
        for (CivItem civItem : itemTypes.values()) {
            if (civItem.getItemType() == CivItem.ItemType.FOLDER ||
                    civilian.getStashItems().containsKey(civItem.getProcessedName()) ||
                    !hasItemUnlocked(civilian, civItem)) {
                continue;
            }
            int count = civilian.getCountStashItems(civItem.getProcessedName()) +
                    civilian.getCountNonStashItems(civItem.getProcessedName());
            if (civItem.getCivQty() > 0) {
                newItems.put(civItem.getProcessedName(), civItem.getQty());
            } else if (civItem.getCivMin() > 0 && civItem.getCivMin() > count) {
                newItems.put(civItem.getProcessedName(), civItem.getCivMin() - count);
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
            for (Map.Entry<String, CivItem> entry : itemTypes.entrySet()) {
                CivItem civItem = entry.getValue();
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
        returnList.removeAll(checkList);
        checkList.clear();
        for (CivItem item : returnList) {
            if (!hasItemUnlocked(civilian, item) ||
                    (isShop && !item.getInShop())) {
                checkList.add(item);
            }
        }
        returnList.removeAll(checkList);

        checkList.clear();
        for (CivItem currentItem : returnList) {
            if (currentItem.getItemType() != CivItem.ItemType.FOLDER) {
                continue;
            }
            if (getAllItemsWithParent(civilian, currentItem, true).isEmpty()) {
                checkList.add(currentItem);
            }
        }
        returnList.removeAll(checkList);
        return returnList;
    }

    public boolean hasItemUnlocked(Civilian civilian, CivItem civItem) {
        if (civItem.getCivReqs().isEmpty()) {
            return true;
        }
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return false;
        }
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
                    String[] townTypeStrings = req.replace("member=", "").split(":");
                    Set<String> townTypes = new HashSet<>(Arrays.asList(townTypeStrings));
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
                                !town.getPeople().get(civilian.getUuid()).contains(Constants.OWNER)) {
                            continue;
                        }
                        if (pop <= town.getPopulation()) {
                            continue outer;
                        }
                    }
                    continue;
                }
                String[] splitReq = req.split(":");
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
                //shack:built=1
                if (reqParams[0].equals("built")) {
                    if (civilian.getCountNonStashItems(splitReq[0]) >= Integer.parseInt(reqParams[1])) {
                        continue outer;
                    } else {
                        continue;
                    }
                //bash:level=4
                } else if (reqParams[0].equals("level")) {
                    CivItem reqItem = itemManager.getItemType(splitReq[0]);
                    if (reqItem == null || civilian.getExp().get(reqItem) == null) {
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
                                !town.getPeople().get(civilian.getUuid()).contains(Constants.OWNER)) {
                            continue;
                        }
                        if (requirement <= town.getPopulation()) {
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

    public ArrayList<CivItem> getItemsByLevel(int level) {
        ArrayList<CivItem> itemSet = new ArrayList<>();
        for (CivItem civItem : this.itemTypes.values()) {
            if (civItem.getLevel() == level && civItem.getInShop()) {
                itemSet.add(civItem);
            }
        }
        return itemSet;
    }
}
