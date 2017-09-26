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

        if (blockPlaceEvent.getBlockPlaced().getState() == null) {
            return;
        }
        String displayName = blockPlaceEvent.getBlockPlaced().getState().getData().toItemStack().getItemMeta().getDisplayName();
        if (displayName != null && displayName.contains("Civs ")) {
            regionManager.detectNewRegion(blockPlaceEvent);
        }
    }
}
