package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.PlayerEnterRegionEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerEnterTownEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerExitRegionEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerExitTownEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public class CommandEffect implements Listener {
    public static final String ENTRY_KEY = "enter_command";
    public static final String EXIT_KEY = "exit_command";

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerEnterRegion(PlayerEnterRegionEvent event) {
        if (isInvalidRegion(event.getRegion(), event.getUuid(), ENTRY_KEY)) {
            return;
        }
        String command = event.getRegion().getEffects().get(ENTRY_KEY);
        sendCommand(event.getUuid(), command);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerExitRegion(PlayerExitRegionEvent event) {
        if (isInvalidRegion(event.getRegion(), event.getUuid(), EXIT_KEY)) {
            return;
        }

        String command = event.getRegion().getEffects().get(EXIT_KEY);
        sendCommand(event.getUuid(), command);
    }

    private boolean isInvalidRegion(Region region, UUID uuid, String key) {
        if (Civs.perm == null) {
            return true;
        }
        if (!region.getEffects().containsKey(key) ||
                !region.getRawPeople().containsKey(uuid)) {
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerEnterTown(PlayerEnterTownEvent event) {
        if (isInvalidTownType(event.getTown(), event.getTownType(), event.getUuid(), ENTRY_KEY)) {
            return;
        }
        String command = event.getTownType().getEffects().get(ENTRY_KEY);
        sendCommand(event.getUuid(), command);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerExitTown(PlayerExitTownEvent event) {
        if (isInvalidTownType(event.getTown(), event.getTownType(), event.getUuid(), EXIT_KEY)) {
            return;
        }

        String command = event.getTownType().getEffects().get(EXIT_KEY);
        sendCommand(event.getUuid(), command);
    }

    private void sendCommand(UUID uuid, String command) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        player.performCommand(command);
    }

    private boolean isInvalidTownType(Town town, TownType townType, UUID uuid, String key) {
        if (Civs.perm == null) {
            return true;
        }
        if (!townType.getEffects().containsKey(key) ||
                !town.getPeople().containsKey(uuid) ||
                town.getPeople().get(uuid).equals("ally")) {
            return true;
        }
        return false;
    }
}
