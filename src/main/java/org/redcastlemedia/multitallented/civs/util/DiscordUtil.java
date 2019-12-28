package org.redcastlemedia.multitallented.civs.util;

import java.util.UUID;

import github.scarsz.discordsrv.dependencies.jda.core.entities.Member;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.towns.Town;

public final class DiscordUtil {
    private DiscordUtil() {

    }

    public static String atAllTownOwners(Town town) {
        String defaultMessage = "";
        for (UUID uuid : town.getRawPeople().keySet()) {
            if (!town.getRawPeople().get(uuid).contains(Constants.OWNER)) {
                continue;
            }
            String discordUserId = Civs.discordSRV.getAccountLinkManager().getDiscordId(uuid);
            if (discordUserId != null) {
                defaultMessage += " @" + discordUserId;
                continue;
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.getName() != null) {
                String mention = "";
                for (Member member : Civs.discordSRV.getMainTextChannel().getMembers()) {
                    if (offlinePlayer.getName().startsWith(member.getUser().getName())) {
                        mention = " " + member.getUser().getAsMention();
                        break;
                    }
                }
                if (mention.isEmpty()) {
                    mention = " @" + offlinePlayer.getName();
                }
                defaultMessage += mention;
            }
        }
        return defaultMessage;
    }

    public static void sendMessageToMainChannel(String message) {
        Civs.discordSRV.getMainTextChannel().sendMessage(ChatColor.stripColor(message)).submit();
    }
}
