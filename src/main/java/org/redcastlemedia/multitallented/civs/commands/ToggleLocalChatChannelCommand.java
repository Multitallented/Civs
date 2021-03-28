package org.redcastlemedia.multitallented.civs.commands;

import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;

@CivsCommand(keys = { "lc" }) @SuppressWarnings("unused")
public class ToggleLocalChatChannelCommand extends ToggleChatChannelCommand {

    public ToggleLocalChatChannelCommand() {
        super(ChatChannel.ChatChannelType.LOCAL);
    }

}
