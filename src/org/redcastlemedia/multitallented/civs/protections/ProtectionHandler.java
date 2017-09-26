package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

public class ProtectionHandler implements Listener {


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.setCancelled(checkLocation(event.getBlock(), event.getPlayer()));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        event.setCancelled(checkLocation(event.getBlockPlaced(), event.getPlayer()));
    }

    private boolean checkLocation(Block block, Player player) {
        Region region = RegionManager.getInstance().getRegionAt(block.getLocation());
        if (region == null) {
            return false;
        }
        if (region.getOwners().contains(player.getUniqueId())) {
            return false;
        }
        if (region.getMembers().contains(player.getUniqueId()) &&
                region.getLocation() != block.getLocation()) {
            return false;
        }
        return true;
    }
}
