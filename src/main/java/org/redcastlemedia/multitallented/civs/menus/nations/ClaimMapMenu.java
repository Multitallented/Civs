package org.redcastlemedia.multitallented.civs.menus.nations;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "claim-map") @SuppressWarnings("unused")
public class ClaimMapMenu extends CustomMenu {
    private static final String ORIENT = "orient";
    private static final String CLAIM_MAP = "claimMap";

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        Player player = Bukkit.getPlayer(civilian.getUuid());
        data.put(Constants.CLAIM, ChunkClaim.fromLocation(player.getLocation()));
        if (params.containsKey(ORIENT)) {
            BlockFace blockFace = BlockFace.valueOf(params.get(ORIENT));
            if (params.containsKey("rotate")) {
                blockFace = getRotatedFace(blockFace);
            }
            data.put(ORIENT, blockFace);
        } else {
            BlockFace facing = Util.getFacing(player.getLocation().getYaw(), false);
            data.put(ORIENT, facing);
        }
        Map<ItemStack, ChunkClaim> claimMap = new HashMap<>();
        data.put(CLAIM_MAP, claimMap);
        return data;
    }

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if ("icon".equals(menuIcon.getKey())) {
            ChunkClaim chunkClaim = (ChunkClaim) MenuManager.getData(civilian.getUuid(), Constants.CLAIM);
            return convertClaimToIcon(civilian, menuIcon, count, player, chunkClaim);
        } else if ("orientation".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            BlockFace blockFace = (BlockFace) MenuManager.getData(civilian.getUuid(), ORIENT);
            String readableName = LocaleManager.getInstance().getTranslation(player, blockFace.name());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getName()).replace("$1", readableName));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("claims".equals(menuIcon.getKey())) {
            ChunkClaim chunkClaim = (ChunkClaim) MenuManager.getData(civilian.getUuid(), Constants.CLAIM);
            int totalItems = itemsPerPage.get("claims");
            double width = Math.min(9, totalItems);
            double height = (int) Math.ceil(Math.min(1.0, totalItems) / 9.0);
            int centerX = (int) Math.floor(width / 2.0);
            int centerY = (int) Math.floor(height / 2.0);
            int horizontalDiff = (int) Math.floor(count % width) - centerX;
            int verticalDiff = (int) Math.floor(count % height) - centerY;
            ChunkClaim currentClaim = ChunkClaim.fromXZ(chunkClaim.getX() + horizontalDiff,
                    chunkClaim.getZ() + verticalDiff, chunkClaim.getWorld());
            return convertClaimToIcon(civilian, menuIcon, count, player, currentClaim);
        }

        return super.createItemStack(civilian, menuIcon, count);
    }

    private ItemStack convertClaimToIcon(Civilian civilian, MenuIcon menuIcon, int count, Player player, ChunkClaim currentClaim) {
        Nation nation = currentClaim.getNation();
        if (nation != null) {
            ItemStack itemStack = nation.getIcon().clone();
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(nation.getName());
            meta.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc())));
            itemStack.setItemMeta(meta);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    "claim-no-nation"));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
    }

    @Override @SuppressWarnings("unchecked")
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("view-claim".equals(actionString)) {
            Map<ItemStack, ChunkClaim> claimMap = (Map<ItemStack, ChunkClaim>) MenuManager.getData(civilian.getUuid(), CLAIM_MAP);
            ChunkClaim claim = claimMap.get(itemStack);
            MenuManager.openMenuFromString(civilian,"claim?claim=" + claim.toString());
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }

    private BlockFace getRotatedFace(BlockFace blockFace) {
        switch (blockFace) {
            case NORTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            default:
                return BlockFace.NORTH;
        }
    }
}
