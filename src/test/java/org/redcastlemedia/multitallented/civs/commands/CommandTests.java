package org.redcastlemedia.multitallented.civs.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.util.Constants;

public class CommandTests extends TestUtil {

    @Before
    public void setup() {
        RegionManager.getInstance().reload();
        TownManager.getInstance().reload();
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

    @Test
    public void playerShouldBeAbleToUpgradeTown() {
        RegionsTests.loadRegionTypeCobble();
        RegionsTests.createNewRegion("cobble");
        TownTests.loadTownTypeHamlet2();
        TownTests.loadTownTypeTribe2();
        Location location = TestUtil.player.getLocation();
        Town town = TownTests.loadTown("test", "hamlet2", location);
        Government government = new Government("DICTATORSHIP", GovernmentType.DICTATORSHIP,
                new HashSet<>(), null, new ArrayList<>(), true);
        TownTests.addGovernmentType(government);
        town.setGovernmentType("DICTATORSHIP");
        town.getRawPeople().put(TestUtil.player.getUniqueId(), Constants.OWNER);
        TownCommand townCommand = new TownCommand();
        String[] args = new String[3];
        args[0] = "town";
        args[1] = "test2";
        try {
            townCommand.runCommand(TestUtil.player, null, "town", args);
        } catch (SuccessException se) {

        }
        assertEquals("tribe", TownManager.getInstance().getTownAt(location).getType());
    }

    @Test
    public void playerShouldBeAbleToUpgradeTownGroup() {
        RegionsTests.loadRegionTypeCobbleGroup();
        RegionsTests.createNewRegion("town_hall");
        TownTests.loadTownTypeHamlet2();
        TownTests.loadTownTypeTribe2();
        Location location = TestUtil.player.getLocation();
        Town town = TownTests.loadTown("test", "hamlet2", location);
        Government government = new Government("DICTATORSHIP", GovernmentType.DICTATORSHIP,
                new HashSet<>(), null, new ArrayList<>(), true);
        TownTests.addGovernmentType(government);
        town.setGovernmentType("DICTATORSHIP");
        town.getRawPeople().put(TestUtil.player.getUniqueId(), Constants.OWNER);
        TownCommand townCommand = new TownCommand();
        String[] args = new String[3];
        args[0] = "town";
        args[1] = "test2";
        try {
            townCommand.runCommand(TestUtil.player, null, "town", args);
        } catch (SuccessException successException) {
            // Do nothing
        }
        assertEquals("tribe", TownManager.getInstance().getTownAt(location).getType());
    }
}
