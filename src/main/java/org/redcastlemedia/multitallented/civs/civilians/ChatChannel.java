package org.redcastlemedia.multitallented.civs.civilians;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.towns.Town;

public class ChatChannel {
    @Getter
    private final ChatChannelType chatChannelType;

    @Getter
    private final Object target;

    public ChatChannel(ChatChannelType chatChannelType, Object target) {
        this.chatChannelType = chatChannelType;
        this.target = target;
    }

    public String getName(OfflinePlayer player) {
        if (chatChannelType == ChatChannelType.GLOBAL) {
            return LocaleManager.getInstance().getTranslation(player,
                    "global-channel");
        } else if (chatChannelType == ChatChannelType.FRIEND) {
            return LocaleManager.getInstance().getTranslation(player,
                    "friend-channel");
        } else if (chatChannelType == ChatChannelType.LOCAL) {
            return LocaleManager.getInstance().getTranslation(player,
                    "local-channel");
        } else if (chatChannelType == ChatChannelType.TOWN && target != null) {
            return ((Town) target).getName();
        } else if (chatChannelType == ChatChannelType.ALLIANCE && target != null) {
            return ((Alliance) target).getName();
        } else {
            return "";
        }
    }

    public String getDesc(Player player) {
        if (chatChannelType == ChatChannelType.GLOBAL) {
            return LocaleManager.getInstance().getTranslation(player,
                    "global-channel-desc");
        } else if (chatChannelType == ChatChannelType.FRIEND) {
            return LocaleManager.getInstance().getTranslation(player,
                    "friend-channel-desc");
        } else if (chatChannelType == ChatChannelType.LOCAL) {
            return LocaleManager.getInstance().getTranslation(player,
                    "local-channel-desc");
        } else if (chatChannelType == ChatChannelType.TOWN && target != null) {
            return LocaleManager.getInstance().getTranslation(player,
                    "town-channel-desc").replace("$1", ((Town) target).getName());
        } else if (chatChannelType == ChatChannelType.ALLIANCE && target != null) {
            return LocaleManager.getInstance().getTranslation(player,
                    "alliance-channel-desc").replace("$1", ((Alliance) target).getName());
        } else {
            return "";
        }
    }

    public enum ChatChannelType {
        FRIEND,
        TOWN,
        NATION,
        GLOBAL,
        ALLIANCE,
        LOCAL
    }
}
