package org.redcastlemedia.multitallented.civs.menus;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

public class GovMenuTests extends TestUtil {
    @Before
    public void setup() {
        MenuManager.getInstance().clearOpenMenus();
        TownManager.getInstance().reload();
    }

    @Test @SuppressWarnings("unchecked")
    public void govMenuShouldPopulateBasedOnTownGovs() {
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        Town town = TownTests.loadTown("test", "hamlet", new Location(TestUtil.world, 0, 0,0));
        TownManager.getInstance().setTownPower(town, 499);
        Town town2 = TownTests.loadTown("test2", "village", new Location(TestUtil.world, 1000, 0,0));
        town2.setGovernmentType(GovernmentType.CAPITALISM.name());
        TownManager.getInstance().setTownPower(town2, 500);
        Town town3 = TownTests.loadTown("test3", "settlement", new Location(TestUtil.world, 2000, 0,0));
        town3.setGovernmentType(GovernmentType.DEMOCRACY.name());
        TownManager.getInstance().setTownPower(town3, 498);
        CustomMenu govListMenu = MenuManager.menus.get("gov-list");
        Map<String, String> params = new HashMap<>();
        params.put("leaderboard", "true");
        Map<String, Object> data = govListMenu.createData(civilian, params);
        HashMap<String, Integer> govPower = (HashMap<String, Integer>) data.get("govPower");
        assertEquals(3, govPower.size());
        assertEquals(GovernmentType.CAPITALISM.name().toLowerCase(), govPower.keySet().iterator().next());
        assertEquals(3, ((List<String>) data.get("govList")).size());
        assertEquals(GovernmentType.CAPITALISM.name(), ((List<String>) data.get("govList")).get(0));
        assertEquals(GovernmentType.DEMOCRACY.name(), ((List<String>) data.get("govList")).get(2));
    }
}
