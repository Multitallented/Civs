package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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

        if (!blockPlaceEvent.getItemInHand().hasItemMeta()) {
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
        Player player = blockBreakEvent.getPlayer();
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if ((!region.getPeople().containsKey(player.getUniqueId()) &&
                Region.hasRequiredBlocks(region.getType(), region.getLocation()).length == 0) ||
                region.getLocation().equals(blockBreakEvent.getBlock().getLocation())) {
            regionManager.removeRegion(region, true);
            return;
        }
        List<HashSet<CVItem>> missingBlocks = Region.hasRequiredBlocks(region.getType(),
                region.getLocation(),
                blockBreakEvent.getBlock().getState().getData().toItemStack());
        if (region.getPeople().containsKey(player.getUniqueId()) &&
                 missingBlocks != null) {
            blockBreakEvent.setCancelled(true);
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "break-own-region").replace("$1", region.getType()));
            StringBuilder missingReqs = new StringBuilder();
            for (HashSet<CVItem> map : missingBlocks) {
                for (CVItem key : map) {
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
