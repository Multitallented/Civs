package org.redcastlemedia.multitallented.civs.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsCommand(keys = { "bounty" })
public class BountyCommand extends CivCommand {

    public boolean runCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!ConfigManager.getInstance().isUseBounties()) {
            Util.sendMessageToPlayerOrConsole(commandSender, LocaleConstants.PERMISSION_DENIED, "No Permission");
            return true;
        }
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        if (Civs.econ == null) {
            return true;
        }
        LocaleManager localeManager = LocaleManager.getInstance();

        Civilian civilian = null;
        if (player != null) {
            civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        }
        if (strings.length < 3) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        LocaleConstants.INVALID_TARGET));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Invalid target");
            }
            return true;
        }

        //0 bounty
        //1 player
        //2 amount
        String playerName = strings[1];
        String amountString = strings[2];
        double amount = Double.parseDouble(amountString);

        if (amount < 1) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        LocaleConstants.INVALID_TARGET));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Invalid target");
            }
            return true;
        }
        if (player != null) {
            if (Civs.perm != null && !Civs.perm.has(player, Constants.BOUNTY_PLAYER_PERMISSION)) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        LocaleConstants.PERMISSION_DENIED));
                return true;
            }
            double balance = Civs.econ.getBalance(player);
            if (balance < amount) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "not-enough-money").replace("$1", amount + ""));
                return true;
            }
        }

        Town town = TownManager.getInstance().getTown(playerName);
        if (town != null) {
            if (civilian != null) {
                if (town.getPeople().containsKey(civilian.getUuid())) {
                    player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                            LocaleConstants.INVALID_TARGET));
                    return true;
                }
                if (Civs.perm != null && !Civs.perm.has(player, Constants.BOUNTY_TOWN_PERMISSION)) {
                    player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                            LocaleConstants.PERMISSION_DENIED));
                    return true;
                }

                town.getBounties().add(new Bounty(civilian.getUuid(), amount));
                Civs.econ.withdrawPlayer(player, amount);
            } else {
                town.getBounties().add(new Bounty(null, amount));
                commandSender.sendMessage(Civs.getPrefix() + "Bounty set on " + playerName + " for $" + amount);
            }
            town.sortBounties();
            for (Player cPlayer : Bukkit.getOnlinePlayers()) {
                cPlayer.sendMessage(Civs.getPrefix() + ChatColor.RED + localeManager.getTranslation(cPlayer,
                        "bounty-set").replace("$1", playerName).replace("$2", amount + ""));
            }
            return true;
        }


        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "player-not-online").replace("$1", playerName));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Player not online");
            }
            return true;
        }
        if (target.equals(player)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    LocaleConstants.INVALID_TARGET));
            return true;
        }
        if (Civs.perm != null && Civs.perm.has(target, Constants.PVP_EXEMPT_PERMISSION)) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        LocaleConstants.INVALID_TARGET));
            }
            return true;
        }

        Civilian targetCiv = CivilianManager.getInstance().getCivilian(target.getUniqueId());
        if (civilian != null) {
            targetCiv.getBounties().add(new Bounty(civilian.getUuid(), amount));
            CivilianManager.getInstance().saveCivilian(targetCiv);
            Civs.econ.withdrawPlayer(player, amount);
        } else {
            targetCiv.getBounties().add(new Bounty(null, amount));
            CivilianManager.getInstance().saveCivilian(targetCiv);
            commandSender.sendMessage(Civs.getPrefix() + "Bounty set on " + playerName + " for $" + amount);
        }
        targetCiv.sortBounties();
        for (Player cPlayer : Bukkit.getOnlinePlayers()) {
            cPlayer.sendMessage(Civs.getPrefix() + ChatColor.RED + localeManager.getTranslation(cPlayer,
                    "bounty-set").replace("$1", playerName).replace("$2", amount + ""));
        }

        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return Civs.econ != null;
    }

    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            addAllOnlinePlayers(suggestions, args[1]);
            suggestions.addAll(getTownNames(args[1]));
            return suggestions;
        } else if (args.length == 3) {
            return getListOfAmounts();
        }
        return super.getWord(commandSender, args);
    }
}
