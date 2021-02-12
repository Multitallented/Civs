package org.redcastlemedia.multitallented.civs.menus;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;

import java.util.ArrayList;

public class MenuTests extends TestUtil {

    @Test
    public void inventorySizeTest() {
        assertEquals(54, MenuUtil.getInventorySize(56));
        assertEquals(18, MenuUtil.getInventorySize(10));
        assertEquals(9, MenuUtil.getInventorySize(1));
    }

    @Test
    public void parseIndexShouldWork() {
        ArrayList<Integer> indexes = MenuIcon.parseIndexArrayFromString("9-35");
        assertEquals(9, (int) indexes.get(0));
        assertEquals(35, (int) indexes.get(indexes.size() - 1));
    }
}
