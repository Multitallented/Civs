package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.redcastlemedia.multitallented.civs.TestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

    @Test
    public void cvItemShouldCreateItemStack() {
        CVItem cvItem = new CVItem(Material.COBBLESTONE,1, -1, 100, "CustomCobble");
        ItemStack is = cvItem.createItemStack();
        assertEquals(Material.COBBLESTONE, is.getType());
    }

    @Test
    public void cvItemFromStringShouldSetValuesProperly() {
        CVItem cvItem = CVItem.createCVItemFromString("COBBLESTONE*2%50");
        assertTrue(cvItem.isWildDamage() && cvItem.getMat() == Material.COBBLESTONE && cvItem.getChance() == .5 && cvItem.getQty() == 2);
    }
    @Test
    public void cvItemFromStringShouldSetValuesProperly2() {
        CVItem cvItem = CVItem.createCVItemFromString("dark oak log.1*64");
        assertTrue(cvItem.getDamage() == 1 && cvItem.getMat() == Material.DARK_OAK_LOG && cvItem.getQty() == 64);
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
}
