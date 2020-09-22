package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsCommand(keys = { "create-nation" }) @SuppressWarnings("unused")
public class CreateNationCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length < 2) {
            Util.sendMessageToPlayerOrConsole(commandSender, LocaleConstants.INVALID_TARGET, "Usage: /cv create-nation TownName");
            return true;
        }
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only in game players can use this command");
            return true;
        }
        handleCreateNation((Player) commandSender, args[1]);
        return true;
    }

    private void handleCreateNation(Player player, String townName) {
        Town town = TownManager.getInstance().getTown(townName);
        if (town == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    LocaleConstants.INVALID_TARGET));
            return;
        }
        if (!NationManager.getInstance().canCreateNation(town)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    LocaleConstants.PERMISSION_DENIED));
            return;
        }
        NationManager.getInstance().createNation(town);
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return commandSender instanceof Player;
    }
}
