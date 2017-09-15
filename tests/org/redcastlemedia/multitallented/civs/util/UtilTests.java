package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;

import static org.junit.Assert.assertEquals;

public class UtilTests {

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Test
    public void cvItemShouldCreateItemStack() {
        CVItem cvItem = new CVItem(Material.COBBLESTONE, 4,1, -1, 100, "CustomCobble");
        ItemStack is = cvItem.createItemStack();
        assertEquals(Material.COBBLESTONE, is.getType());
    }
}
