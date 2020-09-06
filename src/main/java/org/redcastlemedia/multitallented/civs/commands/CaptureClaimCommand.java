package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;

@CivsCommand(keys = {"captureclaim"}) @SuppressWarnings("unused")
public class CaptureClaimCommand extends CivCommand {

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("You must be in game to use this command");
            return true;
        }
        Player player = (Player) commandSender;
        Nation nation = NationManager.getInstance().getNationByPlayer(player.getUniqueId());
        if (nation == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "need-nation-to-claim"));
            return true;
        }
        ChunkClaim claim = ChunkClaim.fromLocation(player.getLocation());
        if (claim.getNation() != null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "claim-unclaimed-chunks"));
            return true;
        }
        CVItem cvItem = CVItem.createCVItemFromString(ConfigManager.getInstance().getClaimMaterial());
        ItemStack itemStack = cvItem.createItemStack();
        if (!player.getInventory().contains(itemStack)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "not-enough-claim-items"));
            return true;
        }
        String northKey = (claim.getX() + 1) + "," + claim.getZ();
        String westKey = claim.getX() + "," + (claim.getZ() + 1);
        String southKey = (claim.getX() - 1) + "," + claim.getZ();
        String eastKey = claim.getX() + "," + (claim.getZ() - 1);
        if (!(nation.getNationClaims().get(claim.getWorld().getUID()).containsKey(northKey) ||
                nation.getNationClaims().get(claim.getWorld().getUID()).containsKey(westKey) ||
                nation.getNationClaims().get(claim.getWorld().getUID()).containsKey(southKey) ||
                nation.getNationClaims().get(claim.getWorld().getUID()).containsKey(eastKey))) {

            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "nation-own-adjacent"));
            return true;
        }
        claim.setNation(nation);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                "chunk-claimed").replace("$1", claim.getId())
                .replace("$2", nation.getName()));
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return commandSender instanceof Player;
    }
}
