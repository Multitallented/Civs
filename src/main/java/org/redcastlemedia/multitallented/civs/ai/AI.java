package org.redcastlemedia.multitallented.civs.ai;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

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
        return ChatColor.GREEN + "[" + town.getName() + "] " +
                ChatColor.BLACK + ChatColor.MAGIC + townName + ChatColor.RESET;
    }

    public UUID hasTownMemberOnline() {
        String currentRole = null;
        UUID highestRankingOnlinePlayer = null;
        for (UUID uuid : town.getRawPeople().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                if (currentRole == null) {
                    currentRole = town.getRawPeople().get(uuid);
                    highestRankingOnlinePlayer = uuid;
                } else if (isHigherRank(town.getRawPeople().get(uuid), currentRole)) {
                    currentRole = town.getRawPeople().get(uuid);
                    highestRankingOnlinePlayer = uuid;
                }
            }
        }
        return highestRankingOnlinePlayer;
    }

    private boolean isHigherRank(String role1, String role2) {
        if (role1.contains(Constants.OWNER)) {
            return !role2.contains(Constants.OWNER);
        } else if (role2.contains(Constants.OWNER)) {
            return false;
        } else if (role1.contains("member")) {
            return !role2.contains("member");
        } else {
            return false;
        }
    }

    public static void broadcastToAllPlayers(String key, String[] args, String aiName) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String message = LocaleManager.getInstance().getTranslation(player, key);
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

    // TODO commission buildings

    // TODO invite players

    // TODO make alliances

    // TODO retaliate against war and raiders

    // TODO buy houses from players when possible

    // TODO raise and lower taxes

    public boolean handleJoiningPlayer(Player player) {
        // TODO other operations here?
        return invitePlayer(player);
    }

    public void handlePlayerChat(AsyncPlayerChatEvent event) {
        // TODO finish this
    }

    private boolean invitePlayer(Player player) {
        if (town.getRawPeople().containsKey(player.getUniqueId())) {
            return false;
        }

        if (town.getHousing() <= town.getPopulation()) {
            return false;
        }
        UUID uuid = hasTownMemberOnline();
        if (uuid == null) {
            return false;
        }

        // TODO add player to list of already invited players

        String[] args = new String[1];
        args[0] = player.getDisplayName();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), () -> broadcastToAllPlayers("ai-invite", args, getDisplayName()), 100);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), () -> {
            Civilian inviteCiv = CivilianManager.getInstance().getCivilian(player.getUniqueId());

            if (TownManager.getInstance().addInvite(player.getUniqueId(), town)) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "invite-player").replace("$1", getDisplayName() + ChatColor.GREEN)
                        .replace("$2", town.getType())
                        .replace("$3", townName));
            }
        }, 120);

        String[] args2 = new String[1];
        args2[0] = Bukkit.getPlayer(uuid).getDisplayName();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), () -> broadcastToAllPlayers("ai-help", args2, getDisplayName()), 130);

        return true;
    }
}
