package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RecipeMenuTests {

    private HashMap<Material, Integer> subItems;
    private HashMap<Integer, List<CVItem>> cycleItems;

    @Before
    public void setup() {
        this.subItems = new HashMap<>();
        this.cycleItems = new HashMap<>();
    }

//    @Test
//    public void cycleItemsShouldHaveLengthOf2() {
//        subItems.put(Material.STONE, 4);
//        subItems.put(Material.CHEST, 2);
//
//
//        RecipeMenu.createCycleItems(0, subItems, cycleItems);
//
//        assertEquals(2, cycleItems.get(0).size());
//    }

    @Test
    public void cycleItemsShouldAddAirIfQtyGreaterThanMaxStackSize() {
        subItems.put(Material.STONE, 4);
        subItems.put(Material.WOODEN_PICKAXE, 4);

        RecipeMenu.createCycleItems(0, subItems, cycleItems);
        assertEquals(Material.AIR, cycleItems.get(1).get(0).getMat());
    }

    @Test
    public void cycleItemsShouldBreakDownItemStacksIntoMaxStackSize() {
        subItems.put(Material.STONE, 4);
        subItems.put(Material.WOODEN_PICKAXE, 4);

        RecipeMenu.createCycleItems(0, subItems, cycleItems);
        assertEquals(Material.WOODEN_PICKAXE, cycleItems.get(0).get(1).getMat());
        assertEquals(1, cycleItems.get(0).get(1).getQty());
    }
}
