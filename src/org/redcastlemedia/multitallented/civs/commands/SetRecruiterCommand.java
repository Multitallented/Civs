package org.redcastlemedia.multitallented.civs.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

@CivsCommand(keys = { "setrecruiter" })
public class SetRecruiterCommand implements CivCommand {

    public boolean runCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        LocaleManager localeManager = LocaleManager.getInstance();

        boolean isAdmin = false;
        Civilian civilian = null;
        if (player != null) {
            civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            isAdmin = player.isOp() || (Civs.perm != null && Civs.perm.has(player, "civs.admin"));
        } else {
            isAdmin = true;
        }
        if (strings.length < 3) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "specify-player-region"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Please specify a player and a town");
            }
            return true;
        }

        //0 invite
        //1 player
        //2 town
        UUID inviteUUID = UUID.fromString(strings[1]);
        String locationString = strings[2];

        Town town = TownManager.getInstance().getTown(locationString);
        if (town == null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "no-permission"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Invalid region");
            }
            return true;
        }
        OfflinePlayer invitee = Bukkit.getOfflinePlayer(inviteUUID);
        Player invitePlayer = invitee.isOnline() ? (Player) invitee : null;
//        if (!isAdmin && (town.getGovernmentType() == GovernmentType.DEMOCRACY ||
//                town.getGovernmentType() == GovernmentType.DEMOCRATIC_SOCIALISM)) {
//            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
//                    "no-permission"));
//            return true;
//        }

        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        boolean hasPermission = civilian == null || government.getGovernmentType() == GovernmentType.ANARCHY ||
                (town.getRawPeople().containsKey(civilian.getUuid()) &&
                        town.getRawPeople().get(civilian.getUuid()).contains("owner"));

        boolean colonialOverride = town.getColonialTown() != null;
        if (colonialOverride) {
            Town colonialTown = TownManager.getInstance().getTown(town.getColonialTown());
            if (civilian != null && (!colonialTown.getRawPeople().containsKey(civilian.getUuid()) ||
                    !colonialTown.getRawPeople().get(civilian.getUuid()).contains("owner"))) {
                colonialOverride = false;
            }
        }

        if (!isAdmin && !hasPermission && !colonialOverride) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "no-permission"));
            return true;
        }

        Civilian inviteCiv = CivilianManager.getInstance().getCivilian(invitee.getUniqueId());

        String name = town.getName();
        if (invitePlayer != null) {
            invitePlayer.sendMessage(Civs.getPrefix() + localeManager.getTranslation(inviteCiv.getLocale(),
                    "add-recruiter-region").replace("$1", name));
        }
        if (player != null && civilian != null && invitePlayer != null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "recruiter-added-region").replace("$1", invitePlayer.getName())
                    .replace("$2", name));
        } else {
            commandSender.sendMessage(Civs.getPrefix() + inviteUUID + " is now a recruiter of your " + name);
        }
        if (town.getPeople().get(invitee.getUniqueId()) != null &&
                !town.getPeople().get(invitee.getUniqueId()).contains("recruiter")) {
            town.setPeople(invitee.getUniqueId(), town.getPeople().get(inviteUUID) + "recruiter");
            TownManager.getInstance().saveTown(town);
        }
        return true;
    }
}
