package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import static org.junit.Assert.assertEquals;

public class CommandTests {

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void setup() {
        new RegionManager();
        new TownManager();
    }

    @Test
    public void newTownShouldStartWithHousing() {
        TownTests.loadTownTypeTribe();
        RegionsTests.loadRegionTypeCobble();
        RegionsTests.createNewRegion("cobble");
        TownType townType = (TownType) ItemManager.getInstance().getItemType("tribe");

        Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);

        TownCommand townCommand = new TownCommand();


        int housing = townCommand.getHousingCount(location, townType);
        assertEquals(2, housing);
    }
}
