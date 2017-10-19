package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.menus.MainMenu;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class SetOwnerCommand implements CivCommand {

    public boolean runCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
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

        Region region = RegionManager.getInstance().getRegionAt(Region.idToLocation(locationString));
        if (region == null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "no-permission"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Invalid region");
            }
            return true;
        }
        if (player != null && !region.getPeople().get(player.getUniqueId()).equals("owner")) {
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

        if (invitee.isOnline()) {
            invitee.sendMessage(Civs.getPrefix() + localeManager.getTranslation(inviteCiv.getLocale(),
                    "add-owner-region").replace("$1", region.getType()));
        }
        if (player != null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "owner-added-region").replace("$1", playerName)
                    .replace("$2", region.getType()));
        } else {
            commandSender.sendMessage(Civs.getPrefix() + playerName + " is now an owner of your " + region.getType());
        }
        if (region.getPeople().get(invitee.getUniqueId()) != null &&
                !region.getPeople().get(invitee.getUniqueId()).contains("owner")) {
            region.getPeople().put(invitee.getUniqueId(), "owner");
            RegionManager.getInstance().saveRegion(region);
        }
        return true;
    }
}
