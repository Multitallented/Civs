package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.UUID;

@CivsCommand(keys = { "setguest" })
public class SetGuestCommand implements CivCommand {

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
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                        "specify-player-region"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Please specify a player and a region");
            }
            return true;
        }

        //0 invite
        //1 player
        //2 regionname
        UUID inviteUUID = UUID.fromString(strings[1]);
        String locationString = strings[2];

        Town town = TownManager.getInstance().getTown(locationString);
        Region region = null;
        if (town == null) {
            region = RegionManager.getInstance().getRegionAt(Region.idToLocation(locationString));
        }
        if (region == null && town == null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                        "no-permission"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Invalid region");
            }
            return true;
        }
        if (region != null && !isAdmin && !Util.hasOverride(region, civilian) && player != null &&
                !region.getPeople().get(player.getUniqueId()).contains("owner")) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "no-permission"));
            return true;
        }
        OfflinePlayer invitee = Bukkit.getOfflinePlayer(inviteUUID);
        Civilian inviteCiv = CivilianManager.getInstance().getCivilian(invitee.getUniqueId());

        if (!isAdmin && town != null && civilian != null) {
            if (OwnershipUtil.shouldDenyOwnershipOverSomeone(town, civilian, inviteCiv, player)) {
                return true;
            }
        }

        String name = town == null ? region.getType() : town.getName();
        if (invitee.isOnline()) {
            Player invitePlayer = (Player) invitee;
            invitePlayer.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(invitePlayer,
                    "add-guest-region").replace("$1", name));
        }
        if (player != null && civilian!= null && invitee.getName() != null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "guest-added-region").replace("$1", invitee.getName())
                    .replace("$2", name));
        } else {
            commandSender.sendMessage(Civs.getPrefix() + inviteUUID + " is now a guest of your " + name);
        }
        if (region != null && region.getPeople().get(invitee.getUniqueId()) != null &&
                !region.getPeople().get(invitee.getUniqueId()).contains("guest")) {
            region.setPeople(inviteUUID, "guest");
            RegionManager.getInstance().saveRegion(region);
        } else if (town != null && town.getPeople().get(invitee.getUniqueId()) != null &&
                !town.getPeople().get(invitee.getUniqueId()).contains("guest")) {
            town.setPeople(inviteUUID, "guest");
            TownManager.getInstance().saveTown(town);
        }
        return true;
    }
}
