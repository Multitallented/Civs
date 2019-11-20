package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.events.PlayerEnterRegionEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerEnterTownEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerExitRegionEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerExitTownEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;

@CivsSingleton
public class PermissionEffect implements Listener {
    private static final String KEY = "permission";
    private static final Map<UUID, HashSet<String>> permissionMap = new HashMap<>();

    public static void getInstance() {
        if (Civs.getInstance() != null) {
            Bukkit.getPluginManager().registerEvents(new PermissionEffect(), Civs.getInstance());
        }
    }

    @EventHandler
    public void onPlayerEnterRegion(PlayerEnterRegionEvent event) {
        if (isInvalidRegion(event.getRegion(), event.getUuid())) {
            return;
        }
        String permission = event.getRegion().getEffects().get(KEY);
        addPermission(event.getUuid(), permission);
    }

    @EventHandler
    public void onPlayerExitRegion(PlayerExitRegionEvent event) {
        if (isInvalidRegion(event.getRegion(), event.getUuid())) {
            return;
        }

        String permission = event.getRegion().getEffects().get(KEY);
        removePermission(event.getUuid(), permission);
    }

    private boolean isInvalidRegion(Region region, UUID uuid) {
        if (Civs.perm == null) {
            return true;
        }
        if (!region.getEffects().containsKey(KEY) ||
                !region.getRawPeople().containsKey(uuid)) {
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerEnterTown(PlayerEnterTownEvent event) {
        if (isInvalidTownType(event.getTown(), event.getTownType(), event.getUuid())) {
            return;
        }
        String permission = event.getTownType().getEffects().get(KEY);
        addPermission(event.getUuid(), permission);
    }

    @EventHandler
    public void onPlayerExitTown(PlayerExitTownEvent event) {
        if (isInvalidTownType(event.getTown(), event.getTownType(), event.getUuid())) {
            return;
        }

        String permission = event.getTownType().getEffects().get(KEY);
        removePermission(event.getUuid(), permission);
    }

    private boolean isInvalidTownType(Town town, TownType townType, UUID uuid) {
        if (Civs.perm == null) {
            return true;
        }
        if (!townType.getEffects().containsKey(KEY) ||
                !town.getPeople().containsKey(uuid) ||
                town.getPeople().get(uuid).equals("ally")) {
            return true;
        }
        return false;
    }

    private void addPermission(UUID uuid, String permission) {
        HashSet<String> permissions = permissionMap.get(uuid);
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        permissions.add(permission);
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        Civs.perm.playerAdd(player, permission);
        permissionMap.put(uuid, permissions);
    }

    private void removePermission(UUID uuid, String permission) {
        HashSet<String> permissions = permissionMap.get(uuid);
        if (permissions == null) {
            return;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            permissionMap.remove(uuid);
            return;
        }
        Civs.perm.playerRemove(player, permission);
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
        Player player = event.getPlayer();
        HashSet<String> permissions = permissionMap.get(player.getUniqueId());
        if (permissions == null) {
            return;
        }
        for (String permission : permissions) {
            Civs.perm.playerRemove(player, permission);
        }
        permissionMap.remove(event.getPlayer().getUniqueId());
    }
}
