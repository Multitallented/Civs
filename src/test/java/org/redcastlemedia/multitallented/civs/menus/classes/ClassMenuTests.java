package org.redcastlemedia.multitallented.civs.menus.classes;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;

public class ClassMenuTests extends TestUtil {

    private Civilian civilian;
    private CivClass civClass;

    @Before
    public void setup() {
        civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civClass = civilian.getCurrentClass();
        if (civClass == null) {
            String defaultClassName = ConfigManager.getInstance().getDefaultClass();
            civClass = new CivClass(0, player.getUniqueId(), defaultClassName);
        }
        civClass.resetSpellSlotOrder();
    }

    @Test
    public void spellsShouldSwapProperly() {
        ClassMenu.swapSpellSlots(civClass, 1, 4);
        assertEquals(4, (int) civClass.getSpellSlotOrder().get(1));
        assertEquals(1, (int) civClass.getSpellSlotOrder().get(4));
        ClassMenu.swapSpellSlots(civClass, 1, 4);
        assertEquals(1, (int) civClass.getSpellSlotOrder().get(1));
    }
}
