package org.redcastlemedia.multitallented.civs.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CivCommand {
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args);

    public boolean canUseCommand(CommandSender commandSender);

//    public List<String> getNextWordList(CommandSender commandSender, String[] args);
}
