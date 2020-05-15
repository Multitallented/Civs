package org.redcastlemedia.multitallented.civs.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsCommand(keys = { "add" }) @SuppressWarnings("unused")
public class AddMemberCommand implements CivCommand {

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
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                        "specify-player-region"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Please specify a player and a region");
            }
            return true;
        }

        //0 add
        //1 player
        //2 regionname
        String playerName = strings[1];
        String locationString = strings[2];

        Region region = RegionManager.getInstance().getRegionAt(Region.idToLocation(locationString));
        if (region == null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                        "no-permission"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Invalid region");
            }
            return true;
        }
        if (!Util.hasOverride(region, civilian) && player != null &&
                !region.getPeople().get(player.getUniqueId()).contains(Constants.OWNER)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "no-permission"));
            return true;
        }
        Player invitee = Bukkit.getPlayer(playerName);
        if (invitee == null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                        "player-not-online").replace("$1", playerName));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Player " + playerName + " is not online");
            }
            return true;
        }

        if (invitee.isOnline()) {
            invitee.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(invitee,
                    "invite-member-region").replace("$1", region.getType()));
        }
        if (player != null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "member-invited-region").replace("$1", playerName)
                    .replace("$2", region.getType()));
        } else {
            commandSender.sendMessage(Civs.getPrefix() + playerName + " has been added to your " + region.getType());
        }
        region.setPeople(invitee.getUniqueId(), "member");
        RegionManager.getInstance().saveRegion(region);
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return true;
    }

//    @Override
//    public List<String> getNextWordList(CommandSender commandSender, String[] args) {
//        List<String> returnList = new ArrayList<>();
//        if (args.length < 2) {
//
//        }
//        //1 player
//        //2 regionname
//        return null;
//    }

}
