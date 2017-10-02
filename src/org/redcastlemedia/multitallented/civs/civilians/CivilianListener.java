package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
}
