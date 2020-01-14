package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.towns.Town;

import lombok.Getter;

public class ChatChannel {
    @Getter
    private ChatChannelType chatChannelType;

    @Getter
    private Object target;

    public ChatChannel(ChatChannelType chatChannelType, Object target) {
        this.chatChannelType = chatChannelType;
        this.target = target;
    }

    public String getName(OfflinePlayer player) {
        if (chatChannelType == ChatChannelType.GLOBAL) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "global-channel");
        } else if (chatChannelType == ChatChannelType.FRIEND) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "friend-channel");
        } else if (chatChannelType == ChatChannelType.LOCAL) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
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
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "global-channel-desc");
        } else if (chatChannelType == ChatChannelType.FRIEND) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "friend-channel-desc");
        } else if (chatChannelType == ChatChannelType.LOCAL) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "local-channel-desc");
        } else if (chatChannelType == ChatChannelType.TOWN && target != null) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "town-channel-desc").replace("$1", ((Town) target).getName());
        } else if (chatChannelType == ChatChannelType.ALLIANCE && target != null) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
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
