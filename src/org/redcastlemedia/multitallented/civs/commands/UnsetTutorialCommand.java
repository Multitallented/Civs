package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redcastlemedia.multitallented.civs.BlockLogger;
import org.redcastlemedia.multitallented.civs.Civs;

public class UnsetTutorialCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (commandSender.isOp() || (Civs.perm != null &&
                Civs.perm.has(commandSender, "civs.admin"))) {
            BlockLogger.getInstance().saveTutorialLocation(null);
        }
        return true;
    }
}
