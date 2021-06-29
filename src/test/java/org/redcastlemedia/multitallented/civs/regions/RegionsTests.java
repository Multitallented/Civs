package org.redcastlemedia.multitallented.civs.regions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.redcastlemedia.multitallented.civs.BlockLogger;
import org.redcastlemedia.multitallented.civs.ItemMetaImpl;
import org.redcastlemedia.multitallented.civs.ItemStackImpl;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.menus.PortMenuTest;
import org.redcastlemedia.multitallented.civs.menus.RecipeMenuTests;
import org.redcastlemedia.multitallented.civs.protections.ProtectionHandler;
import org.redcastlemedia.multitallented.civs.scheduler.DailyScheduler;
import org.redcastlemedia.multitallented.civs.scheduler.RegionTickUtil;
import org.redcastlemedia.multitallented.civs.towns.GovTypeBuff;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.util.Constants;

public class RegionsTests extends TestUtil {

    @Before
    public void onBefore() {
        RegionManager.getInstance().reload();
        TownManager.getInstance().reload();
        MenuManager.getInstance().clearOpenMenus();
    }

    @After
    public void cleanup() {
        world.setChunkLoaded(false);
    }

    @Test
    public void allRegionsShouldDoUpkeep() {
        ArrayList<Region> regions = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            regions.add(createNewRegion("leather_shop"));
        }
        for (int i = 0; i < 10; i++) {
            RegionTickUtil.runUpkeeps();
        }
        for (Region region : regions) {
            assertNotEquals(0, region.lastTick);
        }
    }

    @Test
    public void regionShouldCheckUpkeep() {
        world.setChunkLoaded(true);
        Region region = createNewRegion("greenhouse");
        for (int i = 0; i < 10; i++) {
            RegionTickUtil.runUpkeeps();
        }
        region.lastTick = 0;
        RegionManager.getInstance().removeCheckedRegion(region);
        Chest chest = (Chest) blockUnique.getState();
        chest.getInventory().setItem(0, new ItemStack(Material.SHEARS, 1));
        for (int i = 0; i < 10; i++) {
            RegionTickUtil.runUpkeeps();
        }
        ItemStack firstItem = chest.getInventory().getItem(0);
        assertNotEquals(0, region.lastTick);
        assertNotNull(firstItem);
        assertEquals(Material.SHEARS, firstItem.getType());
        ItemStack secondItem = chest.getInventory().getItem(1);
        assertNotNull(secondItem);
        assertNotEquals(Material.SHEARS, secondItem.getType());
    }

    @Test
    public void getPeriodShouldTakeBuffsIntoAccount() {
        loadRegionTypeCobble();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        HashSet<String> regions = new HashSet<>();
        regions.add("cobble");
        GovTypeBuff buff = new GovTypeBuff(GovTypeBuff.BuffType.COOLDOWN,
                10, new HashSet<>(), regions);
        HashSet<GovTypeBuff> buffs = new HashSet<>();
        buffs.add(buff);
        Government government = new Government("ANARCHY", GovernmentType.ANARCHY,
                buffs, null, new ArrayList<>());
        assertEquals(90, regionType.getPeriod(government));
    }

    @Test
    public void withNoRegionsShouldNotDetectRegion() {
        assertNull(RegionManager.getInstance().getRegionAt(TestUtil.block2.getLocation()));
    }

    @Test
    public void secondsUntilNextTickShouldWork() {
        loadRegionTypeCobble();
        Region region = createNewRegion("cobble");
        region.lastTick = System.currentTimeMillis();
        assertTrue(region.getSecondsTillNextTick() > 0);
    }

    @Test
    public void itemManagerShouldLoadRegionTypesFromConfig() {
        loadRegionTypeCobble();
        assertNotNull(ItemManager.getInstance().getItemType("cobble"));
    }

    @Test
    public void regionIdShouldBeAccurate() {
        Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        assertEquals("d2460330-f815-4339-9b11-cf10755ccef9~0.5~0.5~0.5", Region.locationToString(location));
    }
    @Test
    public void regionIdShouldBeAccurateWithDecimals() {
        Location location = new Location(Bukkit.getWorld("world"), 50.5, 69.5, -447.5);
        assertEquals("d2460330-f815-4339-9b11-cf10755ccef9~50.5~69.5~-447.5", Region.locationToString(location));
    }

    @Test
    public void regionShouldReportIdProperly() {
        loadRegionTypeCobble();
        Location location = new Location(Bukkit.getWorld("world"), 960, 72, 933);
        Region region = new Region("cobble", new HashMap<>(), location, getRadii(),
                new HashMap<>(), 0);
        assertEquals("d2460330-f815-4339-9b11-cf10755ccef9~960.5~72.5~933.5", region.getId());
    }

    @Test
    public void stringToLocationShouldWork() {
        assertEquals(933.5, Region.idToLocation("d2460330-f815-4339-9b11-cf10755ccef9~960.5~72.5~933.5").getZ(), 0.01);
    }

    @Test
    public void convertStringToLocationAndBackShouldBeTheSameNegative() {
        Location location = new Location(Bukkit.getWorld("world"), -809.9937, 65, 781);
        Location location2 = Region.idToLocation(Region.locationToString(location));
        assertEquals(-809.5, location2.getX(), 0.1);
        assertEquals(781.5, location2.getZ(), 0.1);
    }

    @Test
    public void convertStringToLocationAndBackShouldBeTheSameZero() {
        Location location = new Location(Bukkit.getWorld("world"), -809.9937, 65, 0);
        Location location2 = Region.idToLocation(Region.blockLocationToString(location));
        assertEquals(0.5, location2.getZ(), 0.1);
    }

    @Test
    public void convertStringToLocationAndBackShouldBeTheSameZero2() {
        Location location = new Location(Bukkit.getWorld("world"), -1, 65, 0);
        Location location2 = Region.idToLocation(Region.blockLocationToString(location));
        assertEquals(-0.5, location2.getX(), 0.1);
    }

    @Test
    public void convertStringToLocationAndBackShouldEqualOriginalString() {
        String locationString = "d2460330-f815-4339-9b11-cf10755ccef9~-960.5~72.5~933.5";
        Location location = Region.idToLocation(locationString);
        assertEquals(locationString, Region.locationToString(location));
    }

    @Test
    public void stringToLocationShouldHandleDecimals() {
        Location location = new Location(Bukkit.getWorld("world"), -809.9937, 65, -781);
        assertEquals("d2460330-f815-4339-9b11-cf10755ccef9~-809.5~65.5~-780.5", Region.blockLocationToString(location));
    }

    @Test
    public void getRegionAtShouldReturnRegion() {
        loadRegionTypeCobble();
        Location location = new Location(Bukkit.getWorld("world"), 960, 72, 933);
        Region region = new Region("cobble", new HashMap<>(), location, getRadii(),
                new HashMap<>(), 0);
        RegionManager.getInstance().addRegion(region);
        assertEquals(region, RegionManager.getInstance().getRegionAt(location));
    }

    @Test
    public void getRegionAtShouldReturnExactRegion() {
        loadRegionTypeCobble();
        int [] radii = getRadii();
        Location location1 = new Location(Bukkit.getWorld("world"), 480, 70, 480);
        Region region1 = new Region("cobble", new HashMap<>(), location1, radii, new HashMap<>(), 0);
        RegionManager.getInstance().addRegion(region1);
        RegionManager.getInstance().regionLocations.remove(location1);
        for (int i=0;i<24;i++) {
            Location location = new Location(Bukkit.getWorld("world"), 500 + 20 * i, 70, 500+ 20 *i);
            Region region = new Region("cobble", new HashMap<>(), location, radii, new HashMap<>(), 0);
            RegionManager.getInstance().addRegion(region);
            RegionManager.getInstance().regionLocations.remove(location);
            assertEquals(region1, RegionManager.getInstance().getRegionAt(location1));
            assertEquals(region, RegionManager.getInstance().getRegionAt(location));
        }
        Location finalLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        assertNull(RegionManager.getInstance().getRegionAt(finalLocation));
        for (int i = 0; i < 24; i++) {
            Location location = new Location(Bukkit.getWorld("world"), 500 + 20 * i, 70, 500+ 20 *i);
            assertNotNull(RegionManager.getInstance().getRegionAt(location));
        }
    }

    @Test
    public void regionManagerShouldGetRegionBasedOnLocation() {
        Location location = new Location(Bukkit.getWorld("world"), 100, 0, 0);
        HashMap<UUID, String> people = new HashMap<>();
        RegionManager.getInstance().addRegion(new Region("cobble", people, location, getRadii(), new HashMap<>(),0));
        assertNull(RegionManager.getInstance().getRegionAt(new Location(Bukkit.getWorld("world"), 0,0,0)));
    }

    @Test
    public void regionShouldNotBeCreatedWithoutAllReqs() {
        loadRegionTypeCobble2();

        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique);
        doReturn(TestUtil.createUniqueItemStack(Material.CHEST, "Civs Cobble")).when(event1).getItemInHand();
        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event1);
        assertNull(RegionManager.getInstance().getRegionAt(TestUtil.blockUnique.getLocation()));
    }

    @Test
    public void regionShouldBeCreatedWithAllReqs() {
        loadRegionTypeCobble();

        BlockPlaceEvent event3 = mock(BlockPlaceEvent.class);
        when(event3.getBlockPlaced()).thenReturn(TestUtil.block3);
        ItemStack cobbleStack = TestUtil.createItemStack(Material.COBBLESTONE);
        doReturn(cobbleStack).when(event3).getItemInHand();
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.block2);
        when(event2.getItemInHand()).thenReturn(cobbleStack);
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        Location regionLocation = new Location(Bukkit.getWorld("world"), -1 , 0, 0);
        Block chestBlock = TestUtil.createUniqueBlock(Material.CHEST, "Civs cobble", regionLocation, false);
        when(event1.getBlockPlaced()).thenReturn(chestBlock);
        List<String> lore = new ArrayList<>();
        lore.add(TestUtil.player.getUniqueId().toString());
        lore.add("Cobble");
        ItemStack itemStack = TestUtil.mockItemStack(Material.CHEST, 1, "Civs Cobble", lore);
        doReturn(itemStack).when(event1).getItemInHand();

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        Region region = RegionManager.getInstance().getRegionAt(regionLocation);
        assertEquals("cobble", region.getType());
        assertEquals(5, region.getRadiusXP());
        assertEquals(5, region.getRadiusXN());
        assertEquals(-0.5, region.getLocation().getX(), 0.1);
        assertEquals(0.5, region.getLocation().getZ(), 0.1);
    }

    @Test
    public void regionShouldBeCreatedWithAllGroupReqs() {
        loadRegionTypeCobble3();

        BlockPlaceEvent event3 = mock(BlockPlaceEvent.class);
        when(event3.getBlockPlaced()).thenReturn(TestUtil.block3);
        ItemStack cobbleStack = TestUtil.createItemStack(Material.COBBLESTONE);
        doReturn(cobbleStack).when(event3).getItemInHand();
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.block2);
        when(event2.getItemInHand()).thenReturn(cobbleStack);
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique);
        List<String> lore = new ArrayList<>();
        lore.add(TestUtil.player.getUniqueId().toString());
        lore.add("Cobble");
        doReturn(TestUtil.mockItemStack(Material.CHEST, 1, "Civs Cobble", lore)).when(event1).getItemInHand();

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertEquals("cobble", RegionManager.getInstance().getRegionAt(TestUtil.blockUnique.getLocation()).getType());
    }

    @Test
    public void regionShouldNotBeCreatedWithAllReqsOutOfBounds() {
        loadRegionTypeCobble();

        BlockPlaceEvent event3 = mock(BlockPlaceEvent.class);
        ItemStack cobbleStack = TestUtil.createItemStack(Material.COBBLESTONE);
        doReturn(cobbleStack).when(event3).getItemInHand();
        when(event3.getBlockPlaced()).thenReturn(TestUtil.block3);
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.block2);
        doReturn(cobbleStack).when(event2).getItemInHand();
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique2);
        CVItem item = CVItem.createCVItemFromString("CHEST");
        item.setDisplayName("Civs Cobble");
        List<String> lore = new ArrayList<>();
        lore.add(TestUtil.player.getUniqueId().toString());
        item.setLore(lore);
        doReturn(item.createItemStack()).when(event1).getItemInHand();

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertNull(RegionManager.getInstance().getRegionAt(TestUtil.blockUnique2.getLocation()));
    }

    @Test
    public void regionShouldNotBeMissingBlocksAfterPlacingBlock() {
        RegionsTests.loadRegionTypeCobble();
        Region region = RegionsTests.createNewRegion("cobble", player.getUniqueId());
        List<List<CVItem>> missingBlocks = new ArrayList<>();
        List<CVItem> tempList = new ArrayList<>();
        tempList.add(new CVItem(Material.GOLD_BLOCK, 1));
        missingBlocks.add(tempList);
        region.setMissingBlocks(missingBlocks);
        BlockPlaceEvent blockPlaceEvent = mock(BlockPlaceEvent.class);
        when(blockPlaceEvent.getPlayer()).thenReturn(player);
        when(blockPlaceEvent.getBlockPlaced()).thenReturn(goldBlock0x1y1z);
        when(blockPlaceEvent.isCancelled()).thenReturn(false);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockPlace(blockPlaceEvent);
        assertTrue(region.getMissingBlocks().isEmpty());
    }

    @Test
    public void regionShouldBeCreatedWithAllReqsFlex() {
        loadRegionTypeCobble();

        BlockPlaceEvent event3 = mock(BlockPlaceEvent.class);
        when(event3.getBlockPlaced()).thenReturn(TestUtil.block6);
        ItemStack cobbleStack = TestUtil.createItemStack(Material.COBBLESTONE);
        doReturn(cobbleStack).when(event3).getItemInHand();
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.block5);
        doReturn(cobbleStack).when(event2).getItemInHand();
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique3);
        List<String> lore = new ArrayList<>();
        lore.add(TestUtil.player.getUniqueId().toString());
        lore.add("Cobble");
        doReturn(TestUtil.mockItemStack(Material.CHEST, 1, "Civs Cobble", lore)).when(event1).getItemInHand();

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        Region region = RegionManager.getInstance().getRegionAt(TestUtil.blockUnique3.getLocation());
        assertEquals(3, region.getRadiusXN());
        assertEquals(7, region.getRadiusXP());
        assertEquals(5, region.getRadiusYN());
    }

    @Test
    public void detectRegionShouldIgnoreExtraReqs() {
        loadRegionTypeCobble();
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique9);
        doReturn(TestUtil.createUniqueItemStack(Material.CHEST, "Civs Cobble"))
                .when(event1).getItemInHand();
        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event1);
        assertNotNull(RegionManager.getInstance().getRegionAt(TestUtil.blockUnique9.getLocation()));
    }

    @Test
    public void detectRegionShouldIgnoreExtraGroupReqs() {
        RecipeMenuTests.loadRegionTypeCouncilRoom();
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique10);
        doReturn(TestUtil.createUniqueItemStack(Material.CHEST, "Civs Councilroom"))
                .when(event1).getItemInHand();
        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event1);
        assertNotNull(RegionManager.getInstance().getRegionAt(TestUtil.blockUnique10.getLocation()));
    }

    @Test
    public void regionShouldNotBeCreatedIfNotAllReqs() {
        loadRegionTypeCobble2();

        InventoryView transaction = mock(InventoryView.class);
        when(transaction.getPlayer()).thenReturn(TestUtil.player);
        InventoryCloseEvent inventoryCloseEvent = new InventoryCloseEvent(transaction);
        BlockPlaceEvent event3 = mock(BlockPlaceEvent.class);
        when(event3.getBlockPlaced()).thenReturn(TestUtil.block4);
        ItemStack cobbleStack = TestUtil.createItemStack(Material.COBBLESTONE);
        doReturn(cobbleStack).when(event3).getItemInHand();
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.block2);
        doReturn(cobbleStack).when(event2).getItemInHand();
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique);
        doReturn(TestUtil.createUniqueItemStack(Material.CHEST, "Civs Cobble")).when(event1).getItemInHand();


        MenuManager.getInstance().onInventoryClose(inventoryCloseEvent);
        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertNull(RegionManager.getInstance().getRegionAt(TestUtil.blockUnique.getLocation()));
    }

    @Test
    public void regionManagerShouldFindSingleRegion() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 100, 0, 0);
        Location location2 = new Location(Bukkit.getWorld("world"), 0, 100, 0);
        Location location3 = new Location(Bukkit.getWorld("world"), 100, 100, 50);
        Location location4 = new Location(Bukkit.getWorld("world"), 0, 100, 50);
        Location location5 = new Location(Bukkit.getWorld("world"), 500, 100, 50);
        Location location6 = new Location(Bukkit.getWorld("world"), 100, 1000, 0);
        Location location7 = new Location(Bukkit.getWorld("world"), 1000, 0, 0);
        Location location8 = new Location(Bukkit.getWorld("world"), 1050, 0, 0);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0));
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location2, getRadii(), new HashMap<>(),0));
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location3, getRadii(), new HashMap<>(),0));
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location4, getRadii(), new HashMap<>(),0));
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location5, getRadii(), new HashMap<>(),0));
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location6, getRadii(), new HashMap<>(),0));
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location7, getRadii(), new HashMap<>(),0));
        assertEquals(location5.getX(), RegionManager.getInstance().getRegionAt(location5).getLocation().getX(), 0.1);
        assertEquals(location2.getX(), RegionManager.getInstance().getRegionAt(location2).getLocation().getX(), 0.1);
        assertEquals(location1.getX(), RegionManager.getInstance().getRegionAt(location1).getLocation().getX(), 0.1);
        assertEquals(location7.getX(), RegionManager.getInstance().getRegionAt(location7).getLocation().getX(), 0.1);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location8, getRadii(), new HashMap<>(),0));
        assertEquals(location8.getX(), RegionManager.getInstance().getRegionAt(location8).getLocation().getX(), 0.1);
    }

    @Test
    public void shouldNotBeAbleToCreateARegionOnTopOfAnotherRegion() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);


        BlockPlaceEvent event3 = mock(BlockPlaceEvent.class);
        when(event3.getBlockPlaced()).thenReturn(TestUtil.block3);
        ItemStack cobbleStack = TestUtil.createItemStack(Material.COBBLESTONE);
        doReturn(cobbleStack).when(event3).getItemInHand();
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.block2);
        doReturn(cobbleStack).when(event2).getItemInHand();
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique);
        CVItem item = CVItem.createCVItemFromString("CHEST");
        item.setDisplayName("Civs Cobble");
        List<String> lore = new ArrayList<>();
        lore.add(TestUtil.player.getUniqueId().toString());
        item.setLore(lore);
        doReturn(item.createItemStack()).when(event1).getItemInHand();

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertEquals(region, RegionManager.getInstance().getRegionAt(location1));
    }

    @Test
    public void strangeRegionShapeShouldBuildProperly() {
        loadRegionTypeRectangle();

        BlockPlaceEvent event3 = mock(BlockPlaceEvent.class);
        when(event3.getBlockPlaced()).thenReturn(TestUtil.block8);
        ItemStack cobbleStack = TestUtil.createItemStack(Material.COBBLESTONE);
        doReturn(cobbleStack).when(event3).getItemInHand();
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.block7);
        doReturn(cobbleStack).when(event2).getItemInHand();
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique4);
        List<String> lore = new ArrayList<>();
        lore.add(TestUtil.player.getUniqueId().toString());
        lore.add("Cobble");
        ItemStack itemStack = TestUtil.mockItemStack(Material.CHEST, 1, "Civs Cobble", lore);
        doReturn(itemStack).when(event1).getItemInHand();


        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);

        assertNotNull(RegionManager.getInstance().getRegionAt(TestUtil.blockUnique4.getLocation()));
    }

    @Test
    public void playerShouldBeInMultipleEffectRadii() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Location location2 = new Location(Bukkit.getWorld("world"), 5, 0, 0);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0));
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location2, getRadii(), new HashMap<>(),0));
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        assertEquals(2, RegionManager.getInstance().getRegionEffectsAt(TestUtil.player.getLocation(), regionType.getEffectRadius() - regionType.getBuildRadius()).size());
    }

    @Test
    public void regionsInDifferentWorldsShouldntCollide() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        World world3 = mock(World.class);
        when(world3.getUID()).thenReturn(new UUID(1, 7));
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Location location2 = new Location(world3, 0, 0, 0);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0));
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location2, getRadii(), new HashMap<>(),0));
        assertEquals(1, RegionManager.getInstance().getRegionEffectsAt(TestUtil.player.getLocation(), 0).size());
    }

    @Test
    public void regionShouldNotBeBuiltTooClose() {
        loadRegionTypeShelter();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 500, 0, 0);
        RegionManager.getInstance().addRegion(new Region("shelter", owners, location1, getRadii(), new HashMap<>(),0));

        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique6);
        doReturn(TestUtil.createUniqueItemStack(Material.CHEST, "Civs Shelter")).when(event1).getItemInHand();
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getPlayer()).thenReturn(TestUtil.player);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.blockUnique7);
        doReturn(TestUtil.createUniqueItemStack(Material.CHEST, "Civs Shelter")).when(event2).getItemInHand();

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event1);
        assertNull(RegionManager.getInstance().getRegionAt(TestUtil.blockUnique6.getLocation()));
        regionListener.onBlockPlace(event2);
        assertNotNull(RegionManager.getInstance().getRegionAt(TestUtil.blockUnique7.getLocation()));
    }

    @Test
    public void regionShouldBeDestroyedCenter() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0));
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.blockUnique, TestUtil.player);
        CivilianListener civilianListener = new CivilianListener();
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        if (!event.isCancelled()) {
            civilianListener.onCivilianBlockBreak(event);
        }
        assertNull(RegionManager.getInstance().getRegionAt(location1));
    }

    @Test
    public void regionShouldNotBeDestroyedCenter() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0));
        Player player1 = mock(Player.class);
        when(player1.getUniqueId()).thenReturn(new UUID(1,8));
        when(player1.getGameMode()).thenReturn(GameMode.SURVIVAL);
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.blockUnique, player1);
        CVItem cvItem = new CVItem(Material.CHEST, 1);
        cvItem.getLore().add(TestUtil.player.getUniqueId().toString());
        BlockLogger.getInstance().putBlock(location1, cvItem);
        CivilianListener civilianListener = new CivilianListener();
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        if (!event.isCancelled()) {
            civilianListener.onCivilianBlockBreak(event);
        }
        assertNotNull(RegionManager.getInstance().getRegionAt(location1));
        assertEquals(Material.CHEST, TestUtil.world.getBlockAt(location1).getType());
        assertTrue(event.isCancelled());
    }

    @Test
    public void regionShouldBeDestroyedExtra() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        Region region = new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0);
        RegionManager.getInstance().addRegion(region);
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block3, TestUtil.player);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        assertNotNull(RegionManager.getInstance().getRegionAt(location1));
        assertFalse(event.isCancelled());
    }

    @Test
    public void regionShouldBeNotDestroyedUnrelated() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        Region region = new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0);
        RegionManager.getInstance().addRegion(region);
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block4, TestUtil.player);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        assertNotNull(RegionManager.getInstance().getRegionAt(location1));
    }

    @Test
    public void regionShouldBeNotDestroyedSecondary() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        Region region = new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0);
        RegionManager.getInstance().addRegion(region);
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.goldBlock0x1y1z, TestUtil.player);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        assertNotNull(RegionManager.getInstance().getRegionAt(location1));
    }

    @Test
    public void regionShouldNotBeDestroyed() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        Region region = new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0);
        RegionManager.getInstance().addRegion(region);
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.goldBlock0x1y1z, TestUtil.player);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        assertNotNull(RegionManager.getInstance().getRegionAt(location1));
        assertTrue(event.isCancelled());
    }

    @Test
    public void regionShouldBeDestroyedAndRebuilt() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        UUID uuid = new UUID(1, 4);
        owners.put(uuid, Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0));
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.blockUnique, TestUtil.player);
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianBlockBreak(event);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        Block block2 = TestUtil.createUniqueBlock(Material.CHEST, "Civs cobble", location1, false);
        when(event1.getBlockPlaced()).thenReturn(block2);
        ItemStack itemStack = TestUtil.createUniqueItemStack(Material.CHEST, "Civs Cobble");
        when(event1.getItemInHand()).thenReturn(itemStack);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event1);
        assertNotNull(RegionManager.getInstance().getRegionAt(location1));
    }

    @Test
    public void regionShouldHaveUpkeep() {
        loadRegionTypeUtility();
        TownTests.loadTownTypeHamlet2();
        Location location = new Location(Bukkit.getWorld("world"), 4,0,0);
        TownTests.loadTown("test", "hamlet2", location);
        Region region = RegionsTests.createNewRegion("utility");
        assertTrue(region.needsReagentsOrInput());
    }

    @Test
    public void regionShouldNotHaveReagents() {
        loadRegionTypeCobble3();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);
        assertFalse(region.hasUpkeepItems());
    }
    @Test
    @Ignore // TODO fix this
    public void regionShouldHaveReagents() {
        loadRegionTypeCobble4();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);
        assertTrue(region.hasUpkeepItems());
    }

    @Test
    @Ignore // TODO fix this
    public void regionShouldRunUpkeep() {
        loadRegionTypeCobble4();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);
        Chest chest = (Chest) location1.getBlock().getState();
        chest.getBlockInventory().clear();
        chest.getBlockInventory().setItem(0, new ItemStackImpl(Material.IRON_PICKAXE, 1));
        assertTrue(region.runUpkeep());
        assertEquals(Material.GOLDEN_PICKAXE, chest.getBlockInventory().getContents()[0].getType());
        chest.getBlockInventory().setItem(0, TestUtil.mockItemStack(Material.IRON_PICKAXE, 1, null, new ArrayList<>()));
    }

    @Test
    public void regionShouldNotHavePowerReagents() {
        loadRegionTypePower(true);
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);
        assertFalse(region.hasUpkeepItems());
    }

    @Test
    public void regionShouldHavePowerReagents() {
        loadRegionTypePower(true);
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);
        TownTests.loadTownTypeHamlet2();
        Town town = new Town("townName", "hamlet2", location1,
                owners, 300, 300, 2, 0, -1);
        TownManager.getInstance().addTown(town);
        assertTrue(region.hasUpkeepItems());
        region.runUpkeep(false);
        assertEquals(299, town.getPower());
    }

    @Test
    public void regionShouldAddPowerToTown() {
        loadRegionTypePower(false);
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);
        TownTests.loadTownTypeHamlet2();
        Town town = new Town("townName", "hamlet2", location1,
                owners, 300, 305, 2, 0, -1);
        TownManager.getInstance().addTown(town);
        region.runUpkeep(false);
        assertEquals(301, town.getPower());
    }

    @Test
    public void regionShouldConsiderAlliesAsGuests() {
        UUID uuid1 = new UUID(1, 3);
        Region region = load2TownsWith1Region(uuid1, true);
        assertEquals("allyforeign", region.getPeople().get(uuid1));
    }

    @Test
    public void regionShouldNotConsiderEveryoneAsGuest() {
        UUID uuid1 = new UUID(1, 3);
        Region region = load2TownsWith1Region(uuid1, false);
        assertNull(region.getPeople().get(uuid1));
    }

    @Test
    public void dailyRegionShouldUpkeepDaily() {
        loadRegionTypeDaily();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("daily", owners, location1, getRadii(), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);
        TownTests.loadTownTypeHamlet2();
        Town town = new Town("townname", "hamlet2", location1,
                owners, 300, 500, 2, 0, -1);
        TownManager.getInstance().addTown(town);
        try {
            new DailyScheduler().run();
        } catch (SuccessException ignored) {

        }
        assertEquals(412, town.getPower());
    }

    @Test
    public void dailyRegionShouldNotRunUpkeepTick() {
        loadRegionTypeDaily();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("daily", owners, location1, getRadii(), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);
        TownTests.loadTownTypeHamlet2();
        Town town = new Town("townname", "hamlet2", location1,
                owners, 300, 305, 2, 0, -1);
        TownManager.getInstance().addTown(town);
        RegionTickUtil.runUpkeeps();
        assertEquals(300, town.getPower());
    }

    @Test
    public void offCenterRegionShouldDestroy() {
        loadRegionTypeCobbleQuarry();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 6), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 300, 100, 0);
        Region region = new Region("cobblequarry", owners, location1, getRadii(9,5,7,7,7,7), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(TestUtil.blockUnique8, TestUtil.player);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(blockBreakEvent);
        assertNull(RegionManager.getInstance().getRegionAt(location1));
    }

    @Test
    public void membersShouldBeAbleToOpenChests() {
        loadRegionTypePower(false);
        Region region = PortMenuTest.loadRegion("power");
        region.getPeople().put(TestUtil.player.getUniqueId(), "member");
        PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(
                TestUtil.player,
                Action.RIGHT_CLICK_BLOCK,
                new ItemStack(Material.AIR),
                TestUtil.blockUnique,
                BlockFace.NORTH,
                EquipmentSlot.HAND);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockInteract(playerInteractEvent);
        assertSame(Event.Result.ALLOW, playerInteractEvent.useInteractedBlock());
    }

    @Test
    public void regionTypeReqMapShouldNeverMutate() {
        RegionsTests.loadRegionTypeCobble2();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        List<List<CVItem>> reqMap = regionType.getReqs();
        reqMap.get(0).get(0).setQty(2);
        assertEquals(3, regionType.getReqs().get(0).get(0).getQty());
        reqMap.get(0).add(CVItem.createCVItemFromString("OAK_LOG*5"));
        assertEquals(1, regionType.getReqs().get(0).size());
    }

    @Test
    public void regionShouldBeFound() {
        loadRegionTypeCobble();
        Region region = createNewRegion("cobble");
        region.getLocation().setX(2390);
        region.getLocation().setY(65);
        region.getLocation().setZ(1481);
        Set<Region> regions = RegionManager.getInstance().getRegionsXYZ(region.getLocation().add(0,1,0),
                40,
                500,
                40,
                false);
        assertFalse(regions.isEmpty());
    }

    @Test
    public void failingToBuildARegionShouldNotAddBlockToBlockLogger() {
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        boolean[] cancelled = new boolean[1];
        cancelled[0] = false;
        when(event1.isCancelled()).thenAnswer(invocation -> cancelled[0]);
        doAnswer(invocation -> { cancelled[0] = (boolean) invocation.getArguments()[0]; return null; })
                .when(event1).setCancelled(anyBoolean());
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique);
        ItemStack itemStack = mock(ItemStack.class);
        when(itemStack.hasItemMeta()).thenReturn(true);
        ItemMetaImpl itemMeta = new ItemMetaImpl();
        itemMeta.setDisplayName("Civs Coal Mine");
        itemMeta.getLore().add(TestUtil.player.getUniqueId().toString());
        itemMeta.getLore().add("coal_mine");
        when(itemStack.getItemMeta()).thenReturn(itemMeta);
        doReturn(itemStack).when(event1).getItemInHand();

        CivilianListener.getInstance().onBlockPlace(event1);
        RegionManager.getInstance().detectNewRegion(event1);
        if (!cancelled[0]) {
            CivilianListener.getInstance().onPlaceBlockLogger(event1);
        }
        assertNull(BlockLogger.getInstance().getBlock(TestUtil.blockUnique.getLocation()));
    }

    private Region load2TownsWith1Region(UUID uuid1, boolean allied) {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        UUID uuid = new UUID(1, 4);
        owners.put(uuid, Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>(),0);
        RegionManager.getInstance().addRegion(region);

        TownTests.loadTownTypeHamlet2();
        Town town = new Town("townname", "hamlet2", location1,
                new HashMap<>(), 300, 300, 2, 0, -1);
        TownManager.getInstance().addTown(town);

        Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Town town1 = new Town("townname1", "hamlet2", location,
                new HashMap<>(), 300, 300, 2, 0, -1);
        town1.getPeople().put(uuid1, "member");
        TownManager.getInstance().addTown(town1);
        if (allied) {
            AllianceManager.getInstance().allyTheseTowns(town, town1);
        }
        return region;
    }

    public static int[] getRadii() {
        int[] radiuses = new int[6];
        for (int i = 0; i < 6; i++) {
            radiuses[i] = 5;
        }
        return radiuses;
    }
    public static int[] getRadii(int xp, int xn, int yp, int yn, int zp, int zn) {
        int[] radiuses = new int[6];
        radiuses[0] = xp;
        radiuses[1] = zp;
        radiuses[2] = xn;
        radiuses[3] = zn;
        radiuses[4] = yp;
        radiuses[5] = yn;
        return radiuses;
    }

    public static void loadRegionTypeDaily() {
        FileConfiguration config = new YamlConfiguration();
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_place");
        effects.add("block_break");
        config.set("effects", effects);
        config.set("upkeep.0.power-output", 2);
        config.set("period", "daily");
        ItemManager.getInstance().loadRegionType(config, "daily");
    }

    public static void loadRegionTypePower(boolean consume) {
        FileConfiguration config = new YamlConfiguration();
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        reqs.add("g:glass*1");
        reqs.add("g:door*1");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_place");
        effects.add("block_break");
        effects.add("chest_use");
        config.set("effects", effects);
        if (consume) {
            config.set("upkeep.0.power-input", 1);
        } else {
            config.set("upkeep.0.power-output", 1);
        }
        ItemManager.getInstance().loadRegionType(config, "power");
    }

    public static void loadRegionTypeCobble4() {
        FileConfiguration config = new YamlConfiguration();
        config.set("max", 1);
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        reqs.add("g:glass*1");
        reqs.add("g:door*1");
        reqs.add("GRAVEL*3");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_place");
        effects.add("block_break");
        config.set("effects", effects);
        ArrayList<String> input = new ArrayList<>();
        ArrayList<String> output = new ArrayList<>();
        config.set("period", 60);
        input.add("IRON_PICKAXE*1");
        output.add("GOLDEN_PICKAXE*1");
        config.set("upkeep.0.input", input);
        config.set("upkeep.0.output", output);
        ItemManager.getInstance().loadRegionType(config, "cobble");
    }

    public static void loadRegionTypeCobble3() {
        FileConfiguration config = new YamlConfiguration();
        config.set("max", 1);
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        reqs.add("g:glass*1");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_place");
        effects.add("block_break");
        config.set("effects", effects);
        config.set("effect-radius", 7);
        config.set("period", 100);
        ArrayList<String> reagents = new ArrayList<>();
        reagents.add("IRON_PICKAXE");
        reagents.add("GOLD_BLOCK");
        reagents.add("IRON_BLOCK");
        config.set("upkeep.0.input", reagents);
        ArrayList<String> outputs = new ArrayList<>();
        outputs.add("COBBLESTONE");
        config.set("upkeep.0.output", outputs);
        ItemManager.getInstance().loadRegionType(config, "cobble");
    }

    public static void loadRegionTypeCobble2() {
        FileConfiguration config = new YamlConfiguration();
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*3");
        config.set("build-reqs", reqs);
        ArrayList<String> reagents = new ArrayList<>();
        reagents.add("IRON_PICKAXE");
        config.set("reagents", reagents);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_place");
        effects.add("block_break");
        config.set("effects", effects);
        config.set("effect-radius", 7);
        ItemManager.getInstance().loadRegionType(config, "cobble");
    }

    public static void loadRegionTypeCobbleQuarry() {
        FileConfiguration config = new YamlConfiguration();
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("CHEST*2");
        config.set("build-reqs", reqs);
        config.set("effect-radius", 7);
        ItemManager.getInstance().loadRegionType(config, "cobblequarry");
    }

    public static void loadRegionTypeActive() {
        FileConfiguration config = new YamlConfiguration();
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("CHEST*2");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_build");
        effects.add("block_break");
        effects.add("active:120960");
        config.set("build-radius", 5);
        config.set("effects", effects);
        ItemManager.getInstance().loadRegionType(config, "active");
    }

    public static void loadRegionTypeCobble() {
        FileConfiguration config = new YamlConfiguration();
        config.set("max", 1);
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2,GRASS_BLOCK*2");
        reqs.add("gold_block*1");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_build");
        effects.add("block_break");
        effects.add("housing:2");
        config.set("build-radius", 5);
        config.set("effects", effects);
        config.set("effect-radius", 7);
        config.set("period", 100);
        config.set("upkeep.0", new Object());
        ArrayList<String> reagents = new ArrayList<>();
        reagents.add("IRON_PICKAXE");
        config.set("input", reagents);
        ArrayList<String> outputs = new ArrayList<>();
        outputs.add("COBBLESTONE");
        config.set("output", outputs);
        ItemManager.getInstance().loadRegionType(config, "cobble");
    }

    public static void loadRegionTypeCobbleGroup() {
        FileConfiguration config = new YamlConfiguration();
        config.set("max", 1);
        ArrayList<String> groups = new ArrayList<>();
        groups.add("cobble");
        groups.add("utility");
        config.set("groups", groups);
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2,GRASS_BLOCK*2");
        reqs.add("gold_block*1");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_build");
        effects.add("block_break");
        config.set("build-radius", 5);
        config.set("effects", effects);
        config.set("effect-radius", 7);
        ItemManager.getInstance().loadRegionType(config, "town_hall");
    }
    public static void loadRegionTypeCobbleGroup2() {
        FileConfiguration config = new YamlConfiguration();
        config.set("max", 1);
        ArrayList<String> groups = new ArrayList<>();
        groups.add("utility");
        config.set("groups", groups);
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2,GRASS_BLOCK*2");
        reqs.add("gold_block*1");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_build");
        effects.add("block_break");
        config.set("build-radius", 5);
        config.set("effects", effects);
        config.set("effect-radius", 7);
        ItemManager.getInstance().loadRegionType(config, "purifier");
    }

    public static void loadRegionTypeDirt() {
        FileConfiguration config = new YamlConfiguration();
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("dirt*1");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        config.set("effects", effects);
        ItemManager.getInstance().loadRegionType(config, "dirt");
    }
    public static void loadRegionTypeRectangle() {
        FileConfiguration config = new YamlConfiguration();
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        config.set("effects", effects);
        config.set("build-radius", 3);
        config.set("build-radius-z", 10);
        ItemManager.getInstance().loadRegionType(config, "cobble");
    }
    public static void loadRegionTypeShelter() {
        FileConfiguration config = new YamlConfiguration();
        ArrayList<String> reqs = new ArrayList<>();
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_explosion");
        effects.add("deny_mob_spawn");
        effects.add("chest_use");
        effects.add("port");
        config.set("effects", effects);
        config.set("build-radius", 5);
        ItemManager.getInstance().loadRegionType(config, "shelter");
    }
    public static void loadRegionTypeUtility() {
        FileConfiguration config = new YamlConfiguration();
        ArrayList<String> reqs = new ArrayList<>();
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        config.set("effects", effects);
        config.set("build-radius", 5);
        config.set("upkeep.0.power-output", 96);
        ItemManager.getInstance().loadRegionType(config, "utility");
    }

    public static Region createNewRegion(String type) {
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        Location location1 = new Location(Bukkit.getWorld("world"), 4.5, 0.5, 0.5);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        Region region = new Region(type, owners, location1, getRadii(), (HashMap<String, String>) regionType.getEffects().clone(),0);
        RegionManager.getInstance().addRegion(region);
        return region;
    }
    public static Region createNewRegion(String type, Location location) {
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), Constants.OWNER);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        Region region = new Region(type, owners, location, getRadii(), (HashMap<String, String>) regionType.getEffects().clone(),0);
        RegionManager.getInstance().addRegion(region);
        return region;
    }

    public static Region createNewRegion(String type, UUID uuid) {
        Region region = createNewRegion(type);
        region.setPeople(uuid, Constants.OWNER);
        return region;
    }
    public static Region createNewRegion(String type, UUID uuid, Location location) {
        Region region = createNewRegion(type, location);
        region.setPeople(uuid, Constants.OWNER);
        return region;
    }
}
