package org.redcastlemedia.multitallented.civs.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;
import org.springframework.cglib.core.Local;

@CivsCommand(keys = { "power" })
public class PowerCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player) || args.length < 2) {
            return true;
        }
        Player player = (Player) commandSender;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        //0 tax
        //1 townName
        //2 amount

        Town town = TownManager.getInstance().getTown(args[1]);
        if (town == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    LocaleConstants.INVALID_TARGET));
            return true;
        }

        if (args.length > 2) {
            if (!Util.isAdmin(player)) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        LocaleConstants.PERMISSION_DENIED));
                return true;
            }

            int newPower = Math.max(0, Math.min(Integer.parseInt(args[2]), town.getMaxPower()));

            TownManager.getInstance().setTownPower(town, newPower);
        }

        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                "town-power").replace("$1", "" + town.getPower()).replace("$2", "" + town.getMaxPower()));
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return commandSender instanceof Player;
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
