package org.redcastlemedia.multitallented.civs.commands;

import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;

@CivsCommand(keys = { "nc" }) @SuppressWarnings("unused")
public class ToggleNationChatCommand extends ToggleChatChannelCommand {

    public ToggleNationChatCommand() {
        super(ChatChannel.ChatChannelType.NATION);
    }


}
