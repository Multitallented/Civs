package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

import java.util.ArrayList;
import java.util.List;

@CivsCommand(keys = { "reset" }) @SuppressWarnings("unused")
public class ResetCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        boolean isAdmin = !(commandSender instanceof Player) || commandSender.isOp() ||
                (Civs.perm != null && Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION));
        if (!isAdmin || args.length < 2) {
            return true;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
        if (offlinePlayer == null) {
            if (commandSender instanceof Player) {
                Player player = (Player) commandSender;
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                        player, "player-not-found")
                        .replace("$1", args[1]));
            } else {
                commandSender.sendMessage("Player " + args[1] + " not found");
            }
            return true;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(offlinePlayer.getUniqueId());

        ArrayList<Town> townsToDestroy = new ArrayList<>();
        for (Town town : TownManager.getInstance().getTowns()) {
            if (town.getRawPeople().containsKey(civilian.getUuid())) {
                if (1 == town.getRawPeople().size()) {
                    townsToDestroy.add(town);
                } else {
                    town.getRawPeople().remove(civilian.getUuid());
                    TownManager.getInstance().saveTown(town);
                }
            }
        }
        for (Town town : townsToDestroy) {
            TownManager.getInstance().removeTown(town, true);
        }

        ArrayList<Region> regionsToDestroy = new ArrayList<>();
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (1 == region.getOwners().size() && region.getOwners().contains(civilian.getUuid())) {
                regionsToDestroy.add(region);
            }
        }
        for (Region region : regionsToDestroy) {
            RegionManager.getInstance().removeRegion(region, false, true);
            CivilianListener.getInstance().shouldCancelBlockBreak(region.getLocation().getBlock(), null);
        }
        CivilianManager.getInstance().deleteCivilian(civilian);

        commandSender.sendMessage(Civs.getPrefix() + "Completely removed " + args[1]);
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return Civs.perm != null && Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION);
    }

    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            addAllOnlinePlayers(suggestions, args[1]);
            return suggestions;
        }
        return super.getWord(commandSender, args);
    }
}
