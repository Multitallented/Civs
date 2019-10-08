package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

public class WithdrawBankCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player) || args.length < 3 || Civs.econ == null) {
            return true;
        }
        Player player = (Player) commandSender;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        //0 withdraw
        //1 townName
        //2 amount

        double amount = OwnershipUtil.invalidAmountOrTown(player, args, civilian);
        if (amount < 1) {
            return true;
        }

        Town town = TownManager.getInstance().getTown(args[1]);
        town.setBankAccount(town.getBankAccount() - amount);
        TownManager.getInstance().saveTown(town);
        Civs.econ.depositPlayer(player, amount);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                "withdrawn-money").replace("$1", Util.getNumberFormat(amount, civilian.getLocale()))
                .replace("$2", town.getName()));
        return true;
    }
}
