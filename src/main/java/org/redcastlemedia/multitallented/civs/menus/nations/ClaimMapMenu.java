package org.redcastlemedia.multitallented.civs.menus.nations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "claim-map") @SuppressWarnings("unused")
public class ClaimMapMenu extends CustomMenu {
    private static final String ORIENT = "orient";
    private static final String CLAIM_MAP = "claimMap";

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        Player player = Bukkit.getPlayer(civilian.getUuid());
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

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if ("orientation".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            BlockFace blockFace = (BlockFace) MenuManager.getData(civilian.getUuid(), ORIENT);
            String readableName = LocaleManager.getInstance().getTranslation(player, blockFace.name()); // TODO
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getName()).replace("$1", readableName));
        }

        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("view-claim".equals(actionString)) {
            // TODO
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
