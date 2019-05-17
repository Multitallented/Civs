package org.redcastlemedia.multitallented.civs.commands;

import java.text.NumberFormat;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;

public class TaxCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player) || args.length < 3) {
            return true;
        }
        Player player = (Player) commandSender;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        //0 tax
        //1 townName
        //2 amount

        double amount = OwnershipUtil.invalidAmountOrTown(player, args, civilian);
        if (amount < 1) {
            return true;
        }

        Town town = TownManager.getInstance().getTown(args[1]);

        if (town.getGovernmentType() == GovernmentType.LIBERTARIAN ||
                town.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM ||
                town.getGovernmentType() == GovernmentType.ANARCHY ||
                town.getGovernmentType() == GovernmentType.COOPERATIVE ||
                town.getGovernmentType() == GovernmentType.COMMUNISM) {

            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "no-permission"));
            return true;
        }

        if (amount > ConfigManager.getInstance().getMaxTax()) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "tax-too-high").replace("$1", "" + ConfigManager.getInstance().getMaxTax()));
            return true;
        }

        town.setTaxes(amount);
        TownManager.getInstance().saveTown(town);
        String taxString = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(civilian.getLocale()))
                .format(amount);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                "town-tax-set").replace("$1", town.getName())
                .replace("$2", taxString));

        return true;
    }
}
