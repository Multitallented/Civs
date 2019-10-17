package org.redcastlemedia.multitallented.civs.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.towns.Town;

public final class DiscordUtil {
    private DiscordUtil() {

    }

    public static String atAllTownOwners(Town town) {
        String defaultMessage = "";
        for (UUID uuid : town.getRawPeople().keySet()) {
            if (!town.getRawPeople().get(uuid).contains("owner")) {
                continue;
            }
            String discordUserId = Civs.discordSRV.getAccountLinkManager().getDiscordId(uuid);
            if (discordUserId != null) {
                defaultMessage += " @" + discordUserId;
                continue;
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.getName() != null) {
                defaultMessage += " @" + offlinePlayer.getName();
            }
        }
        return defaultMessage;
    }
}
