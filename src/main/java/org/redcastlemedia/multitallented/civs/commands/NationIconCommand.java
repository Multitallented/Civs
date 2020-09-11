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
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;

@CivsCommand(keys = "nation-icon") @SuppressWarnings("unused")
public class NationIconCommand extends CivCommand {

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "only players in game can use this command");
            return true;
        }
        Player player = (Player) commandSender;
        handleNationIconCommand(player);
        return true;
    }

    private void handleNationIconCommand(Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.AIR) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "hold-nation-icon"));
            return;
        }

        Nation nation = NationManager.getInstance().getNationByPlayer(player.getUniqueId());
        if (nation == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "invalid-name"));
            return;
        }

        if (!OwnershipUtil.isAuthorized(player, nation)) {
            return;
        }

        nation.setIcon(itemStack);
        nation.setLastRenamedBy(player.getUniqueId());
        NationManager.getInstance().saveNation(nation);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                "nation-icon-set").replace("$1", nation.getName()));
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return commandSender instanceof Player;
    }
}
