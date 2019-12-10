package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Location;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.commands.MenuCommand;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

public class SelectTownMenuTests extends TestUtil {

    @Before
    public void setup() {
        MenuManager.getInstance().reload();
        TownManager.getInstance().reload();
    }

    @Test
    public void townMenuShouldNotNull() {
        GovernmentManager.getInstance().reload();
        TownTests.loadTown("test", "settlement", new Location(TestUtil.world, 0, 0, 0));
        MenuCommand menuCommand = new MenuCommand();
        String[] args = {"menu", "town?selectedTown=test&preserveData=true"};
        menuCommand.runCommand(TestUtil.player, null, "cv", args);
    }
}
