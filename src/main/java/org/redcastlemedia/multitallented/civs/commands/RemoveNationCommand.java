package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsCommand(keys = { "remove-nation" }) @SuppressWarnings("unused")
public class RemoveNationCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }

        if (args.length != 2) {
            Util.sendMessageToPlayerOrConsole(commandSender, LocaleConstants.INVALID_TARGET,
                    "Usage: /cv remove-nation nationName");
            return true;
        }
        Nation nation = NationManager.getInstance().getNation(args[1]);
        if (nation == null) {
            if (player != null) {
                LocaleManager localeManager = LocaleManager.getInstance();
                commandSender.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "nation-not-found").replace("$1", args[1]));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Unknown nation " + args[1]);
            }

            return true;

        }
        NationManager.getInstance().removeNation(nation);
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION);
    }
}
