package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsCommand(keys = { "reload" }) @SuppressWarnings("unused")
public class ReloadCommand extends CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if ((Civs.perm != null && commandSender.hasPermission(Constants.ADMIN_PERMISSION)) || commandSender.isOp()) {
            CommonScheduler.setRun(false);
            ConfigManager.getInstance().reload();
            ItemManager.getInstance().reload();
            MenuManager.getInstance().reload();
            CivilianManager.getInstance().reload();
            TownManager.getInstance().reload();
            RegionManager.getInstance().reload();
            GovernmentManager.getInstance().reload();
            TutorialManager.getInstance().reload();
            AllianceManager.getInstance().reload();
            LocaleManager.getInstance().reload();
            ClassManager.getInstance().reload();
            NationManager.getInstance().reload();
            CommonScheduler.setRun(true);
            commandSender.sendMessage(Civs.getPrefix() + "reloaded");
            return true;
        } else {
            commandSender.sendMessage(ChatColor.RED + "Permission Denied!");
            return true;
        }
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return Civs.perm != null && commandSender.hasPermission(Constants.ADMIN_PERMISSION);
    }
}
