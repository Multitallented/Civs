package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;

public class ReloadCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(commandSender.hasPermission("civs.admin")) {
            CommonScheduler.run = false;
            ConfigManager.getInstance().reload();
            ItemManager.getInstance().reload();
            CivilianManager.getInstance().reload();
            TownManager.getInstance().reload();
            RegionManager.getInstance().reload();
            TutorialManager.getInstance().reload();
            AllianceManager.getInstance().reload();
            CommonScheduler.run = true;
            commandSender.sendMessage(Civs.NAME + " reloaded");
            return true;
        }
        else
            {
                commandSender.sendMessage(ChatColor.RED + "Permission Denied!");
                return true;
            }
    }
}
