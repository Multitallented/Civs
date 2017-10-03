package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RegionManager {
    private HashMap<String, ArrayList<Region>> regions = new HashMap<>();
    private HashMap<String, RegionType> regionTypes = new HashMap<>();
    private static RegionManager regionManager;
    private HashMap<String, Integer> itemCheck = new HashMap<>();

    public RegionManager() {
        regionManager = this;
    }

    public void addRegion(Region region) {
        String worldName = region.getLocation().getWorld().getName();
        if (!regions.containsKey(worldName)) {
            regions.put(worldName, new ArrayList<Region>());
        }
        regions.get(worldName).add(region);
        Collections.sort(regions.get(worldName),
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
        saveRegion(region);
    }
    public void loadAllRegions() {
        regions.clear();
        Civs civs = Civs.getInstance();
        File regionFolder = new File(civs.getDataFolder(), "regions");
        if (!regionFolder.exists()) {
            regionFolder.mkdir();
        }
        try {
            for (File file : regionFolder.listFiles()) {
                Region region = loadRegion(file);
                String worldName = region.getLocation().getWorld().getName();
                if (!regions.containsKey(worldName)) {
                    regions.put(worldName, new ArrayList<Region>());
                }
                regions.get(worldName).add(region);
            }
        } catch (NullPointerException npe) {
            Civs.logger.warning(Civs.getPrefix() + "No region files found to load");
            return;
        }

    }

    public void removeRegion(Region region) {
        regions.get(region.getLocation().getWorld().getName()).remove(region);
        Civs civs = Civs.getInstance();
        File dataFolder = new File(civs.getDataFolder(), "regions");
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        File regionFile = new File(dataFolder, region.getId() + ".yml");
        if (!regionFile.exists()) {
            return;
        }
        regionFile.delete();
    }

    private void saveRegion(Region region) {
        Civs civs = Civs.getInstance();
        File regionFolder = new File(civs.getDataFolder(), "regions");
        if (!regionFolder.exists()) {
            regionFolder.mkdir();
        }
        File regionFile = new File(regionFolder, region.getId() + ".yml");
        if (!regionFile.exists()) {
            try {
                regionFile.createNewFile();
            } catch (IOException ioexception) {
                Civs.logger.severe(Civs.getPrefix() + "Unable to create " + region.getId() + ".yml");
                return;
            }
        }
        FileConfiguration regionConfig = new YamlConfiguration();
        try {
            regionConfig.load(regionFile);
            regionConfig.set("location", region.getId());
            regionConfig.set("owners",region.getOwners());
            regionConfig.set("members", region.getMembers());
            regionConfig.set("xn-radius", region.getRadiusXN());
            regionConfig.set("xp-radius", region.getRadiusXP());
            regionConfig.set("yn-radius", region.getRadiusYN());
            regionConfig.set("yp-radius", region.getRadiusYP());
            regionConfig.set("zn-radius", region.getRadiusZN());
            regionConfig.set("zp-radius", region.getRadiusZP());
            regionConfig.set("type", region.getType());
            regionConfig.save(regionFile);
        } catch (Exception e) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to write to " + region.getId() + ".yml");
            return;
        }
    }
    private Region loadRegion(File regionFile) {
        FileConfiguration regionConfig = new YamlConfiguration();
        Region region;
        try {
            regionConfig.load(regionFile);
            int[] radii = new int[6];
            radii[0] = regionConfig.getInt("xp-radius");
            radii[1] = regionConfig.getInt("zp-radius");
            radii[2] = regionConfig.getInt("xn-radius");
            radii[3] = regionConfig.getInt("zn-radius");
            radii[4] = regionConfig.getInt("yp-radius");
            radii[5] = regionConfig.getInt("yn-radius");
            Location location = Region.idToLocation(regionConfig.getString("location"));
            region = new Region(
                    regionConfig.getString("type"),
                    processPersonList(regionConfig.getStringList("owners")),
                    processPersonList(regionConfig.getStringList("members")),
                    location,
                    radii
            );
        } catch (Exception e) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to read " + regionFile.getName());
            return null;
        }
        return region;
    }
    private HashSet<UUID> processPersonList(List<String> persons) {
        HashSet<UUID> returnSet = new HashSet<>();
        for (String s : persons) {
            returnSet.add(UUID.fromString(s));
        }
        return returnSet;
    }

    public Region getRegionAt(Location location) {
        String worldName = location.getWorld().getName();
        if (regions.get(worldName) == null || regions.get(worldName).isEmpty()) {
            return null;
        }

        int index;
        double mindex = 0;
        double maxdex = regions.get(worldName).size() -1;
        double prevIndex = 0;
        for (;;) {
            index = (int) Math.round(((maxdex - mindex) / 2) + mindex);
            Region r = regions.get(worldName).get(index);
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
        String worldName = location.getWorld().getName();
        for (int i=index1; i<index2; i++) {
            if (i==index) {
                continue;
            }
            if (withinRegion(regions.get(worldName).get(i), location)) {
                return regions.get(worldName).get(i);
            }
        }
        return null;
    }

    private boolean withinRegion(Region region, Location location) {
        Location rLocation = region.getLocation();
        return rLocation.getX() - region.getRadiusXN() <= location.getX() &&
                rLocation.getX() + 1 +region.getRadiusXP() >= location.getX() &&
                rLocation.getY() - region.getRadiusYN() <= location.getY() &&
                rLocation.getY() + 1 + region.getRadiusYP() >= location.getY() &&
                rLocation.getZ() - region.getRadiusZN() <= location.getZ() &&
                rLocation.getZ() + 1 + region.getRadiusZP() >= location.getZ();
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
        int buildRadiusX = config.getInt("build-radius-x", buildRadius);
        int buildRadiusY = config.getInt("build-radius-y", buildRadius);
        int buildRadiusZ = config.getInt("build-radius-z", buildRadius);
        int effectRadius = config.getInt("effect-radius", buildRadius);
        String rebuild = config.getString("rebuild");
        regionTypes.put(name.toLowerCase(), new RegionType(
                name,
                reqs,
                effects,
                buildRadius,
                buildRadiusX,
                buildRadiusY,
                buildRadiusZ,
                effectRadius,
                rebuild));
    }

    public RegionType getRegionType(String name) {
        return regionTypes.get(name);
    }

    void detectNewRegion(BlockPlaceEvent event) {
        LocaleManager localeManager = LocaleManager.getInstance();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        String regionTypeName = block.getState().getData().toItemStack().getItemMeta().getDisplayName();
        regionTypeName = regionTypeName.replace("Civs ", "");

        RegionType regionType = getRegionType(regionTypeName.toLowerCase());
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        if (regionType == null) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "no-region-type-found")
                            .replace("$1", regionTypeName));
            return;
        }

        Region rebuildRegion = getRegionAt(block.getLocation());
        if (rebuildRegion != null && regionType.getRebuild() == null) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "cant-build-on-region")
                            .replace("$1", regionTypeName).replace("$2", rebuildRegion.getType()));
            return;
        } else if (rebuildRegion != null && !regionType.getRebuild().equals(rebuildRegion.getType())) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "cant-build-on-region")
                            .replace("$1", regionTypeName).replace("$2", rebuildRegion.getType()));
            return;
        } else if (rebuildRegion == null && regionType.getRebuild() != null) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "rebuild-required")
                            .replace("$1", regionTypeName).replace("$2", regionType.getRebuild()));
            return;
        }

        itemCheck.clear();
        for (CVItem currentItem : regionType.getReqs()) {
            itemCheck.put(currentItem.getMat() + ":" + currentItem.getDamage(), currentItem.getQty());
        }
        int[] radii = new int[6];
        radii[0] = 0;
        radii[1] = 0;
        radii[2] = 0;
        radii[3] = 0;
        radii[4] = 0;
        radii[5] = 0;

        World currentWorld = block.getLocation().getWorld();
        Location location = block.getLocation();
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

                    if (itemCheck.containsKey(wildCardString)) {
                        itemCheck.put(wildCardString, itemCheck.get(wildCardString) - 1);
                        hasReqs = checkIfScanFinished();
                        adjustRadii(radii, location, x, y, z);

                    } else if (itemCheck.containsKey(damageString)) {
                        itemCheck.put(damageString, itemCheck.get(damageString) - 1);
                        hasReqs = checkIfScanFinished();
                        adjustRadii(radii, location, x, y, z);
                    }
                    if (hasReqs) {
                        break outer;
                    }
                }
            }
        }

        if (!radiusCheck(radii, regionType)) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "building-too-big")
                            .replace("$1", regionTypeName));
            player.sendMessage(Civs.getPrefix() + "" + regionTypeName);
            return;
        }

        if (!hasReqs) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "no-required-blocks")
                            .replace("$1", regionTypeName));
            return;
        }
        HashSet<UUID> owners;
        HashSet<UUID> members;
        if (rebuildRegion != null) {
            owners = (HashSet<UUID>) rebuildRegion.getOwners().clone();
            members = (HashSet<UUID>) rebuildRegion.getMembers().clone();
            removeRegion(rebuildRegion);
        } else {
            owners = new HashSet<>();
            owners.add(player.getUniqueId());
            members = new HashSet<>();
        }
        addRegion(new Region(regionType.getName(), owners, members, block.getLocation(), radii));
    }

    private void adjustRadii(int[] radii, Location location, int x, int y, int z) {
        int currentRelativeX = x - (int) location.getX();
        int currentRelativeY = y - (int) location.getY();
        int currentRelativeZ = z - (int) location.getZ();
        if (currentRelativeX < 0) {
            currentRelativeX = Math.abs(currentRelativeX);
            radii[2] = radii[2] > currentRelativeX ? radii[2] : currentRelativeX;
        } else if (currentRelativeX > 0) {
            radii[0] = radii[0] > currentRelativeX ? radii[0] : currentRelativeX;
        }
        if (currentRelativeY < 0) {
            currentRelativeY = Math.abs(currentRelativeY);
            radii[5] = radii[5] > currentRelativeY ? radii[5] : currentRelativeY;
        } else if (currentRelativeY > 0) {
            radii[4] = radii[4] > currentRelativeY ? radii[4] : currentRelativeY;
        }
        if (currentRelativeZ < 0) {
            currentRelativeZ = Math.abs(currentRelativeZ);
            radii[3] = radii[3] > currentRelativeZ ? radii[3] : currentRelativeZ;
        } else if (currentRelativeZ > 0) {
            radii[1] = radii[1] > currentRelativeZ ? radii[1] : currentRelativeZ;
        }
    }

    private boolean radiusCheck(int[] radii, RegionType regionType) {
        int xRadius = regionType.getBuildRadiusX();
        int yRadius = regionType.getBuildRadiusY();
        int zRadius = regionType.getBuildRadiusZ();
        boolean xRadiusBigger = xRadius > zRadius;
        boolean xRadiusActuallyBigger = radii[0] + radii[2] > radii[1] + radii[3];
        if ((xRadiusActuallyBigger && xRadiusBigger && radii[0] + radii[2] > xRadius * 2) ||
                xRadiusActuallyBigger && !xRadiusBigger && radii[0] + radii[2] > zRadius * 2) {
            return false;
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
            return false;
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
            return false;
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
        return true;
    }

    private boolean checkIfScanFinished() {
        for (String key : itemCheck.keySet()) {
            if (itemCheck.get(key) > 0) {
                return false;
            }
        }
        return true;
    }

    public Set<Region> getRegionEffectsAt(Location location, int modifier) {
        String worldName = location.getWorld().getName();
        HashSet<Region> effects = new HashSet<>();
        for (int i=regions.get(worldName).size() - 1; i>-1; i--) {
            Region region = regions.get(worldName).get(i);
            boolean withinX = location.getX() > region.getLocation().getX() - region.getRadiusXN() - modifier &&
                    location.getX() < region.getLocation().getX() + region.getRadiusXP() + 1 + modifier;
            boolean withinY = location.getY() > region.getLocation().getY() - region.getRadiusYN() - modifier &&
                    location.getY() < region.getLocation().getY() + region.getRadiusYP() + 1 + modifier;
            boolean withinZ = location.getZ() > region.getLocation().getZ() - region.getRadiusZN() - modifier &&
                    location.getZ() < region.getLocation().getZ() + region.getRadiusZP() + 1 + modifier;

            if (withinX && withinY && withinZ) {
                effects.add(region);
                continue;
            }
            if (location.getX() > region.getLocation().getX() - region.getRadiusXN() - modifier) {
                break;
            }
        }
        return effects;
    }

    public static synchronized RegionManager getInstance() {
        if (regionManager == null) {
            regionManager = new RegionManager();
        }
        return regionManager;
    }
}
