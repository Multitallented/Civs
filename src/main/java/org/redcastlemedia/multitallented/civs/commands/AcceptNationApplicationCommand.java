package org.redcastlemedia.multitallented.civs.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsCommand(keys={"acceptnation"})
public class AcceptNationApplicationCommand extends CivCommand {

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players in game can use this command");
            return true;
        }
        Player player = (Player) commandSender;
        if (args.length < 2) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    LocaleConstants.INVALID_TARGET));
            return true;
        }
        handleAcceptNationApplication(player, args[1]);
        return true;
    }

    private void handleAcceptNationApplication(Player player, String townName) {
        Nation nation = NationManager.getInstance().getNationByOwnerPlayer(player.getUniqueId());
        if (nation == null || nation.getCapitol() == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    LocaleConstants.PERMISSION_DENIED));
            return;
        }
        Town capitol = TownManager.getInstance().getTown(nation.getCapitol());
        if (!capitol.getRawPeople().containsKey(player.getUniqueId()) ||
                !capitol.getRawPeople().get(player.getUniqueId()).contains(Constants.OWNER)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    LocaleConstants.PERMISSION_DENIED));
            return;
        }
        Town town = TownManager.getInstance().getTown(townName);
        if (town == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    LocaleConstants.INVALID_TARGET));
            return;
        }
        if (!nation.getNationApplications().contains(town)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    LocaleConstants.INVALID_TARGET));
            return;
        }
        nation.getNationApplications().remove(town);
        NationManager.getInstance().addMemberToNation(nation, town);
        for (String cTownName : nation.getMembers()) {
            Town town1 = TownManager.getInstance().getTown(cTownName);
            for (UUID uuid : town1.getRawPeople().keySet()) {
                Player player1 = Bukkit.getPlayer(uuid);
                if (player1 != null) {
                    player1.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player1,
                            "new-town-member").replace("$1", town.getName())
                            .replace("$2", nation.getName()));
                }
            }
        }
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player player = (Player) commandSender;
        Nation nation = NationManager.getInstance().getNationByOwnerPlayer(player.getUniqueId());
        if (nation == null) {
            return false;
        }
        return true;
    }
}
