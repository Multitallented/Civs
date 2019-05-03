package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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

    @Test(expected = SuccessException.class)
    public void playerShouldBeAbleToPort() {
        when(Bukkit.getServer().getScheduler()).thenThrow(new SuccessException());
        RegionsTests.loadRegionTypeShelter();
        Region region = RegionsTests.createNewRegion("shelter");
        region.getPeople().put(TestUtil.player.getUniqueId(), "member");
        PortCommand portCommand = new PortCommand();
        String[] args = new String[2];
        args[0] = "port";
        args[1] = region.getId();
        portCommand.runCommand(TestUtil.player, null, "cv", args);
    }
}
