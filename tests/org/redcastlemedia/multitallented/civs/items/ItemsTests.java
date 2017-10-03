package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Bukkit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;

public class ItemsTests {
    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void onBefore() {

    }

    @Test
    public void test() {
        //TODO start a test here
    }
}
