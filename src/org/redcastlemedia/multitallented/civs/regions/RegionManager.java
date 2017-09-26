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
                        if (r1.getLocation().getX() - r1.getXNRadius() > r2.getLocation().getX() - r2.getXNRadius()) {
                            return 1;
                        } else if (r1.getLocation().getX() - r1.getXNRadius() < r2.getLocation().getX() - r2.getXNRadius()) {
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
        int index = (int) Math.floor((double) regions.size() / 2);
        int fragSize = index;
        for (int i= (int) Math.ceil(regions.size() / 4); i>-1; i--) {
            Region r = regions.get(index);
            if (location.getX() < r.getLocation().getX() - r.getXNRadius()) {
                fragSize = (int) Math.floor(fragSize / 2);
                index = (int) Math.floor(index - fragSize);
            } else if (location.getX() > r.getLocation().getX() + r.getXNRadius()) {
                fragSize = (int) Math.floor(fragSize / 2);
                index = (int) Math.floor(index + fragSize);
            } else {
                if (withinRegion(r, location)) {
                    return r;
                }
            }
        }
        return null;
    }
    private boolean withinRegion(Region region, Location location) {
        Location rLocation = region.getLocation();
        return rLocation.getX() - region.getXNRadius() <= location.getX() &&
                rLocation.getX() + 5 >= location.getX() && //TODO fix radius
                rLocation.getY() - 5 <= location.getY() &&
                rLocation.getY() + 5 >= location.getY() &&
                rLocation.getZ() - 5 <= location.getZ() &&
                rLocation.getZ() + 5 >= location.getZ();
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
        regionTypes.put(name.toLowerCase(), new RegionType(name, reqs, effects));
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
        int radius = 5; //TODO fix this and make size flexible
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
            addRegion(new Region(currentRegionType.getName(), owners, members, block.getLocation()));
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
