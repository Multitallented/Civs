package org.redcastlemedia.multitallented.civs.regions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.BlockLogger;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.effects.ActiveEffect;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.regions.effects.CreateRegionListener;
import org.redcastlemedia.multitallented.civs.regions.effects.DestroyRegionListener;
import org.redcastlemedia.multitallented.civs.regions.effects.RegionCreatedListener;
import org.redcastlemedia.multitallented.civs.regions.effects.WarehouseEffect;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.DebugLogger;
import org.redcastlemedia.multitallented.civs.util.StructureUtil;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class RegionManager {

    private HashMap<UUID, ArrayList<Region>> regions = new HashMap<>();
    protected HashMap<String, Region> regionLocations = new HashMap<>();
    private static RegionManager regionManager;
    private HashMap<String, CreateRegionListener> createRegionListeners = new HashMap<>();
    private HashMap<String, RegionCreatedListener> regionCreatedListenerHashMap = new HashMap<>();
    private HashMap<String, DestroyRegionListener> destroyRegionListener = new HashMap<>();
    private HashSet<Region> checkedRegions = new HashSet<>();
    private ArrayList<Region> needsSaving = new ArrayList<>();

    public void reload() {
        regions.clear();
        regionLocations.clear();
        if (Civs.getInstance() != null) {
            loadAllRegions();
        }
        checkedRegions.clear();
    }

    public void addRegionCreatedListener(String key, RegionCreatedListener listener) {
        regionCreatedListenerHashMap.put(key, listener);
    }

    public void addCreateRegionListener(String key, CreateRegionListener listener) {
        createRegionListeners.put(key, listener);
    }

    public void addDestroyRegionListener(String key, DestroyRegionListener listener) {
        destroyRegionListener.put(key, listener);
    }

    public void addRegion(Region region) {
        saveRegionNow(region);
        UUID worldUuid = region.getLocation().getWorld().getUID();
        if (!regions.containsKey(worldUuid)) {
            regions.put(worldUuid, new ArrayList<>());
        }
        regions.get(worldUuid).add(region);
        regionLocations.put(region.getId(), region);
        sortRegions(worldUuid);
        for (String key : regionCreatedListenerHashMap.keySet()) {
            if (region.getEffects().containsKey(key)) {
                regionCreatedListenerHashMap.get(key).regionCreatedHandler(region);
            }
        }
    }
    public void loadAllRegions() {
        regions.clear();
        regionLocations.clear();
        File regionFolder = new File(Civs.dataLocation, Constants.REGIONS);
        if (!regionFolder.exists()) {
            regionFolder.mkdir();
        }
        try {
            for (File file : regionFolder.listFiles()) {
                try {
                    Region region = loadRegion(file);
                    if (region == null || regionLocations.containsKey(region.getId())) {
                        continue;
                    }
                    UUID worldName = region.getLocation().getWorld().getUID();
                    if (!regions.containsKey(worldName)) {
                        regions.put(worldName, new ArrayList<>());
                    }
                    regions.get(worldName).add(region);
                    sortRegions(worldName);
                    regionLocations.put(region.getId(), region);
                } catch (Exception e) {
                    Civs.logger.severe("Unable to load invalid region file " + file.getName());
                }
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            Civs.logger.warning("No region files found to load");
            return;
        }

    }

    public Region getRegionById(String id) {
        return regionLocations.get(id);
    }

    private void sortRegions(UUID worldName) {
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
    }

    public Set<Region> getAllRegions() {
        Set<Region> returnSet = new HashSet<>();
        for (Map.Entry<UUID, ArrayList<Region>> entry : regions.entrySet()) {
            returnSet.addAll(entry.getValue());
        }
        return returnSet;
    }

    public void removeRegion(Region region, boolean broadcast, boolean checkCritReqs) {
        if (broadcast) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (region.getLocation().getWorld() == null ||
                        !region.getLocation().getWorld().equals(player.getWorld())) {
                    continue;
                }
                if (player.getLocation().distance(region.getLocation()) < 25) {
                    Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                    player.sendMessage(Civs.getPrefix() +
                            LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-destroyed").replace("$1", region.getType()));
                }
            }
        }
        for (Map.Entry<String, DestroyRegionListener> entry : this.destroyRegionListener.entrySet()) {
            entry.getValue().destroyRegionHandler(region);
        }
        Bukkit.getPluginManager().callEvent(new RegionDestroyedEvent(region));

        if (checkCritReqs) {
            TownManager.getInstance().checkCriticalRequirements(region);
        }
        Block block = region.getLocation().getBlock();
        if (block instanceof Chest) {
            ItemStack[] contents = ((Chest) block).getBlockInventory().getContents();
            for (ItemStack is : contents) {
                if (is != null) {
                    block.getWorld().dropItemNaturally(block.getLocation(), is);
                }
            }
            ((Chest) block).getBlockInventory().clear();
        }

        if (Civs.getInstance() != null) {
            region.getLocation().getBlock().setType(Material.AIR);
        }
        BlockLogger.getInstance().removeBlock(region.getLocation());
        removeRegion(region);
    }

    private void removeRegion(Region region) {
        for (Map.Entry<UUID, ArrayList<Region>> entry : regions.entrySet()) {
            entry.getValue().remove(region);
        }
        regionLocations.remove(region.getId());
        File dataFolder = new File(Civs.dataLocation, Constants.REGIONS);
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        Civs.logger.info(region.getType() + "@" + region.getId() + " was removed.");
        File regionFile = new File(dataFolder, region.getId() + ".yml");
        if (!regionFile.exists()) {
            return;
        }
        regionFile.delete();
    }

    public void saveRegion(Region region) {
        needsSaving.add(region);
    }

    public int getCountOfPendingSaves() {
        return needsSaving.size();
    }

    public void saveNextRegion() {
        Region r = null;
        for (Region region : needsSaving) {
            r = region;
            saveRegionNow(r);
            break;
        }
        if (r != null) {
            while (needsSaving.contains(r)) {
                needsSaving.remove(r);
            }
        }
    }

    public void saveAllUnsavedRegions() {
        for (Region region : needsSaving) {
            saveRegionNow(region);
        }
        needsSaving.clear();
    }

    private static void saveRegionNow(Region region) {
        if (Civs.getInstance() == null) {
            return;
        }
        if (ConfigManager.getInstance().isDebugLog()) {
            DebugLogger.saves++;
        }
        File regionFolder = new File(Civs.dataLocation, Constants.REGIONS);
        if (!regionFolder.exists()) {
            boolean folderCreated = regionFolder.mkdir();
            if (!folderCreated) {
                Civs.logger.log(Level.SEVERE, "Unable to create {0} folder", Constants.REGIONS);
                return;
            }
        }
        File regionFile = new File(regionFolder, region.getId() + ".yml");
        if (!regionFile.exists()) {
            try {
                boolean fileCreated = regionFile.createNewFile();
                if (!fileCreated) {
                    Civs.logger.severe("Unable to create " + region.getId() + ".yml");
                    return;
                }
            } catch (IOException ioexception) {
                Civs.logger.severe("Unable to create " + region.getId() + ".yml");
                return;
            }
        }
        saveRegionToFile(region, regionFile);
    }

    private static void saveRegionToFile(Region region, File regionFile) {
        FileConfiguration regionConfig = new YamlConfiguration();
        try {
            regionConfig.set("location", region.getId());
            regionConfig.set("xn-radius", region.getRadiusXN());
            regionConfig.set("xp-radius", region.getRadiusXP());
            regionConfig.set("yn-radius", region.getRadiusYN());
            regionConfig.set("yp-radius", region.getRadiusYP());
            regionConfig.set("zn-radius", region.getRadiusZN());
            regionConfig.set("zp-radius", region.getRadiusZP());
            if (region.getForSale() != -1) {
                regionConfig.set("sale", region.getForSale());
            } else {
                regionConfig.set("sale", null);
            }

            for (UUID uuid : region.getRawPeople().keySet()) {
                regionConfig.set("people." + uuid, region.getPeople().get(uuid));
            }
            region.cleanUpkeepHistory();
            for (Long time : region.getUpkeepHistory().keySet()) {
                regionConfig.set("upkeep-history." + time, region.getUpkeepHistory().get(time));
            }
            regionConfig.set("type", region.getType());
            regionConfig.set("exp", region.getExp());
            regionConfig.set("warehouse-enabled", region.isWarehouseEnabled());
            if (region.getLastActive() > 0) {
                regionConfig.set(ActiveEffect.LAST_ACTIVE_KEY, region.getLastActive());
            } else {
                regionConfig.set(ActiveEffect.LAST_ACTIVE_KEY, null);
            }
            regionConfig.save(regionFile);
        } catch (Exception e) {
            Civs.logger.severe("Unable to write to " + region.getId() + ".yml");
        }
    }

    @SuppressWarnings("unchecked")
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
            Location location = Region.idToLocation(Objects.requireNonNull(regionConfig.getString("location")));
            if (location == null) {
                throw new NullPointerException();
            }

            double exp = regionConfig.getDouble("exp");
            HashMap<UUID, String> people = new HashMap<>();
            for (String s : Objects.requireNonNull(regionConfig.getConfigurationSection("people")).getKeys(false)) {
                people.put(UUID.fromString(s), regionConfig.getString("people." + s));
            }
            RegionType regionType = (RegionType) ItemManager.getInstance()
                    .getItemType(Objects.requireNonNull(regionConfig.getString("type")).toLowerCase());
            region = new Region(
                    Objects.requireNonNull(regionConfig.getString("type")).toLowerCase(),
                    people,
                    location,
                    radii,
                    (HashMap<String, String>) regionType.getEffects().clone(),
                    exp);
            region.setWarehouseEnabled(regionConfig.getBoolean("warehouse-enabled", true));
            double forSale = regionConfig.getDouble("sale", -1);
            if (forSale != -1) {
                region.setForSale(forSale);
            }
            long lastActive = regionConfig.getLong(ActiveEffect.LAST_ACTIVE_KEY, -1);
            if (lastActive > -1) {
                region.setLastActive(lastActive);
            }
            if (regionConfig.isSet("upkeep-history")) {
                for (String timeString : Objects.requireNonNull(regionConfig
                        .getConfigurationSection("upkeep-history")).getKeys(false)) {
                    Long time = Long.parseLong(timeString);
                    region.getUpkeepHistory().put(time, regionConfig.getInt("upkeep-history." + timeString));
                }
            }
        } catch (Exception e) {
            Civs.logger.log(Level.SEVERE, "Unable to read " + regionFile.getName(), e);
            return null;
        }
        return region;
    }

    public Region getRegionAt(Location location) {
        if (location == null) {
            return null;
        }
        String id = Region.locationToString(location);
        if (regionLocations.get(id) != null) {
            return regionLocations.get(id);
        }
        if (location.getWorld() == null) {
            return null;
        }
        UUID worldUuid = location.getWorld().getUID();
        if (regions.get(worldUuid) == null || regions.get(worldUuid).isEmpty()) {
            return null;
        }

        double maxdex = regions.get(worldUuid).size() - 1d;
        return treeSort(maxdex, worldUuid, location);
    }

    private Region treeSort(double maxdex, UUID worldUuid, Location location) {
        int index;
        double mindex = 0;
        double prevIndex = -5;
        double prevDiff = 999999;
        boolean roundUp = false;
        for (;;) {
            if (roundUp) {
                index = (int) Math.ceil(((maxdex - mindex) / 2) + mindex);
            } else {
                index = (int) Math.floor(((maxdex - mindex) / 2) + mindex);
            }
            Region r = regions.get(worldUuid).get(index);

            if (withinRegion(r, location)) {
                return r;
            } else if (location.getX() < r.getLocation().getX() - r.getRadiusXN()) {
                maxdex = index;
                roundUp = false;
            } else if (location.getX() > r.getLocation().getX() + r.getRadiusXN()) {
                mindex = index;
                roundUp = true;
            } else {
                return findRegion((int) mindex, (int) maxdex, location, index);
            }
            if (prevIndex == index || prevDiff == Math.abs(maxdex - mindex)) {
                return null;
            }
            prevDiff = Math.abs(maxdex - mindex);
            prevIndex = index;
        }
    }

    private Region findRegion(int index1, int index2, Location location, int index) {
        if (location.getWorld() == null) {
            return null;
        }
        UUID worldUuid = location.getWorld().getUID();
        for (int i=index1; i<=index2; i++) {
            if (i==index) {
                continue;
            }
            if (withinRegion(regions.get(worldUuid).get(i), location)) {
                return regions.get(worldUuid).get(i);
            }
        }
        return null;
    }

    private boolean withinRegion(Region region, Location location) {
        Location rLocation = region.getLocation();
        if (rLocation.equals(location)) {
            return true;
        }
        if (!Objects.equals(rLocation.getWorld(), location.getWorld())) {
            return false;
        }
        return rLocation.getX() - 0.5 - region.getRadiusXN() <= location.getX() &&
               rLocation.getX() + 0.5 + region.getRadiusXP() >= location.getX() &&
               rLocation.getY() - 0.5 - region.getRadiusYN() <= location.getY() &&
               rLocation.getY() + 0.5 + region.getRadiusYP() >= location.getY() &&
               rLocation.getZ() - 0.5 - region.getRadiusZN() <= location.getZ() &&
               rLocation.getZ() + 0.5 + region.getRadiusZP() >= location.getZ();
    }

    void detectNewRegion(BlockPlaceEvent event) {
        LocaleManager localeManager = LocaleManager.getInstance();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        Location location = Region.idToLocation(Region.blockLocationToString(block.getLocation()));
        if (location == null) {
            event.setCancelled(true);
            return;
        }
        String regionTypeName = ChatColor.stripColor(event.getItemInHand().getItemMeta().getLore().get(1));
        regionTypeName = regionTypeName.replace(ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "");

        RegionType regionType;
        try {
            regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName.toLowerCase());
        } catch (Exception e) {
            Civs.logger.severe("Unable to find region type " + regionTypeName.toLowerCase());
            event.setCancelled(true);
            return;
        }
        if (regionType == null) {
            Civs.logger.severe("Unable to find region type " + regionTypeName.toLowerCase());
            event.setCancelled(true);
            return;
        }
        if (regionType == null) {
            Civs.logger.log(Level.SEVERE, "Unable to find region type {0}", regionTypeName.toLowerCase());
            event.setCancelled(true);
            return;
        }

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String localizedRegionName = LocaleManager.getInstance().getTranslation(civilian.getLocale(), regionType.getProcessedName() + LocaleConstants.NAME_SUFFIX);

        if (!regionType.getWorlds().isEmpty() &&
                !regionType.getWorlds().contains(location.getWorld().getName())) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "region-not-allowed-in-world")
                            .replace("$1", localizedRegionName));
            return;
        }

        if (!regionType.getBiomes().isEmpty() &&
                !regionType.getBiomes().contains(location.getBlock().getBiome())) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "region-in-biome")
                            .replace("$1", localizedRegionName).replace("$2", location.getBlock().getBiome().name()));
            return;
        }

        Region rebuildRegion = getRegionAt(location);
        boolean hasType = isRebuildRegionHasType(regionType, rebuildRegion);

        boolean rebuildTransition = false;
        boolean isPlot = false;
        if (rebuildRegion != null) {
            RegionType rebuildType = (RegionType) ItemManager.getInstance().getItemType(rebuildRegion.getType());
            isPlot = rebuildType.getEffects().containsKey("plot") &&
                    rebuildType.getBuildRadius() <= regionType.getBuildRadius() &&
                    rebuildRegion.getRawPeople().containsKey(civilian.getUuid());
        }
        if ((!isPlot && rebuildRegion != null && regionType.getRebuild().isEmpty()) ||
                (!isPlot && rebuildRegion != null && !hasType)) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), LocaleConstants.CANT_BUILD_ON_REGION)
                            .replace("$1", localizedRegionName).replace("$2", rebuildRegion.getType()));
            return;
        } else if (rebuildRegion == null && !regionType.getRebuild().isEmpty() && regionType.isRebuildRequired()) {
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "rebuild-required")
                            .replace("$1", localizedRegionName).replace("$2", regionType.getRebuild().get(0)));
            return;
        } else if (rebuildRegion != null) {
            location = rebuildRegion.getLocation();
            rebuildTransition = true;
        }


        Town town = TownManager.getInstance().getTownAt(location);
        if (town != null) {
            Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            if (government.getGovernmentType() == GovernmentType.FEUDALISM) {
                boolean isOwner = town.getRawPeople().containsKey(player.getUniqueId()) &&
                        town.getRawPeople().get(player.getUniqueId()).contains(Constants.OWNER);
                if (!isOwner) {
                    player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                            .getTranslation(civilian.getLocale(), "cant-build-feudal"));
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (regionType.getTowns() != null && !regionType.getTowns().isEmpty()) {
            if (town == null || !regionType.getTowns().contains(town.getType())) {
                int lowestLevel = 999;
                String lowestLevelString = null;
                for (String inTownName : regionType.getTowns()) {
                    TownType townType = (TownType) ItemManager.getInstance().getItemType(inTownName);
                    if (townType.getLevel() < lowestLevel) {
                        lowestLevelString = inTownName;
                    }
                }
                if (lowestLevelString == null) {
                    lowestLevelString = "towns";
                }
                player.sendMessage(Civs.getPrefix() +
                        localeManager.getTranslation(civilian.getLocale(), "req-build-inside-town")
                                .replace("$1", localizedRegionName).replace("$2", lowestLevelString));
                event.setCancelled(true);
                return;
            }
        }
        if (town != null) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            String townLocalizedName = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    townType.getProcessedName() + LocaleConstants.NAME_SUFFIX);
            int limit = -1;
            if (townType.getRegionLimit(regionTypeName) > -1) {
                limit = townType.getRegionLimit(regionTypeName);
                if (limit < 1) {
                    player.sendMessage(Civs.getPrefix() +
                            localeManager.getTranslation(civilian.getLocale(), LocaleConstants.REGION_LIMIT_REACHED)
                                    .replace("$1", townLocalizedName)
                                    .replace("$2", limit + "")
                                    .replace("$3", localizedRegionName));
                    event.setCancelled(true);
                    return;
                }
            }
            HashMap<String, Integer> groupLimits = new HashMap<>();
            for (String group : regionType.getGroups()) {
                if (townType.getRegionLimit(group) > -1) {
                    groupLimits.put(group, townType.getRegionLimit(group));
                    if (townType.getRegionLimit(group) < 1) {
                        player.sendMessage(Civs.getPrefix() +
                                localeManager.getTranslation(civilian.getLocale(), LocaleConstants.REGION_LIMIT_REACHED)
                                        .replace("$1", townLocalizedName)
                                        .replace("$2", townType.getRegionLimit(group) + "")
                                        .replace("$3", group));
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            int count = 0;
            for (Region region : TownManager.getInstance().getContainingRegions(town.getName())) {
                if (limit > -1 && region.getType().equals(regionTypeName)) {
                    count++;
                    if (count >= limit) {
                        player.sendMessage(Civs.getPrefix() +
                                localeManager.getTranslation(civilian.getLocale(), LocaleConstants.REGION_LIMIT_REACHED)
                                        .replace("$1", townLocalizedName)
                                        .replace("$2", limit + "")
                                        .replace("$3", localizedRegionName));
                        event.setCancelled(true);
                        return;
                    }
                }
                RegionType regionType1 = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                for (String groupType : regionType1.getGroups()) {
                    if (groupLimits.containsKey(groupType)) {
                        if (groupLimits.get(groupType) < 2) {
                            boolean rebuildWithinSameGroup = false;
                            if (rebuildRegion != null) {
                                RegionType rebuildType = (RegionType) ItemManager.getInstance().getItemType(rebuildRegion.getType());
                                if (!rebuildType.getGroups().contains(groupType)) {
                                    rebuildWithinSameGroup = true;
                                }
                            }
                            if (!rebuildWithinSameGroup) {
                                player.sendMessage(Civs.getPrefix() +
                                        localeManager.getTranslation(civilian.getLocale(), LocaleConstants.REGION_LIMIT_REACHED)
                                                .replace("$1", townLocalizedName)
                                                .replace("$2", townType.getRegionLimit(groupType) + "")
                                                .replace("$3", groupType));
                                event.setCancelled(true);
                                return;
                            }
                        } else {
                            groupLimits.put(groupType, groupLimits.get(groupType) - 1);
                        }
                    }
                }
            }
            if (regionType.getEffects().containsKey(Constants.EXCLUSIVE)) {
                HashSet<String> exclusiveSet = new HashSet<>(
                        Arrays.asList(regionType.getEffects().get(Constants.EXCLUSIVE).split("\\.")));
                for (Region region : TownManager.getInstance().getContainingRegions(town.getName())) {
                    if (exclusiveSet.contains(region.getType().toLowerCase())) {
                        RegionType currentRegionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                        String currentRegionLocalizedName = LocaleManager.getInstance()
                                .getTranslation(civilian.getLocale(), currentRegionType.getProcessedName() + LocaleConstants.NAME_SUFFIX);
                        player.sendMessage(Civs.getPrefix() +
                                localeManager.getTranslation(civilian.getLocale(), Constants.EXCLUSIVE)
                                        .replace("$1", localizedRegionName).replace("$2", currentRegionLocalizedName));
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        for (String effect : regionType.getEffects().keySet()) {
            if (createRegionListeners.get(effect) != null &&
                    !createRegionListeners.get(effect).createRegionHandler(block, player, regionType)) {
                event.setCancelled(true);
                return;
            }
        }

        RegionPoints radii = Region.hasRequiredBlocksOnCenter(regionType, location);
        if (!radii.isValid()) {
            radii = Region.hasRequiredBlocks(regionType.getName().toLowerCase(), location, false);
            if (!radii.isValid()) {
                event.setCancelled(true);
                player.sendMessage(Civs.getPrefix() +
                        localeManager.getTranslation(civilian.getLocale(), "no-required-blocks")
                                .replace("$1", localizedRegionName));
                List<HashMap<Material, Integer>> missingBlocks = Region.hasRequiredBlocks(regionType.getName().toLowerCase(), location, null);
                if (missingBlocks != null) {
                    List<List<CVItem>> missingList = new ArrayList<>();
                    for (HashMap<Material, Integer> missingMap : missingBlocks) {
                        List<CVItem> tempList = new ArrayList<>();
                        for (Map.Entry<Material, Integer> entry : missingMap.entrySet()) {
                            tempList.add(new CVItem(entry.getKey(), entry.getValue()));
                        }
                        missingList.add(tempList);
                    }
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("items", missingList);
                    data.put("page", 0);
                    data.put("maxPage", 1);
                    data.put("regionType", regionType.getProcessedName());
                    MenuManager.getInstance().openMenuFromHistory(player, "recipe", data);
                }
                return;
            }
        }

        for (Chunk chunk : NationManager.getInstance().getContainingChunks(location,
                radii.getRadiusXP(), radii.getRadiusXN(), radii.getRadiusZP(), radii.getRadiusZN())) {
            ChunkClaim chunkClaim = ChunkClaim.fromChunk(chunk);
            if (chunkClaim != null &&
                    !NationManager.getInstance().isInNation(civilian.getUuid(), chunkClaim.getNation())) {

                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), "cant-build-in-nation"
                ).replace("$1", chunkClaim.getNation().getName()));
                event.setCancelled(true);
                return;
            }
        }

        for (Region currentRegion : regionManager.getRegionsXYZ(location, radii, false)) {
            if (currentRegion == rebuildRegion) {
                continue;
            }
            event.setCancelled(true);
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(civilian.getLocale(), "too-close-region")
                            .replace("$1", localizedRegionName).replace("$2", currentRegion.getType()));
            return;
        }
        Map<UUID, String> people;
        if (rebuildRegion != null) {
            people = rebuildRegion.getPeople();
            if (Civs.econ != null && people.containsKey(player.getUniqueId()) &&
                    !people.get(player.getUniqueId()).contains("ally") &&
                    !regionType.isRebuildRequired()) {
                RegionType rebuildRegionType = (RegionType) ItemManager.getInstance().getItemType(rebuildRegion.getType());
                Civs.econ.depositPlayer(player, rebuildRegionType.getPrice() / 2);
            }
            //TODO copy over other stuff too?
            removeRegion(rebuildRegion, false, false);
        } else {
            people = new HashMap<>();
            people.put(player.getUniqueId(), Constants.OWNER);
        }
        if (rebuildTransition) {
            event.setCancelled(true);
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getAmount() > 1) {
                itemStack.setAmount(itemStack.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            location.getBlock().setType(itemStack.getType());
        }

        player.sendMessage(Civs.getPrefix() +
                localeManager.getTranslation(civilian.getLocale(), "region-built")
                        .replace("$1", localizedRegionName));

        TutorialManager.getInstance().completeStep(civilian, TutorialManager.TutorialType.BUILD, regionTypeName);

        Region region = new Region(regionType.getName(), people, location, radii, regionType.getEffects(), 0);
        addRegion(region);
        StructureUtil.removeBoundingBox(civilian.getUuid());
        RegionCreatedEvent regionCreatedEvent = new RegionCreatedEvent(region, regionType, player);
        Bukkit.getPluginManager().callEvent(regionCreatedEvent);
    }

    private boolean isRebuildRegionHasType(RegionType regionType, Region rebuildRegion) {
        boolean hasType = false;
        if (rebuildRegion != null) {
            outer: for (String rebuild : regionType.getRebuild()) {
                hasType = rebuildRegion.getType().equalsIgnoreCase(rebuild);
                if (!hasType) {
                    List<CivItem> itemList = ItemManager.getInstance().getItemGroup(rebuild);
                    for (CivItem item : itemList) {
                        if (item.getProcessedName().equalsIgnoreCase(rebuildRegion.getType())) {
                            hasType = true;
                            break outer;
                        }
                        for (String group : item.getGroups()) {
                            if (group.equalsIgnoreCase(rebuildRegion.getType())) {
                                hasType = true;
                                break outer;
                            }
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return hasType;
    }

    void adjustRadii(RegionPoints radii, Location location, double x, double y, double z) {
        int currentRelativeX = (int) Math.round(x - location.getX());
        int currentRelativeY = (int) Math.round(y - location.getY());
        int currentRelativeZ = (int) Math.round(z - location.getZ());
        if (currentRelativeX < 0) {
            currentRelativeX = Math.abs(currentRelativeX);
            radii.setRadiusXN(Math.max(radii.getRadiusXN(), currentRelativeX));
        } else if (currentRelativeX > 0) {
            radii.setRadiusXP(Math.max(radii.getRadiusXP(), currentRelativeX));
        }
        if (currentRelativeY < 0) {
            currentRelativeY = Math.abs(currentRelativeY);
            radii.setRadiusYN(Math.max(radii.getRadiusYN(), currentRelativeY));
        } else if (currentRelativeY > 0) {
            radii.setRadiusYP(Math.max(radii.getRadiusYP(), currentRelativeY));
        }
        if (currentRelativeZ < 0) {
            currentRelativeZ = Math.abs(currentRelativeZ);
            radii.setRadiusZN(Math.max(radii.getRadiusZN(), currentRelativeZ));
        } else if (currentRelativeZ > 0) {
            radii.setRadiusZP(Math.max(radii.getRadiusZP(), currentRelativeZ));
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
        RegionPoints regionPoints = new RegionPoints(modifierX, modifierX,
                modifierY, modifierY,
                modifierZ, modifierZ);
        return getRegionsXYZ(location, regionPoints, useEffects);
    }

    public Set<Region> getRegionsXYZ(Location location, RegionPoints regionPoints, boolean useEffects) {
        UUID worldUuid = location.getWorld().getUID();
        HashSet<Region> returnRegions = new HashSet<>();
        if (this.regions.get(worldUuid) == null) {
            return returnRegions;
        }
        for (int i = this.regions.get(worldUuid).size() - 1; i>-1; i--) {
            Region region = this.regions.get(worldUuid).get(i);
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (!useEffects) {
                boolean withinX = location.getX() > region.getLocation().getX() - region.getRadiusXN() - regionPoints.getRadiusXP() &&
                        location.getX() < region.getLocation().getX() + region.getRadiusXP() + regionPoints.getRadiusXN();
                boolean withinY = location.getY() > region.getLocation().getY() - region.getRadiusYN() - regionPoints.getRadiusYP() &&
                        location.getY() < region.getLocation().getY() + region.getRadiusYP() + regionPoints.getRadiusYN();
                boolean withinZ = location.getZ() > region.getLocation().getZ() - region.getRadiusZN() - regionPoints.getRadiusZP() &&
                        location.getZ() < region.getLocation().getZ() + region.getRadiusZP() + regionPoints.getRadiusZN();

                if (withinX && withinY && withinZ) {
                    returnRegions.add(region);
                    continue;
                }
                if (location.getX() > region.getLocation().getX() - region.getRadiusXN() + regionPoints.getRadiusXN()) {
                    break;
                }
            } else {
                addRegionWithinBounds(location, regionPoints, returnRegions, region, regionType);
            }
        }
        return returnRegions;
    }

    private void addRegionWithinBounds(Location location, RegionPoints regionPoints, HashSet<Region> returnRegions, Region region, RegionType regionType) {

        boolean withinX = location.getX() > region.getLocation().getX() - regionType.getEffectRadius() - regionPoints.getRadiusXP() &&
                location.getX() < region.getLocation().getX() + regionType.getEffectRadius() + regionPoints.getRadiusXN();
        boolean withinY = location.getY() > region.getLocation().getY() - regionType.getEffectRadius() - regionPoints.getRadiusYP() &&
                location.getY() < region.getLocation().getY() + regionType.getEffectRadius() + regionPoints.getRadiusYN();
        boolean withinZ = location.getZ() > region.getLocation().getZ() - regionType.getEffectRadius() - regionPoints.getRadiusZP() &&
                location.getZ() < region.getLocation().getZ() + regionType.getEffectRadius() + regionPoints.getRadiusZN();

        if (withinX && withinY && withinZ) {
            returnRegions.add(region);
        }
    }

    public static synchronized RegionManager getInstance() {
        if (regionManager == null) {
            regionManager = new RegionManager();
            if (Civs.getInstance() != null) {
                regionManager.loadAllRegions();
            }
        }
        return regionManager;
    }

    public boolean hasRegionChestChanged(Region region) {
        return !checkedRegions.contains(region);
    }

    public void removeCheckedRegion(Location location) {
        Region region = RegionManager.getInstance().getRegionAt(location);
        if (region != null) {
            removeCheckedRegion(region);
            if (region.getEffects().containsKey(WarehouseEffect.KEY)) {
                WarehouseEffect.getInstance().refreshChest(region, location);
            }
        }
    }
    public void removeCheckedRegion(Region region) {
        checkedRegions.remove(region);
    }

    public void addCheckedRegion(Region region) {
        checkedRegions.add(region);
    }
}
