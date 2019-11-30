package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.effects.AntiCampEffect;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class AntiCampCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            return true;
        }

        // 0 anticamp
        // 1 town name
        Player player = (Player) commandSender;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        LocaleManager localeManager = LocaleManager.getInstance();

        Town town;
        if (args.length > 1) {
            town = TownManager.getInstance().getTown(args[1]);
        } else {
            town = TownManager.getInstance().getTownAt(player.getLocation());
        }
        if (town == null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "invalid-target"));
            return true;
        }
        if (!town.getEffects().containsKey(AntiCampEffect.KEY)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "invalid-target"));
            return true;
        }
        double antiCampCost = 0;
        if (town.getEffects().get(AntiCampEffect.KEY) != null) {
            String antiCampString = town.getEffects().get(AntiCampEffect.KEY);
            String[] splitString = antiCampString.split("\\.");
            if (splitString.length > 2) {
                antiCampCost = Double.parseDouble(splitString[2]);
            }
        }
        if (antiCampCost > 0 && (Civs.econ == null || !Civs.econ.has(player, antiCampCost)) &&
                town.getBankAccount() < antiCampCost) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "not-enough-money").replace("$1", antiCampCost + ""));
            return true;
        }

        if (!AntiCampEffect.canActivateAntiCamp(player.getUniqueId(), town)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "no-permission"));
            return true;
        }

        if (antiCampCost > 0 && Civs.econ != null) {
            Civs.econ.withdrawPlayer(player, antiCampCost);
        }
        AntiCampEffect.activateAntiCamp(civilian.getUuid(), town);
        return true;
    }
}
