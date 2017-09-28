package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.*;

public class RegionManager {
    private ArrayList<Region> regions = new ArrayList<>();
    private HashMap<String, RegionType> regionTypes = new HashMap<>();
    private static RegionManager regionManager;
    private HashMap<String, Integer> itemCheck = new HashMap<>();

    public RegionManager() {
        regionManager = this;
    }

    public void addRegion(Region region) {
        regions.add(region);
        Collections.sort(regions,
                new Comparator<Region>() {
                    @Override
                    public int compare(Region r1, Region r2) {
                        if (r1.getLocation().getX() - r1.getRadiusXN() > r2.getLocation().getX() - r2.getRadiusXN()) {
                            return 1;
                        } else if (r1.getLocation().getX() - r1.getRadiusXN() < r2.getLocation().getX() - r2.getRadiusXN()) {
                            return -1;
                        }
                        return 0;
                    }
                });
    }

    public Region getRegionAt(Location location) {
        if (regions.isEmpty()) {
            return null;
        }

        int index;
        double mindex = 0;
        double maxdex = regions.size() -1;
        double prevIndex = 0;
        for (;;) {
            index = (int) Math.round(((maxdex - mindex) / 2) + mindex);
            Region r = regions.get(index);
            if (prevIndex == index) {
                if (withinRegion(r, location)) {
                    return r;
                } else {
                    return null;
                }
            }

            if (withinRegion(r, location)) {
                return r;
            } else if (location.getX() < r.getLocation().getX() - r.getRadiusXN()) {

                maxdex = index;
            } else if (location.getX() > r.getLocation().getX() + r.getRadiusXN()) {

                mindex = index;
            } else {
                return findRegion((int) mindex, (int) maxdex, location, index);
            }
            prevIndex = index;
        }
    }

    private Region findRegion(int index1, int index2, Location location, int index) {
        for (int i=index1; i<index2; i++) {
            if (i==index) {
                continue;
            }
            if (withinRegion(regions.get(i), location)) {
                return regions.get(i);
            }
        }
        return null;
    }

    private boolean withinRegion(Region region, Location location) {
        Location rLocation = region.getLocation();
        return rLocation.getX() - region.getRadiusXN() <= location.getX() &&
                rLocation.getX() + region.getRadiusXP() >= location.getX() &&
                rLocation.getY() - region.getRadiusYN() <= location.getY() &&
                rLocation.getY() + region.getRadiusYP() >= location.getY() &&
                rLocation.getZ() - region.getRadiusZN() <= location.getZ() &&
                rLocation.getZ() + region.getRadiusZP() >= location.getZ();
    }

    public void loadRegionType(FileConfiguration config) {
        String name = config.getString("name");
        HashSet<CVItem> reqs = new HashSet<>();
        for (String req : config.getStringList("requirements")) {
            reqs.add(CVItem.createCVItemFromString(req));
        }
        HashSet<String> effects = new HashSet<>();
        for (String effect : config.getStringList("effects")) {
            effects.add(effect);
        }
        int buildRadius = config.getInt("build-radius", 5);
        int buildRadiusX = config.getInt("build-radius-x", 5);
        int buildRadiusY = config.getInt("build-radius-y", 5);
        int buildRadiusZ = config.getInt("build-radius-z", 5);
        regionTypes.put(name.toLowerCase(), new RegionType(
                name,
                reqs,
                effects,
                buildRadius,
                buildRadiusX,
                buildRadiusY,
                buildRadiusZ));
    }

    public RegionType getRegionType(String name) {
        return regionTypes.get(name);
    }

    void detectNewRegion(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        String displayName = block.getState().getData().toItemStack().getItemMeta().getDisplayName();
        displayName = displayName.replace("Civs ", "");

        RegionType currentRegionType = getRegionType(displayName.toLowerCase());

        if (currentRegionType == null) {
            return;
        }

        itemCheck.clear();
        for (CVItem currentItem : currentRegionType.getReqs()) {
            itemCheck.put(currentItem.getMat() + ":" + currentItem.getDamage(), currentItem.getQty());
        }
        int radius = currentRegionType.getBuildRadius(); //TODO fix this and make size flexible
        int[] radiuses = new int[6];
        radiuses[0] = radius;
        radiuses[1] = radius;
        radiuses[2] = radius;
        radiuses[3] = radius;
        radiuses[4] = radius;
        radiuses[5] = radius;

        World currentWorld = block.getLocation().getWorld();
        boolean hasReqs = false;
        outer: for (int x=0; x<radius;x++) {
            for (int y=0; y<radius; y++) {
                for (int z=0; z<radius; z++) {
                    Block currentBlock = currentWorld.getBlockAt(x,y,z);
                    if (currentBlock == null) {
                        continue;
                    }


                    String wildCardString = currentBlock.getType() + ":-1";
                    String damageString = currentBlock.getType() + ":";
                    if (currentBlock.getState() != null) {
                        damageString += currentBlock.getState().getData().toItemStack().getDurability();
                    }

                    if (itemCheck.containsKey(wildCardString)) {
                        itemCheck.put(wildCardString, itemCheck.get(wildCardString) - 1);
                        hasReqs = checkIfScanFinished();
                    } else if (itemCheck.containsKey(damageString)) {
                        itemCheck.put(damageString, itemCheck.get(damageString) - 1);
                        hasReqs = checkIfScanFinished();
                    }
                    if (hasReqs) {
                        break outer;
                    }
                }
            }
        }

        if (hasReqs) {
            HashSet<UUID> owners = new HashSet<>();
            owners.add(player.getUniqueId());
            HashSet<UUID> members = new HashSet<>();
            addRegion(new Region(currentRegionType.getName(), owners, members, block.getLocation(), radiuses));
        }
    }

    private boolean checkIfScanFinished() {
        for (String key : itemCheck.keySet()) {
            if (itemCheck.get(key) > 0) {
                return false;
            }
        }
        return true;
    }

    public static synchronized RegionManager getInstance() {
        if (regionManager == null) {
            regionManager = new RegionManager();
        }
        return regionManager;
    }
}
