package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class RegionManager {
    private ArrayList<Region> regions = new ArrayList<>();
    private HashMap<String, RegionType> regionTypes = new HashMap<>();
    private static RegionManager regionManager;

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
    }

    public RegionType getRegionType(String name) {
        return regionTypes.get(name);
    }

    public void detectNewRegion(BlockPlaceEvent event) {
        String displayName = event.getBlockPlaced().getState().getData().toItemStack().getItemMeta().getDisplayName();
        if (!displayName.contains("Civs ")) {
            return;
        }
        displayName = displayName.replace("Civs ", "").toLowerCase();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        RegionType regionType = regionTypes.get(displayName);
        if (regionType != null) {
            addRegion(new Region(displayName));
        }
    }

    public static synchronized RegionManager getInstance() {
        if (regionManager == null) {
            regionManager = new RegionManager();
        }
        return regionManager;
    }
}
