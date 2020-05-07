package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsCommand(keys = { "karma" })
public class KarmaCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (Civs.perm == null || !Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION)) {
            sendMessage(commandSender, "no-permission", "You don't have permission to use /cv advancetut PlayerName");
            return true;
        }
        if (args.length < 2) {
            sendMessage(commandSender, "invalid-target", "Invalid command. Use /cv advancetut PlayerName");
            return true;
        }

        String townName = args[1];
        Town town = TownManager.getInstance().getTown(townName);
        if (town == null) {
            sendMessage(commandSender, "invalid-target", "Invalid command. Use /cv advancetut PlayerName");
            return true;
        }
        if (args.length < 3) {
            sendMessage(commandSender, "karma{" + town.getKarma(), "Karma: " + town.getKarma());
            return true;
        }

        double newKarma = Double.parseDouble(args[2]);
        town.setKarma(newKarma);
        TownManager.getInstance().saveTown(town);
        sendMessage(commandSender, "karma{" + town.getKarma(), "Karma: " + town.getKarma());
        return true;
    }

    private void sendMessage(CommandSender commandSender, String key, String message) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        if (player != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                    civilian.getLocale(),
                    key
            ));
        } else {
            commandSender.sendMessage(message);
        }
    }
}
