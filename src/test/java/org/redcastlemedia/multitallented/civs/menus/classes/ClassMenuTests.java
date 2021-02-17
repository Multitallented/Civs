package org.redcastlemedia.multitallented.civs.menus.classes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;

public class ClassMenuTests extends TestUtil {

    private Civilian civilian;
    private CivClass civClass;

    @Before
    public void setup() {
        civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civClass = civilian.getCurrentClass();
        if (civClass == null) {
            String defaultClassName = ConfigManager.getInstance().getDefaultClass();
            civClass = new CivClass(UUID.randomUUID(), player.getUniqueId(), defaultClassName);
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

    @Test
    public void switchingClassShouldWork() {
        ClassType elemental = (ClassType) ItemManager.getInstance().getItemType("elemental");
        assertNotNull(elemental);
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType("default");
        assertNotNull(classType);
        Map<ItemStack, ClassType> classMap = new HashMap<>();
        classMap.put(elemental.createItemStack(), elemental);
        classMap.put(classType.createItemStack(), classType);
        Map<String, Object> data = new HashMap<>();
        data.put("classMap", classMap);
        List<ClassType> classes = new ArrayList<>();
        classes.add(classType);
        classes.add(elemental);
        data.put("classes", classes);
        MenuManager.setNewData(player.getUniqueId(), data);
        ClassTypeListMenu classTypeListMenu = new ClassTypeListMenu();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        CivClass civClass = new CivClass(new UUID(1, 7), player.getUniqueId(), "elemental");
        civilian.setCurrentClass(civClass);
        assertEquals("elemental", civilian.getCurrentClass().getType());
        classTypeListMenu.switchToNewClass(civilian, classType.createItemStack());
        assertEquals("default", civilian.getCurrentClass().getType());
    }
}
