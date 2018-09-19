package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class UtilTests {

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void setup() {
        new TownManager();
    }

    @Test
    public void cvItemShouldCreateItemStack() {
        CVItem cvItem = new CVItem(Material.COBBLESTONE,1, 100, "CustomCobble");
        ItemStack is = cvItem.createItemStack();
        assertEquals(Material.COBBLESTONE, is.getType());
    }

    @Test
    public void cvItemFromStringShouldSetValuesProperly() {
        CVItem cvItem = CVItem.createCVItemFromString("COBBLESTONE*2%50");
        assertTrue(cvItem.getMat() == Material.COBBLESTONE && cvItem.getChance() == .5 && cvItem.getQty() == 2);
    }
    @Test
    public void cvItemFromStringShouldSetValuesProperly2() {
        CVItem cvItem = CVItem.createCVItemFromString("dark oak log*64");
        assertTrue(cvItem.getMat() == Material.DARK_OAK_LOG && cvItem.getQty() == 64);
    }
    @Test
    public void itemGroupShouldReturnProperCVItems() {
        List<CVItem> cvItems = CVItem.createListFromString("g:glass*12");
        assertEquals(12, cvItems.get(0).getQty());
    }

    @Test
    public void addItemsShouldAddProperItems() {
        Inventory inventory = mock(Inventory.class);
        List<ItemStack> inventoryContents = new ArrayList<>();
        inventoryContents.add(new ItemStack(Material.COBBLESTONE, 6));
        inventoryContents.add(new ItemStack(Material.WOODEN_AXE));
        inventoryContents.add(new ItemStack(Material.STONE_SWORD));
        inventoryContents.add(null);
        inventoryContents.add(null);
        inventoryContents.add(null);
        inventoryContents.add(null);
        inventoryContents.add(null);
        inventoryContents.add(null);
        ListIterator<ItemStack> itemStacks = inventoryContents.listIterator();
        when(inventory.iterator()).thenReturn(itemStacks);
        ArgumentCaptor<ItemStack> itemStackArgumentCaptor = ArgumentCaptor.forClass(ItemStack.class);
        List<CVItem> tempList = new ArrayList<>();
        tempList.add(CVItem.createCVItemFromString("GRASS"));
        List<List<CVItem>> returnList = new ArrayList<>();
        returnList.add(tempList);
        Util.addItems(returnList, inventory);
        verify(inventory).addItem(itemStackArgumentCaptor.capture());
        List<ItemStack> stacks = itemStackArgumentCaptor.getAllValues();
        assertEquals(Material.GRASS, stacks.get(0).getType());
    }

    @Test
    public void placeHookShouldReportOwnerName() {
        CivilianManager.getInstance().createDefaultCivilian(TestUtil.player);
        TownTests.loadTownTypeHamlet();
        Location location = new Location(Bukkit.getWorld("world"), 0,0,0);
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        Town town = new Town("mytown", "hamlet", location, people, 100, 100,
                1, 1);
        TownManager.getInstance().addTown(town);
        PlaceHook placeHook = new PlaceHook();
        assertEquals("mytown", placeHook.onPlaceholderRequest(TestUtil.player, ""));
    }

    @Test
    public void placeHookShouldReportHighestPop() {
        CivilianManager.getInstance().createDefaultCivilian(TestUtil.player);
        TownTests.loadTownTypeHamlet();
        Location location = new Location(Bukkit.getWorld("world"), 0,0,0);
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "member");
        Town town = new Town("mytown1", "hamlet", location, people, 100, 100,
                3, 2);
        TownManager.getInstance().addTown(town);
        Town town1 = new Town("mytown2", "hamlet", location, people, 100, 100,
                8, 5);
        TownManager.getInstance().addTown(town1);
        PlaceHook placeHook = new PlaceHook();
        assertEquals("mytown2", placeHook.onPlaceholderRequest(TestUtil.player, ""));
    }
}
