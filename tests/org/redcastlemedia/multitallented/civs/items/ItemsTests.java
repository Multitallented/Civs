package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

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

    }

    @Test
    public void newPlayerShouldRecieveAShelterItem() {
        loadRegionTypeShelter();
        CivilianManager civilianManager = new CivilianManager();
        PlayerJoinEvent event = new PlayerJoinEvent(TestUtil.player, "blah");
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianJoin(event);
        Civilian civilian = civilianManager.getCivilian(TestUtil.player.getUniqueId());
        boolean hasShelter = false;
        for (CivItem civItem : civilian.getStashItems()) {
            if (civItem.getDisplayName().equals("Civs Shelter")) {
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
        CivilianManager civilianManager = new CivilianManager();
        PlayerJoinEvent event = new PlayerJoinEvent(TestUtil.player, "blah");
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianJoin(event);
        Civilian civilian = civilianManager.getCivilian(TestUtil.player.getUniqueId());
        boolean hasCityHall = false;
        for (CivItem civItem : civilian.getStashItems()) {
            if (civItem.getDisplayName().equals("Civs CityHall")) {
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
        config.set("icon", "WOOD");
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
}
