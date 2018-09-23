package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDeathEvent;
import org.junit.*;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegionEffectTests {

    private Town town;
    private RegionManager regionManager;
    private Location townLocation;
    private TownManager townManager;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void setup() {
        this.regionManager = new RegionManager();
        this.townManager = new TownManager();
        TownTests.loadTownTypeHamlet();
        this.townLocation = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        this.town = new Town("Hamlet1", "hamlet", townLocation, new HashMap<>(), 300, 300, 2, 1, 1);
        townManager.addTown(town);
    }

    @Test
    @Ignore
    public void villagerEffectShouldBumpPopulation() {
        VillagerEffect villagerEffect = new VillagerEffect();
        Player player = mock(Player.class);
        RegionsTests.loadRegionTypeCobble();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
//        assertTrue(villagerEffect.createRegionHandler(TestUtil.block, player, regionType));
        Region region = new Region("cobble", new HashMap<>(), townLocation, RegionsTests.getRadii(), new HashMap<>(), 0);
        villagerEffect.regionCreatedHandler(region);
        assertEquals(2, town.getPopulation());
    }

    @Test
    public void villagerEffectShouldDecrementPower() {
        VillagerEffect villagerEffect = new VillagerEffect();
        Villager villager = mock(Villager.class);
        when(villager.getLocation()).thenReturn(this.townLocation);
        EntityDeathEvent entityDeathEvent = mock(EntityDeathEvent.class);
        when(entityDeathEvent.getEntity()).thenReturn(villager);
        villagerEffect.onVillagerDeath(entityDeathEvent);
        assertEquals(299, this.town.getPower());
    }

    @Test
    @Ignore
    public void villagerShouldSpawnNewVillager() {
        RegionsTests.loadRegionTypeCobble();
        Region region = RegionsTests.createNewRegion("cobble");
        Villager villager = VillagerEffect.spawnVillager(region);
        assertNotNull(villager);
    }

    @Test
    public void villagerShouldNotSpawnIfOnCooldown() {
        RegionsTests.loadRegionTypeCobble();
        Region region = RegionsTests.createNewRegion("cobble");
        VillagerEffect.spawnVillager(region);
        Villager villager = VillagerEffect.spawnVillager(region);
        assertNull(villager);
    }

    @Test
    public void villagerShouldNotSpawnIfAtMaxVillagers() {
        RegionsTests.loadRegionTypeCobble();
        Region region = RegionsTests.createNewRegion("cobble");
        VillagerEffect villagerEffect = new VillagerEffect();
        Player player = mock(Player.class);
        Block block = TestUtil.createBlock(Material.CHEST, townLocation);
        doReturn(TestUtil.createBlock(Material.AIR, townLocation.add(0, 1,0))).when(block).getRelative(any(), anyInt());
        when(block.getWorld()).thenReturn(mock(World.class));

        RegionsTests.loadRegionTypeCobble();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        villagerEffect.createRegionHandler(block, player, regionType);
        Villager villager = VillagerEffect.spawnVillager(region);
        assertNotNull(villager);
        VillagerEffect.townCooldowns.clear();
        villager = VillagerEffect.spawnVillager(region);
        assertNull(villager);
    }

    @After
    public void cleanUp() {
        VillagerEffect.townCooldowns.clear();
    }
}
