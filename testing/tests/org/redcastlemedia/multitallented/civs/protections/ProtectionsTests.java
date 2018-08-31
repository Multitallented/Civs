package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.Bukkit;
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
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;

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
        block = mock(Block.class);
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        when(block.getLocation()).thenReturn(new Location(world, 0, 0,0));
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
        RegionManager.getInstance().addRegion(new Region("cobble", owners, regionLocation, RegionsTests.getRadii(), effects));
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
        RegionManager.getInstance().addRegion(new Region("cobble", owners, regionLocation, RegionsTests.getRadii(), new HashMap<String, String>()));

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
        RegionManager.getInstance().addRegion(new Region("dirt", owners, regionLocation, RegionsTests.getRadii(), new HashMap<String, String>()));

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
        RegionManager.getInstance().addRegion(new Region("cobble", owners, regionLocation, RegionsTests.getRadii(), effects));
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
        RegionManager.getInstance().addRegion(new Region("cobble", owners, regionLocation, RegionsTests.getRadii(), effects));
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
                effects);
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
}
