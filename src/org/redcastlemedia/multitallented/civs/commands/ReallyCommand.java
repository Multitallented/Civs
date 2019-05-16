package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public class ReallyCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        boolean isPlayer = (commandSender instanceof Player);
        if (args.length < 3) {
            if (isPlayer) {
                Player player = (Player) commandSender;
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "invalid-name"));
            } else {
                commandSender.sendMessage("invalid alliance name");
            }
            return true;
        }


        Alliance alliance = AllianceManager.getInstance().getAlliance(args[1]);

        if (alliance == null) {
            if (isPlayer) {
                Player player = (Player) commandSender;
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "invalid-target"));
            } else {
                commandSender.sendMessage("invalid alliance target");
            }
            return true;
        }

        boolean isOwnerOfTown = !(commandSender instanceof Player);
        if (!isOwnerOfTown) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(((Player) commandSender).getUniqueId());
            for (String townName : alliance.getMembers()) {
                Town town = TownManager.getInstance().getTown(townName);
                if (town.getPeople().containsKey(civilian.getUuid()) &&
                        town.getPeople().get(civilian.getUuid()).contains("owner")) {
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

        if (!Util.validateFileName(args[2])) {
            if (isPlayer) {
                Player player = (Player) commandSender;
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "invalid-name"));
            } else {
                commandSender.sendMessage("invalid alliance name");
            }
            return true;
        }
        String validName = Util.getValidFileName(args[2]);
        if (isPlayer) {
            alliance.setLastRenamedBy(((Player) commandSender).getUniqueId());
        }
        AllianceManager.getInstance().renameAlliance(args[1], validName);

        if (!isPlayer) {
            commandSender.sendMessage(Civs.getPrefix() + "Alliance " + args[1] + " has been renamed to " + validName);
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
