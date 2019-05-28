package org.redcastlemedia.multitallented.civs.ai;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;


public class AIListener implements Listener {

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
        if (!AIManager.getInstance().getChatHandler().containsKey(event.getPlayer())) {
            return;
        }
        AI ai = AIManager.getInstance().getChatHandler().get(event.getPlayer());
        ai.handlePlayerChat(event);
    }
}
