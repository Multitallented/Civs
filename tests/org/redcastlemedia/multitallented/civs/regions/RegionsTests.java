package org.redcastlemedia.multitallented.civs.regions;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

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
    public void regionShouldHaveAType() {
        Region region = new Region("cobble");
        assertEquals("cobble", region.getType());
    }

    @Test
    public void regionShouldNotBeCreatedWithoutAllReqs() {
        loadRegionTypeCobble();

        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.block);
        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event1);
        assertNull(regionManager.getRegionAt(TestUtil.block.getLocation()));
    }

    @Test
    public void regionShouldBeCreatedWithAllReqs() {
        loadRegionTypeCobble();

        BlockPlaceEvent event3 = mock(BlockPlaceEvent.class);
        when(event3.getBlockPlaced()).thenReturn(TestUtil.block3);
        BlockPlaceEvent event2 = mock(BlockPlaceEvent.class);
        when(event2.getBlockPlaced()).thenReturn(TestUtil.block2);
        BlockPlaceEvent event1 = mock(BlockPlaceEvent.class);
        when(event1.getPlayer()).thenReturn(TestUtil.player);
        when(event1.getBlockPlaced()).thenReturn(TestUtil.block);

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertEquals("cobble", regionManager.getRegionAt(TestUtil.block.getLocation()).getType());
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
        when(event1.getBlockPlaced()).thenReturn(TestUtil.block);

        RegionListener regionListener = new RegionListener();
        regionListener.onBlockPlace(event2);
        regionListener.onBlockPlace(event3);
        regionListener.onBlockPlace(event1);
        assertNull(regionManager.getRegionAt(TestUtil.block.getLocation()).getType());
    }

    private void loadRegionTypeCobble() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "cobble");
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        config.set("requirements", reqs);
        regionManager.loadRegionType(config);
    }

//    private Block createUniqueChestCobble() {
//        CVItem cvItem = new CVItem(Material.CHEST, 24, 1, -1, 100, "CobbleChest");
//        Block block = mock(Block.class);
//        when(block.getType()).thenReturn(Material.CHEST);
//        BlockState blockState = mock(BlockState.class);
//        MaterialData materialData = mock(MaterialData.class);
//        when(block.getState()).thenReturn(blockState);
//        when(blockState.getData()).thenReturn(materialData);
//        ItemStack is = cvItem.createItemStack();
//        when(materialData.toItemStack()).thenReturn(is);
//        when(block.getType()).thenReturn(Material.CHEST);
//        return block;
//    }
}
