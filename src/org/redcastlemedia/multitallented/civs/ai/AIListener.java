package org.redcastlemedia.multitallented.civs.ai;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class AIListener implements Listener {
    private HashMap<Player, AI> chatHandler = new HashMap<>();


    // TODO commission buildings

    // TODO invite players

    // TODO make alliances

    // TODO retaliate against war and raiders

    // TODO buy houses from players when possible

    // TODO raise and lower taxes

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            return;
        }

        for (AI ai : AIManager.getInstance().getRandomAIs()) {
            if (ai.handleJoiningPlayer(event.getPlayer())) {
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!chatHandler.containsKey(event.getPlayer())) {
            return;
        }

    }
}
