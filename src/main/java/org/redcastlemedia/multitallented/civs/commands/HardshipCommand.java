package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsCommand(keys = { "hardship" }) @SuppressWarnings("unused")
public class HardshipCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (Civs.perm == null || !Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION)) {
            sendMessage(commandSender, "no-permission", "You don't have permission to use /cv hardship PlayerName");
            return true;
        }
        if (args.length < 2) {
            sendMessage(commandSender, "invalid-target", "Invalid command. Use /cv hardship PlayerName");
            return true;
        }

        String playerName = args[1];
        OfflinePlayer player = Bukkit.getPlayer(playerName);
        if (player == null) {
            player = Bukkit.getOfflinePlayer(playerName);
        }
        if (!player.hasPlayedBefore()) {
            sendMessage(commandSender, "invalid-target", "Invalid command. Use /cv hardship PlayerName");
            return true;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (args.length < 3) {
            sendMessage(commandSender, "hardship{" + civilian.getHardship(), "Hardship: " + civilian.getHardship());
            return true;
        }

        int newHardship = Integer.parseInt(args[2]);
        civilian.setHardship(newHardship);
        CivilianManager.getInstance().saveCivilian(civilian);
        sendMessage(commandSender, "hardship{" + civilian.getHardship(), "Hardship: " + civilian.getHardship());
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return Civs.perm != null && Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION);
    }

    private void sendMessage(CommandSender commandSender, String key, String message) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        if (player != null) {
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslationWithPlaceholders(player, key));
        } else {
            commandSender.sendMessage(message);
        }
    }
}
