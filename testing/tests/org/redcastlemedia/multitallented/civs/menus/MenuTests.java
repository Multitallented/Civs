package org.redcastlemedia.multitallented.civs.menus;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MenuTests {

    @Test
    public void inventorySizeTest() {
        assertEquals(63, Menu.getInventorySize(56));
        assertEquals(18, Menu.getInventorySize(10));
        assertEquals(9, Menu.getInventorySize(1));
    }
}
