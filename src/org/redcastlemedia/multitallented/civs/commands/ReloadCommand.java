package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
        CommonScheduler.run = false;
        ItemManager.getInstance().reload();
        CivilianManager.getInstance().reload();
        TownManager.getInstance().reload();
        RegionManager.getInstance().reload();
        TutorialManager.getInstance().reload();
        AllianceManager.getInstance().reload();
        CommonScheduler.run = true;
        return true;
    }
}
