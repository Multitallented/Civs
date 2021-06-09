package org.redcastlemedia.multitallented.civs.chat;

import org.bukkit.Material;
import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;

public class ChatChannelConfig {
    public final ChatChannel.ChatChannelType channelType;
    public final boolean enabled;
    public final String icon;
    public final String format;
    public final boolean override;

    public ChatChannelConfig(ChatChannel.ChatChannelType channelType,
                             boolean enabled,
                             Material icon,
                             String format,
                             boolean override) {
        this.channelType = channelType;
        this.enabled = enabled;
        this.icon = icon.name();
        this.format = format;
        this.override = override;
    }
}
