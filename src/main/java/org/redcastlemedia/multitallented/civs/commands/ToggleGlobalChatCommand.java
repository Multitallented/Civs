package org.redcastlemedia.multitallented.civs.commands;

import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;

@CivsCommand(keys = { "gc" }) @SuppressWarnings("unused")
public class ToggleGlobalChatCommand extends ToggleChatChannelCommand {

    public ToggleGlobalChatCommand() {
        super(ChatChannel.ChatChannelType.GLOBAL);
    }

}
