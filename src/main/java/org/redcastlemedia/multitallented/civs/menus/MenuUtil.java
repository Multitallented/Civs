package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class MenuUtil {
    private MenuUtil() {

    }
    public static void sanitizeItem(ItemStack item) {
        Material mat = item.getType();
        if (mat == Material.RED_BED || mat == Material.BLACK_BED || mat == Material.BLUE_BED
                || mat == Material.BROWN_BED || mat == Material.CYAN_BED
                || mat == Material.GRAY_BED || mat == Material.GREEN_BED || mat == Material.LIGHT_BLUE_BED
                || mat == Material.LIGHT_GRAY_BED || mat == Material.LIME_BED || mat == Material.MAGENTA_BED
                || mat == Material.ORANGE_BED || mat == Material.PINK_BED || mat == Material.PURPLE_BED
                || mat == Material.WHITE_BED || mat == Material.YELLOW_BED) {
            divideByTwo(item);
        } else if (mat == Material.OAK_DOOR || mat == Material.IRON_DOOR || mat == Material.DARK_OAK_DOOR
                || mat == Material.BIRCH_DOOR || mat == Material.ACACIA_DOOR || mat == Material.SPRUCE_DOOR
                || mat == Material.JUNGLE_DOOR) {
            divideByTwo(item);
        } else if (mat == Material.REDSTONE_WIRE) {
            item.setType(Material.REDSTONE);
//        } else if (mat == Material.WALL_SIGN) {
//            item.setType(Material.SIGN);
        } else if (mat == Material.WATER) {
            item.setType(Material.WATER_BUCKET);
        } else if (mat == Material.LAVA) {
            item.setType(Material.LAVA_BUCKET);
        } else if (mat == Material.POTATOES) {
            item.setType(Material.POTATO);
        }
    }
    private static void divideByTwo(ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(Math.round(item.getAmount() / 2));
        }
    }

    public static int getInventorySize(int count) {
        int size = 9;
        if (count > size) {
            size = count + 9 - (count % 9);
            if (count % 9 == 0) {
                size -= 9;
            }
        }
        return Math.min(size, 54);
    }
}
