package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Alliance;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public class ReallyCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length < 3) {
            // TODO send message
            return true;
        }


        Alliance alliance = null;
        outer: for (Town town : TownManager.getInstance().getTowns()) {
            for (Alliance ally : town.getAllies()) {
                if (ally.getName().equalsIgnoreCase(args[1])) {
                    alliance = ally;
                    break outer;
                }
            }
        }
        if (alliance == null) {
            // TODO send error message
            return true;
        }

        boolean isOwnerOfTown = !(commandSender instanceof Player);
        if (!isOwnerOfTown) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(((Player) commandSender).getUniqueId());
            for (String townName : alliance.getMembers()) {
                Town town = TownManager.getInstance().getTown(townName);
                if (town.getPeople().containsKey(civilian.getUuid()) &&
                        town.getPeople().get(civilian.getUuid()).equals("owner")) {
                    isOwnerOfTown = true;
                    break;
                }
            }
        }

        if (!isOwnerOfTown) {
            Player player = (Player) commandSender;
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "no-permission"));
            return true;
        }

        if (Util.validateFileName(args[2])) {
            // TODO send error message
            return true;
        }
        String validName = Util.getValidFileName(args[2]);
        for (String townName : alliance.getMembers()) {
            Town town = TownManager.getInstance().getTown(townName);
            for (Alliance ally : town.getAllies()) {
                if (ally.getName().equalsIgnoreCase(args[1])) {
                    ally.setName(validName);
                }
            }
            TownManager.getInstance().saveTown(town);
        }

        if (commandSender instanceof Player) {
            commandSender.sendMessage("alliance " + args[1] + " has been renamed to " + validName);
        } else {
            Player player = (Player) commandSender;
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "alliance-renamed").replace("$1", args[1])
                        .replace("$2", validName));
        }
        return true;
    }
}
