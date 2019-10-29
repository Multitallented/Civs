package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class ColonyCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player) || args.length < 3) {
            return true;
        }
        Player player = (Player) commandSender;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        //0 colony
        //1 colonyTown
        //2 owningTown

        Town colonyTown = TownManager.getInstance().getTown(args[1]);
        Town owningTown = TownManager.getInstance().getTown(args[2]);
        if (colonyTown == null || owningTown == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "invalid-target"));
            return true;
        }
        Government government = GovernmentManager.getInstance().getGovernment(colonyTown.getGovernmentType());
        if (colonyTown.getColonialTown() != null ||
                government.getGovernmentType() != GovernmentType.COLONIALISM) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "invalid-target"));
            return true;
        }

        boolean isOwner = owningTown.getRawPeople().containsKey(player.getUniqueId()) &&
                owningTown.getRawPeople().get(player.getUniqueId()).contains("owner");
        if (!isOwner || player.isOp() ||
                (Civs.perm != null && Civs.perm.has(player, "civs.admin"))) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "no-permission"));
            return true;
        }

        colonyTown.setColonialTown(owningTown.getName());
        TownManager.getInstance().saveTown(colonyTown);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                "colony-town-set").replace("$1", colonyTown.getName())
                .replace("$2", owningTown.getName()));
        return true;
    }
}
