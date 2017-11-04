package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CivilianTests {


    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Test
    public void localeTestShouldReturnProperLanguageString() {
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = new Civilian(TestUtil.player.getUniqueId(), "es", new ArrayList<CivItem>(), null, new HashMap<CivItem, Integer>(),
                0, 0,0,0,0, 0);

        assertEquals("No se encontró ningún tipo de región",
                localeManager.getTranslation(civilian.getLocale(), "no-region-type-found"));
    }

    @Test(expected = SuccessException.class)
    public void inventoryClickOnUnownedCivItemShouldBeCancelled() {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        ItemStack is = TestUtil.createUniqueItemStack(Material.CHEST, "Civs Cobble");
        when(event.getCurrentItem()).thenReturn(is);
        Inventory inventory = mock(Inventory.class);
        when(inventory.getTitle()).thenReturn("");
        when(event.getClickedInventory()).thenReturn(inventory);
        Player player = mock(Player.class);
        UUID uuid = new UUID(1,8);
        when(player.getUniqueId()).thenReturn(uuid);
        when(event.getWhoClicked()).thenReturn(player);
        doThrow(new SuccessException()).when(event).setCancelled(true);

        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianClickItem(event);
        fail("set cancelled not called");
    }

    @Test
    public void civilianShouldNotBeOverMaxItems() {
        ItemManager itemManager = ItemManager.getInstance();
        RegionsTests.loadRegionTypeCobble();
        CivilianManager civilianManager = CivilianManager.getInstance();
        civilianManager.loadCivilian(TestUtil.player);
        Civilian civilian = civilianManager.getCivilian(TestUtil.player.getUniqueId());
        assertFalse(civilian.isAtMax(itemManager.getItemType("cobble")));
    }
}
