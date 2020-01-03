package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;

public final class PermissionUtil {
    private PermissionUtil() {

    }
    public static void applyPermission(OfflinePlayer offlinePlayer, String permission) {
        boolean remove = false;
        boolean transientPerm = false;
        String finalPermission = permission;
        for (;;) {
            if (finalPermission.startsWith("!")) {
                remove = true;
                finalPermission = finalPermission.substring(1);
            } else if (finalPermission.startsWith("^")) {
                transientPerm = true;
                finalPermission = finalPermission.substring(1);
            } else {
                break;
            }
        }

        if (offlinePlayer.isOnline()) {
            Player player = (Player) offlinePlayer;
            if (transientPerm) {
                if (remove) {
                    Civs.perm.playerRemove(player, finalPermission);
                } else {
                    Civs.perm.playerAddTransient(player, finalPermission);
                }
            } else {
                if (remove) {
                    Civs.perm.playerRemove(player, finalPermission);
                } else {
                    Civs.perm.playerAdd(player, finalPermission);
                }
            }
        } else {
            Player player1 = offlinePlayer.getPlayer();
            if (player1 != null && player1.getLocation().getWorld() != null) {
                String worldName = player1.getLocation().getWorld().getName();
                if (remove) {
                    Civs.perm.playerRemove(worldName, offlinePlayer, finalPermission);
                } else {
                    Civs.perm.playerAdd(worldName, offlinePlayer, finalPermission);
                }
            }
        }
    }
}
