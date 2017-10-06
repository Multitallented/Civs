package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.menus.MainMenu;

public class MenuCommand implements CivCommand {

    public boolean runCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "Unable to open menu for non-players");
            return true;
        }
        Player player = (Player) commandSender;

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        Inventory menu = MainMenu.createMenu(civilian);
        player.openInventory(menu);
        return true;
    }
}
