package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test(expected = SuccessException.class)
    public void messageShouldBeSentWhenEnterTown() {
        TownTests.loadTownTypeHamlet();
        World world = mock(World.class);
        Town town = TownTests.loadTown("hamlet", new Location(world, 0, 0, 0));
        CommonScheduler commonScheduler = new CommonScheduler();
        Player player = mock(Player.class);
        UUID uuid = new UUID(1, 8);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getLocation()).thenReturn(new Location(world, 1000,0,0));
        doThrow(new SuccessException()).when(player).sendMessage(Matchers.anyString());
        commonScheduler.playerInTown(player);

        when(player.getLocation()).thenReturn(new Location(world, 0,0,0));
        commonScheduler.playerInTown(player);
    }
}
