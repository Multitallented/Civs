package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.scheduler.DailyScheduler;
import org.redcastlemedia.multitallented.civs.scheduler.RegionTickThread;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void regionManagerShouldLoadRegionTypesFromConfig() {
        loadRegionTypeCobble();
        assertNotNull(ItemManager.getInstance().getItemType("cobble"));
    }

    @Test
    public void regionManagerShouldGetRegionBasedOnLocation() {
        Location location = new Location(Bukkit.getWorld("world"), 100, 0, 0);
        HashMap<UUID, String> people = new HashMap<>();
        regionManager.addRegion(new Region("cobble", people, location, getRadii(), new HashMap<String, String>()));
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

//        World world = Bukkit.getWorld("world");
//        when(world.getBlockAt(1,0,0)).thenReturn(TestUtil.block2);
//        when(world.getBlockAt(2,0,0)).thenReturn(TestUtil.block3);
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
        assertEquals("cobble", regionManager.getRegionAt(regionLocation).getType());
    }

    @Test
    public void regionShouldBeCreatedWithAllGroupReqs() {
        loadRegionTypeCobble3();

        World world = Bukkit.getWorld("world");
//        when(world.getBlockAt(1,0,0)).thenReturn(TestUtil.block2);
//        when(world.getBlockAt(2,0,0)).thenReturn(TestUtil.block3);
//        when(world.getBlockAt(1,1,1)).thenReturn(TestUtil.block9);
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
        assertEquals("cobble", regionManager.getRegionAt(TestUtil.blockUnique.getLocation()).getType());
    }

    @Test
    public void regionShouldReportCorrectMissingGroupReqs() {
        loadRegionTypeCobble4();

        Location regionLocation = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        List<HashSet<CVItem>> missingItems = Region.hasRequiredBlocks("cobble", regionLocation, null);
        List<String> missingMessage = regionManager.generateMissingReqsMessage(missingItems);
//        for (String s : missingMessage) {
//            System.out.println(s);
//        }
        assertTrue(missingMessage.get(0).startsWith("§cGRAVEL"));
    }

    @Test
    public void regionShouldNotBeCreatedWithAllReqsOutOfBounds() {
        loadRegionTypeCobble();

        World world = Bukkit.getWorld("world");
//        when(world.getBlockAt(1,50,0)).thenReturn(TestUtil.block2);
//        when(world.getBlockAt(11,50,0)).thenReturn(TestUtil.block3);
//        when(world.getBlockAt(2,50,0)).thenReturn(TestUtil.blockUnique2);
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
        Region region = regionManager.getRegionAt(TestUtil.blockUnique3.getLocation());
        assertEquals(3, region.getRadiusXN());
        assertEquals(7, region.getRadiusXP());
        assertEquals(5, region.getRadiusYN());
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
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<String, String>()));
        regionManager.addRegion(new Region("cobble", owners, location2, getRadii(), new HashMap<String, String>()));
        regionManager.addRegion(new Region("cobble", owners, location3, getRadii(), new HashMap<String, String>()));
        regionManager.addRegion(new Region("cobble", owners, location4, getRadii(), new HashMap<String, String>()));
        regionManager.addRegion(new Region("cobble", owners, location5, getRadii(), new HashMap<String, String>()));
        regionManager.addRegion(new Region("cobble", owners, location6, getRadii(), new HashMap<String, String>()));
        regionManager.addRegion(new Region("cobble", owners, location7, getRadii(), new HashMap<String, String>()));
        assertSame(location5, regionManager.getRegionAt(location5).getLocation());
        assertSame(location2, regionManager.getRegionAt(location2).getLocation());
        assertSame(location1, regionManager.getRegionAt(location1).getLocation());
        assertSame(location7, regionManager.getRegionAt(location7).getLocation());
        regionManager.addRegion(new Region("cobble", owners, location8, getRadii(), new HashMap<String, String>()));
        assertSame(location8, regionManager.getRegionAt(location8).getLocation());
    }

    @Test
    public void shouldNotBeAbleToCreateARegionOnTopOfAnotherRegion() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<String, String>());
        regionManager.addRegion(region);


//        World world = Bukkit.getWorld("world");
//        when(world.getBlockAt(1,0,0)).thenReturn(TestUtil.block2);
//        when(world.getBlockAt(2,0,0)).thenReturn(TestUtil.block3);
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

        assertNotNull(regionManager.getRegionAt(TestUtil.blockUnique4.getLocation()));
    }

    @Test
    public void playerShouldBeInMultipleEffectRadii() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Location location2 = new Location(Bukkit.getWorld("world"), 5, 0, 0);
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<String, String>()));
        regionManager.addRegion(new Region("cobble", owners, location2, getRadii(), new HashMap<String, String>()));
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        assertEquals(2, regionManager.getRegionEffectsAt(TestUtil.player.getLocation(), regionType.getEffectRadius() - regionType.getBuildRadius()).size());
    }

    @Test
    public void regionsInDifferentWorldsShouldntCollide() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Location location2 = new Location(Bukkit.getWorld("world2"), 0, 0, 0);
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<String, String>()));
        regionManager.addRegion(new Region("cobble", owners, location2, getRadii(), new HashMap<String, String>()));
        assertEquals(1, regionManager.getRegionEffectsAt(TestUtil.player.getLocation(), 0).size());
    }

    @Test
    public void regionShouldNotBeBuiltTooClose() {
        loadRegionTypeShelter();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 500, 0, 0);
        regionManager.addRegion(new Region("shelter", owners, location1, getRadii(), new HashMap<String, String>()));

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
    public void regionShouldBeDestroyed() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<String, String>()));
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.blockUnique, TestUtil.player);
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianBlockBreak(event);
        RegionListener regionListener = new RegionListener();
        regionListener.onBlockBreak(event);
        assertNull(regionManager.getRegionAt(location1));
    }

    @Test
    public void regionShouldNotBeDestroyed() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<String, String>()));
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block10, TestUtil.player);
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianBlockBreak(event);
        RegionListener regionListener = new RegionListener();
        regionListener.onBlockBreak(event);
        assertNotNull(regionManager.getRegionAt(location1));
    }

    @Test
    public void regionShouldBeDestroyedAndRebuilt() {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        UUID uuid = new UUID(1, 4);
        owners.put(uuid, "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        regionManager.addRegion(new Region("cobble", owners, location1, getRadii(), new HashMap<String, String>()));
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.blockUnique, TestUtil.player);
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianBlockBreak(event);
        RegionListener regionListener = new RegionListener();
        regionListener.onBlockBreak(event);
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        Block block2 = TestUtil.createUniqueBlock(Material.CHEST, "Civs cobble", location1, false);
        when(event1.getBlockPlaced()).thenReturn(block2);
        CVItem cvItem = CVItem.createCVItemFromString("CHEST");
        cvItem.setDisplayName("Civs cobble");
        ItemStack itemStack = cvItem.createItemStack();
        when(event1.getItemInHand()).thenReturn(itemStack);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        regionListener.onBlockPlace(event1);
        assertNotNull(regionManager.getRegionAt(location1));
    }

    @Test
    public void regionShouldNotHaveReagents() {
        loadRegionTypeCobble3();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<>());
        regionManager.addRegion(region);
        assertFalse(region.hasUpkeepItems());
    }
    @Test
    public void regionShouldHaveReagents() {
        loadRegionTypeCobble4();
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("cobble", owners, location1, getRadii(), new HashMap<>());
        regionManager.addRegion(region);
        assertTrue(region.hasUpkeepItems());
    }

    @Test
    public void regionShouldNotHavePowerReagents() {
        loadRegionTypePower(true);
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>());
        regionManager.addRegion(region);
        assertFalse(region.hasUpkeepItems());
    }

    @Test
    public void regionShouldHavePowerReagents() {
        loadRegionTypePower(true);
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>());
        regionManager.addRegion(region);
        TownTests.loadTownTypeHamlet();
        Town town = new Town("townName", "hamlet", location1,
                owners, 300, 300, 2, 1);
        TownManager.getInstance().addTown(town);
        assertTrue(region.hasUpkeepItems());
        region.runUpkeep();
        assertEquals(299, town.getPower());
    }

    @Test
    public void regionShouldAddPowerToTown() {
        loadRegionTypePower(false);
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>());
        regionManager.addRegion(region);
        TownTests.loadTownTypeHamlet();
        Town town = new Town("townName", "hamlet", location1,
                owners, 300, 305, 2, 1);
        TownManager.getInstance().addTown(town);
        region.runUpkeep();
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
        Region region = new Region("daily", owners, location1, getRadii(), new HashMap<>());
        regionManager.addRegion(region);
        TownTests.loadTownTypeHamlet();
        Town town = new Town("townname", "hamlet", location1,
                owners, 300, 305, 2, 1);
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
        Region region = new Region("daily", owners, location1, getRadii(), new HashMap<>());
        regionManager.addRegion(region);
        TownTests.loadTownTypeHamlet();
        Town town = new Town("townname", "hamlet", location1,
                owners, 300, 305, 2, 1);
        TownManager.getInstance().addTown(town);
        new RegionTickThread().run();
        assertEquals(300, town.getPower());
    }

    private Region load2TownsWith1Region(UUID uuid1, boolean allied) {
        loadRegionTypeCobble();
        HashMap<UUID, String> owners = new HashMap<>();
        UUID uuid = new UUID(1, 4);
        owners.put(uuid, "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 3, 100, 0);
        Region region = new Region("power", owners, location1, getRadii(), new HashMap<>());
        regionManager.addRegion(region);

        TownTests.loadTownTypeHamlet();
        Town town = new Town("townname", "hamlet", location1,
                new HashMap<>(), 300, 300, 2, 1);
        townManager.addTown(town);

        Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        Town town1 = new Town("townname1", "hamlet", location,
                new HashMap<>(), 300, 300, 2, 1);
        town1.getPeople().put(uuid1, "member");
        townManager.addTown(town1);
        if (allied) {
            town.getAllies().add("townname1");
            town1.getAllies().add("townname");
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
        ArrayList<String> inputs = new ArrayList<>();
        inputs.add("IRON_PICKAXE*1");
        config.set("upkeeps.0.inputs", inputs);
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

    public static void loadRegionTypeCobble() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "cobble");
        config.set("max", 1);
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        reqs.add("gold_block*1");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_place");
        effects.add("block_break");
        config.set("effects", effects);
        config.set("effect-radius", 7);
        config.set("period", 100);
        ArrayList<String> reagents = new ArrayList<>();
        reagents.add("IRON_PICKAXE");
        config.set("input", reagents);
        ArrayList<String> outputs = new ArrayList<>();
        outputs.add("COBBLESTONE");
        config.set("output", outputs);
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
        config.set("effects", effects);
        config.set("build-radius", 5);
        ItemManager.getInstance().loadRegionType(config);
    }

    public static Region createNewRegion(String type) {
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(new UUID(1, 4), "owner");
        Location location1 = new Location(Bukkit.getWorld("world"), 4, 0, 0);
        Region region = new Region(type, owners, location1, getRadii(), new HashMap<String, String>());
        RegionManager.getInstance().addRegion(region);
        return region;
    }
}
