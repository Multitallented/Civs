package org.redcastlemedia.multitallented.civs.menus.nations;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "claim") @SuppressWarnings("unused")
public class ClaimMenu extends CustomMenu {

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        ChunkClaim chunkClaim = null;
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (params.containsKey(Constants.CLAIM)) {
            chunkClaim = ChunkClaim.fromString(params.get(Constants.CLAIM));
            data.put(Constants.CLAIM, chunkClaim);
        } else {
            chunkClaim = ChunkClaim.fromLocation(player.getLocation());
            data.put(Constants.CLAIM, chunkClaim);
        }
        if (chunkClaim != null) {
            data.put("captureTime", chunkClaim.getTimeUntilCapture(player));
        } else {
            data.put("captureTime", LocaleManager.getInstance().getTranslation(player, "capture-not-started"));
        }
        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        ChunkClaim claim = (ChunkClaim) MenuManager.getData(civilian.getUuid(), Constants.CLAIM);
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem;
            if (claim != null && claim.getNation() != null) {
                cvItem = claim.getNation().getIconAsCVItem();
                cvItem.getLore().clear();
            } else {
                cvItem = menuIcon.createCVItem(player, count);
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                        "claim-no-nation"));
            }
            cvItem.getLore().addAll(Util.textWrap(civilian,
                    LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc())));

            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("timer-ok".equals(menuIcon.getKey())) {
            if (claim.getLastEnter() != -1) {
                return new ItemStack(Material.AIR);
            }
        } else if ("timer-bad".equals(menuIcon.getKey())) {
            if (claim.getLastEnter() < 0) {
                return new ItemStack(Material.AIR);
            }
        } else if ("capture-claim".equals(menuIcon.getKey())) {
            Nation nation = NationManager.getInstance().getNationByPlayer(civilian.getUuid());
            if (nation == null || claim.getNation() != null ||
                    !NationManager.getInstance().nationHasAdjacentClaim(claim, nation)) {
                return new ItemStack(Material.AIR);
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("capture-claim".equals(actionString)) {
            ChunkClaim claim = (ChunkClaim) MenuManager.getData(civilian.getUuid(), Constants.CLAIM);
            Player player = Bukkit.getPlayer(civilian.getUuid());
            player.performCommand("cv captureclaim " + claim.getX() + " " + claim.getZ());
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}