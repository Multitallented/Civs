package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockPlaceEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegionsTests {
    private RegionManager regionManager;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void onBefore() {
        regionManager = new RegionManager();
    }

    @Test
    public void withNoRegionsShouldNotDetectRegion() {
        assertNull(RegionManager.getInstance().getRegionAt(TestUtil.block2.getLocation()));
    }

    @Test
    public void regionManagerShouldLoadRegionTypesFromConfig() {
        loadRegionTypeCobble();
        assertNotNull(regionManager.getRegionType("cobble"));
    }

    @Test
    public void regionShouldNotBeCreatedWithoutAllReqs() {
        loadRegionTypeCobble();

        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique);
        World world = Bukkit.getWorld("world");
        Block dirtBlock = mock(Block.class);
        when(dirtBlock.getType()).thenReturn(Material.DIRT);
        when(world.getBlockAt(1,0,0)).thenReturn(dirtBlock);
        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event1);
        assertNull(regionManager.getRegionAt(TestUtil.blockUnique.getLocation()));
    }

    @Test
    public void regionShouldBeCreatedWithAllReqs() {
        loadRegionTypeCobble();

        World world = Bukkit.getWorld("world");
        when(world.getBlockAt(1,0,0)).thenReturn(TestUtil.block2);
        when(world.getBlockAt(2,0,0)).thenReturn(TestUtil.block3);
        BlockPlaceEvent event3 = mock(BlockPlaceEvent.class);
        when(event3.getBlockPlaced()).thenReturn(TestUtil.block3);
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.block2);
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique);

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertEquals("cobble", regionManager.getRegionAt(TestUtil.blockUnique.getLocation()).getType());
    }

    @Test
    public void regionShouldNotBeCreatedIfNotAllReqs() {
        loadRegionTypeCobble();

        BlockPlaceEvent event3 = mock(BlockPlaceEvent.class);
        when(event3.getBlockPlaced()).thenReturn(TestUtil.block4);
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.block2);
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.blockUnique);
        World world = Bukkit.getWorld("world");
        Block dirtBlock = mock(Block.class);
        when(dirtBlock.getType()).thenReturn(Material.DIRT);
        when(world.getBlockAt(2,0,0)).thenReturn(dirtBlock);

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertNull(regionManager.getRegionAt(TestUtil.blockUnique.getLocation()));
    }

    public static void loadRegionTypeCobble() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "cobble");
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        config.set("requirements", reqs);
        RegionManager.getInstance().loadRegionType(config);
    }

}
