package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class RegionListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        RegionManager regionManager = RegionManager.getInstance();
        if (blockPlaceEvent.isCancelled() ||
                regionManager.getRegionAt(blockPlaceEvent.getBlockPlaced().getLocation()) != null) {
            return;
        }
        //TODO protect if the should not be placed

        regionManager.detectNewRegion(blockPlaceEvent);
    }
}
