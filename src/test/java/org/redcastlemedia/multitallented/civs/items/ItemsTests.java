package org.redcastlemedia.multitallented.civs.items;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

public class ItemsTests extends TestUtil {

    @Before
    public void onBefore() {
        TownManager.getInstance().reload();
        RegionManager.getInstance().reload();
    }

    @After
    public void after() {
        TestUtil.world.setChunkLoaded(true);
    }

    @Test
    public void itemTypesShouldLoadProperly() {
        FolderType folderType = (FolderType) ItemManager.getInstance().getItemType("defense");
        assertTrue(folderType.getChildren().contains(ItemManager.getInstance().getItemType("church")));
    }

    @Test
    public void playerShouldHaveGroupUnlocked() {
        loadRegionTypeShack2();
        loadRegionTypeNPCShack2();
        RegionsTests.createNewRegion("shack2", TestUtil.player.getUniqueId());
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        CivItem npcShack = ItemManager.getInstance().getItemType("npc_shack2");
        assertTrue(ItemManager.getInstance().hasItemUnlocked(civilian, npcShack));
    }

    @Test
    public void newPlayerShouldRecieveAShelterItem() {
        loadRegionTypeShelter();
        CivilianManager.getInstance();
        PlayerJoinEvent event = new PlayerJoinEvent(TestUtil.player, "blah");
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianJoin(event);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        boolean hasShelter = false;
        for (String currentName : civilian.getStashItems().keySet()) {
            if (currentName.equalsIgnoreCase("shelter")) {
                hasShelter = true;
                break;
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
                break;
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
        loadRegionTypeShack2();
        TownTests.loadTownTypeHamlet2();
        Location location1 = new Location(Bukkit.getWorld("world"), 0,0,0);
        Town town = TownTests.loadTown("something", "hamlet2", location1);
        town.getPeople().put(TestUtil.player.getUniqueId(), Constants.OWNER);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertTrue(ItemManager.getInstance().hasItemUnlocked(civilian,
                ItemManager.getInstance().getItemType("shack2")));
    }

    @Test
    public void playerShouldHaveCityHall2Unlocked() {
        loadRegionTypeCityHall2();
        loadRegionTypeShack2();
        RegionsTests.createNewRegion("shack2", TestUtil.player.getUniqueId());
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertTrue(ItemManager.getInstance().hasItemUnlocked(civilian,
                ItemManager.getInstance().getItemType("cityhall2")));
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
        Town town = TownTests.loadTown("test", "hamlet2", TestUtil.player.getLocation());
        town.setVillagers(4);
        town.getRawPeople().put(TestUtil.player.getUniqueId(), Constants.OWNER);
        TownTests.loadTownTypeHamlet2();
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertTrue(ItemManager.getInstance().hasItemUnlocked(civilian, ItemManager.getInstance().getItemType("tribe")));
    }

    @Test
    public void folderShouldBeCorrect() {
        FolderType folderType = (FolderType) ItemManager.getInstance().getItemType("animals");
        CivItem civItem = ItemManager.getInstance().getItemType("ranch");
        assertTrue(folderType.getChildren().contains(civItem));
    }

    @Test
    public void folderShouldNotHaveDuplicateChildren() {
        HashSet<CivItem> items = new HashSet<>();
        for (CivItem civItem : ((FolderType) ItemManager.getInstance().getItemType("utilities")).getChildren()) {
            if (items.contains(civItem)) {
                fail("Dupicate folder children found " + civItem.getProcessedName());
            }
            items.add(civItem);
        }
    }

    @Test
    public void shelterShouldNotDupe() {
        RegionsTests.createNewRegion("shelter", TestUtil.player.getUniqueId());
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        Map<String, Integer> newItems = ItemManager.getInstance().getNewItems(civilian);
        assertFalse(newItems.containsKey("shelter"));
    }

    @Test
    public void cvInventoryAddItemsShouldAddToCorrectIndexes() {
        TestUtil.world.setChunkLoaded(false);
        CVInventory cvInventory = new CVInventory(new Location(TestUtil.world, 0, 0, 0));
        ItemStack[] itemStacks = {
                new ItemStack(Material.COBBLESTONE, 64),
                new ItemStack(Material.COBBLESTONE, 32),
                new ItemStack(Material.GRAVEL, 4)
        };
        cvInventory.addItem(itemStacks);
        assertEquals(Material.GRAVEL, cvInventory.getItem(2).getType());
        ItemStack[] itemStack2 = { new ItemStack(Material.COBBLESTONE, 4) };
        cvInventory.addItem(itemStack2);
        assertNull(cvInventory.getItem(3));
        assertEquals(36, cvInventory.getItem(1).getAmount());
        ItemStack[] itemStack3 = { new ItemStack(Material.COBBLESTONE, 64) };
        cvInventory.addItem(itemStack3);
        assertEquals(64, cvInventory.getItem(1).getAmount());
        assertEquals(36, cvInventory.getItem(3).getAmount());
        cvInventory.removeItem(itemStack2);
        assertEquals(60, cvInventory.getItem(0).getAmount());
    }

    @Test
    public void cvInventoryCheckItemsShouldNotAdd() {
        TestUtil.world.setChunkLoaded(false);
        CVInventory cvInventory = new CVInventory(new Location(TestUtil.world, 0, 0, 0));
        ItemStack[] itemStacks = {
                new ItemStack(Material.COBBLESTONE, 64),
                new ItemStack(Material.COBBLESTONE, 32),
                new ItemStack(Material.GRAVEL, 4)
        };
        Map<Integer, ItemStack> returnedItems = cvInventory.checkAddItems(itemStacks);
        assertNull(cvInventory.getItem(0));
        assertTrue(returnedItems.isEmpty());
    }

    @Test
    public void cvInventoryShouldRemoveIndex() {
        TestUtil.world.setChunkLoaded(false);
        CVInventory cvInventory = new CVInventory(new Location(TestUtil.world, 0, 0, 0));
        cvInventory.setItem(3, new ItemStack(Material.COAL, 6));
        assertEquals(Material.COAL, cvInventory.getItem(3).getType());
        List<List<CVItem>> inputs = new ArrayList<>();
        List<CVItem> input = new ArrayList<>();
        input.add(new CVItem(Material.COAL, 6));
        input.add(new CVItem(Material.CHARCOAL, 30));
        inputs.add(input);
        assertTrue(Util.removeItems(inputs, cvInventory));
        assertNull(cvInventory.getItem(3));
        assertFalse(Util.containsItems(inputs, cvInventory));
    }

    @Test
    public void itemKeyShouldAlwaysBeCorrect() {
        CivItem civItem = ItemManager.getInstance().getItemType("ranch");
        assertEquals("ranch", civItem.getProcessedName());
        assertEquals("ranch", civItem.getKey());
        assertEquals(ChatColor.BLACK + "ranch", civItem.createItemStack().getItemMeta().getLore().get(1));
        assertEquals(ChatColor.BLACK + "ranch", civItem.createItemStack(player).getItemMeta().getLore().get(1));
    }

    @Test
    public void createCivItemFromString() {
        assertNotNull(CVItem.createCVItemFromString("civ:arrow_factory*2"));
    }

    @Test
    public void test() {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("npc_shack");
        assertNotNull(regionType);
        ItemStack itemStack = regionType.createItemStack(player);
        assertEquals("npc_shack", ChatColor.stripColor(itemStack.getItemMeta().getLore().get(1)));
    }

    @Test
    public void imLosingMyMind() {
        Pattern pattern = Pattern.compile("g:fence(?![_A-Za-z])");
        assertTrue(pattern.matcher("LADDER*4,g:fence*4,").find());
    }

    @Test
    public void groupsWithinGroups() {
        ConfigManager.getInstance().getItemGroups().put("asdf", "LADDER,g:stairs");
        List<CVItem> itemList = CVItem.createListFromString("g:asdf*2");
        assertEquals(Material.LADDER, itemList.get(0).getMat());
        assertEquals(Material.QUARTZ_STAIRS, itemList.get(1).getMat());
    }

    @Test
    public void groupShouldBePrimary() {
        ItemGroupList itemGroupList = new ItemGroupList();
        itemGroupList.findAllGroupsRecursively("g:primary*2");
        assertEquals("primary", itemGroupList.getMainGroup());
    }

    private void loadSpellTypeBackflip() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "SLIME_BLOCK");
        config.set("velocity", 2);
        config.set("qty", 1);
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("shelter");
        config.set("pre-reqs", preReqs);
        itemManager.loadRegionType(config, "backflip");
    }
    private void loadSpellTypeRage() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "NETHERRACK");
        config.set("qty", 1);
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("backflip:level=5|shelter:level=5");
        config.set("pre-reqs", preReqs);
        itemManager.loadRegionType(config, "rage");
    }

    private void loadRegionTypeShelter() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "OAK_WOOD");
        config.set("build-radius", 5);
        config.set("qty", 1);
        itemManager.loadRegionType(config, "shelter");
    }

    private void loadRegionTypeCityHall() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "GOLD_BLOCK");
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("townhall:built=1");
        preReqs.add("town:built=1");
        config.set("pre-reqs", preReqs);
        config.set("build-radius", 7);
        itemManager.loadRegionType(config, "cityhall");
    }
    private void loadRegionTypeCityHall2() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "GOLD_BLOCK");
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("shack2:built=1");
        config.set("pre-reqs", preReqs);
        config.set("build-radius", 7);
        itemManager.loadRegionType(config, "cityhall2");
    }

    private void loadRegionTypeShack2() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        ArrayList<String> groups = new ArrayList<>();
        groups.add("baseshack");
        config.set("groups", groups);
        config.set("icon", "CHEST");
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("member=hamlet2");
        config.set("pre-reqs", preReqs);
        config.set("build-radius", 7);
        itemManager.loadRegionType(config, "shack2");
    }

    private void loadRegionTypeNPCShack2() {
        ItemManager itemManager = ItemManager.getInstance();
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "CHEST");
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("baseshack:built=1");
        config.set("pre-reqs", preReqs);
        config.set("build-radius", 7);
        itemManager.loadRegionType(config, "npc_shack2");
    }
}
