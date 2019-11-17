package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

@CivsCommand(keys = { "toggleann" })
public class ToggleAnnouncementCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        Player player = (Player) commandSender;

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        civilian.setUseAnnouncements(!civilian.isUseAnnouncements());
        CivilianManager.getInstance().saveCivilian(civilian);
        return true;
    }
}
