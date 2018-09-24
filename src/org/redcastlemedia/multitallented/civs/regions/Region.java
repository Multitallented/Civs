package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.RegionUpkeepEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.*;

public class Region {

    private String type;
    private final HashMap<UUID, String> people;
    private final Location location;
    private final int radiusXP;
    private final int radiusZP;
    private final int radiusXN;
    private final int radiusZN;
    private final int radiusYP;
    private final int radiusYN;
    private double exp;
    public HashMap<String, String> effects;
    private long lastTick = 0;

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
    public HashMap<UUID, String> getPeople() {
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTownAt(location);
        if (town == null) {
            return people;
        }
        HashMap<UUID, String> newPeople = (HashMap<UUID, String>) people.clone();
        for (UUID uuid : town.getPeople().keySet()) {
            if (!newPeople.containsKey(uuid)) {
                newPeople.put(uuid, "ally");
            }
        }

        for (String name : town.getAllies()) {
            Town currentTown = townManager.getTown(name);
            if (currentTown != null) {
                for (UUID uuid : currentTown.getPeople().keySet()) {
                    if (!newPeople.containsKey(uuid)) {
                        newPeople.put(uuid, "ally");
                    }
                }
            }
        }
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
    public int getSecondsTillNextTick() {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        long difference = System.currentTimeMillis() - lastTick;
        int remainingCooldown = (int) ((regionType.getPeriod() - difference) / 1000);
        return remainingCooldown < 0 ? 0 : remainingCooldown;
    }

    public String getId() {
        return locationToString(location);
    }
    public static String locationToString(Location location) {
        //TODO test this
        return location.getWorld().getName() + "~" + (int) location.getX() + "~" + (int) location.getY() + "~" + (int) location.getZ();
    }
    public static Location idToLocation(String id) {
        String[] idSplit = id.split("~");
        return new Location(Bukkit.getWorld(idSplit[0]),
                Double.parseDouble(idSplit[1]),
                Double.parseDouble(idSplit[2]),
                Double.parseDouble(idSplit[3]));
    }
    public static int[] hasRequiredBlocks(String type, Location location) {
        return hasRequiredBlocks(type, location, true);
    }

    public static int[] addItemCheck(int[] radii, Location location, World currentWorld,
                                     int xMin, int xMax, int yMin, int yMax, int zMin, int zMax,
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
        outer: for (int x=xMin; x<xMax;x++) {
            for (int y=yMin; y<yMax; y++) {
                for (int z=zMin; z<zMax; z++) {

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
                    outer1: for (HashMap<Material, Integer> tempMap : itemCheck) {
                        if (tempMap.containsKey(mat)) {
                            blocksFound.add(currentBlock);

                            if (tempMap.get(mat) < 2) {
                                destroyIndex = true;
                            } else {
                                tempMap.put(mat, tempMap.get(mat) - 1);
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
        ItemManager itemManager = ItemManager.getInstance();
        List<HashMap<Material, Integer>> itemCheck = new ArrayList<>();
        RegionType regionType = (RegionType) itemManager.getItemType(type);

        for (List<CVItem> currentList : regionType.getReqs()) {
            HashMap<Material, Integer> currentReqMap = new HashMap<>();
            for (CVItem currentItem : currentList) {
                CVItem clone = currentItem.clone();
                currentReqMap.put(clone.getMat(), clone.getQty());
            }
            itemCheck.add(currentReqMap);
        }
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
        int xMax = (int) location.getX() + 1 + (int) ((double) biggestXZRadius * 1.5);
        int xMin = (int) location.getX() - (int) ((double) biggestXZRadius * 1.5);
        int yMax = (int) location.getY() + 1 + (int) ((double) regionType.getBuildRadiusY() * 1.5);
        int yMin = (int) location.getY() - (int) ((double) regionType.getBuildRadiusY() * 1.5);
        int zMax = (int) location.getZ() + 1 + (int) ((double) biggestXZRadius * 1.5);
        int zMin = (int) location.getZ() - (int) ((double) biggestXZRadius * 1.5);

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
        return hasReqs ? radii : new int[0];
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
        List<HashMap<Material, Integer>> itemCheck = new ArrayList<>();
        CVItem missingItem = null;
        if (missingStack != null) {
            missingItem = CVItem.createFromItemStack(missingStack);
        }
        RegionType regionType = (RegionType) itemManager.getItemType(type);
        for (List<CVItem> currentList : regionType.getReqs()) {
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
        int xMax = (int) location.getX() + 1 + (int) ((double) biggestXZRadius * 1.5);
        int xMin = (int) location.getX() - (int) ((double) biggestXZRadius * 1.5);
        int yMax = (int) location.getY() + 1 + (int) ((double) regionType.getBuildRadiusY() * 1.5);
        int yMin = (int) location.getY() - (int) ((double) regionType.getBuildRadiusY() * 1.5);
        int zMax = (int) location.getZ() + 1 + (int) ((double) biggestXZRadius * 1.5);
        int zMin = (int) location.getZ() - (int) ((double) biggestXZRadius * 1.5);

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

        long period = regionType.getPeriod();
        return lastTick + period * 1000 < System.currentTimeMillis();
    }
    public boolean hasUpkeepItems() {
        return hasUpkeepItems(false);
    }
    public boolean hasUpkeepItems(boolean ignoreReagents) {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        if (regionType.getUpkeeps().isEmpty()) {
            return true;
        }
        Block block = location.getBlock();
        if (!(block.getState() instanceof Chest)) {
            return needsReagentsOrInput();
        }
        Chest chest = (Chest) block.getState();
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
    public void tick() {
        this.lastTick = System.currentTimeMillis();
    }

    public boolean needsReagentsOrInput() {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
            if (!regionUpkeep.getReagents().isEmpty() ||
                    !regionUpkeep.getInputs().isEmpty()) {
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

        Block block = getLocation().getBlock();
        ItemManager itemManager = ItemManager.getInstance();
        RegionType regionType = (RegionType) itemManager.getItemType(getType());
        Chest chest = null;
        if (block.getState() instanceof Chest) {
            chest = (Chest) block.getState();
        }

        boolean needsItems = needsReagentsOrInput();
        if (chest == null && needsItems) {
            return false;
        }
        boolean hadUpkeep = false;
        int i=0;
        for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
            boolean hasReagents = !needsItems || (Util.containsItems(regionUpkeep.getReagents(), chest.getBlockInventory()) &&
                    Util.containsItems(regionUpkeep.getInputs(), chest.getBlockInventory()));
            if (!hasReagents) {
                i++;
                continue;
            }

            boolean emptyOutput = regionUpkeep.getOutputs().isEmpty();
            boolean fullChest = chest == null || chest.getBlockInventory().firstEmpty() == -1;

            if (!emptyOutput && fullChest) {
                i++;
                continue;
            }
            boolean hasMoney = false;
            if (regionUpkeep.getPayout() != 0 && Civs.econ != null) {
                double payout = regionUpkeep.getPayout();
                payout = payout / getOwners().size();
                for (UUID uuid : getOwners()) {
                    OfflinePlayer player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        continue;
                    }
                    if (payout > 0) {
                        Civs.econ.depositPlayer(player, payout);
                        hasMoney = true;
                    } else if (Civs.econ.has(player, payout)) {
                        Civs.econ.withdrawPlayer(player, Math.abs(payout));
                        hasMoney = true;
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
            if (chest != null) {
                Util.removeItems(regionUpkeep.getInputs(), chest.getBlockInventory());
                Util.addItems(regionUpkeep.getOutputs(), chest.getBlockInventory());
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
        return hadUpkeep;
    }
}
