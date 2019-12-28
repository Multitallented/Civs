package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class CommandUtil {
    private CommandUtil() {

    }
    public static void performCommand(OfflinePlayer offlinePlayer, String command) {
        String finalCommand = command;
        boolean runAsOp = false;
        boolean runFromConsole = false;
        for (;;) {
            if (finalCommand.startsWith("^")) {
                runAsOp = true;
                finalCommand = finalCommand.substring(1);
            } else if (finalCommand.startsWith("!")) {
                runFromConsole = true;
                finalCommand = finalCommand.substring(1);
            } else {
                break;
            }
        }
        if (offlinePlayer.isOnline()) {
            finalCommand = finalCommand.replace("$name$", ((Player) offlinePlayer).getName());
        } else {
            Player player1 = offlinePlayer.getPlayer();
            if (player1 != null && player1.isValid()) {
                finalCommand = finalCommand.replace("$name$", player1.getName());
            }
        }
        if (runFromConsole) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        } else if (offlinePlayer.isOnline()) {
            Player player = (Player) offlinePlayer;
            boolean setOp = runAsOp && !player.isOp();
            if (setOp) {
                player.setOp(true);
            }
            player.performCommand(finalCommand);
            if (setOp) {
                player.setOp(false);
            }
        }
    }
}
