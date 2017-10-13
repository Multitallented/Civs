package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.BlockLogger;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.UUID;

public class CivilianListener implements Listener {

    @EventHandler
    public void onCivilianJoin(PlayerJoinEvent event) {
        CivilianManager civilianManager = CivilianManager.getInstance();
        civilianManager.loadCivilian(event.getPlayer());
    }

    @EventHandler
    public void onCivilianQuit(PlayerQuitEvent event) {
        CivilianManager civilianManager = CivilianManager.getInstance();
        civilianManager.unloadCivilian(event.getPlayer());
        CommonScheduler.lastRegion.remove(event.getPlayer().getUniqueId());
        CommonScheduler.lastTown.remove(event.getPlayer().getUniqueId());
        TownManager.getInstance().clearInvite(event.getPlayer().getUniqueId());
    }
    @EventHandler
    public void onCivilianDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (!ConfigManager.getInstance().getAllowSharingCivsItems() &&
                item.getItemStack().getItemMeta() != null &&
                item.getItemStack().getItemMeta().getDisplayName().contains("Civs ")) {
            item.remove();
        }
    }

    @EventHandler
    public void onCivilianBlockPlace(BlockPlaceEvent event) {
        ItemStack is = event.getBlockPlaced().getState().getData().toItemStack();
        if (!CVItem.isCivsItem(is)) {
            return;
        }
        String itemTypeName = is.getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        CivItem civItem = ItemManager.getInstance().getItemType(itemTypeName);
        if (!civItem.isPlaceable()) {
            event.setCancelled(true);
            return;
        }
    }
    @EventHandler
    public void onCivilianDispense(BlockDispenseEvent event) {
        ItemStack is = event.getItem();
        if (!CVItem.isCivsItem(is)) {
            return;
        }
        String itemTypeName = is.getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        CivItem civItem = ItemManager.getInstance().getItemType(itemTypeName);
        if (!civItem.isPlaceable()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onCivilianBlockBreak(BlockBreakEvent event) {
        ItemStack is = event.getBlock().getState().getData().toItemStack();
        if (!CVItem.isCivsItem(is)) {
            return;
        }
        BlockLogger blockLogger = BlockLogger.getInstance();
        blockLogger.removeBlock(event.getBlock().getLocation());
        if (ConfigManager.getInstance().getAllowSharingCivsItems()) {
            return;
        }
        UUID uuid = blockLogger.getBlock(event.getBlock().getLocation());
        if (uuid != null && uuid != event.getPlayer().getUniqueId()) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack is = event.getBlock().getState().getData().toItemStack();
        if (event.getPlayer() == null || !CVItem.isCivsItem(is)) {
            return;
        }
        BlockLogger blockLogger = BlockLogger.getInstance();
        blockLogger.putBlock(event.getBlock().getLocation(), event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onCivilianClickItem(InventoryClickEvent event) {
        if (!CVItem.isCivsItem(event.getCurrentItem())) {
            return;
        }
        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack clickedStack = event.getCurrentItem();
        String uuidString;
        try {
            uuidString = clickedStack.getItemMeta().getLore().get(0);
        } catch (Exception e) {
            Civs.logger.warning("Unable to find Civs Item UUID");
            return;
        }
        if (!ConfigManager.getInstance().getAllowSharingCivsItems() &&
                CVItem.isCivsItem(clickedStack) &&
                !humanEntity.getUniqueId().toString().equals(uuidString)) {
            event.setCancelled(true);
            Civilian civilian = CivilianManager.getInstance().getCivilian(humanEntity.getUniqueId());
            humanEntity.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "prevent-civs-item-share"));
            return;
        }
    }
}
