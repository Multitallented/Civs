package org.redcastlemedia.multitallented.civs.regions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionUpkeepEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.towns.GovTypeBuff;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import lombok.Getter;
import lombok.Setter;

public class Region {

    private String type;
    private final HashMap<UUID, String> people;
    private Location location;
    private final int radiusXP;
    private final int radiusZP;
    private final int radiusXN;
    private final int radiusZN;
    private final int radiusYP;
    private final int radiusYN;
    private double exp;
    public HashMap<String, String> effects;
    long lastTick = 0;
    @Getter
    @Setter
    private HashSet<Integer> failingUpkeeps = new HashSet<>();

    @Getter
    @Setter
    private long lastActive = 0;

    @Getter
    @Setter
    private double forSale = -1;

    public Region(String type,
                  HashMap<UUID, String> people,
                  Location location,
                  int[] buildRadius,
                  HashMap<String, String> effects,
                  double exp) {
        this.type = type;
        this.people = people;
        this.location = location;
        radiusXP = buildRadius[0];
        radiusZP = buildRadius[1];
        radiusXN = buildRadius[2];
        radiusZN = buildRadius[3];
        radiusYP = buildRadius[4];
        radiusYN = buildRadius[5];
        this.effects = effects;
        this.exp = exp;
    }

    protected void setLocation(Location location) {
        this.location = location;
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

    public HashMap<UUID, String> getRawPeople() {
        return people;
    }
    public HashMap<UUID, String> getPeople() {
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTownAt(location);
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

//        for (Alliance alliance : AllianceManager.getInstance().getAlliances(town)) {
//            for (String name : alliance.getMembers()) {
//                Town currentTown = townManager.getTown(name);
//                if (currentTown != null) {
//                    for (UUID uuid : currentTown.getPeople().keySet()) {
//                        if (!newPeople.containsKey(uuid) &&
//                                !currentTown.getPeople().get(uuid).contains("ally")) {
//                            newPeople.put(uuid, "ally");
//                        }
//                    }
//                }
//            }
//        }
        return newPeople;
    }
    public HashMap<String, String> getEffects() { return effects; }
    public Set<UUID> getOwners() {
        Set<UUID> owners = new HashSet<>();
        for (UUID uuid : people.keySet()) {
            if (people.get(uuid).contains("owner")) {
                owners.add(uuid);
            }
        }
        return owners;
    }
    public Location getLocation() {
        return location;
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
        return locationToString(location);
    }
    public static String blockLocationToString(Location location) {
//        double x = location.getX();
//        double z = location.getZ();
//        Location location1 = new Location(location.getWorld(), x, location.getY(), z);
        return locationToString(location);
    }
    public static String locationToString(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(location.getWorld().getUID().toString());
        builder.append("~");
        builder.append(Math.floor(location.getX()) + 0.5);
        builder.append("~");
        builder.append(Math.floor(location.getY()) + 0.5);
        builder.append("~");
        builder.append(Math.floor(location.getZ()) + 0.5);

        return builder.toString();
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
            Civs.logger.severe("Null world for " + idSplit[0] + ", " + idSplit.length);
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

    public boolean hasRequiredBlocks() {
        ItemManager itemManager = ItemManager.getInstance();
        RegionType regionType = (RegionType) itemManager.getItemType(type);
        List<HashMap<Material, Integer>> itemCheck = cloneReqMap(regionType.getReqs());

        if (itemCheck.isEmpty()) {
            return true;
        }

        return addItemCheck(itemCheck);
    }

    private boolean addItemCheck(List<HashMap<Material, Integer>> itemCheck) {
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

    public static int[] hasRequiredBlocks(String type, Location location) {
        return hasRequiredBlocks(type, location, true);
    }

    private static int[] addItemCheck(int[] radii, Location location, World currentWorld,
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

    private static int[] buildNewRadii(List<Block> blocks, Location location) {
        int[] radii = new int[6];
        for (int i = 0; i < 6; i++) {
            radii[i] = 0;
        }
        for (Block block : blocks) {
            RegionManager.getInstance().adjustRadii(radii, location,
                    block.getX(), block.getY(), block.getZ());
        }
        return radii;
    }

    private static int[] trimExcessRegion(List<Block> blocksFound,
                                     List<HashMap<Material, Integer>> itemCheck,
                                     HashMap<Material, Integer> maxCheck,
                                     int[] radii, Location location, RegionType regionType) {
        if (radiusCheck(radii, regionType).length > 0) {
            return radii;
        }

        int[] returnRadii;
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
        } while (!blocksFound.isEmpty() && (Arrays.equals(returnRadii, radii) || radiusCheck(returnRadii, regionType).length < 1));
        return returnRadii;
    }

    public static int[] hasRequiredBlocks(String type, Location location, boolean useCivItem) {
        return hasRequiredBlocks(null, type, location, useCivItem);
    }

    public static int[] hasRequiredBlocks(Player player, String type, Location location, boolean useCivItem) {
        ItemManager itemManager = ItemManager.getInstance();
        RegionType regionType = (RegionType) itemManager.getItemType(type);
        List<HashMap<Material, Integer>> itemCheck = cloneReqMap(regionType.getReqs());

        int[] radii = new int[6];
        for (int i = 0; i < 6; i++) {
            radii[i] = 0;
        }
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
            if (centerBlock == null) {
                hasReqs = false;
            } else if (regionType.getMat() != centerBlock.getType()) {
                hasReqs = false;
            }
        }

        if (radii.length == 0) {
            return radii;
        }
//        if (!hasReqs && player != null) {
//            StructureUtil.showGuideBoundingBox(player, location, radii);
//        }
        return hasReqs ? radii : new int[0];
    }

    public static int[] hasRequiredBlocksOnCenter(RegionType regionType, Location location) {
        if (regionType.getBuildRadiusX() != regionType.getBuildRadiusZ() ||
                regionType.getBuildRadiusX() != regionType.getBuildRadiusY()) {
            return new int[0];
        }
        List<HashMap<Material, Integer>> itemCheck = cloneReqMap(regionType.getReqs());
        World currentWorld = location.getWorld();
        if (currentWorld == null) {
            return new int[0];
        }

        int[] radii = new int[6];
        for (int i=0; i< 6; i++) {
            radii[i]=regionType.getBuildRadiusX();
        }

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
            return new int[0];
        } else {
            return radii;
        }
    }

    public static int[] radiusCheck(int[] radii, RegionType regionType) {
        int xRadius = regionType.getBuildRadiusX();
        int yRadius = regionType.getBuildRadiusY();
        int zRadius = regionType.getBuildRadiusZ();
        boolean xRadiusBigger = xRadius > zRadius;
        boolean xRadiusActuallyBigger = radii[0] + radii[2] > radii[1] + radii[3];
        if ((xRadiusActuallyBigger && xRadiusBigger && radii[0] + radii[2] > xRadius * 2) ||
                xRadiusActuallyBigger && !xRadiusBigger && radii[0] + radii[2] > zRadius * 2) {
            return new int[0];
        } else {
            while ((radii[0] + radii[2] < xRadius * 2 && xRadiusActuallyBigger) ||
                    (radii[0] + radii[2] < zRadius * 2 && !xRadiusActuallyBigger)) {
                if (radii[0] < radii[2]) {
                    radii[0]++;
                } else {
                    radii[2]++;
                }
            }
        }
        if (radii[4] + radii[5] > yRadius * 2) {
            return new int[0];
        } else {

            while (radii[4] + radii[5] < yRadius * 2) {
                if (radii[4] < radii[5]) {
                    radii[4]++;
                } else {
                    radii[5]++;
                }
            }
        }
        if ((!xRadiusActuallyBigger && !xRadiusBigger && radii[1] + radii[3] > zRadius * 2) ||
                !xRadiusActuallyBigger && xRadiusBigger && radii[1] + radii[3] > xRadius * 2) {
            return new int[0];
        } else {
            while ((radii[1] + radii[3] < zRadius * 2 && xRadiusActuallyBigger) ||
                    (radii[1] + radii[3] < xRadius * 2 && !xRadiusActuallyBigger)) {
                if (radii[1] < radii[3]) {
                    radii[1]++;
                } else {
                    radii[3]++;
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

        int[] radii = new int[6];
        for (int i = 0; i < 6; i++) {
            radii[i] = 0;
        }
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
        if (radii.length == 0) {
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

        Town town = TownManager.getInstance().getTownAt(location);
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
        Block block = location.getBlock();
        BlockState state = null;
        try {
            state = block.getState();
        } catch (Exception e) {
            e.printStackTrace();
            return needsReagentsOrInput();
        }
        if (!(state instanceof Chest)) {
            return needsReagentsOrInput();
        }
        Chest chest = (Chest) state;
        for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
            if ((ignoreReagents || Util.containsItems(regionUpkeep.getReagents(), chest.getBlockInventory())) &&
                    Util.containsItems(regionUpkeep.getInputs(), chest.getBlockInventory())) {
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
        Block block = location.getBlock();
        BlockState state = block.getState();
        if (!(state instanceof Chest)) {
            return needsReagentsOrInput();
        }
        Chest chest = (Chest) state;

        if ((ignoreReagents || Util.containsItems(regionUpkeep.getReagents(), chest.getBlockInventory())) &&
                Util.containsItems(regionUpkeep.getInputs(), chest.getBlockInventory())) {
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
        if (checkTick && !shouldTick()) {
            return false;
        }

        ItemManager itemManager = ItemManager.getInstance();
        RegionType regionType = (RegionType) itemManager.getItemType(getType());

        boolean hadUpkeep = false;
        Inventory chestInventory = null;
        boolean hasItemUpkeep = false;
        int i=0;
        for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
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
            if (needsItems && chestInventory == null) {
                continue;
            }
            boolean containsReagents = chestInventory != null &&
                    Util.containsItems(regionUpkeep.getReagents(), chestInventory);
            boolean containsInputs = chestInventory != null &&
                    Util.containsItems(regionUpkeep.getInputs(), chestInventory);
            boolean hasReagents = !needsItems || (containsReagents && containsInputs);
            if (!hasReagents) {
                i++;
                continue;
            }

            boolean emptyOutput = regionUpkeep.getOutputs().isEmpty();
            boolean fullChest = chestInventory == null || chestInventory.firstEmpty() == -1;
            if (!emptyOutput && fullChest) {
                i++;
                continue;
            }
            hasItemUpkeep = true;
            failingUpkeeps.remove(i);
            boolean hasMoney = false;
            if (regionUpkeep.getPayout() != 0 && Civs.econ != null) {
                double payout = regionUpkeep.getPayout();
                Town town = TownManager.getInstance().getTownAt(location);
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

                if (payout > 0 && town != null && (town.getGovernmentType() == GovernmentType.COMMUNISM ||
                        town.getGovernmentType() == GovernmentType.COOPERATIVE)) {
                    double size = (double) town.getRawPeople().size();
                    if (town.getGovernmentType() == GovernmentType.COMMUNISM) {
                        payout = payout / size;
                        for (UUID uuid : town.getRawPeople().keySet()) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                            Civs.econ.depositPlayer(offlinePlayer, payout);
                            hasMoney = true;
                        }
                    } else if (town.getGovernmentType() == GovernmentType.COOPERATIVE) {
                        double coopCut = payout * 0.1;
                        town.setBankAccount(town.getBankAccount() + coopCut);
                        HashMap<UUID, Double> payouts = OwnershipUtil.getCooperativeSplit(town);
                        for (UUID uuid : payouts.keySet()) {
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                            Civs.econ.depositPlayer(offlinePlayer, payouts.get(uuid) * payout);
                            hasMoney = true;
                        }
                    }
                } else {
                    payout = payout / (double) getOwners().size();
                    for (UUID uuid : getOwners()) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                        if (payout == 0) {
                            hasMoney = true;
                        } else if (payout > 0) {
                            Civs.econ.depositPlayer(player, payout);
                            hasMoney = true;
                        } else if (Civs.econ.has(player, payout)) {
                            Civs.econ.withdrawPlayer(player, Math.abs(payout));
                            hasMoney = true;
                        }
                    }
                }
            } else {
                hasMoney = true;
            }
            if (!hasMoney) {
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
            if (chestInventory != null) {
                Util.removeItems(regionUpkeep.getInputs(), chestInventory);
                Util.addItems(regionUpkeep.getOutputs(), chestInventory);
            }
            if (regionUpkeep.getExp() > 0) {
                exp += regionUpkeep.getExp();
                RegionManager.getInstance().saveRegion(this);
            }

            if (checkTick) {
                tick();
            }
            hadUpkeep = true;
            Bukkit.getPluginManager().callEvent(new RegionUpkeepEvent(this, i));
            i++;
        }
        if (hadUpkeep) {
            for (UUID uuid : getOwners()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) {
                    continue;
                }
                Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
                TutorialManager.getInstance().completeStep(civilian, TutorialManager.TutorialType.UPKEEP, type);
            }
        }
        if (!hasItemUpkeep) {
            RegionManager.getInstance().addCheckedRegion(this);
        } else {
            RegionManager.getInstance().removeCheckedRegion(this);
        }
        return hadUpkeep;
    }
}
