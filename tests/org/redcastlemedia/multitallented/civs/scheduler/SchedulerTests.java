package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class SchedulerTests {
    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }
    /*@Test(expected = SuccessException.class)
    public void regionShouldUpkeep() {
        RegionsTests.loadRegionTypeCobble();
        RegionManager regionManager = RegionManager.getInstance();
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        HashMap<String, String> effects = new HashMap<>();
        Region region = new Region("cobble", people,
                TestUtil.blockUnique.getLocation(),
                RegionsTests.getRadii(), effects);
        regionManager.addRegion(region);

        RegionTickThread regionTickThread = new RegionTickThread();
        regionTickThread.run();
    }*/

    @Test
    public void checkReagentsBeforeProvidingUpkeep() {
        RegionsTests.loadRegionTypeCobble();
        RegionManager regionManager = RegionManager.getInstance();
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        HashMap<String, String> effects = new HashMap<>();
        Region region = new Region("cobble", people,
                TestUtil.blockUnique2.getLocation(),
                RegionsTests.getRadii(), effects);
        regionManager.addRegion(region);

        RegionTickThread regionTickThread = new RegionTickThread();
        regionTickThread.run();
    }
}
