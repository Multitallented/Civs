package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.PlayerEnterRegionEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerEnterTownEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerExitRegionEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerExitTownEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public class PermissionEffect implements Listener {
    private static final String KEY = "permission";
    private static final Map<UUID, HashSet<String>> permissionMap = new HashMap<>();

    @EventHandler
    public void onPlayerEnterRegion(PlayerEnterRegionEvent event) {
        if (Civs.perm == null) {
            return;
        }
        Region region = event.getRegion();
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        String permission = region.getEffects().get(KEY);
        addPermission(event.getUuid(), permission);
    }

    @EventHandler
    public void onPlayerExitRegion(PlayerExitRegionEvent event) {
        if (Civs.perm == null) {
            return;
        }
        Region region = event.getRegion();
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        String permission = region.getEffects().get(KEY);
        removePermission(event.getUuid(), permission);
    }

    @EventHandler
    public void onPlayerEnterTown(PlayerEnterTownEvent event) {
        if (Civs.perm == null) {
            return;
        }
        TownType townType = event.getTownType();
        if (!townType.getEffects().containsKey(KEY)) {
            return;
        }
        String permission = townType.getEffects().get(KEY);
        addPermission(event.getUuid(), permission);
    }

    @EventHandler
    public void onPlayerExitTown(PlayerExitTownEvent event) {
        if (Civs.perm == null) {
            return;
        }
        TownType townType = event.getTownType();
        if (!townType.getEffects().containsKey(KEY)) {
            return;
        }
        String permission = townType.getEffects().get(KEY);
        removePermission(event.getUuid(), permission);
    }

    private void addPermission(UUID uuid, String permission) {
        HashSet<String> permissions = permissionMap.get(uuid);
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        permissions.add(permission);
        permissionMap.put(uuid, permissions);
    }

    private void removePermission(UUID uuid, String permission) {
        HashSet<String> permissions = permissionMap.get(uuid);
        if (permissions == null) {
            return;
        }
        if (permissions.size() < 2) {
            permissionMap.remove(uuid);
            return;
        }
        permissionMap.get(uuid).remove(permission);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Civs.perm == null) {
            return;
        }
        permissionMap.remove(event.getPlayer().getUniqueId());
    }
}
