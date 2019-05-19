package org.redcastlemedia.multitallented.civs.ai;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import lombok.Getter;

@Getter
public class AI {
    private final String townName;
    private Town town;

    public AI(String townName) {
        this.townName = townName;
        town = TownManager.getInstance().getTown(townName);
    }

    public String getDisplayName() {
        return ChatColor.MAGIC + townName + ChatColor.RESET;
    }

    public boolean hasTownMemberOnline() {
        for (UUID uuid : town.getRawPeople().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                return true;
            }
        }
        return false;
    }

    public static void broadcastToAllPlayers(String key, String[] args, String aiName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            String message = LocaleManager.getInstance().getTranslation(civilian.getLocale(), key);
            for (int i=0; i<args.length; i++) {
                message = message.replace("$" + i, args[i]);
            }
            if (aiName == null) {
                player.sendMessage(Civs.getPrefix() + message);
            } else {
                player.sendMessage(aiName + message);
            }
        }
    }

    public boolean handleJoiningPlayer(Player player) {
        if (town.getRawPeople().containsKey(player.getUniqueId())) {
            return false;
        }

        // TODO finish this
        return true;
    }
}
