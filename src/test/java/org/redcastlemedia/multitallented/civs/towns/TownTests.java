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
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TownTests extends TestUtil {

    @Before
    public void onBefore() {
        TownManager.getInstance().reload();
        RegionManager.getInstance().reload();
        GovernmentManager.getInstance().reload();
    }

    @Test
    public void governmentShouldGetProperBuffString() {
        HashSet<String> groups = new HashSet<>();
        groups.add("mine");
        HashSet<String> regions = new HashSet<>();
        regions.add("inn");

        GovTypeBuff buff = new GovTypeBuff(GovTypeBuff.BuffType.COST, 15,
                groups, regions);
        Government government = new Government("CAPITALISM", GovernmentType.CAPITALISM,
                null, null, new ArrayList<>());
        assertEquals("mine, inn", government.getApplyString(buff));
    }

    @Test
    public void findTownAtShouldReturnTown() {
        loadTownTypeHamlet2();
        Town town = loadTown("BizRep", "hamlet2", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        loadTown("Silverstone", "hamlet2", new Location(Bukkit.getWorld("world"), 100, 0, 0));
        loadTown("Cupcake", "hamlet2", new Location(Bukkit.getWorld("world"), -100, 0, 0));

        assertEquals(town, TownManager.getInstance().getTownAt(new Location(Bukkit.getWorld("world"), 0, 0,0)));
    }

    @Test
    public void shouldNotFindTown() {
        loadTownTypeHamlet2();
        loadTown("BizRep", "hamlet2", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        assertNull(TownManager.getInstance().getTownAt(new Location(Bukkit.getWorld("world"), 0, 55,0)));
    }
    @Test
    public void shouldFindTown() {
        loadTownTypeHamlet2();
        Town town = loadTown("BizRep", "hamlet2", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        assertEquals(town, TownManager.getInstance().getTownAt(new Location(Bukkit.getWorld("world"), 0, 0,0)));
    }

    @Test
    public void memberShouldBeAdded() {
        loadTownTypeHamlet2();
        Town town = loadTown("Aeria", "hamlet2", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        UUID uuid = new UUID(1,5);
        TownManager.getInstance().addInvite(uuid, town);
        TownManager.getInstance().acceptInvite(uuid);
        assertEquals("member", town.getPeople().get(uuid));
    }

    @Test
    public void townsShouldIntersect() {
        loadTownTypeHamlet2();
        loadTown("Summertown", "hamlet2", new Location(Bukkit.getWorld("world"), 0, 0, 0));
        TownType townType = (TownType) ItemManager.getInstance().getItemType("hamlet2");
        assertEquals(1, TownManager.getInstance().checkIntersect(new Location(Bukkit.getWorld("world"), 26, 0, 0), townType, 0).size());
    }
    @Test
    public void townShouldNotIntersect() {
        loadTownTypeHamlet2();
        loadTown("Summertown", "hamlet2", new Location(Bukkit.getWorld("world"), 0, 0, 0));
        TownType townType = (TownType) ItemManager.getInstance().getItemType("hamlet2");
        assertEquals(0, TownManager.getInstance().checkIntersect(new Location(Bukkit.getWorld("world"), 51, 0, 0), townType, 0).size());
    }
    @Test
    public void townShouldNotIntersectWithModifer() {
        loadTownTypeHamlet2();
        loadTown("Summertown", "hamlet2", new Location(Bukkit.getWorld("world"), 0, 0, 0));
        TownType townType = (TownType) ItemManager.getInstance().getItemType("hamlet2");
        assertEquals(1, TownManager.getInstance().checkIntersect(new Location(Bukkit.getWorld("world"), 51, 0, 0), townType, 27).size());
    }

    @Test
    public void newTownShouldStartWithHousing() {
        TownTests.loadTownTypeTribe();
        RegionsTests.loadRegionTypeCobble();
        RegionsTests.createNewRegion("cobble");
        TownType townType = (TownType) ItemManager.getInstance().getItemType("tribe");

        Location location = new Location(Bukkit.getWorld("world"), 0, 0, 0);

        int housing = TownManager.getInstance().getHousingCount(location, townType);
        assertEquals(2, housing);
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
        regionManager.addRegion(region);
        loadTown("Sanmak-kol", "tribe", townLocation);
        if (TownManager.getInstance().getTowns().isEmpty()) {
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
        assertNull(regionManager.getRegionAt(regionLocation));
        assertTrue(TownManager.getInstance().getTowns().isEmpty());
    }

    @Test
    public void townShouldDestroyWhenCriticalRegionDestroyed2() {
        RegionsTests.loadRegionTypeCobbleGroup();
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        HashMap<String, String> effects = new HashMap<>();
        Location regionLocation = new Location(Bukkit.getWorld("world2"), 0,0,0);
        Region region = new Region("town_hall", people,
                regionLocation,
                RegionsTests.getRadii(),
                effects,0);
        loadTownTypeTribe();
        Location townLocation = new Location(Bukkit.getWorld("world2"), 1,0,0);

        RegionManager regionManager = RegionManager.getInstance();
        regionManager.addRegion(region);
        loadTown("Sanmak-kol", "tribe", townLocation);
        if (TownManager.getInstance().getTowns().isEmpty()) {
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
        assertNull(regionManager.getRegionAt(regionLocation));
        assertTrue(TownManager.getInstance().getTowns().isEmpty());
    }

    @Test
    public void townShouldDestroyWhenCriticalRegionDestroyed3() {
        RegionsTests.loadRegionTypeCobbleGroup();
        RegionsTests.loadRegionTypeCobbleGroup2();
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        HashMap<String, String> effects = new HashMap<>();
        Location regionLocation = new Location(Bukkit.getWorld("world2"), 0,0,0);
        Location regionLocation2 = new Location(Bukkit.getWorld("world2"), 0,20,0);
        Region region = new Region("town_hall", people,
                regionLocation,
                RegionsTests.getRadii(),
                effects,0);
        loadTownTypeTribe();
        Location townLocation = new Location(Bukkit.getWorld("world2"), 1,0,0);

        RegionsTests.createNewRegion("purifier", regionLocation2);
        RegionManager regionManager = RegionManager.getInstance();
        regionManager.addRegion(region);
        loadTown("Sanmak-kol", "tribe", townLocation);
        if (TownManager.getInstance().getTowns().isEmpty()) {
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
        assertNull(regionManager.getRegionAt(regionLocation));
        assertTrue(TownManager.getInstance().getTowns().isEmpty());
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
        regionManager.addRegion(region);
        Town town = loadTown("Sanmak-kol", "tribe", townLocation);
        regionManager.removeRegion(region, false, true);
        assertEquals(town, TownManager.getInstance().getTownAt(townLocation));
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
        loadTownTypeHamlet2();
        loadTownTypeTribe2();
        Town town = loadTown("test", "tribe", TestUtil.block.getLocation());
        TownManager.getInstance().setTownPower(town, 0);
        assertEquals("hamlet2", town.getType());
    }

    @Test
    public void townShouldHaveGrace() {
        loadTownTypeHamlet2();
        Town town = loadTown("test", "hamlet2", TestUtil.block.getLocation());
        TownManager.getInstance().setTownPower(town, 0);
        assertTrue(TownManager.getInstance().hasGrace(town, false));
        ProtectionHandler protectionHandler = new ProtectionHandler();
        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(TestUtil.block, TestUtil.player2);
        protectionHandler.onBlockBreak(blockBreakEvent);
        assertTrue(blockBreakEvent.isCancelled());
    }

    @Test
    public void townShouldNotProtectWithoutGrace() {
        loadTownTypeHamlet2();
        Town town = loadTown("test", "hamlet2", TestUtil.block.getLocation());
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
        loadTownTypeHamlet2();
        Town town = loadTown("test", "hamlet2", TestUtil.block.getLocation());
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
        loadTownTypeHamlet2();
        Town town = loadTown("test", "hamlet2", TestUtil.block.getLocation());
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

    @Test
    public void townTransitionTest() {
        TownTests.loadTownTypeHamlet2();
        Town town = TownTests.loadTown("test", "hamlet2", TestUtil.player.getLocation());
        town.setPower(2);
        ArrayList<GovTransition> transitions = new ArrayList<>();
        GovTransition govTransition = new GovTransition(-1, -1, 30, -1,
                GovernmentType.ANARCHY.name());
        transitions.add(govTransition);
        Government government = new Government("DICTATORSHIP", GovernmentType.DICTATORSHIP,
                new HashSet<>(), null, transitions);
        GovernmentManager.getInstance().addGovernment(government);
        Government anarchyGov = new Government("ANARCHY", GovernmentType.ANARCHY,
                new HashSet<>(), null, transitions);
        GovernmentManager.getInstance().addGovernment(anarchyGov);
        TownTransitionUtil.checkTown(town);
        assertEquals(GovernmentType.ANARCHY.name(), town.getGovernmentType());
    }

    @Test
    public void townShouldNotTransition() {
        TownTests.loadTownTypeHamlet2();
        Town town = TownTests.loadTown("test", "hamlet2", TestUtil.player.getLocation());
        town.setPower(160);
        ArrayList<GovTransition> transitions = new ArrayList<>();
        GovTransition govTransition = new GovTransition(-1, -1, 30, -1,
                GovernmentType.ANARCHY.name());
        transitions.add(govTransition);
        Government government = new Government("DICTATORSHIP", GovernmentType.DICTATORSHIP, new HashSet<>(), null,
                transitions);
        GovernmentManager.getInstance().addGovernment(government);
        TownTransitionUtil.checkTown(town);
        assertEquals(GovernmentType.DICTATORSHIP.name(), town.getGovernmentType());
    }

    @Test
    public void townShouldNotInactiveTransition() {
        TownTests.loadTownTypeHamlet2();
        Town town = TownTests.loadTown("test", "hamlet2", TestUtil.player.getLocation());
        town.setPower(160);
        town.setLastActive(System.currentTimeMillis());
        ArrayList<GovTransition> transitions = new ArrayList<>();
        GovTransition govTransition = new GovTransition(-1, -1, -1, 50000,
                GovernmentType.ANARCHY.name());
        transitions.add(govTransition);
        Government government = new Government("DICTATORSHIP", GovernmentType.DICTATORSHIP, new HashSet<>(), null,
                transitions);
        GovernmentManager.getInstance().addGovernment(government);
        TownTransitionUtil.checkTown(town);
        assertEquals(GovernmentType.DICTATORSHIP.name(), town.getGovernmentType());
    }

    @Test
    public void townMaxPowerShouldBeAdjustedOnCreation() {
        loadTownTypeTribe();
        RegionsTests.loadRegionTypeCobble();
        RegionsTests.createNewRegion("cobble");
        HashSet<GovTypeBuff> buffs = new HashSet<>();
        buffs.add(new GovTypeBuff(GovTypeBuff.BuffType.MAX_POWER, 10, new HashSet<>(), new HashSet<>()));
        Government government = new Government("DICTATORSHIP", GovernmentType.DICTATORSHIP, buffs, null, new ArrayList<>());
        GovernmentManager.getInstance().addGovernment(government);
        TownCommand townCommand = new TownCommand();
        String[] args = new String[2];
        args[0] = "town";
        args[1] = "test";
        try {
            townCommand.runCommand(TestUtil.player, null, "town", args);
        } catch (SuccessException exception) {

        }
        assertEquals(550, TownManager.getInstance().getTown("test").getMaxPower());
    }

    public static Town loadTown(String name, String type, Location location) {
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), "owner");
        TownType townType = (TownType) ItemManager.getInstance().getItemType(type);
        Town town = new Town(name, type,
                location,
                owners, townType.getPower(), townType.getMaxPower(), 2, 0, -1);
        TownManager.getInstance().addTown(town);
        return town;
    }

    public static void loadTownTypeHamlet2() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "Hamlet2");
        config.set("type", "town");
        config.set("build-radius", 25);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_explosion");
        effects.add("deny_mob_spawn");
        effects.add("block_break");
        config.set("effects", effects);
        config.set("power", 100);
        config.set("max-power", 500);
        ItemManager.getInstance().loadTownType(config, "hamlet2");
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
        config.set("max-power", 500);
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
        config.set("child", "hamlet2");
        config.set("build-reqs", critReqs);
        config.set("critical-build-reqs", critReqs);
        config.set("build-radius", 25);
        ItemManager.getInstance().loadTownType(config, "tribe");
    }
    public static void addGovernmentType(Government government) {
        GovernmentManager.getInstance().addGovernment(government);
    }
}
