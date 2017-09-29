package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;

public class SchedulerTests {
    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }
    @Test
    public void test() {
        //TODO finish this section
    }
}
