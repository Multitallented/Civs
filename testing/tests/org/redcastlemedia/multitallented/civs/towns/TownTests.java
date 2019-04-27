package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.protections.ProtectionHandler;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TownTests {
    private TownManager townManager;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void onBefore() {
        townManager = new TownManager();
        new RegionManager();
    }

    @Test
    public void findTownAtShouldReturnTown() {
        loadTownTypeHamlet();
        Town town = loadTown("BizRep", "hamlet", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        loadTown("Silverstone", "hamlet", new Location(Bukkit.getWorld("world"), 100, 0, 0));
        loadTown("Cupcake", "hamlet", new Location(Bukkit.getWorld("world"), -100, 0, 0));

        assertEquals(town, townManager.getTownAt(new Location(Bukkit.getWorld("world"), 0, 0,0)));
    }

    @Test
    public void shouldNotFindTown() {
        loadTownTypeHamlet();
        loadTown("BizRep", "hamlet", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        assertNull(townManager.getTownAt(new Location(Bukkit.getWorld("world"), 0, 55,0)));
    }
    @Test
    public void shouldFindTown() {
        loadTownTypeHamlet();
        Town town = loadTown("BizRep", "hamlet", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        assertEquals(town, townManager.getTownAt(new Location(Bukkit.getWorld("world"), 0, 0,0)));
    }

    @Test
    public void memberShouldBeAdded() {
        loadTownTypeHamlet();
        Town town = loadTown("Aeria", "hamlet", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        UUID uuid = new UUID(1,5);
        townManager.addInvite(uuid, town);
        townManager.acceptInvite(uuid);
        assertEquals("member", town.getPeople().get(uuid));
    }

    @Test
    public void townsShouldIntersect() {
        loadTownTypeHamlet();
        loadTown("Summertown", "hamlet", new Location(Bukkit.getWorld("world"), 0, 0, 0));
        TownType townType = (TownType) ItemManager.getInstance().getItemType("hamlet");
        assertEquals(1, townManager.checkIntersect(new Location(Bukkit.getWorld("world"), 26, 0, 0), townType).size());
    }
    @Test
    public void townShouldNotIntersect() {
        loadTownTypeHamlet();
        loadTown("Summertown", "hamlet", new Location(Bukkit.getWorld("world"), 0, 0, 0));
        TownType townType = (TownType) ItemManager.getInstance().getItemType("hamlet");
        assertEquals(0, townManager.checkIntersect(new Location(Bukkit.getWorld("world"), 51, 0, 0), townType).size());
    }

    @Test
    public void townShouldDestroyWhenCriticalRegionDestroyed() {
        RegionsTests.loadRegionTypeCobble();
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        HashMap<String, String> effects = new HashMap<>();
        Location regionLocation = new Location(Bukkit.getWorld("world2"), 0,0,0);
        Region region = new Region("cobble", people,
                regionLocation,
                RegionsTests.getRadii(),
                effects,0);
        loadTownTypeTribe();
        Location townLocation = new Location(Bukkit.getWorld("world2"), 1,0,0);

        RegionManager regionManager = RegionManager.getInstance();
        TownManager townManager = TownManager.getInstance();
        regionManager.addRegion(region);
        loadTown("Sanmak-kol", "tribe", townLocation);
        if (townManager.getTowns().isEmpty()) {
            fail("No town found");
        }
        ProtectionHandler protectionHandler = new ProtectionHandler();
        Block block = mock(Block.class);
        when(block.getLocation()).thenReturn(regionLocation);
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, TestUtil.player);
        CivilianListener civilianListener = new CivilianListener();
        protectionHandler.onBlockBreak(blockBreakEvent);
        if (!blockBreakEvent.isCancelled()) {
            civilianListener.onCivilianBlockBreak(blockBreakEvent);
        }
//        regionManager.removeRegion(region, false);
        assertTrue(townManager.getTowns().isEmpty());
    }

    @Test
    public void townShouldNotBeDestroyedWhenNormalRegionDestroyed() {
        RegionsTests.loadRegionTypeCobble2();
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        HashMap<String, String> effects = new HashMap<>();
        Region region = new Region("cobble2", people,
                new Location(Bukkit.getWorld("world"), 0,0,0),
                RegionsTests.getRadii(),
                effects,0);
        loadTownTypeTribe();
        Location townLocation = new Location(Bukkit.getWorld("world"), 1,0,0);

        RegionManager regionManager = RegionManager.getInstance();
        TownManager townManager = TownManager.getInstance();
        regionManager.addRegion(region);
        Town town = loadTown("Sanmak-kol", "tribe", townLocation);
        regionManager.removeRegion(region, false, true);
        assertEquals(town, townManager.getTownAt(townLocation));
    }

    public static Town loadTown(String name, String type, Location location) {
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), "owner");
        Town town = new Town(name, type,
                location,
                owners, 500, 500, 2, 1, 0, -1);
        TownManager.getInstance().addTown(town);
        return town;
    }

    public static void loadTownTypeHamlet() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "Hamlet");
        config.set("type", "town");
        config.set("build-radius", 25);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_explosion");
        effects.add("deny_mob_spawn");
        config.set("effects", effects);
        ItemManager.getInstance().loadTownType(config, "hamlet");
    }

    public static void loadTownTypeTribe() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "Tribe");
        config.set("type", "town");
        ArrayList<String> critReqs = new ArrayList<>();
        critReqs.add("cobble");
        config.set("critical-build-reqs", critReqs);
        config.set("build-radius", 25);
        ItemManager.getInstance().loadTownType(config, "tribe");
    }
}
