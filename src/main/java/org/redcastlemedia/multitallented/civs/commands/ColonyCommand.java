package org.redcastlemedia.multitallented.civs.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsCommand(keys = { "colony" }) @SuppressWarnings("unused")
public class ColonyCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player) || args.length < 3) {
            return true;
        }
        Player player = (Player) commandSender;

        //0 colony
        //1 colonyTown
        //2 owningTown

        Town colonyTown = TownManager.getInstance().getTown(args[1]);
        Town owningTown = TownManager.getInstance().getTown(args[2]);
        if (colonyTown == null || owningTown == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "invalid-target"));
            return true;
        }
        Government government = GovernmentManager.getInstance().getGovernment(colonyTown.getGovernmentType());
        if (colonyTown.getColonialTown() != null ||
                government.getGovernmentType() != GovernmentType.COLONIALISM) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "invalid-target"));
            return true;
        }

        boolean isOwner = owningTown.getRawPeople().containsKey(player.getUniqueId()) &&
                owningTown.getRawPeople().get(player.getUniqueId()).contains(Constants.OWNER);
        if (!isOwner || player.isOp() ||
                (Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_PERMISSION))) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "no-permission"));
            return true;
        }

        colonyTown.setColonialTown(owningTown.getName());
        TownManager.getInstance().saveTown(colonyTown);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                "colony-town-set").replace("$1", colonyTown.getName())
                .replace("$2", owningTown.getName()));
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
            return getTownNames(args[2]);
        }
        return super.getWord(commandSender, args);
    }
}
