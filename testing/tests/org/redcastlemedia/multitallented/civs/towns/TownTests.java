package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.commands.TownCommand;
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
        RegionsTests.loadRegionTypeShelter();
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        HashMap<String, String> effects = new HashMap<>();
        Region region = new Region("shelter", people,
                new Location(Bukkit.getWorld("world"), 0,0,0),
                RegionsTests.getRadii(),
                effects,0);
        RegionsTests.createNewRegion("cobble");
        loadTownTypeTribe();
        Location townLocation = new Location(Bukkit.getWorld("world"), 1,0,0);

        RegionManager regionManager = RegionManager.getInstance();
        TownManager townManager = TownManager.getInstance();
        regionManager.addRegion(region);
        Town town = loadTown("Sanmak-kol", "tribe", townLocation);
        regionManager.removeRegion(region, false, true);
        assertEquals(town, townManager.getTownAt(townLocation));
    }

    @Test
    public void townShouldBeCreated() {
        RegionsTests.loadRegionTypeCobble();
        loadTownTypeTribe();
        RegionsTests.createNewRegion("cobble", TestUtil.player.getUniqueId());
        TownCommand townCommand = new TownCommand();
        String[] params = new String[2];
        params[0] = "town";
        params[1] = "test";
        try {
            townCommand.runCommand(TestUtil.player, null, "cv", params);
        } catch (SuccessException successException) {
            // Do nothing
        }
        assertEquals(1, TownManager.getInstance().getTowns().size());
    }

    @Test
    public void townShouldBeCreatedWithNegatives() {
        RegionsTests.loadRegionTypeCobble();
        loadTownTypeTribe();
        RegionsTests.createNewRegion("cobble", TestUtil.player2.getUniqueId(), TestUtil.block14.getLocation());
        TownCommand townCommand = new TownCommand();
        String[] params = new String[2];
        params[0] = "town";
        params[1] = "test";
        try {
            townCommand.runCommand(TestUtil.player2, null, "cv", params);
        } catch (SuccessException successException) {
            // Do nothing
        }
        assertEquals(1, TownManager.getInstance().getTowns().size());
    }

    @Test
    public void townShouldDowngrade() {
        loadTownTypeHamlet();
        loadTownTypeTribe2();
        Town town = loadTown("test", "tribe", TestUtil.block.getLocation());
        TownManager.getInstance().setTownPower(town, 0);
        assertEquals("hamlet", town.getType());
    }

    @Test
    public void townShouldHaveGrace() {
        loadTownTypeHamlet();
        Town town = loadTown("test", "hamlet", TestUtil.block.getLocation());
        TownManager.getInstance().setTownPower(town, 0);
        assertTrue(TownManager.getInstance().hasGrace(town, false));
        ProtectionHandler protectionHandler = new ProtectionHandler();
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(TestUtil.block, TestUtil.player2);
        protectionHandler.onBlockBreak(blockBreakEvent);
        assertTrue(blockBreakEvent.isCancelled());
    }

    @Test
    public void townShouldNotProtectWithoutGrace() {
        loadTownTypeHamlet();
        Town town = loadTown("test", "hamlet", TestUtil.block.getLocation());
        TownManager.getInstance().setTownPower(town, 0);
        town.setLastDisable(System.currentTimeMillis() - (24*60*60*1000));
        assertFalse(TownManager.getInstance().hasGrace(town, true));

        ProtectionHandler protectionHandler = new ProtectionHandler();
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(TestUtil.block, TestUtil.player2);
        protectionHandler.onBlockBreak(blockBreakEvent);
        assertFalse(blockBreakEvent.isCancelled());
    }

    @Test
    public void townShouldProtectWithoutGraceButWithPower() {
        loadTownTypeHamlet();
        Town town = loadTown("test", "hamlet", TestUtil.block.getLocation());
        TownManager.getInstance().setTownPower(town, 0);
        town.setLastDisable(System.currentTimeMillis() - (24*60*60*1000));
        TownManager.getInstance().hasGrace(town, true);
        TownManager.getInstance().setTownPower(town, 500);

        ProtectionHandler protectionHandler = new ProtectionHandler();
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(TestUtil.block, TestUtil.player2);
        protectionHandler.onBlockBreak(blockBreakEvent);
        assertTrue(blockBreakEvent.isCancelled());
    }

    @Test
    public void townShouldNotProtectWithoutGraceAndTinyPower() {
        loadTownTypeHamlet();
        Town town = loadTown("test", "hamlet", TestUtil.block.getLocation());
        TownManager.getInstance().setTownPower(town, 0);
        town.setLastDisable(System.currentTimeMillis() - (24*60*60*1000));
        TownManager.getInstance().hasGrace(town, true);
        TownManager.getInstance().setTownPower(town, 1);
        TownManager.getInstance().hasGrace(town, true);
        TownManager.getInstance().setTownPower(town, 0);

        ProtectionHandler protectionHandler = new ProtectionHandler();
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(TestUtil.block, TestUtil.player2);
        protectionHandler.onBlockBreak(blockBreakEvent);
        assertFalse(blockBreakEvent.isCancelled());
    }

    public static Town loadTown(String name, String type, Location location) {
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), "owner");
        Town town = new Town(name, type,
                location,
                owners, 500, 500, 2, 0, -1);
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
        effects.add("block_break");
        config.set("effects", effects);
        config.set("power", 500);
        config.set("max-power", 500);
        ItemManager.getInstance().loadTownType(config, "hamlet");
    }

    public static void loadTownTypeTribe() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "Tribe");
        config.set("type", "town");
        ArrayList<String> critReqs = new ArrayList<>();
        critReqs.add("cobble");
        config.set("build-reqs", critReqs);
        config.set("critical-build-reqs", critReqs);
        config.set("build-radius", 25);
        ItemManager.getInstance().loadTownType(config, "tribe");
    }
    public static void loadTownTypeTribe2() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "Tribe");
        config.set("type", "town");
        ArrayList<String> critReqs = new ArrayList<>();
        critReqs.add("cobble");
        ArrayList<String> preReqs = new ArrayList<>();
        preReqs.add("tribe:population=5");
        config.set("pre-reqs", preReqs);
        config.set("child", "hamlet");
        config.set("build-reqs", critReqs);
        config.set("critical-build-reqs", critReqs);
        config.set("build-radius", 25);
        ItemManager.getInstance().loadTownType(config, "tribe");
    }
}
