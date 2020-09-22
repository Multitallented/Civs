package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.NationRenamedEvent;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsCommand(keys = { "rename-nation "}) @SuppressWarnings("unused")
public class RenameNationCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length < 3) {
            Util.sendMessageToPlayerOrConsole(commandSender, LocaleConstants.INVALID_TARGET,
                    "Usage: /cv rename-nation OldName NewName");
            return true;
        }
        handleRenameNation(commandSender, args[1], args[2]);
        return true;
    }

    private void handleRenameNation(CommandSender commandSender, String oldName, String newName) {
        Nation nation = NationManager.getInstance().getNation(oldName);
        if (nation == null || nation.getCapitol() == null) {
            Util.sendMessageToPlayerOrConsole(commandSender, LocaleConstants.INVALID_TARGET, "No nation named " + oldName);
            return;
        }
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            Town capitol = TownManager.getInstance().getTown(nation.getCapitol());
            if (!capitol.getRawPeople().containsKey(player.getUniqueId()) ||
                    !capitol.getRawPeople().get(player.getUniqueId()).contains(Constants.OWNER)) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        LocaleConstants.PERMISSION_DENIED));
                return;
            }
        }
        if (!Util.validateFileName(newName)) {
            Util.sendMessageToPlayerOrConsole(commandSender, "invalid-name", "Invalid name");
            return;
        }
        NationManager.getInstance().renameNation(oldName, newName);
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "alliance-renamed").replace("$1", oldName).replace("$2", newName));
        } else {
            commandSender.sendMessage(oldName + " has been renamed to " + newName);
        }
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return true;
    }
}
