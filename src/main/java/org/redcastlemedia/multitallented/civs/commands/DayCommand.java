package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.scheduler.DailyScheduler;

@CivsCommand(keys = { "newday" }) @SuppressWarnings("unused")
public class DayCommand implements CivCommand {

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        Player player = null;
        if ((commandSender instanceof Player)) {
            player = (Player) commandSender;
        }
        if (player != null && (!player.isOp() ||
                (Civs.perm == null || !Civs.perm.has(player, "civs.admin")))) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                    .getTranslationWithPlaceholders(player, "no-permission"));
            return true;
        }

        DailyScheduler dailyScheduler = new DailyScheduler();
        dailyScheduler.run();
        commandSender.sendMessage(Civs.getPrefix() + "new day started");
        return true;
    }
}
