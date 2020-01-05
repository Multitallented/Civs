package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.entity.Player;
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

    public String getName(Player player) {
        if (chatChannelType == ChatChannelType.GLOBAL) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "global-channel");
        } else if (chatChannelType == ChatChannelType.FRIEND) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "friend-channel");
        } else if (chatChannelType == ChatChannelType.LOCAL) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "local-channel");
        } else if (chatChannelType == ChatChannelType.TOWN) {
            return ((Town) target).getName();
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
        } else if (chatChannelType == ChatChannelType.TOWN) {
            return LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "local-channel-desc").replace("$1", ((Town) target).getName());
        } else {
            return "";
        }
    }

    public enum ChatChannelType {
        FRIEND,
        TOWN,
        NATION,
        GLOBAL,
        LOCAL
    }
}
