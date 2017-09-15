package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

public class ProtectionHandler implements Listener {


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Region region = RegionManager.getInstance().getRegionAt(event.getBlock().getLocation());
        if (region == null) {
            return;
        }
        event.setCancelled(true);
    }
}
