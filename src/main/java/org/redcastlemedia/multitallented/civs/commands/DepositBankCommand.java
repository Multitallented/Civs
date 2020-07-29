package org.redcastlemedia.multitallented.civs.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsCommand(keys = { "deposit" }) @SuppressWarnings("unused")
public class DepositBankCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player) || args.length < 3 || Civs.econ == null) {
            return true;
        }
        Player player = (Player) commandSender;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        //0 deposit
        //1 townName
        //2 amount

        double amount = OwnershipUtil.invalidAmountOrTown(player, args, civilian);
        if (amount < 1) {
            return true;
        }


        Town town = TownManager.getInstance().getTown(args[1]);
        town.setBankAccount(town.getBankAccount() + amount);
        TownManager.getInstance().saveTown(town);
        Civs.econ.withdrawPlayer(player, amount);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                "deposit-money").replace("$1", Util.getNumberFormat(amount, civilian.getLocale()))
                .replace("$2", town.getName()));
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return commandSender instanceof Player && Civs.econ != null;
    }

    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            return getTownNames(args[1]);
        }
        if (args.length == 3) {
            return getListOfAmounts();
        }
        return super.getWord(commandSender, args);
    }
}
