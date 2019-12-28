package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CivCommand {
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args);
}
