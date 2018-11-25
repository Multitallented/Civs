package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.items.CivItem;

public class RegionCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "Unable to use region command for non-players");
            return true;
        }
        Player player = (Player) commandSender;

        //0 region
        //1 regionName
        if (args.length < 2) {
            return true;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || !CivItem.isCivsItem(itemStack)) {
            return true;
        }

//        RegionManager.getInstance().detectNewRegion();

        return true;
    }
}
