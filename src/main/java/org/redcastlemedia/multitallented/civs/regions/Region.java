package org.redcastlemedia.multitallented.civs.regions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionUpkeepEvent;
import org.redcastlemedia.multitallented.civs.items.CVInventory;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.towns.GovTypeBuff;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.CommandUtil;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.DebugLogger;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import lombok.Getter;
import lombok.Setter;

public class Region {

    private String type;
    private final Map<UUID, String> people;
    private final World world;
    private final double x;
    private final double y;
    private final double z;
    private final int radiusXP;
    private final int radiusZP;
    private final int radiusXN;
    private final int radiusZN;
    private final int radiusYP;
    private final int radiusYN;
    @Getter
    private HashMap<Long, Integer> upkeepHistory = new HashMap<>();
    private double exp;
    @Getter
    public Map<String, String> effects;
    long lastTick = 0;
    @Getter @Setter
    private HashSet<Integer> failingUpkeeps = new HashSet<>();
    @Getter @Setter
    private long lastActive = 0;
    @Getter @Setter
    private double forSale = -1;
    @Getter @Setter
    private boolean warehouseEnabled = true;
    @Getter @Setter
    private List<List<CVItem>> missingBlocks = new ArrayList<>();
    @Getter
    private List<String> chests = new ArrayList<>();
    @Getter
    private boolean idle = false;

    public Region(String type,
                  HashMap<UUID, String> people,
                  Location location,
                  int[] buildRadius,
                  HashMap<String, String> effects,
                  double exp) {
        this.type = type;
        this.people = people;
        this.world = location.getWorld();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        radiusXP = buildRadius[0];
        radiusZP = buildRadius[1];
        radiusXN = buildRadius[2];
        radiusZN = buildRadius[3];
        radiusYP = buildRadius[4];
        radiusYN = buildRadius[5];
        this.effects = effects;
        this.exp = exp;
    }

    public Region(String type,
                  Map<UUID, String> people,
                  Location location,
                  RegionPoints regionPoints,
                  Map<String, String> effects,
                  double exp) {
        this.type = type;
        this.people = people;
        this.world = location.getWorld();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        radiusXP = regionPoints.getRadiusXP();
        radiusZP = regionPoints.getRadiusZP();
        radiusXN = regionPoints.getRadiusXN();
        radiusZN = regionPoints.getRadiusZN();
        radiusYP = regionPoints.getRadiusYP();
        radiusYN = regionPoints.getRadiusYN();
        this.effects = effects;
        this.exp = exp;
    }

    public double getExp() {
        return exp;
    }
    public void setExp(double exp) {
        this.exp = exp;
    }
    public void setEffects(HashMap<String, String> effects) {
        this.effects = effects;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) { this.type = type; }
    public void setPeople(UUID uuid, String role) {
        people.put(uuid, role);
    }

    public Map<UUID, String> getRawPeople() {
        return people;
    }
    public Map<UUID, String> getPeople() {
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTownAt(getLocation());
        if (town == null) {
            return people;
        }
        HashMap<UUID, String> newPeople = new HashMap<>(people);
        for (UUID uuid : town.getPeople().keySet()) {
            if (!newPeople.containsKey(uuid)) {
                if (town.getPeople().get(uuid).contains("foreign")) {
                    newPeople.put(uuid, "allyforeign");
                } else {
                    newPeople.put(uuid, "ally");
                }
            }
        }
        return newPeople;
    }
    public Set<UUID> getOwners() {
        Set<UUID> owners = new HashSet<>();
        for (UUID uuid : people.keySet()) {
            if (people.get(uuid).contains(Constants.OWNER)) {
                owners.add(uuid);
            }
        }
        return owners;
    }
    public Location getLocation() {
        return new Location(world, x, y, z);
    }
    public int getRadiusXP() {
        return radiusXP;
    }
    public int getRadiusZP() {
        return radiusZP;
    }
    public int getRadiusXN() {
        return radiusXN;
    }
    public int getRadiusZN() {
        return radiusZN;
    }
    public int getRadiusYP() {
        return radiusYP;
    }
    public int getRadiusYN() {
        return radiusYN;
    }
    public long getSecondsTillNextTick() {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        if (regionType.isDailyPeriod()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            return ((86400000 + calendar.getTimeInMillis() - System.currentTimeMillis()) / 50) / 1000;
        }

        long difference = new Date().getTime() - lastTick;
        long remainingCooldown = ((regionType.getPeriod()*1000 - difference) / 1000);
        return remainingCooldown < 0 ? 0 : remainingCooldown;
    }

    public String getId() {
        return locationToString(getLocation());
    }
    public static String blockLocationToString(Location location) {
        return locationToString(location);
    }
    public static String locationToString(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        return location.getWorld().getUID().toString() +
                "~" +
                (Math.floor(location.getX()) + 0.5) +
                "~" +
                (Math.floor(location.getY()) + 0.5) +
                "~" +
                (Math.floor(location.getZ()) + 0.5);
    }
    public static Location idToLocation(String id) {
        String[] idSplit = id.split("~");
        if (idSplit.length < 4) {
            return null;
        }
        World world = Bukkit.getWorld(UUID.fromString(idSplit[0]));
        if (world == null) {
            world = Bukkit.getWorld(idSplit[0]);
        }
        if (world == null) {
            Object[] params = { idSplit[0], idSplit.length };
            Civs.logger.log(Level.SEVERE, "Null world for {0}, {1}", params);
        }
        return new Location(world,
                Double.parseDouble(idSplit[1]),
                Double.parseDouble(idSplit[2]),
                Double.parseDouble(idSplit[3]));
    }

    private static List<HashMap<Material, Integer>> cloneReqMap(List<List<CVItem>> reqMap) {
        return cloneReqMap(reqMap, null);
    }

    private static List<HashMap<Material, Integer>> cloneReqMap(List<List<CVItem>> reqMap, CVItem missingItem) {
        List<HashMap<Material, Integer>> itemCheck = new ArrayList<>();
        for (List<CVItem> currentList : reqMap) {
            HashMap<Material, Integer> currentReqMap = new HashMap<>();
            for (CVItem currentItem : currentList) {
                CVItem clone = currentItem.clone();
                if (missingItem != null && missingItem.equivalentCVItem(clone)) {
                    currentReqMap.put(clone.getMat(), clone.getQty() + 1);
                } else {
                    currentReqMap.put(clone.getMat(), clone.getQty());
                }
            }
            itemCheck.add(currentReqMap);
        }
        return itemCheck;
    }

    public RegionBlockCheckResponse hasRequiredBlocks() {
        ItemManager itemManager = ItemManager.getInstance();
        RegionType regionType = (RegionType) itemManager.getItemType(type);
        List<HashMap<Material, Integer>> itemCheck = cloneReqMap(regionType.getReqs());

        RegionPoints regionPoints = new RegionPoints(radiusXP, radiusXN, radiusYP, radiusYN, radiusZP, radiusZN);
        if (itemCheck.isEmpty()) {
            return new RegionBlockCheckResponse(regionPoints, itemCheck);
        }

        if (!addItemCheck(itemCheck)) {
            return new RegionBlockCheckResponse(new RegionPoints(), itemCheck);
        } else {
            return new RegionBlockCheckResponse(regionPoints, itemCheck);
        }
    }

    private boolean addItemCheck(List<HashMap<Material, Integer>> itemCheck) {
        Location location = getLocation();
        World currentWorld = location.getWorld();
        int xMax = (int) location.getX() + radiusXP;
        int xMin = (int) location.getX() - radiusXN;
        int yMax = (int) location.getY() + radiusYP;
        int yMin = (int) location.getY() - radiusYN;
        int zMax = (int) location.getZ() + radiusZP;
        int zMin = (int) location.getZ() - radiusZN;

        yMax = yMax > currentWorld.getMaxHeight() ? currentWorld.getMaxHeight() : yMax;
        yMin = yMin < 0 ? 0 : yMin;

        HashMap<Material, Integer> maxCheck = new HashMap<>();
        for (HashMap<Material, Integer> tempMap : itemCheck) {
            for (Material mat : tempMap.keySet()) {
                if (maxCheck.containsKey(mat)) {
                    maxCheck.put(mat, maxCheck.get(mat) + tempMap.get(mat));
                } else {
                    maxCheck.put(mat, tempMap.get(mat).intValue());
                }
            }
        }
        for (int x=xMin; x<=xMax;x++) {
            for (int y=yMin; y<=yMax; y++) {
                for (int z=zMin; z<=zMax; z++) {

                    Block currentBlock = currentWorld.getBlockAt(x,y,z);
                    if (currentBlock == null) {
                        continue;
                    }
                    Material mat = currentBlock.getType();
                    if (maxCheck.containsKey(mat)) {
                        maxCheck.put(mat, maxCheck.get(mat) - 1);
                    }
                    boolean destroyIndex = false;
                    int i=0;
                    for (HashMap<Material, Integer> tempMap : itemCheck) {
                        if (tempMap.containsKey(mat)) {

                            if (tempMap.get(mat) < 2) {
                                destroyIndex = true;
                            } else {
                                for (Material currentMat : tempMap.keySet()) {
                                    tempMap.put(currentMat, tempMap.get(mat) - 1);
                                }
                            }
                            break;
                        }
                        i++;
                    }
                    if (destroyIndex) {
                        if (itemCheck.size() < 2) {
                            itemCheck.remove(i);
                            if (itemCheck.isEmpty()) {
                                return true;
                            }
                        } else {
                            itemCheck.remove(i);
                        }
                    }
                }
            }
        }
        return false;
    }

    private static RegionPoints addItemCheck(RegionPoints radii, Location location, World currentWorld,
                                     double xMin, double xMax, double yMin, double yMax, double zMin, double zMax,
                                     List<HashMap<Material, Integer>> itemCheck, RegionType regionType) {

        HashMap<Material, Integer> maxCheck = new HashMap<>();
        for (HashMap<Material, Integer> tempMap : itemCheck) {
            for (Material mat : tempMap.keySet()) {
                if (maxCheck.containsKey(mat)) {
                    maxCheck.put(mat, maxCheck.get(mat) + tempMap.get(mat));
                } else {
                    maxCheck.put(mat, tempMap.get(mat).intValue());
                }
            }
        }
        List<Block> blocksFound = new ArrayList<>();
        outer: for (double x=xMin; x<=xMax;x++) {
            for (double y=yMin; y<=yMax; y++) {
                for (double z=zMin; z<=zMax; z++) {

                    Location location1 = new Location(currentWorld, x, y, z);
                    Block currentBlock = location1.getBlock();
                    if (currentBlock == null) {
                        continue;
                    }
                    Material mat = currentBlock.getType();
                    if (maxCheck.containsKey(mat)) {
                        maxCheck.put(mat, maxCheck.get(mat) - 1);
                    }
                    boolean destroyIndex = false;
                    int i=0;
                    outer1: for (HashMap<Material, Integer> tempMap : itemCheck) {
                        if (tempMap.containsKey(mat)) {
                            blocksFound.add(currentBlock);

                            if (tempMap.get(mat) < 2) {
                                destroyIndex = true;
                            } else {
                                for (Material currentMat : tempMap.keySet()) {
                                    tempMap.put(currentMat, tempMap.get(mat) - 1);
                                }
                            }
                            RegionManager.getInstance().adjustRadii(radii, location, x,y,z);
                            break outer1;
                        }
                        i++;
                    }
                    if (destroyIndex) {
                        if (itemCheck.size() < 2) {
                            itemCheck.remove(i);
                            radii = trimExcessRegion(blocksFound, itemCheck,
                                    maxCheck, radii, location, regionType);
                            if (itemCheck.isEmpty()) {
                                break outer;
                            }
                        } else {
                            itemCheck.remove(i);
                        }
                    }
                }
            }
        }
        return radii;
    }

    private static RegionPoints buildNewRadii(List<Block> blocks, Location location) {
        RegionPoints radii = new RegionPoints(0, 0, 0, 0, 0, 0);
        for (Block block : blocks) {
            RegionManager.getInstance().adjustRadii(radii, location,
                    block.getX(), block.getY(), block.getZ());
        }
        return radii;
    }

    private static RegionPoints trimExcessRegion(List<Block> blocksFound,
                                     List<HashMap<Material, Integer>> itemCheck,
                                     HashMap<Material, Integer> maxCheck,
                                     RegionPoints radii, Location location, RegionType regionType) {
        if (radiusCheck(radii, regionType).isValid()) {
            return radii;
        }

        RegionPoints returnRadii;
        do {
            Block block = blocksFound.remove(0);
            if (maxCheck.containsKey(block.getType())) {
                maxCheck.put(block.getType(), maxCheck.get(block.getType()) + 1);
                if (maxCheck.get(block.getType()) > 0) {
                    boolean foundMat = false;
                    for (HashMap<Material, Integer> tempMap : itemCheck) {
                        if (tempMap.containsKey(block.getType())) {
                            foundMat = true;
                            tempMap.put(block.getType(), tempMap.get(block.getType()) + 1);
                        }
                    }
                    if (!foundMat) {
                        boolean unfullFilled = true;
                        itemLoop: for (List<CVItem> tempList : regionType.getReqs()) {
                            for (CVItem item : tempList) {
                                if (item.getMat() != block.getType() &&
                                        maxCheck.get(item.getMat()) < 1) {
                                    unfullFilled = false;
                                    break itemLoop;
                                }
                            }
                        }
                        if (unfullFilled) {
                            HashMap<Material, Integer> tempMap = new HashMap<>();
                            tempMap.put(block.getType(), 1);
                            itemCheck.add(tempMap);
                        }
                    }
                }
            }
            returnRadii = buildNewRadii(blocksFound, location);
        } while (!blocksFound.isEmpty() && (returnRadii.isEquivalentTo(radii) || !radiusCheck(returnRadii, regionType).isValid()));
        return returnRadii;
    }

    public static RegionPoints hasRequiredBlocks(String type, Location location, boolean useCivItem) {
        ItemManager itemManager = ItemManager.getInstance();
        RegionType regionType = (RegionType) itemManager.getItemType(type);
        List<HashMap<Material, Integer>> itemCheck = cloneReqMap(regionType.getReqs());

        RegionPoints radii = new RegionPoints(0, 0, 0, 0, 0, 0);
        if (itemCheck.isEmpty()) {
            radiusCheck(radii, regionType);
            return radii;
        }

        World currentWorld = location.getWorld();
        int biggestXZRadius = Math.max(regionType.getBuildRadiusX(), regionType.getBuildRadiusZ());
        double xMax = location.getX() + biggestXZRadius * 1.5;
        double xMin = location.getX() - biggestXZRadius * 1.5;
        double yMax = location.getY() + regionType.getBuildRadiusY() * 1.5;
        double yMin = location.getY() - regionType.getBuildRadiusY() * 1.5;
        double zMax = location.getZ() + biggestXZRadius * 1.5;
        double zMin = location.getZ() - biggestXZRadius * 1.5;

        yMax = yMax > currentWorld.getMaxHeight() ? currentWorld.getMaxHeight() : yMax;
        yMin = yMin < 0 ? 0 : yMin;

        radii = addItemCheck(radii, location, currentWorld, xMin, xMax, yMin, yMax, zMin, zMax,
                itemCheck, regionType);
        boolean hasReqs = itemCheck.isEmpty();
        if (itemCheck.isEmpty() && useCivItem) {
            Block centerBlock = location.getBlock();
            if (regionType.getMat() != centerBlock.getType()) {
                hasReqs = false;
            }
        }

        if (!hasReqs) {
            radii.setValid(false);
        }
        return radii;
    }

    public static RegionBlockCheckResponse hasRequiredBlocksOnCenter(RegionType regionType, Location location) {
        List<HashMap<Material, Integer>> itemCheck = cloneReqMap(regionType.getReqs());
        World currentWorld = location.getWorld();
        if (currentWorld == null) {
            return new RegionBlockCheckResponse(new RegionPoints(), null);
        }

        RegionPoints regionPoints = new RegionPoints(regionType.getBuildRadiusX(),
                regionType.getBuildRadiusX(),
                regionType.getBuildRadiusY(),
                regionType.getBuildRadiusY(),
                regionType.getBuildRadiusZ(),
                regionType.getBuildRadiusZ());

        double xMax = location.getX() + regionType.getBuildRadiusX();
        double xMin = location.getX() - regionType.getBuildRadiusX();
        double yMax = location.getY() + regionType.getBuildRadiusY();
        double yMin = location.getY() - regionType.getBuildRadiusY();
        double zMax = location.getZ() + regionType.getBuildRadiusX();
        double zMin = location.getZ() - regionType.getBuildRadiusX();

        yMax = yMax > currentWorld.getMaxHeight() ? currentWorld.getMaxHeight() : yMax;
        yMin = yMin < 0 ? 0 : yMin;

        outer: for (double x=xMin; x<=xMax;x++) {
            for (double y=yMin; y<=yMax; y++) {
                for (double z=zMin; z<=zMax; z++) {

                    Location location1 = new Location(currentWorld, x, y, z);
                    Block currentBlock = location1.getBlock();
                    if (currentBlock.getType() == Material.AIR) {
                        continue;
                    }
                    Material mat = currentBlock.getType();
                    boolean destroyIndex = false;
                    int i=0;
                    outer1: for (HashMap<Material, Integer> tempMap : itemCheck) {
                        if (tempMap.containsKey(mat)) {
                            if (tempMap.get(mat) < 2) {
                                destroyIndex = true;
                            } else {
                                for (Material currentMat : tempMap.keySet()) {
                                    tempMap.put(currentMat, tempMap.get(mat) - 1);
                                }
                            }
                            break outer1;
                        }
                        i++;
                    }
                    if (destroyIndex) {
                        itemCheck.remove(i);
                        if (itemCheck.isEmpty()) {
                            break outer;
                        }
                    }
                }
            }
        }
        if (!itemCheck.isEmpty()) {
            return new RegionBlockCheckResponse(new RegionPoints(), itemCheck);
        } else {
            return new RegionBlockCheckResponse(regionPoints, null);
        }
    }

    public static RegionPoints radiusCheck(RegionPoints radii, RegionType regionType) {
        int xRadius = regionType.getBuildRadiusX();
        int yRadius = regionType.getBuildRadiusY();
        int zRadius = regionType.getBuildRadiusZ();
        boolean xRadiusBigger = xRadius > zRadius;
        boolean xRadiusActuallyBigger = radii.getRadiusXP() + radii.getRadiusXN() > radii.getRadiusZP() + radii.getRadiusZN();
        if ((xRadiusActuallyBigger && xRadiusBigger && radii.getRadiusXP() + radii.getRadiusXN() > xRadius * 2) ||
                xRadiusActuallyBigger && !xRadiusBigger && radii.getRadiusXP() + radii.getRadiusXN() > zRadius * 2) {
            return new RegionPoints();
        } else {
            while ((radii.getRadiusXP() + radii.getRadiusXN() < xRadius * 2 && xRadiusActuallyBigger) ||
                    (radii.getRadiusXP() + radii.getRadiusXN() < zRadius * 2 && !xRadiusActuallyBigger)) {
                if (radii.getRadiusXP() < radii.getRadiusXN()) {
                    radii.setRadiusXP(radii.getRadiusXP() + 1);
                } else {
                    radii.setRadiusXN(radii.getRadiusXN() + 1);
                }
            }
        }
        if (radii.getRadiusYP() + radii.getRadiusYN() > yRadius * 2) {
            return new RegionPoints();
        } else {

            while (radii.getRadiusYP() + radii.getRadiusYN() < yRadius * 2) {
                if (radii.getRadiusYP() < radii.getRadiusYN()) {
                    radii.setRadiusYP(radii.getRadiusYP() + 1);
                } else {
                    radii.setRadiusYN(radii.getRadiusYN() + 1);
                }
            }
        }
        if ((!xRadiusActuallyBigger && !xRadiusBigger && radii.getRadiusZP() + radii.getRadiusZN() > zRadius * 2) ||
                !xRadiusActuallyBigger && xRadiusBigger && radii.getRadiusZP() + radii.getRadiusZN() > xRadius * 2) {
            return new RegionPoints();
        } else {
            while ((radii.getRadiusZP() + radii.getRadiusZN() < zRadius * 2 && xRadiusActuallyBigger) ||
                    (radii.getRadiusZP() + radii.getRadiusZN() < xRadius * 2 && !xRadiusActuallyBigger)) {
                if (radii.getRadiusZP() < radii.getRadiusZN()) {
                    radii.setRadiusZP(radii.getRadiusZP() + 1);
                } else {
                    radii.setRadiusZN(radii.getRadiusZN() + 1);
                }
            }
        }
        return radii;
    }
    public static List<HashMap<Material, Integer>> hasRequiredBlocks(String type, Location location, ItemStack missingStack) {
        ItemManager itemManager = ItemManager.getInstance();
        CVItem missingItem = null;
        if (missingStack != null) {
            missingItem = CVItem.createFromItemStack(missingStack);
        }
        RegionType regionType = (RegionType) itemManager.getItemType(type);
        List<HashMap<Material, Integer>> itemCheck = cloneReqMap(regionType.getReqs(), missingItem);

        RegionPoints radii = new RegionPoints(0, 0, 0, 0, 0, 0);
        if (itemCheck.isEmpty()) {
            radiusCheck(radii, regionType);
            return itemCheck;
        }

        World currentWorld = location.getWorld();
        int biggestXZRadius = Math.max(regionType.getBuildRadiusX(), regionType.getBuildRadiusZ());
        double xMax = location.getX() + biggestXZRadius * 1.5;
        double xMin = location.getX() - biggestXZRadius * 1.5;
        double yMax = location.getY() + regionType.getBuildRadiusY() * 1.5;
        double yMin = location.getY() - regionType.getBuildRadiusY() * 1.5;
        double zMax = location.getZ() + biggestXZRadius * 1.5;
        double zMin = location.getZ() - biggestXZRadius * 1.5;

        yMax = yMax > currentWorld.getMaxHeight() ? currentWorld.getMaxHeight() : yMax;
        yMin = yMin < 0 ? 0 : yMin;

        radii = addItemCheck(radii, location, currentWorld, xMin, xMax, yMin, yMax, zMin, zMax,
                itemCheck, regionType);
        radii = radiusCheck(radii, regionType);
        if (!radii.isValid()) {
            return itemCheck;
        }
        return itemCheck.isEmpty() ? null : itemCheck;
    }

    public boolean shouldTick() {
        ItemManager itemManager = ItemManager.getInstance();
        RegionType regionType = (RegionType) itemManager.getItemType(type);

        if (regionType.getPeriod() == 0) {
            return false;
        }

        Town town = TownManager.getInstance().getTownAt(getLocation());
        long period = regionType.getPeriod();
        if (town != null && town.getGovernmentType() != null) {
            period = regionType.getPeriod(GovernmentManager.getInstance()
                    .getGovernment(town.getGovernmentType()));
        }

        return lastTick + period * 1000 < new Date().getTime();
    }
    public boolean hasUpkeepItems() {
        return RegionManager.getInstance().hasRegionChestChanged(this) &&
                hasUpkeepItems(false);
    }
    public boolean hasUpkeepItems(boolean ignoreReagents) {
        if (!RegionManager.getInstance().hasRegionChestChanged(this)) {
            return false;
        }
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        if (regionType.getUpkeeps().isEmpty()) {
            return true;
        }
        Location location = getLocation();
        CVInventory cvInventory = UnloadedInventoryHandler.getInstance().getChestInventory(location);
        for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
            if ((ignoreReagents || Util.containsItems(regionUpkeep.getReagents(), cvInventory)) &&
                    Util.containsItems(regionUpkeep.getInputs(), cvInventory)) {
                if ((!ignoreReagents && regionUpkeep.getPowerReagent() > 0) || regionUpkeep.getPowerInput() > 0) {
                    Town town = TownManager.getInstance().getTownAt(location);
                    if (town == null || town.getPower() < Math.max(regionUpkeep.getPowerReagent(), regionUpkeep.getPowerInput())) {
                        continue;
                    }
                }
                return true;
            }
        }
        return false;
    }
    public boolean hasInput() {
        return hasUpkeepItems(true);
    }

    public boolean hasUpkeepItems(int upkeepIndex, boolean ignoreReagents) {
        if (!RegionManager.getInstance().hasRegionChestChanged(this)) {
            return false;
        }
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        if (regionType.getUpkeeps().size() <= upkeepIndex) {
            return false;
        }
        RegionUpkeep regionUpkeep = regionType.getUpkeeps().get(upkeepIndex);
        CVInventory cvInventory = UnloadedInventoryHandler.getInstance().getChestInventory(getLocation());

        if ((ignoreReagents || Util.containsItems(regionUpkeep.getReagents(), cvInventory)) &&
                Util.containsItems(regionUpkeep.getInputs(), cvInventory)) {
            return true;
        }
        return false;
    }

    private void tick() {
        this.lastTick = new Date().getTime();
    }

    boolean needsReagentsOrInput() {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
            if (regionUpkeep.getReagents().isEmpty() &&
                    regionUpkeep.getInputs().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public boolean runUpkeep() {
        return runUpkeep(true);
    }

    public boolean runUpkeep(boolean checkTick) {
        ItemManager itemManager = ItemManager.getInstance();
        RegionType regionType = (RegionType) itemManager.getItemType(getType());

        if (regionType.getUpkeeps().isEmpty() || !missingBlocks.isEmpty()) {
            return false;
        }
        if (checkTick) {
            boolean shouldTick = shouldTick();
            if (!shouldTick) {
                return false;
            }
            tick();
            idle = true;
        }
        if (ConfigManager.getInstance().isDisableRegionsInUnloadedChunks() && !Util.isChunkLoadedAt(getLocation())) {
            return false;
        }

        Location location = getLocation();
        boolean hadUpkeep = false;
        CVInventory chestInventory = null;
        boolean hasItemUpkeep = false;
        int i=0;
        for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
            if (!hasUpkeepPerm(regionUpkeep)) {
                continue;
            }

            boolean needsItems = !regionUpkeep.getReagents().isEmpty() ||
                    !regionUpkeep.getInputs().isEmpty();

            if (needsItems) {
                failingUpkeeps.add(i);
            }

            if (chestInventory == null && needsItems &&
                    RegionManager.getInstance().hasRegionChestChanged(this)) {
                chestInventory = UnloadedInventoryHandler.getInstance().getChestInventory(getLocation());
                RegionManager.getInstance().addCheckedRegion(this);
            }
            if (needsItems && (chestInventory == null || !chestInventory.isValid())) {
                if (ConfigManager.getInstance().isWarningLogger()) {
                    Civs.logger.log(Level.WARNING, "{0} has an invalid chestInventory {1}x {2}y {3}z",
                            new Object[] {type, x, y, z});
                }
                continue;
            }
            boolean containsReagents = !needsItems || Util.containsItems(regionUpkeep.getReagents(), chestInventory);
            boolean containsInputs = !needsItems || Util.containsItems(regionUpkeep.getInputs(), chestInventory);
            boolean hasReagents = !needsItems || (containsReagents && containsInputs);
            if (!hasReagents) {
                i++;
                continue;
            }

            boolean emptyOutput = regionUpkeep.getOutputs().isEmpty();
            ItemStack[] output = Util.getItems(regionUpkeep.getOutputs());
            boolean fullChest = chestInventory == null ||
                    !chestInventory.checkAddItems(output).isEmpty();
            if (fullChest) {
                failingUpkeeps.remove(i);
            }
            if (!emptyOutput && fullChest) {
                i++;
                continue;
            }
            if (regionUpkeep.getPowerReagent() > 0 || regionUpkeep.getPowerInput() > 0 || regionUpkeep.getPowerOutput() > 0) {
                Town town = TownManager.getInstance().getTownAt(location);
                if (town == null || town.getPower() < Math.max(regionUpkeep.getPowerReagent(), regionUpkeep.getPowerInput())) {
                    i++;
                    continue;
                }
                boolean powerMod = regionUpkeep.getPowerInput() > 0 || regionUpkeep.getPowerOutput() > 0;
                if (regionUpkeep.getPowerInput() > 0) {
                    TownManager.getInstance().setTownPower(town, town.getPower() - regionUpkeep.getPowerInput());
                }
                if (regionUpkeep.getPowerOutput() > 0) {
                    TownManager.getInstance().setTownPower(town, town.getPower() + regionUpkeep.getPowerOutput());
                }
                if (powerMod) {
                    TownManager.getInstance().saveTown(town);
                }
            }
            if (!runRegionUpkeepPayout(regionUpkeep)) {
                i++;
                continue;
            }
            if (regionUpkeep.getCommand() != null && !regionUpkeep.getCommand().isEmpty()) {
                Set<UUID> owners = getOwners();
                if (!owners.isEmpty()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(owners.iterator().next());
                    String regionCommand = regionUpkeep.getCommand();
                    regionCommand = regionCommand.replace("$region_x$", "" + location.getX());
                    regionCommand = regionCommand.replace("$region_y$", "" + location.getY());
                    regionCommand = regionCommand.replace("$region_z$", "" + location.getZ());

                    CommandUtil.performCommand(offlinePlayer, regionCommand);
                }
            }
            hasItemUpkeep = true;
            if (chestInventory != null) {
                if (ConfigManager.getInstance().isDebugLog()) {
                    DebugLogger.incrementRegion(this);
                    DebugLogger.inventoryModifications++;
                }
                Util.removeItems(regionUpkeep.getInputs(), chestInventory);
                chestInventory.addItem(output);

                containsReagents = Util.containsItems(regionUpkeep.getReagents(), chestInventory);
                containsInputs = Util.containsItems(regionUpkeep.getInputs(), chestInventory);
                if (containsReagents && containsInputs) {
                    failingUpkeeps.remove(i);
                }
            }
            if (regionUpkeep.getExp() > 0) {
                exp += regionUpkeep.getExp();
            }
            if (regionUpkeep.getPayout() != 0 || regionUpkeep.getPowerInput() != 0 ||
                    regionUpkeep.getPowerOutput() != 0) {
                upkeepHistory.put(System.currentTimeMillis(), i);
            }

            if (checkTick) {
                idle = false;
                tick();
            }
            hadUpkeep = true;
            Bukkit.getPluginManager().callEvent(new RegionUpkeepEvent(this, i));
            i++;
        }
        if (hadUpkeep) {
            RegionManager.getInstance().saveRegion(this);
            for (UUID uuid : getOwners()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    continue;
                }
                Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
                TutorialManager.getInstance().completeStep(civilian, TutorialManager.TutorialType.UPKEEP, type);
            }
        }
        if (!hasItemUpkeep && Util.isChunkLoadedAt(getLocation())) {
            RegionManager.getInstance().addCheckedRegion(this);
        } else if (hasItemUpkeep) {
            RegionManager.getInstance().removeCheckedRegion(this);
        }
        return hadUpkeep;
    }

    private boolean hasUpkeepPerm(RegionUpkeep regionUpkeep) {
        if (regionUpkeep.getPerm().isEmpty()) {
            return true;
        }
        boolean allHavePerm = true;
        for (UUID uuid : getOwners()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            World world = getLocation().getWorld();
            if (!offlinePlayer.isOp() && (Civs.perm == null ||
                    !Civs.perm.playerHas(world.getName(), offlinePlayer, regionUpkeep.getPerm()))) {
                allHavePerm = false;
                break;
            }
        }
        return allHavePerm;
    }

    private boolean runRegionUpkeepPayout(RegionUpkeep regionUpkeep) {
        boolean hasMoney = false;
        if (regionUpkeep.getPayout() != 0 && Civs.econ != null) {
            double payout = regionUpkeep.getPayout();
            Town town = TownManager.getInstance().getTownAt(getLocation());
            if (town != null && town.getGovernmentType() != null) {
                Government government = GovernmentManager.getInstance()
                        .getGovernment(town.getGovernmentType());
                for (GovTypeBuff buff : government.getBuffs()) {
                    if (buff.getBuffType() != GovTypeBuff.BuffType.PAYOUT) {
                        continue;
                    }
                    payout = payout * (1 + (double) buff.getAmount() / 100);
                    break;
                }
            }

            Government government = null;
            if (town != null) {
                government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            }
            if (payout > 0 && town != null && (government.getGovernmentType() == GovernmentType.COMMUNISM ||
                    government.getGovernmentType() == GovernmentType.COOPERATIVE)) {
                double size = town.getRawPeople().size();
                if (government.getGovernmentType() == GovernmentType.COMMUNISM) {
                    payout = payout / size;
                    for (UUID uuid : town.getRawPeople().keySet()) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                        Civs.econ.depositPlayer(offlinePlayer, payout);
                        hasMoney = true;
                    }
                } else if (government.getGovernmentType() == GovernmentType.COOPERATIVE) {
                    Map<UUID, Double> payouts = OwnershipUtil.getCooperativeSplit(town, this);
                    for (UUID uuid : payouts.keySet()) {
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                        Civs.econ.depositPlayer(offlinePlayer, payouts.get(uuid) * payout);
                        hasMoney = true;
                    }
                    if (hasMoney) {
                        double coopCut = payout * 0.1;
                        town.setBankAccount(town.getBankAccount() + coopCut);
                    }
                }
            } else {
                payout = payout / (double) getOwners().size();
                if (payout == 0) {
                    hasMoney = true;
                } else {
                    for (UUID uuid : getOwners()) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                        if (payout > 0) {
                            Civs.econ.depositPlayer(player, payout);
                            hasMoney = true;
                        } else if (Civs.econ.has(player, payout)) {
                            Civs.econ.withdrawPlayer(player, Math.abs(payout));
                            hasMoney = true;
                        }
                    }
                }
            }
        } else {
            hasMoney = true;
        }
        return hasMoney;
    }

    public void cleanUpkeepHistory() {
        for (Long time : new HashSet<>(upkeepHistory.keySet())) {
            if (System.currentTimeMillis() - 604800000 > time) {
                upkeepHistory.remove(time);
            }
        }
    }

    public HashMap<Integer, Integer> getNumberOfUpkeepsWithin24Hours() {
        return getNumberOfUpkeeps(86400000);
    }

    public HashMap<Integer, Integer> getNumberOfUpkeepsWithin1Week() {
        return getNumberOfUpkeeps(604800000);
    }

    private HashMap<Integer, Integer> getNumberOfUpkeeps(long cutoff) {
        HashMap<Integer, Integer> upkeeps = new HashMap<>();
        for (Long time : upkeepHistory.keySet()) {
            if (System.currentTimeMillis() - cutoff > time) {
                continue;
            }
            int upkeepIndex = upkeepHistory.get(time);
            if (upkeeps.containsKey(upkeepIndex)) {
                upkeeps.put(upkeepIndex, upkeeps.get(upkeepIndex) + 1);
            } else {
                upkeeps.put(upkeepIndex, 1);
            }
        }
        return upkeeps;
    }
}
