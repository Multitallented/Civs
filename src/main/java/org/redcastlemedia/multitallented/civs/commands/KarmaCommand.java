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

@CivsCommand(keys = { "karma" }) @SuppressWarnings("unused")
public class KarmaCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (Civs.perm == null || !Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION)) {
            sendMessage(commandSender, "no-permission", "You don't have permission to use /cv karma PlayerName");
            return true;
        }
        if (args.length < 2) {
            sendMessage(commandSender, "invalid-target", "Invalid command. Use /cv karma PlayerName");
            return true;
        }

        String playerName = args[1];
        OfflinePlayer player = Bukkit.getPlayer(playerName);
        if (player == null) {
            player = Bukkit.getOfflinePlayer(playerName);
        }
        if (!player.hasPlayedBefore()) {
            sendMessage(commandSender, "invalid-target", "Invalid command. Use /cv karma PlayerName");
            return true;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (args.length < 3) {
            sendMessage(commandSender, "karma{" + civilian.getKarma(), "Karma: " + civilian.getKarma());
            return true;
        }

        int newKarma = Integer.parseInt(args[2]);
        civilian.setKarma(newKarma);
        CivilianManager.getInstance().saveCivilian(civilian);
        sendMessage(commandSender, "karma{" + civilian.getKarma(), "Karma: " + civilian.getKarma());
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
