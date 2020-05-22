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
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CivsCommand(keys = { "setmember" })
public class SetMemberCommand extends CivCommand {

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
            isAdmin = player.isOp() || (Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_PERMISSION));
        } else {
            isAdmin = true;
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
        //2 townname
        UUID inviteUUID = UUID.fromString(strings[1]);
        String locationString = strings[2];

        Town town = TownManager.getInstance().getTown(locationString);
        Region region = null;
        Town overrideTown = null;
        if (town == null) {
            region = RegionManager.getInstance().getRegionAt(Region.idToLocation(locationString));
            overrideTown = TownManager.getInstance().getTownAt(region.getLocation());
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
        if (region != null && !isAdmin && !Util.hasOverride(region, civilian, overrideTown) &&
                !region.getPeople().get(player.getUniqueId()).contains(Constants.OWNER)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "no-permission"));
            return true;
        }
        OfflinePlayer invitee = Bukkit.getOfflinePlayer(inviteUUID);
        if (invitee == null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                        "player-not-online").replace("$1", invitee.getName()));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Player " + inviteUUID + " is not online");
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
            Player invitePlayer = (Player) invitee;
            invitePlayer.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(invitePlayer,
                    "add-member-region").replace("$1", name));
        }
        if (player != null && civilian != null && invitee.getName() != null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "member-added-region").replace("$1", invitee.getName())
                    .replace("$2", name));
        } else {
            commandSender.sendMessage(Civs.getPrefix() + inviteUUID + " is now a member of your " + name);
        }
        if (region != null && region.getPeople().get(invitee.getUniqueId()) != null &&
                !region.getPeople().get(invitee.getUniqueId()).contains("member")) {
            region.setPeople(invitee.getUniqueId(), "member");
            RegionManager.getInstance().saveRegion(region);
        } else if (town != null && town.getPeople().get(invitee.getUniqueId()) != null &&
                !town.getPeople().get(invitee.getUniqueId()).contains("member")) {
            town.setPeople(invitee.getUniqueId(), "member");
            TownManager.getInstance().saveTown(town);
        }
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return true;
    }


    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 2) {
            addAllOnlinePlayers(suggestions, args[1]);
            return suggestions;
        }
        if (args.length == 3 && commandSender instanceof Player) {
            Player player = (Player) commandSender;
            Region region = RegionManager.getInstance().getRegionAt(player.getLocation());
            if (region != null) {
                suggestions.add(region.getId());
                suggestions.addAll(getTownNames(args[2]));
                return suggestions;
            }
        } else if (args.length == 3) {
            return getTownNames(args[2]);
        }
        return super.getWord(commandSender, args);
    }
}
