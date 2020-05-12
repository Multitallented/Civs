package org.redcastlemedia.multitallented.civs.menus;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;

public class RegionListMenuTest extends TestUtil {

    private Civilian civilian;

    @Before
    public void setup() {
        MenuManager.clearData(TestUtil.player.getUniqueId());
        civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
    }

    @Test
    public void regionListMenuShouldProperlySetAction() {
        RegionsTests.createNewRegion("shelter", TestUtil.player.getUniqueId());
        Inventory inventory = MenuManager.openMenuFromString(civilian, "region-list");
        Map<ItemStack, Region> regionMap = (Map<ItemStack, Region>) MenuManager.getData(TestUtil.player.getUniqueId(), "regionMap");
        assertEquals(1, regionMap.values().size());
        assertEquals(inventory.getItem(9), regionMap.keySet().iterator().next());
    }
}
