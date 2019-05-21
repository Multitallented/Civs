package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

public class RemoveMemberCommand implements CivCommand {

    public boolean runCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = null;
        boolean isAdmin = false;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
            isAdmin = player.isOp() || (Civs.perm != null && Civs.perm.has(player, "civs.admin"));
        } else {
            isAdmin = true;
        }
        LocaleManager localeManager = LocaleManager.getInstance();

        Civilian civilian = null;
        if (player != null) {
            civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        }
        if (strings.length < 3) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "specify-player-region"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Please specify a player and a region");
            }
            return true;
        }

        //0 invite
        //1 player
        //2 regionname
        String playerName = strings[1];
        String locationString = strings[2];

        Town town = TownManager.getInstance().getTown(locationString);
        Region region = null;
        if (town == null) {
            region = RegionManager.getInstance().getRegionAt(Region.idToLocation(locationString));
        }
        if (region == null && town == null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "no-permission"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Invalid region");
            }
            return true;
        }
        if (!isAdmin && !playerName.equalsIgnoreCase(player.getDisplayName().toLowerCase()) && region != null &&
                !Util.hasOverride(region, civilian, town) && player != null &&
                !region.getPeople().get(player.getUniqueId()).contains("owner")) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "no-permission"));
            return true;
        }
        Player invitee = Bukkit.getPlayer(playerName);
        if (invitee == null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "player-not-online").replace("$1", playerName));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Player " + playerName + " is not online");
            }
            return true;
        }
        Civilian inviteCiv = CivilianManager.getInstance().getCivilian(invitee.getUniqueId());

        if (!isAdmin && town != null && civilian != null) {
            if (OwnershipUtil.shouldDenyOwnershipOverSomeone(town, civilian, inviteCiv, player)) {
                return true;
            }
        }

        String name = town == null ? region.getType() : town.getName();
        if (invitee.isOnline()) {
            invitee.sendMessage(Civs.getPrefix() + localeManager.getTranslation(inviteCiv.getLocale(),
                    "remove-member-region").replace("$1", name));
        }
        if (player != null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "member-removed-region").replace("$1", playerName)
                    .replace("$2", name));
        } else {
            commandSender.sendMessage(Civs.getPrefix() + playerName + " is no longer a member of your " + name);
        }
        if (region != null && region.getPeople().get(invitee.getUniqueId()) != null) {
            region.getPeople().remove(invitee.getUniqueId());
            RegionManager.getInstance().saveRegion(region);
        } else if (town != null && town.getPeople().get(invitee.getUniqueId()) != null) {
            town.getPeople().remove(invitee.getUniqueId());
            TownManager.getInstance().saveTown(town);
        }
        return true;
    }
}
