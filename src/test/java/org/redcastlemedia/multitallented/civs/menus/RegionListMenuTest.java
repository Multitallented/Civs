package org.redcastlemedia.multitallented.civs.menus;

import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

public class RegionListMenuTest extends TestUtil {

    private Civilian civilian;

    @Before
    public void setup() {
        MenuManager.clearData(TestUtil.player.getUniqueId());
        civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
    }

    @Test
    public void regionListMenuShouldProperlySetAction() {
        MenuManager.openMenuFromString(civilian, "region-list");
    }
}
