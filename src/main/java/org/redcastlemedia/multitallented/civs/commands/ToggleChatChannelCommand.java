package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.chat.ChatChannelConfig;
import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;

public abstract class ToggleChatChannelCommand<T> extends CivCommand {

    protected final ChatChannel.ChatChannelType channelType;

    public ToggleChatChannelCommand(ChatChannel.ChatChannelType channelType) {
        this.channelType = channelType;
    }


    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Player player = (Player) commandSender;

        CivilianManager manager = CivilianManager.getInstance();
        Civilian civilian = manager.getCivilian(player.getUniqueId());

        T o = getRelevantTarget(civilian, args);

        LocaleManager localeManager = LocaleManager.getInstance();
    
        ChatChannelConfig chatChannelConfig = ConfigManager.getInstance().getChatChannels().get(this.channelType);
    
        if (isValid(civilian, o) && chatChannelConfig.enabled) {
            civilian.setChatChannel(getChatChannel(civilian, o));
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "chat-channel-set").replace("$1", civilian.getChatChannel().getName(player)));
        } else {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "invalid-target"));
        }
        return false;
    }

    public T getRelevantTarget(Civilian civilian, String[] a) {
        return null;
    }

    public boolean isValid(Civilian c, T t) {
        return true;
    }

    protected ChatChannel getChatChannel(Civilian civilian, T t) {
        return new ChatChannel(channelType, t);
    }


    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return commandSender instanceof Player;
    }


}
