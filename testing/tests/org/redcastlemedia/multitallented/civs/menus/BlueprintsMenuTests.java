package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.InventoryImpl;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlueprintsMenuTests {
    private BlueprintsMenu blueprintsMenu;
    private InventoryImpl inventory;
    private InventoryCloseEvent event;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void setup() {
        loadRegionTypeShelter();
        blueprintsMenu = new BlueprintsMenu();
        this.inventory = new InventoryImpl();
        this.inventory.setTitle("CivsRegionStash");
        InventoryView inventoryView = mock(InventoryView.class);
        when(inventoryView.getTopInventory()).thenReturn(inventory);
        when(inventoryView.getPlayer()).thenReturn(TestUtil.player);

        this.event = new InventoryCloseEvent(inventoryView);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civilian.getStashItems().add(ItemManager.getInstance().getItemType("cobble"));
    }

    @Test
    public void stashRegionItemsShouldBeEmpty() {
        blueprintsMenu.onInventoryClose(event);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertEquals(1, civilian.getStashItems().size());
    }

    @Test
    @Ignore //fix so that it doesn't use item meta
    public void stashItemsShouldSaveShelter() {
        CVItem cvItem = CVItem.createCVItemFromString("CHEST");
        cvItem.setDisplayName("Civs Cobble");
        inventory.setItem(0,cvItem.createItemStack());
        blueprintsMenu.onInventoryClose(event);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertEquals(2, civilian.getStashItems().size());
    }

    private void loadRegionTypeShelter() {
        FileConfiguration fileConfiguration = new YamlConfiguration();
        fileConfiguration.set("name", "cobble");
        fileConfiguration.set("icon", "CHEST");
        fileConfiguration.set("min", 1);
        fileConfiguration.set("max", 1);
        fileConfiguration.set("type", "region");
        fileConfiguration.set("is-in-shop", false);
        ItemManager.getInstance().loadRegionType(fileConfiguration);
    }
}
