package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.util.UUID;

@CivsCommand(keys = { "accept" }) @SuppressWarnings("unused")
public class AcceptInviteCommand implements CivCommand {

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "Unable to invite for non-players");
            return true;
        }
        Player player = (Player) commandSender;
        LocaleManager localeManager = LocaleManager.getInstance();

        if (Civs.perm != null && !Civs.perm.has(player, "civs.join")) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "no-permission"));
            return true;
        }

        //0 accept
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getInviteTown(player.getUniqueId());
        if (town != null) {
            townManager.acceptInvite(player.getUniqueId());
            for (UUID uuid : town.getPeople().keySet()) {
                Player player1 = Bukkit.getPlayer(uuid);
                if (player1 != null && player1.isOnline()) {
                    player1.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(
                            player1,
                            "new-town-member"
                    ).replace("$1", player.getDisplayName()).replace("$2", town.getName()));
                }
            }
        } else {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "no-invite"));
        }

        return true;
    }
}
