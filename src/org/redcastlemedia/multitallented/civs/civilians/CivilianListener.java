package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.redcastlemedia.multitallented.civs.ConfigManager;

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
        if (!ConfigManager.getInstance().getAllowCivItemDropping() &&
                item.getItemStack().getItemMeta() != null &&
                item.getItemStack().getItemMeta().getDisplayName().contains("Civs ")) {
            item.remove();
        }
    }
}
