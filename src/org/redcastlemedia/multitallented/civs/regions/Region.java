package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.*;

public class Region {

    private final String type;
    private final HashSet<UUID> members;
    private final HashSet<UUID> owners;
    private final Location location;
    private final int radiusXP;
    private final int radiusZP;
    private final int radiusXN;
    private final int radiusZN;
    private final int radiusYP;
    private final int radiusYN;
    public HashSet<String> effects;

    public Region(String type, HashSet<UUID> owners, HashSet<UUID> members, Location location, int[] buildRadius, HashSet<String> effects) {
        this.type = type;
        this.owners = owners;
        this.members = members;
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
    public HashSet<UUID> getOwners() {
        return owners;
    }
    public HashSet<UUID> getMembers() {
        return members;
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
        return location.getWorld().getName() + "-" + (int) location.getX() + "-" + (int) location.getY() + "-" + (int) location.getZ();
    }
    public static Location idToLocation(String id) {
        String[] idSplit = id.split("-");
        return new Location(Bukkit.getWorld(idSplit[0]),
                Double.parseDouble(idSplit[1]),
                Double.parseDouble(idSplit[2]),
                Double.parseDouble(idSplit[3]));
    }
    public static int[] hasRequiredBlocks(String type, Location location) {
        RegionManager regionManager = RegionManager.getInstance();
        ItemManager itemManager = ItemManager.getInstance();
        List<HashMap<String, Integer>> itemCheck = new ArrayList<>();
        RegionType regionType = (RegionType) itemManager.getItemType(type);
        for (List<CVItem> currentList : regionType.getReqs()) {
            HashMap<String, Integer> currentReqMap = new HashMap<>();
            for (CVItem currentItem : currentList) {
                currentReqMap.put(currentItem.getMat() + ":" + currentItem.getDamage(), currentItem.getQty());
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


                    String wildCardString = currentBlock.getType() + ":-1";
                    String damageString = currentBlock.getType() + ":";
                    if (currentBlock.getState() != null) {
                        damageString += currentBlock.getState().getData().toItemStack().getDurability();
                    }

                    String destroyIndex = null;
                    int i=0;
                    outer1: for (HashMap<String, Integer> tempMap : itemCheck) {
                        if (tempMap.containsKey(wildCardString)) {
                            int currentQty = tempMap.get(wildCardString) - 1;
                            if (currentQty < 1) {
                                destroyIndex = wildCardString;
                            } else {
                                tempMap.put(wildCardString, currentQty);
                            }
                            regionManager.adjustRadii(radii, location, x, y, z);
                            break outer1;

                        } else if (tempMap.containsKey(damageString)) {
                            int currentQty = tempMap.get(damageString) - 1;
                            if (currentQty < 1) {
                                destroyIndex = damageString;
                            } else {
                                tempMap.put(damageString, currentQty);
                            }
                            regionManager.adjustRadii(radii, location, x, y, z);
                            break outer1;
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
//
//    private static boolean checkIfScanFinished(HashMap<String, Integer> itemCheck) {
//        for (String key : itemCheck.keySet()) {
//            if (itemCheck.get(key) > 0) {
//                return false;
//            }
//        }
//        return true;
//    }
}
