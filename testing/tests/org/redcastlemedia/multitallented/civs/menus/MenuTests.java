package org.redcastlemedia.multitallented.civs.menus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.List;

public class MenuTests {

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Test
    public void inventorySizeTest() {
        assertEquals(54, Menu.getInventorySize(56));
        assertEquals(18, Menu.getInventorySize(10));
        assertEquals(9, Menu.getInventorySize(1));
    }

    @Test
    public void cvItemShouldKeepGroup() {
        RecipeMenuTests.loadRegionTypeCouncilRoom();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("councilroom");
        List<List<CVItem>> reqs = regionType.getReqs();
        assertNotNull(reqs.get(1).get(0).getGroup());
        Inventory inventory = RecipeMenu.createMenuCVItem(reqs, TestUtil.player.getUniqueId(), regionType.createItemStack());
        assertEquals(1, inventory.getItem(11).getItemMeta().getLore().size());
        assertEquals("g:sign", inventory.getItem(11).getItemMeta().getLore().get(0));
        Menu.GUI gui = Menu.getGuis().get(TestUtil.player.getUniqueId());
        gui.advanceItemPositions();
        assertEquals("g:sign", inventory.getItem(11).getItemMeta().getLore().get(0));
    }
}
