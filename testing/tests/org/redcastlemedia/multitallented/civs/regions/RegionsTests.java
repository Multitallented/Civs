package org.redcastlemedia.multitallented.civs.regions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.ItemStackImpl;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.PortMenuTests;
import org.redcastlemedia.multitallented.civs.menus.RecipeMenuTests;
import org.redcastlemedia.multitallented.civs.protections.ProtectionHandler;
import org.redcastlemedia.multitallented.civs.scheduler.DailyScheduler;
import org.redcastlemedia.multitallented.civs.scheduler.RegionTickThread;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.util.CVItem;

public class RegionsTests {
    private RegionManager regionManager;
    private TownManager townManager;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void onBefore() {
        regionManager = new RegionManager();
        townManager = new TownManager();
        new ItemManager();
    }

    @Test
    public void withNoRegionsShouldNotDetectRegion() {
        assertNull(RegionManager.getInstance().getRegionAt(TestUtil.block2.getLocation()));
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
//            System.out.println("Index: 0, Size: " + (i+1));
            assertEquals(region1, RegionManager.getInstance().getRegionAt(location1));
//            System.out.println("Index: " + (i+1) + ", Size: " + (i+1));
            assertEquals(region, RegionManager.getInstance().getRegionAt(location));
        }
        Location finalLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
//        System.out.println("Null Index, Size: 25");
        assertNull(RegionManager.getInstance().getRegionAt(finalLocation));
        for (int i = 0; i < 24; i++) {
//            System.out.println("Index: " + (i+1) + ", Size: 25");
            Location location = new Location(Bukkit.getWorld("world"), 500 + 20 * i, 70, 500+ 20 *i);
            assertNotNull(RegionManager.getInstance().getRegionAt(location));
        }
    }

    @Test
    public void regionManagerShouldGetRegionBasedOnLocation() {
        Location location = new Location(Bukkit.getWorld("world"), 100, 0, 0);
        HashMap<UUID, String> people = new HashMap<>();
        regionManager.addRegion(new Region("cobble", people, location, getRadii(), new HashMap<String, String>(),0));
        assertNull(regionManager.getRegionAt(new Location(Bukkit.getWorld("world"), 0,0,0)));
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
        assertNull(regionManager.getRegionAt(TestUtil.blockUnique.getLocation()));
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
        Location regionLocation = new Location(Bukkit.getWorld("world"), -4 , 0, 0);
        Block chestBlock = TestUtil.createUniqueBlock(Material.CHEST, "Civs cobble", regionLocation, false);
        when(event1.getBlockPlaced()).thenReturn(chestBlock);
        List<String> lore = new ArrayList<>();
        lore.add(TestUtil.player.getUniqueId().toString());
        lore.add("Civs Cobble");
        ItemStack itemStack = TestUtil.mockItemStack(Material.CHEST, 1, "Civs Cobble", lore);
        doReturn(itemStack).when(event1).getItemInHand();

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertEquals("cobble", regionManager.getRegionAt(regionLocation).getType());
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
        lore.add("Civs Cobble");
        doReturn(TestUtil.mockItemStack(Material.CHEST, 1, "Civs Cobble", lore)).when(event1).getItemInHand();

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertEquals("cobble", regionManager.getRegionAt(TestUtil.blockUnique.getLocation()).getType());
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
        assertNull(regionManager.getRegionAt(TestUtil.blockUnique2.getLocation()));
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
        lore.add("Civs Cobble");
        doReturn(TestUtil.mockItemStack(Material.CHEST, 1, "Civs Cobble", lore)).when(event1).getItemInHand();

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        Region region = regionManager.getRegionAt(TestUtil.blockUnique3.getLocation());
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
        assertNotNull(regionManager.getRegionAt(TestUtil.blockUnique9.getLocation()));
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
        assertNotNull(regionManager.getRegionAt(TestUtil.blockUnique10.getLocation()));
    }

    @Test
    public void regionShouldNotBeCreatedIfNotAllReqs() {
        loadRegionTypeCobble2();

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

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertNull(regionManager.getRegionAt(TestUtil.blockUnique.getLocation()));
    }

    @Test
    public void regionManagerShouldFindSingleRegion() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 100, 0, 0);
        Location location2 = new Location(Bukkit.getWorld("world"), 0, 100, 0);
        Location location3 = new Location(Bukkit.getWorld("world"), 100, 100, 50);
        Location location4 = new Location(Bukkit.getWorld("world"), 0, 100, 50);
        Location location5 = new Location(Bukkit.getWorld("world"), 500, 100, 50);
        Location location6 = new Location(Bukkit.getWorld("world"), 100, 1000, 0);
        Location location7 = new Location(Bukkit.getWorld("world"), 1000, 0, 0);
        Location location8 = new Location(Bukkit.getWorld("world"), 1050, 0, 0);
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0));
        regionManager.addRegion(new Region("cobble", owners, location2, getRadii(), new HashMap<>(),0));
        regionManager.addRegion(new Region("cobble", owners, location3, getRadii(), new HashMap<>(),0));
        regionManager.addRegion(new Region("cobble", owners, location4, getRadii(), new HashMap<>(),0));
        regionManager.addRegion(new Region("cobble", owners, location5, getRadii(), new HashMap<>(),0));
        regionManager.addRegion(new Region("cobble", owners, location6, getRadii(), new HashMap<>(),0));
        regionManager.addRegion(new Region("cobble", owners, location7, getRadii(), new HashMap<>(),0));
        assertSame(location5, regionManager.getRegionAt(location5).getLocation());
        assertSame(location2, regionManager.getRegionAt(location2).getLocation());
        assertSame(location1, regionManager.getRegionAt(location1).getLocation());
        assertSame(location7, regionManager.getRegionAt(location7).getLocation());
        regionManager.addRegion(new Region("cobble", owners, location8, getRadii(), new HashMap<>(),0));
        assertSame(location8, regionManager.getRegionAt(location8).getLocation());
    }

    @Test
    public void shouldNotBeAbleToCreateARegionOnTopOfAnotherRegion() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0);
        regionManager.addRegion(region);


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
        assertEquals(region, regionManager.getRegionAt(location1));
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
        lore.add("Civs Cobble");
        ItemStack itemStack = TestUtil.mockItemStack(Material.CHEST, 1, "Civs Cobble", lore);
        doReturn(itemStack).when(event1).getItemInHand();


        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);

        assertNotNull(regionManager.getRegionAt(TestUtil.blockUnique4.getLocation()));
    }

    @Test
    public void playerShouldBeInMultipleEffectRadii() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Location location2 = new Location(Bukkit.getWorld("world"), 5, 0, 0);
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0));
        regionManager.addRegion(new Region("cobble", owners, location2, getRadii(), new HashMap<>(),0));
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        assertEquals(2, regionManager.getRegionEffectsAt(TestUtil.player.getLocation(), regionType.getEffectRadius() - regionType.getBuildRadius()).size());
    }

    @Test
    public void regionsInDifferentWorldsShouldntCollide() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        World world3 = mock(World.class);
        when(world3.getUID()).thenReturn(new UUID(1, 7));
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Location location2 = new Location(world3, 0, 0, 0);
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0));
        regionManager.addRegion(new Region("cobble", owners, location2, getRadii(), new HashMap<>(),0));
        assertEquals(1, regionManager.getRegionEffectsAt(TestUtil.player.getLocation(), 0).size());
    }

    @Test
    public void regionShouldNotBeBuiltTooClose() {
        loadRegionTypeShelter();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 500, 0, 0);
        regionManager.addRegion(new Region("shelter", owners, location1, getRadii(), new HashMap<>(),0));

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
        assertNull(regionManager.getRegionAt(TestUtil.blockUnique6.getLocation()));
        regionListener.onBlockPlace(event2);
        assertNotNull(regionManager.getRegionAt(TestUtil.blockUnique7.getLocation()));
    }

    @Test
    public void regionShouldBeDestroyedCenter() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0));
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.blockUnique, TestUtil.player);
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianBlockBreak(event);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        assertNull(regionManager.getRegionAt(location1));
    }

    @Test
    public void regionShouldBeDestroyedExtra() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        Region region = new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0);
        regionManager.addRegion(region);
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block3, TestUtil.player);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        assertNotNull(regionManager.getRegionAt(location1));
        assertFalse(event.isCancelled());
    }

    @Test
    public void regionShouldBeNotDestroyedUnrelated() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        Region region = new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0);
        regionManager.addRegion(region);
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block4, TestUtil.player);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        assertNotNull(regionManager.getRegionAt(location1));
    }

    @Test
    public void regionShouldBeNotDestroyedSecondary() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        Region region = new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0);
        regionManager.addRegion(region);
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block10, TestUtil.player);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        assertNotNull(regionManager.getRegionAt(location1));
    }

    @Test
    public void regionShouldNotBeDestroyed() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        Region region = new Region("cobble", owners, location1, getRadii(), regionType.getEffects(),0);
        regionManager.addRegion(region);
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block10, TestUtil.player);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(event);
        assertNotNull(regionManager.getRegionAt(location1));
        assertTrue(event.isCancelled());
    }

    @Test
    public void regionShouldBeDestroyedAndRebuilt() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        UUID uuid = new UUID(1, 4);
        owners.put(uuid, "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0));
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
        assertNotNull(regionManager.getRegionAt(location1));
    }

    @Test
    public void regionShouldHaveUpkeep() {
        loadRegionTypeUtility();
        TownTests.loadTownTypeHamlet();
        Location location = new Location(Bukkit.getWorld("world"), 4,0,0);
        TownTests.loadTown("test", "hamlet", location);
        Region region = RegionsTests.createNewRegion("utility");
        assertTrue(region.needsReagentsOrInput());
    }

    @Test
    public void regionShouldNotHaveReagents() {
        loadRegionTypeCobble3();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0);
        regionManager.addRegion(region);
        assertFalse(region.hasUpkeepItems());
    }
    @Test
    @Ignore // TODO fix this
    public void regionShouldHaveReagents() {
        loadRegionTypeCobble4();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0);
        regionManager.addRegion(region);
        assertTrue(region.hasUpkeepItems());
    }

    @Test
    @Ignore // TODO fix this
    public void regionShouldRunUpkeep() {
        loadRegionTypeCobble4();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<>(),0);
        regionManager.addRegion(region);
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
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>(),0);
        regionManager.addRegion(region);
        assertFalse(region.hasUpkeepItems());
    }

    @Test
    public void regionShouldHavePowerReagents() {
        loadRegionTypePower(true);
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>(),0);
        regionManager.addRegion(region);
        TownTests.loadTownTypeHamlet();
        Town town = new Town("townName", "hamlet", location1,
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
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>(),0);
        regionManager.addRegion(region);
        TownTests.loadTownTypeHamlet();
        Town town = new Town("townName", "hamlet", location1,
                owners, 300, 305, 2, 0, -1);
        TownManager.getInstance().addTown(town);
        region.runUpkeep(false);
        assertEquals(301, town.getPower());
    }

    @Test
    public void regionShouldConsiderAlliesAsGuests() {
        UUID uuid1 = new UUID(1, 3);
        Region region = load2TownsWith1Region(uuid1, true);
        assertEquals("ally", region.getPeople().get(uuid1));
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
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("daily", owners, location1, getRadii(), new HashMap<>(),0);
        regionManager.addRegion(region);
        TownTests.loadTownTypeHamlet();
        Town town = new Town("townname", "hamlet", location1,
                owners, 300, 305, 2, 0, -1);
        TownManager.getInstance().addTown(town);
        new DailyScheduler().run();
        assertEquals(302, town.getPower());
    }

    @Test
    public void dailyRegionShouldNotRunUpkeepTick() {
        loadRegionTypeDaily();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("daily", owners, location1, getRadii(), new HashMap<>(),0);
        regionManager.addRegion(region);
        TownTests.loadTownTypeHamlet();
        Town town = new Town("townname", "hamlet", location1,
                owners, 300, 305, 2, 0, -1);
        TownManager.getInstance().addTown(town);
        new RegionTickThread().run();
        assertEquals(300, town.getPower());
    }

    @Test
    public void offCenterRegionShouldDestroy() {
        loadRegionTypeCobbleQuarry();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 6), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 300, 100, 0);
        Region region = new Region("cobblequarry", owners, location1, getRadii(9,5,7,7,7,7), new HashMap<>(),0);
        regionManager.addRegion(region);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(TestUtil.blockUnique8, TestUtil.player);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onBlockBreak(blockBreakEvent);
        assertNull(regionManager.getRegionAt(location1));
    }

    @Test
    public void membersShouldBeAbleToOpenChests() {
        loadRegionTypePower(false);
        Region region = PortMenuTests.loadRegion("power");
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
        assertTrue(!regions.isEmpty());
    }

    private Region load2TownsWith1Region(UUID uuid1, boolean allied) {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        UUID uuid = new UUID(1, 4);
        owners.put(uuid, "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>(),0);
        regionManager.addRegion(region);

        TownTests.loadTownTypeHamlet();
        Town town = new Town("townname", "hamlet", location1,
                new HashMap<>(), 300, 300, 2, 0, -1);
        townManager.addTown(town);

        Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Town town1 = new Town("townname1", "hamlet", location,
                new HashMap<>(), 300, 300, 2, 0, -1);
        town1.getPeople().put(uuid1, "member");
        townManager.addTown(town1);
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
        config.set("name", "daily");
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_place");
        effects.add("block_break");
        config.set("effects", effects);
        config.set("upkeep.0.power-output", 2);
        config.set("period", "daily");
        ItemManager.getInstance().loadRegionType(config);
    }

    public static void loadRegionTypePower(boolean consume) {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "power");
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
        ItemManager.getInstance().loadRegionType(config);
    }

    public static void loadRegionTypeCobble4() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "cobble");
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
        ItemManager.getInstance().loadRegionType(config);
    }

    public static void loadRegionTypeCobble3() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "cobble");
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
        ItemManager.getInstance().loadRegionType(config);
    }

    public static void loadRegionTypeCobble2() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "cobble");
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
        ItemManager.getInstance().loadRegionType(config);
    }

    public static void loadRegionTypeWarehouse() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "warehouse");
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_place");
        effects.add("block_break");
        effects.add("warehouse");
        config.set("effects", effects);
        ItemManager.getInstance().loadRegionType(config);
    }

    public static void loadRegionTypeCobbleQuarry() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "cobblequarry");
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("CHEST*2");
        config.set("build-reqs", reqs);
        config.set("effect-radius", 7);
        ItemManager.getInstance().loadRegionType(config);
    }

    public static void loadRegionTypeCobble() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "cobble");
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
        ItemManager.getInstance().loadRegionType(config);
    }

    public static void loadRegionTypeCobbleGroup() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "town_hall");
        config.set("max", 1);
        ArrayList<String> groups = new ArrayList<>();
        groups.add("cobble");
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
        ItemManager.getInstance().loadRegionType(config);
    }

    public static void loadRegionTypeDirt() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "dirt");
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("dirt*1");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        config.set("effects", effects);
        ItemManager.getInstance().loadRegionType(config);
    }
    public static void loadRegionTypeRectangle() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "cobble");
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        config.set("effects", effects);
        config.set("build-radius", 3);
        config.set("build-radius-z", 10);
        ItemManager.getInstance().loadRegionType(config);
    }
    public static void loadRegionTypeShelter() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "shelter");
        ArrayList<String> reqs = new ArrayList<>();
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_explosion");
        effects.add("deny_mob_spawn");
        effects.add("port");
        config.set("effects", effects);
        config.set("build-radius", 5);
        ItemManager.getInstance().loadRegionType(config);
    }
    public static void loadRegionTypeUtility() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "utility");
        ArrayList<String> reqs = new ArrayList<>();
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        config.set("effects", effects);
        config.set("build-radius", 5);
        config.set("upkeep.0.power-output", 96);
        ItemManager.getInstance().loadRegionType(config);
    }

    public static Region createNewRegion(String type) {
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        Region region = new Region(type, owners, location1, getRadii(), (HashMap) regionType.getEffects().clone(),0);
        RegionManager.getInstance().addRegion(region);
        return region;
    }
    public static Region createNewRegion(String type, Location location) {
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        Region region = new Region(type, owners, location, getRadii(), (HashMap) regionType.getEffects().clone(),0);
        RegionManager.getInstance().addRegion(region);
        return region;
    }

    public static Region createNewRegion(String type, UUID uuid) {
        Region region = createNewRegion(type);
        region.setPeople(uuid, "owner");
        return region;
    }
    public static Region createNewRegion(String type, UUID uuid, Location location) {
        Region region = createNewRegion(type, location);
        region.setPeople(uuid, "owner");
        return region;
    }
}
