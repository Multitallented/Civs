package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.tutorials.AnnouncementUtil;

@CivsCommand(keys = { "tutaction" })
public class TutorialActionCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player) || args.length < 2) {
            return true;
        }
        //0 tutaction
        //1 key
        String key = args[1];
        AnnouncementUtil.doAnnouncerAction(key, (Player) commandSender);
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return commandSender instanceof Player;
    }
}
