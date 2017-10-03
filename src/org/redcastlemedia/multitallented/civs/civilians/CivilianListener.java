package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

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
    }
    @EventHandler
    public void onCivilianDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Item item = event.getItemDrop();
        if (!ConfigManager.getInstance().getAllowSharingCivsItems() &&
                item.getItemStack().getItemMeta() != null &&
                item.getItemStack().getItemMeta().getDisplayName().contains("Civs ")) {
            item.remove();
        }
    }
    @EventHandler
    public void onCivilianClickItem(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack clickedStack = event.getCursor();
        String uuidString;
        try {
            uuidString = clickedStack.getItemMeta().getLore().get(0);
        } catch (Exception e) {
            Civs.logger.warning(Civs.getPrefix() + "Unable to find Civs Item UUID");
            return;
        }
        if (!ConfigManager.getInstance().getAllowSharingCivsItems() &&
                CVItem.isCivsItem(clickedStack) &&
                !humanEntity.getUniqueId().toString().equals(uuidString)) {
            event.setCancelled(true);
        }
    }
}
