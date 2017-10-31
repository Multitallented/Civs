package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.util.UUID;

public class AcceptInviteCommand implements CivCommand {

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "Unable to invite for non-players");
            return true;
        }
        Player player = (Player) commandSender;
        LocaleManager localeManager = LocaleManager.getInstance();

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        if (Civs.perm != null && !Civs.perm.has(player, "civs.join")) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "no-permission"));
            return true;
        }

        //0 accept
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getInviteTown(player.getUniqueId());
        if (town != null) {
            townManager.acceptInvite(player.getUniqueId());
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "invite-accepted"));
            for (UUID uuid : town.getPeople().keySet()) {
                Player player1 = Bukkit.getPlayer(uuid);
                if (player1 != null && player1.isOnline()) {
                    Civilian civilian1 = CivilianManager.getInstance().getCivilian(uuid);
                    player1.sendMessage(Civs.getPrefix() + localeManager.getTranslation(
                            civilian1.getLocale(),
                            "new-town-member"
                    ).replace("$1", player.getDisplayName()).replace("$2", town.getName()));
                }
            }
        } else {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "no-invite"));
        }

        return true;
    }
}
