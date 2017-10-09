package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.Bukkit;
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
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RegionManager {
    private HashMap<String, ArrayList<Region>> regions = new HashMap<>();
    private static RegionManager regionManager;

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
            Civs.logger.warning("No region files found to load");
            return;
        }

    }
    public Set<Region> getAllRegions() {
        Set<Region> returnSet = new HashSet<>();
        for (String worldName : regions.keySet()) {
            returnSet.addAll(regions.get(worldName));
        }
        return returnSet;
    }

    public void removeRegion(Region region, boolean broadcast) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(region.getLocation()) < 25) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-destroyed").replace("$1", region.getType()));
            }
        }
        removeRegion(region);
    }

    public void removeRegion(Region region) {
        regions.get(region.getLocation().getWorld().getName()).remove(region);
        Civs civs = Civs.getInstance();
        if (civs == null) {
            return;
        }
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
        if (civs == null) {
            return;
        }
        File regionFolder = new File(civs.getDataFolder(), "regions");
        if (!regionFolder.exists()) {
            regionFolder.mkdir();
        }
        File regionFile = new File(regionFolder, region.getId() + ".yml");
        if (!regionFile.exists()) {
            try {
                regionFile.createNewFile();
            } catch (IOException ioexception) {
                Civs.logger.severe("Unable to create " + region.getId() + ".yml");
                return;
            }
        }
        FileConfiguration regionConfig = new YamlConfiguration();
        try {
            regionConfig.load(regionFile);
            regionConfig.set("location", region.getId());
            regionConfig.set("xn-radius", region.getRadiusXN());
            regionConfig.set("xp-radius", region.getRadiusXP());
            regionConfig.set("yn-radius", region.getRadiusYN());
            regionConfig.set("yp-radius", region.getRadiusYP());
            regionConfig.set("zn-radius", region.getRadiusZN());
            regionConfig.set("zp-radius", region.getRadiusZP());
            for (UUID uuid : region.getPeople().keySet()) {
                regionConfig.set("people." + uuid, region.getPeople().get(uuid));
            }
            regionConfig.set("type", region.getType());
            regionConfig.save(regionFile);
        } catch (Exception e) {
            Civs.logger.severe("Unable to write to " + region.getId() + ".yml");
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

            HashMap<UUID, String> people = new HashMap<>();
            for (String s : regionConfig.getConfigurationSection("people").getKeys(false)) {
                people.put(UUID.fromString(s), regionConfig.getString("people." + s));
            }
            region = new Region(
                    regionConfig.getString("type"),
                    people,
                    location,
                    radii,
                    (HashSet<String>) ((RegionType) ItemManager.getInstance().getItemType(regionConfig.getString("type"))).getEffects().clone()
            );
        } catch (Exception e) {
            Civs.logger.severe("Unable to read " + regionFile.getName());
            return null;
        }
        return region;
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

    void detectNewRegion(BlockPlaceEvent event) {
        LocaleManager localeManager = LocaleManager.getInstance();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        String regionTypeName = event.getItemInHand().getItemMeta().getDisplayName();
        regionTypeName = regionTypeName.replace("Civs ", "");

        RegionType regionType;
        try {
            regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName.toLowerCase());
        } catch (Exception e) {
            Civs.logger.severe("Unable to find region type " + regionTypeName.toLowerCase());
            return;
        }
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

        int[] radii = Region.hasRequiredBlocks(regionType.getName().toLowerCase(), block.getLocation());
        if (radii.length == 0) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "no-required-blocks")
                            .replace("$1", regionTypeName));
            return;
        }
        HashMap<UUID, String> people;
        if (rebuildRegion != null) {
            people = (HashMap<UUID, String>) rebuildRegion.getPeople().clone();
            //TODO copy over other stuff too
            removeRegion(rebuildRegion);
        } else {
            people = new HashMap<>();
            people.put(player.getUniqueId(), "owner");
        }
        for (Region currentRegion : regionManager.getRegionsXYZ(block.getLocation(),
                regionType.getBuildRadiusX(), regionType.getBuildRadiusY(), regionType.getBuildRadiusZ(), false)) {
            if (currentRegion == rebuildRegion) {
                continue;
            }
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "too-close-region")
                            .replace("$1", regionTypeName).replace("$2",currentRegion.getType()));
            return;
        }

        //TODO remove rebuildRegion

        player.sendMessage(Civs.getPrefix() +
            localeManager.getTranslation(civilian.getLocale(), "region-built").replace("$1", regionTypeName));
        addRegion(new Region(regionType.getName(), people, block.getLocation(), radii, regionType.getEffects()));
    }

    void adjustRadii(int[] radii, Location location, int x, int y, int z) {
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

    public Set<Region> getContainingRegions(Location location, int modifier) {
        return getRegions(location, modifier, false);
    }

    public Set<Region> getRegionEffectsAt(Location location, int modifier) {
        return getRegions(location, modifier, true);
    }

    public Set<Region> getRegions(Location location, int modifier, boolean useEffects) {
        return getRegionsXYZ(location, modifier, modifier, modifier, useEffects);
    }
    public Set<Region> getRegionsXYZ(Location location, int modifierX, int modifierY, int modifierZ, boolean useEffects) {
        String worldName = location.getWorld().getName();
        HashSet<Region> effects = new HashSet<>();
        if (regions.get(worldName) == null) {
            return effects;
        }
        for (int i=regions.get(worldName).size() - 1; i>-1; i--) {
            Region region = regions.get(worldName).get(i);
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (!useEffects) {
                boolean withinX = location.getX() > region.getLocation().getX() - region.getRadiusXN() - modifierX &&
                        location.getX() < region.getLocation().getX() + region.getRadiusXP() + 1 + modifierX;
                boolean withinY = location.getY() > region.getLocation().getY() - region.getRadiusYN() - modifierY &&
                        location.getY() < region.getLocation().getY() + region.getRadiusYP() + 1 + modifierY;
                boolean withinZ = location.getZ() > region.getLocation().getZ() - region.getRadiusZN() - modifierZ &&
                        location.getZ() < region.getLocation().getZ() + region.getRadiusZP() + 1 + modifierZ;

                if (withinX && withinY && withinZ) {
                    effects.add(region);
                    continue;
                }
                if (location.getX() > region.getLocation().getX() - region.getRadiusXN() - modifierX) {
                    break;
                }
            } else {
                boolean withinX = location.getX() > region.getLocation().getX() - regionType.getEffectRadius() - modifierX &&
                        location.getX() < region.getLocation().getX() + regionType.getEffectRadius() + 1 + modifierX;
                boolean withinY = location.getY() > region.getLocation().getY() - regionType.getEffectRadius() - modifierY &&
                        location.getY() < region.getLocation().getY() + regionType.getEffectRadius() + 1 + modifierY;
                boolean withinZ = location.getZ() > region.getLocation().getZ() - regionType.getEffectRadius() - modifierZ &&
                        location.getZ() < region.getLocation().getZ() + regionType.getEffectRadius() + 1 + modifierZ;

                if (withinX && withinY && withinZ) {
                    effects.add(region);
                    continue;
                }
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
