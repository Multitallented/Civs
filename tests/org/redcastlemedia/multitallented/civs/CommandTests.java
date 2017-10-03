package org.redcastlemedia.multitallented.civs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.commands.MenuCommand;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CommandTests {

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void onBefore() {

    }
//
//    @Test
//    public void menuCommandShouldAddItemStack() {
//        Inventory inventory = Bukkit.createInventory(null, 9, "something");
//        ArgumentCaptor<ItemStack> itemStackArgumentCaptor = ArgumentCaptor.forClass(ItemStack.class);
//        MenuCommand menuCommand = new MenuCommand();
//        menuCommand.runCommand(TestUtil.player, mock(Command.class), "cv", new String[0]);
//        verify(inventory).setItem(Matchers.anyInt(), itemStackArgumentCaptor.capture());
//        List<ItemStack> stacks = itemStackArgumentCaptor.getAllValues();
//        assertEquals(Material.GRASS, stacks.get(0).getType());
//    }

    @Test(expected = SuccessException.class)
    public void playerShouldNotBeAbleToDropItem() {
        PlayerDropItemEvent playerDropItemEvent = mock(PlayerDropItemEvent.class);
        Item item = mock(Item.class);
        ItemStack itemStack = mock(ItemStack.class);
        ItemMeta itemMeta = mock(ItemMeta.class);
        when(itemMeta.getDisplayName()).thenReturn("Civs Cobble");
        when(itemStack.getItemMeta()).thenReturn(itemMeta);
        when(item.getItemStack()).thenReturn(itemStack);
        when(playerDropItemEvent.getItemDrop()).thenReturn(item);
        doThrow(new SuccessException()).when(item).remove();
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianDropItem(playerDropItemEvent);
    }
}
