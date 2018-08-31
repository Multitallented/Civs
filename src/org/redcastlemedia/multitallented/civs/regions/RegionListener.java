package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.RegionTypeInfoMenu;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RegionListener implements Listener {

    /**
     * If placing a region block, try to create a region
     * @param blockPlaceEvent
     */
    @EventHandler
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

    /**
     * If breaking a block inside a region
     * Check to see if the region is destroyed
     * Prevent non-owners from destroying regions
     * @param blockBreakEvent
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
        RegionManager regionManager = RegionManager.getInstance();
        if (ConfigManager.getInstance().getBlackListWorlds().contains(blockBreakEvent.getBlock().getLocation().getWorld().getName())) {
            return;
        }
        Region region = regionManager.getRegionAt(blockBreakEvent.getBlock().getLocation());
        if (region == null) {
            return;
        }
        Player player = blockBreakEvent.getPlayer();
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if ((!region.getPeople().containsKey(player.getUniqueId()) &&
                Region.hasRequiredBlocks(region.getType(), region.getLocation()).length == 0) ||
                region.getLocation().equals(blockBreakEvent.getBlock().getLocation())) {
            regionManager.removeRegion(region, true);
            return;
        }
        List<HashMap<Material, Integer>> missingBlocks = Region.hasRequiredBlocks(region.getType(),
                region.getLocation(),
                blockBreakEvent.getBlock().getState().getData().toItemStack(1));
        if (region.getPeople().containsKey(player.getUniqueId()) &&
                 missingBlocks != null && !missingBlocks.isEmpty()) {
            blockBreakEvent.setCancelled(true);
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "broke-own-region").replace("$1", region.getType()));
            StringBuilder missingReqs = new StringBuilder();
            for (HashMap<Material, Integer> map : missingBlocks) {
                for (Material mat : map.keySet()) {
                    CVItem key = new CVItem(mat, map.get(mat));
                    missingReqs.append(key.getMat().toString());
                    missingReqs.append("*");
                    missingReqs.append(key.getQty());
                    missingReqs.append(" or ");
                }
                missingReqs.substring(missingReqs.length() - 4);
                missingReqs.append("and ");
            }
            missingReqs.substring(missingReqs.length() - 6);
            List<String> missingList = Util.textWrap(ChatColor.RED + "", missingReqs.toString());
            for (String s : missingList) {
                player.sendMessage(s);
            }
            return;
        }
    }
}
