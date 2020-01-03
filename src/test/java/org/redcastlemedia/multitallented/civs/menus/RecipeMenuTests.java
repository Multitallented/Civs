package org.redcastlemedia.multitallented.civs.menus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionPoints;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

public class RecipeMenuTests extends TestUtil {

    @Before
    public void setup() {
        MenuManager.clearData(TestUtil.player.getUniqueId());
    }


    // TODO make sure the primary group items link to the primary group

//    @Test TODO
//    public void cycleItemsShouldHaveLengthOf2() {
//        subItems.add(new CVItem(Material.STONE, 4));
//        subItems.add(new CVItem(Material.CHEST, 2));
//
//
//        RecipeMenu.createCycleItems(0, subItems, cycleItems, proxyInv);
//
//        assertEquals(2, cycleItems.get(0).size());
//    }
//
//    @Test TODO
//    public void cycleItemsShouldAddAirIfQtyGreaterThanMaxStackSize() {
//        subItems.add(new CVItem(Material.STONE, 4));
//        subItems.add(new CVItem(Material.WOODEN_PICKAXE, 4));
//
//        RecipeMenu.createCycleItems(0, subItems, cycleItems, proxyInv);
//        assertEquals(Material.AIR, cycleItems.get(1).get(0).getMat());
//    }
//
//    @Test TODO
//    public void cycleItemsShouldBreakDownItemStacksIntoMaxStackSize() {
//        subItems.add(new CVItem(Material.STONE, 4));
//        subItems.add(new CVItem(Material.WOODEN_PICKAXE, 4));
//
//        RecipeMenu.createCycleItems(0, subItems, cycleItems, proxyInv);
//        assertTrue(cycleItems.get(0).get(1).getMat() == Material.WOODEN_PICKAXE ||
//                cycleItems.get(0).get(0).getMat() == Material.WOODEN_PICKAXE);
//        assertEquals(1, cycleItems.get(0).get(1).getQty());
//    }

    @Test
    public void regionReqsShouldNeverChange() {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("council_room");
        HashMap<String, String> params = new HashMap<>();
        params.put("recipe", "reqs");
        params.put("regionType", regionType.getProcessedName());
        Inventory inventory = MenuManager.getInstance().openMenu(TestUtil.player, "recipe", params);
        assertEquals(Material.CHEST, regionType.getReqs().get(0).get(0).getMat());
        assertEquals(Material.QUARTZ_STAIRS, regionType.getReqs().get(3).get(0).getMat());
        assertEquals(Material.JUNGLE_PLANKS, regionType.getReqs().get(8).get(2).getMat());
        assertEquals(Material.BOOKSHELF, regionType.getReqs().get(5).get(0).getMat());
        assertEquals(Material.RED_STAINED_GLASS_PANE, regionType.getReqs().get(2).get(3).getMat());
        assertEquals(Material.CHEST, inventory.getItem(9).getType());
    }

    @Test
    public void radiusCheckShouldNotMutate() {
        loadRegionTypeCouncilRoom();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("councilroom");
        RegionPoints radii = new RegionPoints(5, 6, 5, 5, 5, 5);
        RegionPoints newRadii = Region.radiusCheck(radii,regionType);
        assertFalse(newRadii.isValid());
        assertTrue(radii.isValid());
    }

    public static void loadRegionTypeCouncilRoom() {
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "SIGN");
        config.set("type", "region");
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("CHEST*4");
        reqs.add("g:door*2");
        reqs.add("g:window*16");
        reqs.add("g:roof*121");
        reqs.add("g:secondary*60");
        reqs.add("g:primary*350");
        reqs.add("g:stairs*4");
        reqs.add("BOOKSHELF*8");
        reqs.add("g:sign*8");
        config.set("build-reqs", reqs);
        config.set("build-radius",5);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_break");
        effects.add("block_build");
        effects.add("block_liquid");
        effects.add("block_fire");
        effects.add("door_use");
        effects.add("chest_use");
        effects.add("port");
        config.set("period", "daily");
        config.set("upkeep.0.power-output", 50);
        config.set("description.en", "The central structure for a town");
        ItemManager.getInstance().loadRegionType(config, "councilroom");
    }
}
