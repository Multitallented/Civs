package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProtectionsTests {
    private Block block;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void onBefore() {
        new RegionManager();
        new TownManager();
        new RegionManager();
        block = mock(Block.class);
        when(block.getLocation()).thenReturn(new Location(TestUtil.world, 0, 0,0));
    }

    @Test
    public void blockBreakInProtectionShouldNotBeCancelled() {
        ProtectionHandler protectionHandler = new ProtectionHandler();
        Player player = mock(Player.class);
        BlockBreakEvent event = new BlockBreakEvent(block, player);
        protectionHandler.onBlockBreak(event);
        assertFalse(event.isCancelled());
    }

    @Test
    public void blockBreakInProtectionShouldBeCancelled() {
        RegionsTests.loadRegionTypeCobble();
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        UUID uuid = new UUID(1, 2);
        when(player.getUniqueId()).thenReturn(uuid);
        Player player2 = mock(Player.class);
        when(player2.getGameMode()).thenReturn(GameMode.SURVIVAL);
        UUID uuid2 = new UUID(1, 3);
        when(player2.getUniqueId()).thenReturn(uuid2);

        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(uuid2, "owner");
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        HashMap<String, String> effects = new HashMap<>();
        effects.put("block_break", null);
        effects.put("block_build", null);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, regionLocation, RegionsTests.getRadii(), effects,0));
        ProtectionHandler protectionHandler = new ProtectionHandler();
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block3, player);
        protectionHandler.onBlockBreak(event);
        assertTrue(event.isCancelled());
    }

    @Test
    public void blockPlaceShouldNotBeCancelledByOwner() {
        RegionsTests.loadRegionTypeCobble();
        Player player = mock(Player.class);
        UUID uuid = new UUID(1, 2);
        BlockPlaceEvent event = mock(BlockPlaceEvent.class);
        when(event.getBlockPlaced()).thenReturn(block);
        when(event.getPlayer()).thenReturn(player);
        when(player.getUniqueId()).thenReturn(uuid);

        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(uuid, "owner");
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, regionLocation, RegionsTests.getRadii(), new HashMap<String, String>(),0));

        ProtectionHandler protectionHandler = new ProtectionHandler();

        protectionHandler.onBlockPlace(event);
        assertFalse(event.isCancelled());
    }

    @Test
    public void blockPlaceShouldNotBeCancelledInUnprotected() {
        RegionsTests.loadRegionTypeDirt();

        Player player = mock(Player.class);
        UUID uuid = new UUID(1, 2);
        when(player.getUniqueId()).thenReturn(uuid);

        Player player2 = mock(Player.class);
        UUID uuid2 = new UUID(1, 3);
        when(player2.getUniqueId()).thenReturn(uuid2);

        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(uuid2, "owner");
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        RegionManager.getInstance().addRegion(new Region("dirt", owners, regionLocation, RegionsTests.getRadii(), new HashMap<String, String>(),0));

        ProtectionHandler protectionHandler = new ProtectionHandler();
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block3, player);
        protectionHandler.onBlockBreak(event);
        assertFalse(event.isCancelled());
    }

    @Test
    public void chestUseShouldBeCancelled() {
        RegionsTests.loadRegionTypeCobble();
        Player player = mock(Player.class);
        UUID uuid = new UUID(1, 2);
        when(player.getUniqueId()).thenReturn(uuid);
        Player player2 = mock(Player.class);
        UUID uuid2 = new UUID(1, 3);
        when(player2.getUniqueId()).thenReturn(uuid2);

        HashMap<UUID, String> owners = new HashMap<>();

        owners.put(uuid2, "owner");
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        HashMap<String, String> effects = new HashMap<>();
        effects.put("block_break", null);
        effects.put("block_build", null);
        effects.put("chest_use", null);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, regionLocation, RegionsTests.getRadii(), effects,0));
        ProtectionHandler protectionHandler = new ProtectionHandler();
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK,null,Bukkit.getWorld("world").getBlockAt(0,0,0), BlockFace.NORTH);
//        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block3, player);
        protectionHandler.onBlockInteract(event);
        assertTrue(event.isCancelled());
    }

    @Test
    public void chestUseShouldNotBeCancelled() {
        RegionsTests.loadRegionTypeCobble();
        Player player = mock(Player.class);
        UUID uuid = new UUID(1, 2);
        when(player.getUniqueId()).thenReturn(uuid);
        Player player2 = mock(Player.class);
        UUID uuid2 = new UUID(1, 3);
        when(player2.getUniqueId()).thenReturn(uuid2);

        HashMap<UUID, String> owners = new HashMap<>();

        owners.put(uuid2, "owner");
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        HashMap<String, String> effects = new HashMap<>();
        effects.put("block_break", null);
        effects.put("block_build", null);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, regionLocation, RegionsTests.getRadii(), effects,0));
        ProtectionHandler protectionHandler = new ProtectionHandler();
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK,null,Bukkit.getWorld("world").getBlockAt(0,0,0), BlockFace.NORTH);
//        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block3, player);
        protectionHandler.onBlockInteract(event);
        assertTrue(!event.isCancelled());
    }

    private void explodeInRegion(boolean throwException, Location regionLocation) throws SuccessException {
        RegionsTests.loadRegionTypeCobble();
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        HashMap<String, String> effects = new HashMap<>();
        Region region = new Region("cobble", people,
                regionLocation,
                RegionsTests.getRadii(),
                effects,0);
        RegionManager.getInstance().addRegion(region);
        TNTPrimed tntPrimed = mock(TNTPrimed.class);
        ArrayList<Block> blockList = new ArrayList<>();
        if (throwException) {
            when(Bukkit.getServer().getScheduler()).thenThrow(new SuccessException());
        }
        EntityExplodeEvent event = new EntityExplodeEvent(tntPrimed,
                regionLocation.add(0, 1,0),
                blockList,
                (float) 2);
        if (throwException) {
            ProtectionHandler protectionHandler = new ProtectionHandler();
            protectionHandler.onEntityExplode(event);
        }
    }

    @Test(expected = SuccessException.class)
    public void explosionShouldDestroyRegion() {
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0 , 0, 0);
        explodeInRegion(true, regionLocation);
    }

    @Test
    public void explosionCheckShouldRemoveRegionIfBlocksDestroyed() {
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0 , 0, -7);
        explodeInRegion(false, regionLocation);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        ProtectionHandler.CheckRegionBlocks checkRegionBlocks = protectionHandler.new CheckRegionBlocks(regionLocation);
        checkRegionBlocks.run();
        assertNull(RegionManager.getInstance().getRegionAt(regionLocation));
    }

    @Test
    public void explosionCheckShouldRemoveRegionIfCenterDestroyed() {
        Location regionLocation = new Location(Bukkit.getWorld("world"), -4 , 0, 0);
        explodeInRegion(false, regionLocation);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        ProtectionHandler.CheckRegionBlocks checkRegionBlocks = protectionHandler.new CheckRegionBlocks(regionLocation);
        checkRegionBlocks.run();
        assertNull(RegionManager.getInstance().getRegionAt(regionLocation));
    }

    @Test
    public void explosionInRegionShouldBeProtected() {
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0 , 0, 0);
        explodeInProtectedRegion(regionLocation, false);
        assertNotNull(RegionManager.getInstance().getRegionAt(regionLocation));
    }

    @Test
    public void explosionInTownShouldBeProtected() {
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0 , 0, 0);
        explodeInProtectedRegion(regionLocation, true);
        assertNotNull(RegionManager.getInstance().getRegionAt(regionLocation));
    }

    private void explodeInProtectedRegion(Location regionLocation, boolean useTown) {
        Region region;
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        HashMap<String, String> effects = new HashMap<>();
        if (useTown) {
            RegionsTests.loadRegionTypeCobble();
            region = new Region("cobble", people,
                    regionLocation,
                    RegionsTests.getRadii(),
                    effects,0);
            TownTests.loadTownTypeHamlet();
            TownTests.loadTown("testTown", "hamlet", regionLocation);
        } else {
            RegionsTests.loadRegionTypeShelter();
            effects.put("block_explosion", "");
            region = new Region("cobble", people,
                    regionLocation,
                    RegionsTests.getRadii(),
                    effects,0);
        }
        try {
            when(Bukkit.getServer().getScheduler()).thenThrow(new SuccessException());
        } catch (SuccessException e) {
            // Do nothing
        }
        RegionManager.getInstance().addRegion(region);
        TNTPrimed tntPrimed = mock(TNTPrimed.class);
        ArrayList<Block> blockList = new ArrayList<>();
        EntityExplodeEvent event = new EntityExplodeEvent(tntPrimed,
                regionLocation.add(0, 1,0),
                blockList,
                (float) 2);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.onEntityExplode(event);
    }

    @Test
    public void explosionShouldBeCancelled() {
        RegionsTests.loadRegionTypeShelter();
        RegionsTests.createNewRegion("shelter");
        Location secondaryLocation = new Location(Bukkit.getWorld("world"), 11, 0,0);
        Location tertiaryLocation = new Location(Bukkit.getWorld("world"), 15, 0,0);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        assertTrue(protectionHandler.shouldBlockActionEffect(TestUtil.block.getLocation(),null, "block_explosion", 5));
        assertTrue(protectionHandler.shouldBlockActionEffect(secondaryLocation,null, "block_explosion", 5));
        assertFalse(protectionHandler.shouldBlockActionEffect(tertiaryLocation,null, "block_explosion", 5));
    }

    @Test
    public void denyMonsterSpawnShouldProtect() {
        RegionsTests.loadRegionTypeShelter();
        RegionsTests.createNewRegion("shelter");
        Location location = TestUtil.block.getLocation();
        assertTrue(ProtectionHandler.shouldBlockAction(location, null, "deny_mob_spawn"));
    }

    @Test
    public void denyMonsterSpawnShouldProtectInTown() {
        TownTests.loadTownTypeHamlet();
        TownTests.loadTown("test", "hamlet", TestUtil.block.getLocation());
        Location location = TestUtil.block.getLocation();
        assertTrue(ProtectionHandler.shouldBlockAction(location, null, "deny_mob_spawn"));
    }
}
