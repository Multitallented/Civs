package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.menus.LanguageMenu;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class LanguageTests {

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

    @Test
    public void languageMenuShouldSetLocale() {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        HumanEntity humanEntity = mock(HumanEntity.class);
        when(event.getWhoClicked()).thenReturn(humanEntity);
        UUID uuid = new UUID(1, 6);
        when(humanEntity.getUniqueId()).thenReturn(uuid);

        ItemStack itemStack = mock(ItemStack.class);
        ItemMeta itemMeta = mock(ItemMeta.class);
        ArrayList<String> lore = new ArrayList<>();
        lore.add("es");
        when(itemMeta.getDisplayName()).thenReturn("Spanish");
        when(itemMeta.getLore()).thenReturn(lore);
        when(itemStack.getItemMeta()).thenReturn(itemMeta);
        when(itemStack.hasItemMeta()).thenReturn(true);
        when(event.getCurrentItem()).thenReturn(itemStack);
        Inventory inventory = mock(Inventory.class);
        when(event.getClickedInventory()).thenReturn(inventory);
        TestUtil.setLanguageEntry("es", "language-set","blah");
        CivilianManager civilianManager = CivilianManager.getInstance();
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        civilianManager.createDefaultCivilian(player);

        LanguageMenu languageMenu = new LanguageMenu();
        languageMenu.handleInteract(event);
        Civilian civilian = civilianManager.getCivilian(uuid);
        assertEquals("es", civilian.getLocale());
    }
}
