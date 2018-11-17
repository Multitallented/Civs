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
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class InviteTownCommand implements CivCommand {

    public boolean runCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "Unable to invite for non-players");
            return true;
        }
        Player player = (Player) commandSender;
        LocaleManager localeManager = LocaleManager.getInstance();

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (strings.length < 3) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "specify-player-town"));
            return true;
        }

        //0 invite
        //1 player
        //2 townname
        String playerName = strings[1];
        String townName = strings[2];

        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(townName.toLowerCase());
        if (town == null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "town-not-exist").replace("$1", townName));
            return true;
        }
        if (town.getPopulation() >= town.getHousing()) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "not-enough-housing"));
            return true;
        }
        if (!town.getPeople().containsKey(player.getUniqueId()) ||
                !town.getPeople().get(player.getUniqueId()).contains("owner") ||
                        !town.getPeople().get(player.getUniqueId()).contains("recruiter")) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "no-permission-invite").replace("$1", townName));
            return true;
        }
        Player invitee = Bukkit.getPlayer(playerName);
        if (invitee == null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "player-not-online").replace("$1", playerName));
            return true;
        }
        if (town.getPeople().keySet().contains(invitee.getUniqueId())) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "already-member").replace("$1", player.getDisplayName())
                    .replace("$2", townName));
            return true;
        }
        Civilian inviteCiv = CivilianManager.getInstance().getCivilian(invitee.getUniqueId());

        invitee.sendMessage(Civs.getPrefix() + localeManager.getTranslation(inviteCiv.getLocale(),
                "invite-player").replace("$1", player.getDisplayName())
                .replace("$2", town.getType())
                .replace("$3", townName));
        townManager.addInvite(invitee.getUniqueId(), town);
        return true;
    }
}
