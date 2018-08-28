package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.*;

public class Region {

    private final String type;
    private final HashMap<UUID, String> people;
    private final Location location;
    private final int radiusXP;
    private final int radiusZP;
    private final int radiusXN;
    private final int radiusZN;
    private final int radiusYP;
    private final int radiusYN;
    public HashMap<String, String> effects;
    private long lastTick = 0;

    public Region(String type, HashMap<UUID, String> people, Location location, int[] buildRadius, HashMap<String, String> effects) {
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
    }
    public String getType() {
        return type;
    }
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
            System.out.println(name);
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

    public String getId() {
        return locationToString(location);
    }
    public static String locationToString(Location location) {
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
    public static int[] hasRequiredBlocks(String type, Location location, boolean useCivItem) {
        RegionManager regionManager = RegionManager.getInstance();
        ItemManager itemManager = ItemManager.getInstance();
        List<HashSet<CVItem>> itemCheck = new ArrayList<>();
        RegionType regionType = (RegionType) itemManager.getItemType(type);
        for (List<CVItem> currentList : regionType.getReqs()) {
            HashSet<CVItem> currentReqMap = new HashSet<>();
            for (CVItem currentItem : currentList) {
                currentReqMap.add(currentItem.clone());
            }
            itemCheck.add(currentReqMap);
        }
        int[] radii = new int[6];
        radii[0] = 0;
        radii[1] = 0;
        radii[2] = 0;
        radii[3] = 0;
        radii[4] = 0;
        radii[5] = 0;
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

        boolean hasReqs = false;
        outer: for (int x=xMin; x<xMax;x++) {
            for (int y=yMin; y<yMax; y++) {
                for (int z=zMin; z<zMax; z++) {
                    Block currentBlock = currentWorld.getBlockAt(x,y,z);
                    if (currentBlock == null) {
                        continue;
                    }

                    CVItem destroyIndex = null;
                    int i=0;
                    outer1: for (HashSet<CVItem> tempMap : itemCheck) {
                        for (CVItem item : tempMap) {
                            if (item.equivalentItem(currentBlock.getState().getData().toItemStack(1), false)) {
                                if (item.getQty() < 2) {
                                    destroyIndex = item;
                                } else {
                                    item.setQty(item.getQty() - 1);
                                }
                                regionManager.adjustRadii(radii, location, x,y,z);
                                break outer1;
                            }
                        }
                        i++;
                    }
                    if (destroyIndex != null) {
                        if (itemCheck.size() < 2) {
                            hasReqs = true;
                            break outer;
                        } else {
                            itemCheck.remove(i);
                        }
                    }
                }
            }
        }
        if (hasReqs && useCivItem) {
            Block centerBlock = location.getBlock();
            if (centerBlock == null) {
                hasReqs = false;
            } else if (!regionType.equivalentItem(centerBlock.getState().getData().toItemStack(1), false)) {
                hasReqs = false;
            }
        }

        radii = radiusCheck(radii, regionType);
        if (radii.length == 0) {
            return radii;
        }
        return hasReqs ? radii : new int[0];
    }
    private static int[] radiusCheck(int[] radii, RegionType regionType) {
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
    public static List<HashSet<CVItem>> hasRequiredBlocks(String type, Location location, ItemStack missingStack) {
        RegionManager regionManager = RegionManager.getInstance();
        ItemManager itemManager = ItemManager.getInstance();
        List<HashSet<CVItem>> itemCheck = new ArrayList<>();
        CVItem missingItem = null;
        if (missingStack != null) {
            missingItem = CVItem.createFromItemStack(missingStack);
        }
        RegionType regionType = (RegionType) itemManager.getItemType(type);
        for (List<CVItem> currentList : regionType.getReqs()) {
            HashSet<CVItem> currentReqMap = new HashSet<>();
            for (CVItem currentItem : currentList) {
                CVItem currentClone = currentItem.clone();
                if (missingItem != null && missingItem.equivalentCVItem(currentClone)) {
                    currentClone.setQty(currentClone.getQty() + 1);
                }
                currentReqMap.add(currentClone);
            }
            itemCheck.add(currentReqMap);
        }
        int[] radii = new int[6];
        radii[0] = 0;
        radii[1] = 0;
        radii[2] = 0;
        radii[3] = 0;
        radii[4] = 0;
        radii[5] = 0;
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

        boolean hasReqs = false;
        outer: for (int x=xMin; x<xMax;x++) {
            for (int y=yMin; y<yMax; y++) {
                for (int z=zMin; z<zMax; z++) {
                    Block currentBlock = currentWorld.getBlockAt(x,y,z);
                    if (currentBlock == null) {
                        continue;
                    }

                    CVItem destroyIndex = null;
                    int i=0;
                    outer1: for (HashSet<CVItem> tempMap : itemCheck) {
                        for (CVItem item : tempMap) {
                            if (item.equivalentItem(currentBlock.getState().getData().toItemStack(1), false)) {
                                if (item.getQty() < 2) {
                                    destroyIndex = item;
                                } else {
                                    item.setQty(item.getQty() - 1);
                                }
                                regionManager.adjustRadii(radii, location, x,y,z);
                                break outer1;
                            }
                        }
                        i++;
                    }
                    if (destroyIndex != null) {
                        if (itemCheck.size() < 2) {
                            hasReqs = true;
                            break outer;
                        } else {
                            itemCheck.remove(i);
                        }
                    }
                }
            }
        }

        radii = radiusCheck(radii, regionType);
        if (radii.length == 0) {
            return itemCheck;
        }
        return hasReqs ? null : itemCheck;
    }
//    public boolean hasUpkeepItems() {
//        Block block = location.getBlock();
//        if (!(block.getState() instanceof Chest)) {
//            return false;
//        }
//        Chest chest = (Chest) block.getState();
//
//        if (chest.getInventory().firstEmpty() == -1) {
//            return false;
//        }
//        ItemManager itemManager = ItemManager.getInstance();
//        RegionType regionType = (RegionType) itemManager.getItemType(type);
//        outer: for (List<CVItem> currentList : regionType.getReagents()) {
//            boolean hasItem = false;
//            for (CVItem item : currentList) {
//                if (item.isWildDamage() && item.getDisplayName() == null &&
//                        chest.getInventory().contains(item.getMat())) {
//                    hasItem = true;
//                    break;
//                } else if (item.isWildDamage() && item.getDisplayName() != null) {
//                    for (ItemStack is : chest.getInventory()) {
//                        if (is != null &&
//                                is.hasItemMeta() &&
//                                is.getItemMeta().getDisplayName().equals(item.getDisplayName()) &&
//                                is.getType() == item.getMat()) {
//                            hasItem = true;
//                            break;
//                        }
//                    }
//                } else if (!item.isWildDamage() &&
//                        chest.getInventory().contains(item.createItemStack())) {
//                    hasItem = true;
//                    break;
//                }
//            }
//            if (!hasItem) {
//                return false;
//            }
//        }
//        return true;
//    }
    public boolean shouldTick() {
        ItemManager itemManager = ItemManager.getInstance();
        RegionType regionType = (RegionType) itemManager.getItemType(type);

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
        if (!shouldTick()) {
            return false;
        }

        Block block = getLocation().getBlock();
        ItemManager itemManager = ItemManager.getInstance();
        RegionType regionType = (RegionType) itemManager.getItemType(getType());
        Chest chest = null;
        if (block.getState() instanceof Chest) {
            chest = (Chest) block.getState();
        }

        if (chest == null) {
            return needsReagentsOrInput();
        }
        boolean hadUpkeep = false;
        for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
            boolean hasReagents = Util.containsItems(regionUpkeep.getReagents(), chest.getBlockInventory()) &&
                    Util.containsItems(regionUpkeep.getInputs(), chest.getBlockInventory());
            if (!hasReagents) {
                continue;
            }

            boolean emptyOutput = regionUpkeep.getOutputs().isEmpty();
            boolean fullChest = chest.getBlockInventory().firstEmpty() == -1;

            if (!emptyOutput && fullChest) {
                continue;
            }
            boolean hasMoney = false;
            if (Civs.econ != null) {
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
                continue;
            }
            Util.removeItems(regionUpkeep.getInputs(), chest.getBlockInventory());
            Util.addItems(regionUpkeep.getOutputs(), chest.getBlockInventory());
            tick();
            hadUpkeep = true;
        }
        return hadUpkeep;
    }
}
