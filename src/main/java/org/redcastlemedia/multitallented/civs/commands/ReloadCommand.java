package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;

@CivsCommand(keys = { "reload" })
public class ReloadCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if ((Civs.perm != null && commandSender.hasPermission("civs.admin")) || commandSender.isOp()) {
            CommonScheduler.run = false;
            ConfigManager.getInstance().reload();
            ItemManager.getInstance().reload();
            MenuManager.getInstance().reload();
            CivilianManager.getInstance().reload();
            TownManager.getInstance().reload();
            RegionManager.getInstance().reload();
            GovernmentManager.getInstance().reload();
            TutorialManager.getInstance().reload();
            AllianceManager.getInstance().reload();
            new LocaleManager();
            CommonScheduler.run = true;
            commandSender.sendMessage(Civs.getPrefix() + "reloaded");
            return true;
        } else {
            commandSender.sendMessage(ChatColor.RED + "Permission Denied!");
            return true;
        }
    }
}
