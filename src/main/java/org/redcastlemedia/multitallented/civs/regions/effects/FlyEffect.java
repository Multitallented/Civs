package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.events.EnterCombatEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerExitTownEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;

@CivsSingleton
public class FlyEffect implements Listener {

    private static final String KEY = "fly";
    private static final Set<UUID> flyingPlayers = new HashSet<>();

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new IntruderEffect(), Civs.getInstance());
    }

    @EventHandler
    public void playerInRegion(PlayerInRegionEvent event) {
        if (!event.getRegionType().getEffects().containsKey(KEY) ||
                !event.getRegion().getPeople().containsKey(event.getUuid())) {
            return;
        }
        Player player = Bukkit.getPlayer(event.getUuid());
        if (player != null) {
            flyingPlayers.add(event.getUuid());
            player.setFlying(true);
        }
    }

    @EventHandler
    public void onPlayerEnterCombat(EnterCombatEvent event) {
        if (flyingPlayers.contains(event.UUID)) {
            Player player = Bukkit.getPlayer(event.UUID);
            if (player != null) {
                player.setFlying(false);
            }
            flyingPlayers.remove(event.UUID);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (flyingPlayers.contains(event.getPlayer().getUniqueId())) {
            removeFlyFromPlayer(event.getPlayer());
            flyingPlayers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerLeaveTown(PlayerExitTownEvent event) {
        if (flyingPlayers.contains(event.getUuid())) {
            Player player = Bukkit.getPlayer(event.getUuid());
            removeFlyFromPlayer(player);
            flyingPlayers.remove(event.getUuid());
        }
    }

    public static void removeFlyFromAllPlayers() {
        for (UUID uuid : flyingPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            removeFlyFromPlayer(player);
        }
        flyingPlayers.clear();
    }

    private static void removeFlyFromPlayer(Player player) {
        if (player != null) {
            player.setFlying(false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 1));
        }
    }
}
