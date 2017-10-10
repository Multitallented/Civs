package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.redcastlemedia.multitallented.civs.ConfigManager;

public class RegionListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        RegionManager regionManager = RegionManager.getInstance();
        if (blockPlaceEvent.getBlockPlaced().getState() == null) {
            return;
        }

        if (ConfigManager.getInstance().getBlackListWorlds().contains(blockPlaceEvent.getBlockPlaced().getLocation().getWorld().getName())) {
            return;
        }

        String displayName = blockPlaceEvent.getItemInHand().getItemMeta().getDisplayName();

        if (displayName != null && displayName.contains("Civs ")) {
            regionManager.detectNewRegion(blockPlaceEvent);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        RegionManager regionManager = RegionManager.getInstance();
        if (ConfigManager.getInstance().getBlackListWorlds().contains(blockBreakEvent.getBlock().getLocation().getWorld().getName())) {
            return;
        }
        Region region = regionManager.getRegionAt(blockBreakEvent.getBlock().getLocation());
        if (region == null) { //TODO check for towns
            return;
        }
        if ((!region.getPeople().containsKey(blockBreakEvent.getPlayer().getUniqueId()) &&
                Region.hasRequiredBlocks(region.getType(), region.getLocation()).length == 0) ||
                region.getLocation().equals(blockBreakEvent.getBlock().getLocation())) {
            regionManager.removeRegion(region, true);
        }
    }
}
