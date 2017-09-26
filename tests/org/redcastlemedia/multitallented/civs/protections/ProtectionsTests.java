package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;

import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.Assert.*;
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
        HashSet<UUID> owners = new HashSet<>();
        owners.add(uuid2);
        HashSet<UUID> members = new HashSet<>();
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, members, regionLocation));
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
        HashSet<UUID> owners = new HashSet<>();
        owners.add(uuid);
        HashSet<UUID> members = new HashSet<>();
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, members, regionLocation));

        ProtectionHandler protectionHandler = new ProtectionHandler();

        protectionHandler.onBlockPlace(event);
        assertFalse(event.isCancelled());
    }
}
