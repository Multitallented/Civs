package org.redcastlemedia.multitallented.civs.commands;

import static org.junit.Assert.assertEquals;

import org.bukkit.Location;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

public class TaxCommandTests extends TestUtil {

    @Before
    public void setup() {
        RegionManager.getInstance().reload();
        TownManager.getInstance().reload();
    }

    @Test
    public void taxCommandShouldSetTax() {
        Town town = TownTests.loadTown("Summervale", "village", new Location(TestUtil.world, 0, 0, 0));
        town.getRawPeople().put(TestUtil.player.getUniqueId(), "owner");
        TaxCommand taxCommand = new TaxCommand();
        String[] args = {"tax", "Summervale", "50"};
        taxCommand.runCommand(TestUtil.player, null, "cv", args);
        assertEquals(50, town.getTaxes(), 0.1);
    }

    @Test
    public void taxCommandShouldNotSetInvalidTax() {
        Town town = TownTests.loadTown("Summervale", "village", new Location(TestUtil.world, 0, 0, 0));
        town.getRawPeople().put(TestUtil.player.getUniqueId(), "owner");
        TaxCommand taxCommand = new TaxCommand();
        String[] args = {"tax", "Summervale", "-20"};
        taxCommand.runCommand(TestUtil.player, null, "cv", args);
        assertEquals(0, town.getTaxes(), 0.1);
    }
}
