package org.redcastlemedia.multitallented.civs.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

public abstract class CivCommand {
    public abstract boolean runCommand(CommandSender commandSender, Command command, String label, String[] args);

    public abstract boolean canUseCommand(CommandSender commandSender);

    public List<String> getWord(CommandSender commandSender, String[] args) {
        return new ArrayList<>();
    }

    protected void addAllOnlinePlayers(List<String> suggestions, String playerName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if ((playerName.isEmpty() || player.getName().startsWith(playerName)) &&
                    (Civs.perm == null || !Civs.perm.has(player, Constants.ADMIN_INVISIBLE))) {
                suggestions.add(player.getName());
            }
        }
    }

    protected List<String> getListOfAmounts() {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("100");
        suggestions.add("500");
        suggestions.add("1000");
        return suggestions;
    }

    protected List<String> getTownNames(String townName) {
        List<String> townNames = new ArrayList<>();
        for (String name : TownManager.getInstance().getTownNames()) {
            if (name.startsWith(townName)) {
                townNames.add(name);
            }
        }
        return townNames;
    }
    protected List<String> getAllianceNames(String allianceName) {
        List<String> allianceNames = new ArrayList<>();
        for (Alliance alliance : AllianceManager.getInstance().getAllAlliances()) {
            if (alliance.getName().startsWith(allianceName)) {
                allianceNames.add(alliance.getName());
            }
        }
        return allianceNames;
    }
    protected List<String> getTownNamesForPlayer(String townName, Player player) {
        List<String> suggestions = new ArrayList<>();
        for (Town town : TownManager.getInstance().getTownsForPlayer(player.getUniqueId())) {
            if (town.getName().startsWith(townName)) {
                suggestions.add(town.getName());
            }
        }
        return suggestions;
    }
}
