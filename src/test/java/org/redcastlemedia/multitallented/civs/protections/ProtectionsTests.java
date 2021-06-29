package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.PlayerInventoryImpl;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProtectionsTests extends TestUtil {
    private Block block;

    @Before
    public void onBefore() {
        RegionManager.getInstance().reload();
        TownManager.getInstance().reload();
        block = mock(Block.class);
        when(block.getLocation()).thenReturn(new Location(TestUtil.world, 0, 0,0));
    }

    @Test
    public void missingBlocksShouldBeRemoved() {
        Region region = RegionsTests.createNewRegion("greenhouse");
        List<CVItem> tempList = new ArrayList<>();
        tempList.add(new CVItem(Material.DIRT, 1));
        region.getMissingBlocks().add(tempList);
        ProtectionHandler.getInstance().removeBlockFromMissingBlocks(region, Material.DIRT);
        assertTrue(region.getMissingBlocks().isEmpty());
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
        owners.put(uuid2, Constants.OWNER);
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
    public void blockBreakInProtectionInTownShouldBeCancelled() {
        Town town = TownTests.loadTown("test", "settlement", new Location(TestUtil.world, 0, 0, 0));
        RegionsTests.loadRegionTypeCobble();
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        UUID uuid = new UUID(1, 2);
        when(player.getUniqueId()).thenReturn(uuid);
        Player player2 = mock(Player.class);
        when(player2.getGameMode()).thenReturn(GameMode.SURVIVAL);
        UUID uuid2 = new UUID(1, 3);
        when(player2.getUniqueId()).thenReturn(uuid2);
        town.getRawPeople().put(uuid, "member");
        town.getRawPeople().put(uuid2, "member");

        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(uuid2, Constants.OWNER);
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
        owners.put(uuid, Constants.OWNER);
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, regionLocation, RegionsTests.getRadii(), new HashMap<>(),0));

        ProtectionHandler protectionHandler = new ProtectionHandler();

        protectionHandler.onBlockPlace(event);
        assertFalse(event.isCancelled());
    }

    @Test
    public void blockPlaceShouldNotBeCancelledInUnprotected() {
        RegionsTests.loadRegionTypeDirt();

        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(new PlayerInventoryImpl());
        UUID uuid = new UUID(1, 2);
        when(player.getUniqueId()).thenReturn(uuid);

        Player player2 = mock(Player.class);
        when(player2.getInventory()).thenReturn(new PlayerInventoryImpl());
        UUID uuid2 = new UUID(1, 3);
        when(player2.getUniqueId()).thenReturn(uuid2);

        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(uuid2, Constants.OWNER);
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        RegionManager.getInstance().addRegion(new Region("dirt", owners, regionLocation, RegionsTests.getRadii(), new HashMap<>(),0));

        ProtectionHandler protectionHandler = new ProtectionHandler();
        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block3, player);
        protectionHandler.onBlockBreak(event);
        assertFalse(event.isCancelled());
    }

    @Test
    public void chestUseShouldBeCancelled() {
        RegionsTests.loadRegionTypeCobble();
        Player player = mock(Player.class);
        when(player.getInventory()).thenReturn(new PlayerInventoryImpl());
        UUID uuid = new UUID(1, 2);
        when(player.getUniqueId()).thenReturn(uuid);
        Player player2 = mock(Player.class);
        when(player2.getInventory()).thenReturn(new PlayerInventoryImpl());
        UUID uuid2 = new UUID(1, 3);
        when(player2.getUniqueId()).thenReturn(uuid2);

        HashMap<UUID, String> owners = new HashMap<>();

        owners.put(uuid2, Constants.OWNER);
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
        when(player.getInventory()).thenReturn(new PlayerInventoryImpl());
        when(player.getUniqueId()).thenReturn(uuid);
        Player player2 = mock(Player.class);
        when(player2.getInventory()).thenReturn(new PlayerInventoryImpl());
        UUID uuid2 = new UUID(1, 3);
        when(player2.getUniqueId()).thenReturn(uuid2);

        HashMap<UUID, String> owners = new HashMap<>();

        owners.put(uuid2, Constants.OWNER);
        Location regionLocation = new Location(Bukkit.getWorld("world"), 0,0,0);
        HashMap<String, String> effects = new HashMap<>();
        effects.put("block_break", null);
        effects.put("block_build", null);
        RegionManager.getInstance().addRegion(new Region("cobble", owners, regionLocation, RegionsTests.getRadii(), effects,0));
        ProtectionHandler protectionHandler = new ProtectionHandler();
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK,null,Bukkit.getWorld("world").getBlockAt(0,0,0), BlockFace.NORTH);
//        BlockBreakEvent event = new BlockBreakEvent(TestUtil.block3, player);
        protectionHandler.onBlockInteract(event);
        assertFalse(event.isCancelled());
    }

    private void explodeInRegion(boolean throwException, Location regionLocation) throws SuccessException {
        RegionsTests.loadRegionTypeCobble();
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), Constants.OWNER);
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
        protectionHandler.checkRegionBlocks(regionLocation);
        assertNull(RegionManager.getInstance().getRegionAt(regionLocation));
    }

    @Test
    public void explosionCheckShouldRemoveRegionIfCenterDestroyed() {
        Location regionLocation = new Location(Bukkit.getWorld("world"), -4 , 0, 0);
        explodeInRegion(false, regionLocation);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        protectionHandler.checkRegionBlocks(regionLocation);
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
        people.put(TestUtil.player.getUniqueId(), Constants.OWNER);
        HashMap<String, String> effects = new HashMap<>();
        if (useTown) {
            RegionsTests.loadRegionTypeCobble();
            region = new Region("cobble", people,
                    regionLocation,
                    RegionsTests.getRadii(),
                    effects,0);
            TownTests.loadTownTypeHamlet2();
            TownTests.loadTown("testTown", "hamlet2", regionLocation);
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
        TownTests.loadTownTypeHamlet2();
        TownTests.loadTown("test", "hamlet2", TestUtil.block.getLocation());
        Location location = TestUtil.block.getLocation();
        assertTrue(ProtectionHandler.shouldBlockAction(location, null, "deny_mob_spawn"));
    }

    @Test
    public void chestAccessAtFourCornersShouldBeBlocked() {
        RegionsTests.loadRegionTypeShelter();
        Region shelter = RegionsTests.createNewRegion("shelter"); // 4.5 0.5 0.5
        Location location1 = new Location(Bukkit.getWorld("world"), 10, 0,6);
        Location location2 = new Location(Bukkit.getWorld("world"), 10, 0,-5);
        Location location3 = new Location(Bukkit.getWorld("world"), -1, 0,6);
        Location location4 = new Location(Bukkit.getWorld("world"), -1, 0,-5);
        ProtectionHandler protectionHandler = new ProtectionHandler();
        assertEquals(location1.getWorld().getUID() + "~4.5~0.5~0.5", shelter.getId());
        assertTrue(ProtectionHandler.shouldBlockAction(location1,null, "chest_use"));
        assertTrue(ProtectionHandler.shouldBlockAction(location2,null, "chest_use"));
        assertTrue(ProtectionHandler.shouldBlockAction(location3,null, "chest_use"));
        assertTrue(ProtectionHandler.shouldBlockAction(location4,null, "chest_use"));
    }
}
