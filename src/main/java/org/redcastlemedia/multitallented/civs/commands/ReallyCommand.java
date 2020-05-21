package org.redcastlemedia.multitallented.civs.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsCommand(keys = { "really" }) @SuppressWarnings("unused")
public class ReallyCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        boolean isPlayer = (commandSender instanceof Player);
        if (args.length < 3) {
            if (isPlayer) {
                Player player = (Player) commandSender;
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
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
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
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
                        town.getPeople().get(civilian.getUuid()).contains(Constants.OWNER)) {
                    isOwnerOfTown = true;
                    break;
                }
            }
        }

        if (!isOwnerOfTown) {
            Player player = (Player) commandSender;
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "no-permission"));
            return true;
        }

        if (!Util.validateFileName(args[2])) {
            if (isPlayer) {
                Player player = (Player) commandSender;
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
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
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "alliance-renamed").replace("$1", args[1])
                        .replace("$2", validName));
        }
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return true;
    }

    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            return getAllianceNames(args[1]);
        }
        return super.getWord(commandSender, args);
    }
}
