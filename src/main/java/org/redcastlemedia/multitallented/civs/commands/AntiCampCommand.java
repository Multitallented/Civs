package org.redcastlemedia.multitallented.civs.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.effects.AntiCampEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.RaidPortEffect;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

@CivsCommand(keys = { "anticamp" }) @SuppressWarnings("unused")
public class AntiCampCommand extends CivCommand {
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
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "invalid-target"));
            return true;
        }
        if (!town.getEffects().containsKey(AntiCampEffect.KEY)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
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
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "not-enough-money").replace("$1", antiCampCost + ""));
            return true;
        }

        if (!AntiCampEffect.canActivateAntiCamp(player.getUniqueId(), town)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "no-permission"));
            return true;
        }

        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        boolean raidPorterInRange = false;
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            boolean hasRaidPortKey = region.getEffects().containsKey(RaidPortEffect.KEY) ||
                    region.getEffects().containsKey(RaidPortEffect.CHARGING_KEY);
            if (hasRaidPortKey && region.getLocation().distance(town.getLocation()) < townType.getBuildRadius() + 200) {
                raidPorterInRange = true;
                break;
            }
        }
        if (!raidPorterInRange) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslationWithPlaceholders(player,
                    "no-anti-camp-duing-raidporter"));
            return true;
        }

        if (antiCampCost > 0 && Civs.econ != null) {
            Civs.econ.withdrawPlayer(player, antiCampCost);
        }
        AntiCampEffect.activateAntiCamp(civilian.getUuid(), town);
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            return false;
        }

        Player player = (Player) commandSender;

        Town town = TownManager.getInstance().getTownAt(player.getLocation());
        if (town == null) {
            return false;
        }
        if (!town.getEffects().containsKey(AntiCampEffect.KEY)) {
            return false;
        }
        if (!AntiCampEffect.canActivateAntiCamp(player.getUniqueId(), town)) {
            return false;
        }
        return true;
    }

    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            if (commandSender instanceof Player) {
                return getTownNames(args[1]);
            } else {
                return getTownNamesForPlayer(args[1], (Player) commandSender);
            }
        }
        return super.getWord(commandSender, args);
    }
}
