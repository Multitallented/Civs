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
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "claim") @SuppressWarnings("unused")
public class ClaimMenu extends CustomMenu {

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        if (params.containsKey("claim")) {
            ChunkClaim chunkClaim = ChunkClaim.fromLocation(Region.idToLocation(params.get("claim")));
            data.put("claim", chunkClaim);
        } else {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            ChunkClaim chunkClaim = ChunkClaim.fromLocation(player.getLocation());
            data.put("claim", chunkClaim);
        }
        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("icon".equals(menuIcon.getKey())) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            ChunkClaim claim = (ChunkClaim) MenuManager.getData(civilian.getUuid(), "claim");
            if (player == null) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem;
            if (claim.getNation() != null) {
                cvItem = claim.getNation().getIconAsCVItem(civilian);
                cvItem.getLore().clear();
            } else {
                cvItem = CVItem.createCVItemFromString(ConfigManager.getInstance().getUnclaimedIcon());
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "claim-no-nation"));
            }
            cvItem.getLore().addAll(Util.textWrap(civilian,
                    LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    menuIcon.getDesc())));

            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("capture-claim".equals(actionString)) {
            // TODO
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}
