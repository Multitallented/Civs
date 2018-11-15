package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.RegionTypeInfoMenu;
import org.redcastlemedia.multitallented.civs.util.CVItem;

public class RegionListener implements Listener {

    /**
     * If placing a region block, try to create a region
     * @param blockPlaceEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        RegionManager regionManager = RegionManager.getInstance();
        if (blockPlaceEvent.getBlockPlaced().getState() == null) {
            return;
        }

        if (ConfigManager.getInstance().getBlackListWorlds().contains(blockPlaceEvent.getBlockPlaced().getLocation().getWorld().getName())) {
            return;
        }

        if (!blockPlaceEvent.getItemInHand().hasItemMeta()) {
            return;
        }
        ItemStack heldItem = blockPlaceEvent.getItemInHand();

        if (CVItem.isCivsItem(heldItem)) {
            regionManager.detectNewRegion(blockPlaceEvent);
        }
    }

    /**
     * Open region info menu if right clicking air with region
     * @param event
     */
    @EventHandler
    public void onRegionInfo(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (event.getAction() != Action.RIGHT_CLICK_AIR || !CVItem.isCivsItem(heldItem)) {
            return;
        }
        CivItem civItem = ItemManager.getInstance().getItemType(heldItem.getItemMeta().getDisplayName());
        if (civItem.getItemType() != CivItem.ItemType.REGION) {
            return;
        }
        RegionType regionType = (RegionType) civItem;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        player.openInventory(RegionTypeInfoMenu.createMenu(civilian, regionType));
    }
}
