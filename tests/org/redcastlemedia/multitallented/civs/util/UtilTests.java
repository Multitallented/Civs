package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;

import static org.junit.Assert.*;

public class UtilTests {

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Test
    public void cvItemShouldCreateItemStack() {
        CVItem cvItem = new CVItem(Material.COBBLESTONE,1, -1, 100, "CustomCobble");
        ItemStack is = cvItem.createItemStack();
        assertEquals(Material.COBBLESTONE, is.getType());
    }

    @Test
    public void cvItemFromStringShouldSetValuesProperly() {
        CVItem cvItem = CVItem.createCVItemFromString("COBBLESTONE*2%50");
        assertTrue(cvItem.isWildDamage() && cvItem.getMat() == Material.COBBLESTONE && cvItem.getChance() == .5 && cvItem.getQty() == 2);
    }
    @Test
    public void cvItemFromStringShouldSetValuesProperly2() {
        CVItem cvItem = CVItem.createCVItemFromString("log 2.1*64");
        assertTrue(cvItem.getDamage() == 1 && cvItem.getMat() == Material.LOG_2 && cvItem.getQty() == 64);
    }
}
