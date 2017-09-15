package org.redcastlemedia.multitallented.civs;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.redcastlemedia.multitallented.civs.commands.MenuCommand;

import java.util.List;

import static org.junit.Assert.*;
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

    @Test(expected = SuccessException.class)
    public void menuCommandShouldOpenMenu() {
        Player player = mock(Player.class);
        when(player.openInventory(Matchers.any(Inventory.class))).thenThrow(SuccessException.class);
        MenuCommand menuCommand = new MenuCommand();
        menuCommand.runCommand(player, mock(Command.class), "cv", new String[0]);
    }

    @Test
    public void menuCommandShouldAddItemStack() {
        Inventory inventory = Bukkit.createInventory(null, 9, "something");
        Player player = mock(Player.class);
        ArgumentCaptor<ItemStack> itemStackArgumentCaptor = ArgumentCaptor.forClass(ItemStack.class);
        MenuCommand menuCommand = new MenuCommand();
        menuCommand.runCommand(player, mock(Command.class), "cv", new String[0]);
        verify(inventory).setItem(Matchers.anyInt(), itemStackArgumentCaptor.capture());
        List<ItemStack> stacks = itemStackArgumentCaptor.getAllValues();
        assertEquals(Material.MAP, stacks.get(0).getType());
    }


}
