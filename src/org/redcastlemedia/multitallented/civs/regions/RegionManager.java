package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RegionManager {
    private ArrayList<Region> regions = new ArrayList<>();
    private HashMap<String, RegionType> regionTypes = new HashMap<>();
    private static RegionManager regionManager;
    private HashMap<Player, HashSet<Block>> cachedBlocks = new HashMap<>();
    private HashSet<Material> blockCheck = new HashSet<>();

    public RegionManager() {
        regionManager = this;
    }

    public void addRegion(Region region) {
        regions.add(region);
    }

    public Region getRegionAt(Location location) {
        Region region = null;
        for (Region r : regions) {
            region = r;
        }
        return region;
    }

    public void loadRegionType(FileConfiguration config) {
        regionTypes.put(config.getString("name").toLowerCase(), new RegionType());
        for (String req : config.getStringList("requirements")) {
            CVItem cvItem = CVItem.createCVItemFromString(req);
        }
    }

    public RegionType getRegionType(String name) {
        return regionTypes.get(name);
    }

    void detectNewRegion(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        HashSet<Block> blockHashSet;
        if (cachedBlocks.containsKey(player)) {
            blockHashSet = cachedBlocks.get(player);
        } else {
            blockHashSet = new HashSet<>();
        }
        boolean shouldScanArea = blockHashSet.isEmpty();
        blockHashSet.add(block);

        if (shouldScanArea) {
            scanArea(blockHashSet);
        }

        addRegion(new Region("cobble"));
    }

    void scanArea(HashSet<Block> blockHashSet) {
        //TODO check all region types and keep expanding until found all possible regions
    }

    public static synchronized RegionManager getInstance() {
        if (regionManager == null) {
            regionManager = new RegionManager();
        }
        return regionManager;
    }
}
