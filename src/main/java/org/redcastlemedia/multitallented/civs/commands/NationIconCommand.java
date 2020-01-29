package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;

@CivsCommand(keys = "nation-icon") @SuppressWarnings("unused")
public class NationIconCommand implements CivCommand {

    //args
    //0 nation-icon
    //1 NationName

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "only players in game can use this command");
            return true;
        }
        Player player = (Player) commandSender;

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.AIR) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "hold-nation-icon"));
            return true;
        }

        Nation nation = NationManager.getInstance().getNation(args[0]);
        if (nation == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "invalid-name"));
            return true;
        }

        nation.setIcon(itemStack);
        NationManager.getInstance().saveNation(nation);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                "nation-icon-set").replace("$1", nation.getName()));
        return true;
    }
}
