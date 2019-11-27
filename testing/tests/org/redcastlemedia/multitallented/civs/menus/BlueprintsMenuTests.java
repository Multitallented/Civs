package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.InventoryImpl;
import org.redcastlemedia.multitallented.civs.ItemMetaImpl;
import org.redcastlemedia.multitallented.civs.ItemStackImpl;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlueprintsMenuTests {
    private CustomMenu blueprintsMenu;
    private Civilian civilian;
    private InventoryImpl inventory;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void setup() {
        MenuManager.clearData(TestUtil.player.getUniqueId());
        loadRegionTypeShelter();
        blueprintsMenu = MenuManager.menus.get("blueprints");
        this.inventory = new InventoryImpl();
        this.inventory.setTitle("CivsRegionStash");

        this.civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civilian.getStashItems().put("shelter", 1);
    }

    @Test
    public void stashRegionItemsShouldBeEmpty() {
        blueprintsMenu.createMenu(this.civilian, new HashMap<>());
        blueprintsMenu.onCloseMenu(this.civilian, this.inventory);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertEquals(0, civilian.getStashItems().size());
    }

    @Test
    public void stashItemsShouldSaveShelter() {
        RegionsTests.loadRegionTypeShelter();
        ItemStack itemStack = TestUtil.createUniqueItemStack(Material.CHEST, "Civs Shelter");
        blueprintsMenu.createMenu(this.civilian, new HashMap<>());
        inventory.setItem(0,itemStack);
        blueprintsMenu.onCloseMenu(this.civilian, this.inventory);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertEquals(1, civilian.getStashItems().size());
    }

    @Test
    public void reloggingShouldNotReAddTheItem() {
        loadRegionTypeShelter();
        RegionsTests.createNewRegion("shelter", TestUtil.player.getUniqueId());
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civilian.setStashItems(new HashMap<>());
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianJoin(new PlayerJoinEvent(TestUtil.player, ""));
        assertEquals(0, civilian.getStashItems().size());
    }

    @Test
    public void menuShouldNotDupeItems() {
        RegionsTests.loadRegionTypeCobble();
        this.blueprintsMenu.createMenu(civilian, new HashMap<>());
        ItemStackImpl itemStack = new ItemStackImpl(Material.CHEST, 1);
        itemStack.getItemMeta().setDisplayName("Civs Shelter");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(TestUtil.player.getUniqueId().toString());
        lore.add("Civs Shelter");
        itemStack.getItemMeta().setLore(lore);
        ItemStackImpl itemStack2 = new ItemStackImpl(Material.CHEST, 2);
        itemStack2.getItemMeta().setDisplayName("Civs Shelter");
        itemStack2.getItemMeta().setLore(lore);
        inventory.setItem(0, itemStack);
        inventory.setItem(1, itemStack2);
        this.blueprintsMenu.onCloseMenu(this.civilian, this.inventory);
        assertEquals(3, (int) this.civilian.getStashItems().get("shelter"));
    }

    @Test
    public void itemShouldBeCivsItem() {
        RegionsTests.loadRegionTypeCobble();
        ItemStackImpl itemStack = new ItemStackImpl(Material.CHEST, 1);
        ArrayList<String> lore = new ArrayList<>();
        lore.add("something");
        lore.add("Civs Cobble");
        ItemMetaImpl itemMeta = new ItemMetaImpl("Civs Cobble", lore);
        itemStack.setItemMeta(itemMeta);
        assertTrue(CivItem.isCivsItem(itemStack));
    }

    @Test
    public void itemShouldNotBeCivsItem() {
        RegionsTests.loadRegionTypeCobble();
        ItemStackImpl itemStack = new ItemStackImpl(Material.CHEST, 1);
        ArrayList<String> lore = new ArrayList<>();
        ItemMetaImpl itemMeta = new ItemMetaImpl("Civs Cobble", lore);
        itemStack.setItemMeta(itemMeta);
        assertFalse(CivItem.isCivsItem(itemStack));
    }

    private void loadRegionTypeShelter() {
        FileConfiguration fileConfiguration = new YamlConfiguration();
        fileConfiguration.set("icon", "CHEST");
        fileConfiguration.set("min", 1);
        fileConfiguration.set("max", 1);
        fileConfiguration.set("type", "region");
        fileConfiguration.set("is-in-shop", false);
        ItemManager.getInstance().loadRegionType(fileConfiguration, "shelter");
    }
}
