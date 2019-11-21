package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.common.RecipeMenu;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RecipeMenuTests {
    private CustomMenu recipeMenu;

    private ArrayList<CVItem> subItems;
    private HashMap<Integer, List<CVItem>> cycleItems;
    private HashMap<Integer, CVItem> proxyInv;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void setup() {
        MenuManager.clearData(TestUtil.player.getUniqueId());
        this.recipeMenu = MenuManager.menus.get("recipe");
        this.subItems = new ArrayList<>();
        this.cycleItems = new HashMap<>();
        this.proxyInv = new HashMap<>();
    }

//    @Test
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
//    @Test
//    public void cycleItemsShouldAddAirIfQtyGreaterThanMaxStackSize() {
//        subItems.add(new CVItem(Material.STONE, 4));
//        subItems.add(new CVItem(Material.WOODEN_PICKAXE, 4));
//
//        RecipeMenu.createCycleItems(0, subItems, cycleItems, proxyInv);
//        assertEquals(Material.AIR, cycleItems.get(1).get(0).getMat());
//    }
//
//    @Test
//    public void cycleItemsShouldBreakDownItemStacksIntoMaxStackSize() {
//        subItems.add(new CVItem(Material.STONE, 4));
//        subItems.add(new CVItem(Material.WOODEN_PICKAXE, 4));
//
//        RecipeMenu.createCycleItems(0, subItems, cycleItems, proxyInv);
//        assertTrue(cycleItems.get(0).get(1).getMat() == Material.WOODEN_PICKAXE ||
//                cycleItems.get(0).get(0).getMat() == Material.WOODEN_PICKAXE);
//        assertEquals(1, cycleItems.get(0).get(1).getQty());
//    }

    @Test @Ignore
    public void regionReqsShouldNeverChange() {
        loadRegionTypeCouncilRoom();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("councilroom");
        UUID uuid = TestUtil.player.getUniqueId();
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        HashMap<String, String> params = new HashMap<>();
        params.put("recipe", "reqs");
        params.put("regionType", regionType.getProcessedName());
        recipeMenu.itemIndexes = new HashSet<>();
        recipeMenu.itemIndexes.add(new MenuIcon("icon", 0, "", "", ""));
        FileConfiguration config = new YamlConfiguration();
        config.set("index", "9-48");
        recipeMenu.itemIndexes.add(new MenuIcon("icon", 0, "", "", ""));
        recipeMenu.itemsPerPage.put("items", 48);
        Inventory inventory = recipeMenu.createMenu(civilian, params);
        assertEquals(Material.CHEST, regionType.getReqs().get(0).get(0).getMat());
        assertEquals(Material.OAK_SIGN, regionType.getReqs().get(8).get(0).getMat());
        assertEquals(Material.QUARTZ_BLOCK, regionType.getReqs().get(5).get(0).getMat());
        assertEquals(Material.SPRUCE_PLANKS, regionType.getReqs().get(5).get(2).getMat());
        assertEquals(Material.BOOKSHELF, regionType.getReqs().get(7).get(0).getMat());
        assertEquals(Material.RED_STAINED_GLASS_PANE, regionType.getReqs().get(2).get(3).getMat());
//        for (int i = 0; i < 17; i++) {
//            if (inventory.getItem(i) != null) {
//                System.out.println(inventory.getItem(i).getType().name());
//            }
//        }
        assertEquals(Material.OAK_SIGN, inventory.getItem(0).getType());
        assertEquals(Material.CHEST, inventory.getItem(9).getType());
        assertEquals(Material.BOOKSHELF, inventory.getItem(22).getType());
        assertEquals(Material.OAK_SIGN, inventory.getItem(23).getType());
    }

    @Test
    public void radiusCheckShouldNotMutate() {
        loadRegionTypeCouncilRoom();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("councilroom");
        int[] radii = new int[6];
        radii[0] = 5;
        radii[1] = 5;
        radii[2] = 6;
        radii[3] = 5;
        radii[4] = 5;
        radii[5] = 5;
        int[] newRadii = Region.radiusCheck(radii,regionType);
        assertEquals(0, newRadii.length);
        assertEquals(6, radii.length);
    }

    public static void loadRegionTypeCouncilRoom() {
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "OAK_SIGN");
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
