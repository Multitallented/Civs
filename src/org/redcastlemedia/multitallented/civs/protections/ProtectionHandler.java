package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

public class ProtectionHandler implements Listener {


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.setCancelled(checkLocation(event.getBlock(), event.getPlayer(), "block_break"));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        event.setCancelled(checkLocation(event.getBlockPlaced(), event.getPlayer(), "block_place"));
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Material mat = event.getClickedBlock().getType();
        if (mat == Material.WOODEN_DOOR ||
                mat == Material.TRAP_DOOR ||
                mat == Material.IRON_DOOR_BLOCK ||
                mat == Material.IRON_TRAPDOOR) {
            event.setCancelled(checkLocation(event.getClickedBlock(), event.getPlayer(), "door_use"));
        } else if (mat == Material.CHEST ||
                mat == Material.FURNACE ||
                mat == Material.BURNING_FURNACE ||
                mat == Material.TRAPPED_CHEST ||
                mat == Material.ENDER_CHEST ||
                mat == Material.BOOKSHELF) {
            event.setCancelled(checkLocation(event.getClickedBlock(), event.getPlayer(), "chest_use"));
        } else if (mat == Material.CROPS ||
                mat == Material.CARROT ||
                mat == Material.POTATO) {
            event.setCancelled(checkLocation(event.getClickedBlock(), event.getPlayer(), "block_break"));
        } else if (mat == Material.LEVER ||
                mat == Material.STONE_BUTTON ||
                mat == Material.WOOD_BUTTON) {
            event.setCancelled(checkLocation(event.getClickedBlock(), event.getPlayer(), "button_use"));
        }
        //TODO protect paintings
        event.setCancelled(checkLocation(event.getClickedBlock(), event.getPlayer(), "block_use"));
    }

    private boolean checkLocation(Block block, Player player, String type) {
        RegionManager regionManager = RegionManager.getInstance();
        Region region = regionManager.getRegionAt(block.getLocation());
        if (region == null) {
            return false;
        }
        RegionType regionType = regionManager.getRegionType(region.getType());
        if (!regionType.getEffects().contains(type)) {
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
