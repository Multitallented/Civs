package org.redcastlemedia.multitallented.civs.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsCommand(keys = { "hardship" }) @SuppressWarnings("unused")
public class HardshipCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (Civs.perm == null || !Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION)) {
            Util.sendMessageToPlayerOrConsole(commandSender, "no-permission", "You don't have permission to use /cv hardship PlayerName");
            return true;
        }
        if (args.length < 2) {
            Util.sendMessageToPlayerOrConsole(commandSender, "invalid-target", "Invalid command. Use /cv hardship PlayerName");
            return true;
        }

        String playerName = args[1];
        OfflinePlayer player = Bukkit.getPlayer(playerName);
        if (player == null) {
            player = Bukkit.getOfflinePlayer(playerName);
        }
        if (!player.hasPlayedBefore()) {
            Util.sendMessageToPlayerOrConsole(commandSender, "invalid-target", "Invalid command. Use /cv hardship PlayerName");
            return true;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (args.length < 3) {
            Util.sendMessageToPlayerOrConsole(commandSender, "hardship{" + civilian.getHardship(), "Hardship: " + civilian.getHardship());
            return true;
        }

        int newHardship = Integer.parseInt(args[2]);
        civilian.setHardship(newHardship);
        CivilianManager.getInstance().saveCivilian(civilian);
        Util.sendMessageToPlayerOrConsole(commandSender, "hardship{" + civilian.getHardship(), "Hardship: " + civilian.getHardship());
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return Civs.perm != null && Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION);
    }

    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            return getListOfAmounts();
        }
        return super.getWord(commandSender, args);
    }
}
