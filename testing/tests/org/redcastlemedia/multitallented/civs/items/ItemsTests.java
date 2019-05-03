package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.civilians.CivilianTests;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

import java.util.ArrayList;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class ItemsTests {
    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void onBefore() {
        new TownManager();
        new RegionManager();
    }

    @Test
    public void newPlayerShouldRecieveAShelterItem() {
        loadRegionTypeShelter();
        CivilianTests.skipLoadingFiles();
        PlayerJoinEvent event = new PlayerJoinEvent(TestUtil.player, "blah");
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianJoin(event);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        boolean hasShelter = false;
        for (String currentName : civilian.getStashItems().keySet()) {
            if (currentName.equalsIgnoreCase("shelter")) {
                hasShelter = true;
            }
        }
        assertTrue(hasShelter);
    }

    @Test
    public void newPlayerShouldNotReceiveACityHall() {
        loadSpellTypeBackflip();
        loadRegionTypeShelter();
        loadRegionTypeCityHall();
        PlayerJoinEvent event = new PlayerJoinEvent(TestUtil.player, "blah");
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianJoin(event);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        boolean hasCityHall = false;
        for (String currentName : civilian.getStashItems().keySet()) {
            if (currentName.equalsIgnoreCase("cityhall")) {
                hasCityHall = true;
            }
        }
        assertFalse(hasCityHall);
    }

    @Test
    public void playerShouldNotHavePreReqsForUnlockItem() {
        loadRegionTypeCityHall();
        ItemManager itemManager = ItemManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertFalse(itemManager.hasItemUnlocked(civilian, itemManager.getItemType("cityhall")));
    }

    @Test
    public void playerShouldHaveShackUnlocked() {
        loadRegionTypeShack();
        TownTests.loadTownTypeHamlet();
        Location location1 = new Location(Bukkit.getWorld("world"), 0,0,0);
        Town town = TownTests.loadTown("something", "hamlet", location1);
        town.getPeople().put(TestUtil.player.getUniqueId(), "owner");
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertTrue(ItemManager.getInstance().hasItemUnlocked(civilian,
                ItemManager.getInstance().getItemType("shack")));
    }

    @Test
    public void playerShouldHaveEmptyPreReqsForUnlockItem() {
        loadRegionTypeShelter();
        ItemManager itemManager = ItemManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertTrue(itemManager.hasItemUnlocked(civilian, itemManager.getItemType("shelter")));
    }

    @Test
    public void playerShouldHavePreReqsToUnlockItem() {
        loadRegionTypeShelter();
        loadSpellTypeBackflip();
        PlayerJoinEvent event = new PlayerJoinEvent(TestUtil.player, "blah");
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianJoin(event);
        ItemManager itemManager = ItemManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertTrue(itemManager.hasItemUnlocked(civilian, itemManager.getItemType("backflip")));
    }

    @Test
    public void playerShouldHaveExpToUnlockItem() {
        loadRegionTypeShelter();
        loadSpellTypeBackflip();
        loadSpellTypeRage();
        ItemManager itemManager = ItemManager.getInstance();
        PlayerJoinEvent event = new PlayerJoinEvent(TestUtil.player, "blah");
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianJoin(event);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        CivItem shelter = itemManager.getItemType("shelter");
        civilian.getExp().put(shelter, 520);
        assertTrue(itemManager.hasItemUnlocked(civilian, itemManager.getItemType("rage")));
    }

    @Test
    public void hamletShouldBeUnlocked() {
        TownTests.loadTownTypeTribe();
        Town town = TownTests.loadTown("test", "tribe", TestUtil.player.getLocation());
        town.setVillagers(4);
        town.getRawPeople().put(TestUtil.player.getUniqueId(), "owner");
        TownTests.loadTownTypeTribe2();
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertTrue(ItemManager.getInstance().hasItemUnlocked(civilian, ItemManager.getInstance().getItemType("tribe2")));
    }

    private void loadSpellTypeBackflip() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "Backflip");
        config.set("icon", "SLIME_BLOCK");
        config.set("velocity", 2);
        config.set("qty", 1);
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("shelter");
        config.set("pre-reqs", preReqs);
        itemManager.loadRegionType(config);
    }
    private void loadSpellTypeRage() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "Rage");
        config.set("icon", "NETHERRACK");
        config.set("qty", 1);
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("backflip:level=5|shelter:level=5");
        config.set("pre-reqs", preReqs);
        itemManager.loadRegionType(config);
    }

    private void loadRegionTypeShelter() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "Shelter");
        config.set("icon", "OAK_WOOD");
        config.set("build-radius", 5);
        config.set("qty", 1);
        itemManager.loadRegionType(config);
    }

    private void loadRegionTypeCityHall() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "CityHall");
        config.set("icon", "GOLD_BLOCK");
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("townhall:built=1");
        preReqs.add("town:built=1");
        config.set("pre-reqs", preReqs);
        config.set("build-radius", 7);
        itemManager.loadRegionType(config);
    }

    private void loadRegionTypeShack() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "Shack");
        config.set("icon", "CHEST");
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("member=hamlet");
        config.set("pre-reqs", preReqs);
        config.set("build-radius", 7);
        itemManager.loadRegionType(config);
    }
}
