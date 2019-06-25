package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDeathEvent;
import org.junit.*;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.WorldImpl;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

import java.util.ArrayList;
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
        this.town = new Town("Hamlet1", "hamlet", townLocation, new HashMap<>(),
                300, 300, 2, 0, -1);
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
    @Ignore // TODO fix this
    public void villagerShouldSpawnNewVillager() {
        CommonScheduler.lastTown.put(TestUtil.player.getUniqueId(), this.town);
        RegionsTests.loadRegionTypeCobble();
        Region region = RegionsTests.createNewRegion("cobble");
        HashMap<String, String> effectMap = new HashMap<>();
        effectMap.put("villager","");
        region.setEffects(effectMap);
        VillagerEffect villagerEffect = new VillagerEffect();
        villagerEffect.regionCreatedHandler(region);
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
    @Ignore // TODO fix this
    public void villagerShouldNotSpawnIfAtMaxVillagers() {
        RegionsTests.loadRegionTypeCobble();
        Region region = RegionsTests.createNewRegion("cobble");
        HashMap<String, String> effectMap = new HashMap<>();
        effectMap.put("villager","");
        region.setEffects(effectMap);
        VillagerEffect villagerEffect = new VillagerEffect();
//        Block block = TestUtil.createBlock(Material.CHEST, townLocation);
//        doReturn(TestUtil.createBlock(Material.AIR, townLocation.add(0, 1,0))).when(block).getRelative(any(), anyInt());
//        when(block.getWorld()).thenReturn(mock(World.class));
        CommonScheduler.lastTown.put(TestUtil.player.getUniqueId(), this.town);

        villagerEffect.regionCreatedHandler(region);
        Villager villager = VillagerEffect.spawnVillager(region);
        assertNotNull(villager);
        VillagerEffect.townCooldowns.clear();
        villager = VillagerEffect.spawnVillager(region);
        assertNull(villager);
    }

    @Test
    public void warehouseShouldFindNeededItems() {
        RegionsTests.loadRegionTypeCobble3();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        RegionsTests.loadRegionTypeWarehouse();
        RegionType warehouseType = (RegionType) ItemManager.getInstance().getItemType("warehouse");
        Location location = new Location(Bukkit.getWorld("world"), 2,50,0);
        Region cobbleRegion = RegionsTests.createNewRegion("cobble", location);
        Location location2 = new Location(Bukkit.getWorld("world"), 3,100,0);
        Region warehouse = RegionsTests.createNewRegion("warehouse", location2);
        Chest cobbleChest = (Chest) TestUtil.blockUnique2.getState();
        Chest warehouseChest = (Chest) TestUtil.blockUnique3.getState();

        WarehouseEffect warehouseEffect = new WarehouseEffect();
        RegionTickEvent regionTickEvent = new RegionTickEvent(warehouse, warehouseType, false, false);
        ArrayList<Location> inventoryLocations = new ArrayList<>();
        inventoryLocations.add(TestUtil.blockUnique3.getLocation());
        warehouseEffect.invs.put(warehouse, inventoryLocations);
        HashMap<Location, Chest> chestMap = new HashMap<>();
        chestMap.put(TestUtil.blockUnique3.getLocation(), warehouseChest);
        warehouseEffect.availableItems.put(warehouse, chestMap);

        TownTests.loadTownTypeHamlet();
        Location townLocation = new Location(Bukkit.getWorld("world"), 2, 75, 0);
        TownTests.loadTown("test", "hamlet", townLocation);

        warehouseEffect.onCustomEvent(regionTickEvent);
        assertEquals(Material.IRON_PICKAXE, cobbleChest.getBlockInventory().getItem(0).getType());
    }

    @Test
    public void activeEffectShouldProperlyDetectLastActive() {
        RegionsTests.loadRegionTypeActive();
        Region region = RegionsTests.createNewRegion("active");
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        ActiveEffect activeEffect = new ActiveEffect();
        RegionTickEvent event = new RegionTickEvent(region, regionType, true, true);
        activeEffect.onRegionTick(event);
        assertEquals(3, region.getEffects().size());
    }

    @Test
    public void activeEffectShouldProperlySetLastActive() {
        RegionsTests.loadRegionTypeActive();
        Region region = RegionsTests.createNewRegion("active", TestUtil.player.getUniqueId());
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        ActiveEffect activeEffect = new ActiveEffect();
        PlayerInRegionEvent event = new PlayerInRegionEvent(TestUtil.player.getUniqueId(), region, regionType);
        activeEffect.onPlayerInRegion(event);
        assertTrue(region.getLastActive() > 0);
    }

    @After
    public void cleanUp() {
        ((WorldImpl) Bukkit.getWorld("world")).nearbyEntities.clear();
        VillagerEffect.townCooldowns.clear();
    }
}
