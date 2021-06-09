package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.*;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.WorldImpl;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.CVInventory;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegionEffectTests extends TestUtil {

    private Town town;
    private Location townLocation;

    @Before
    public void setup() {
        RegionManager.getInstance().reload();
        TownManager.getInstance().reload();
        TownTests.loadTownTypeHamlet2();
        this.townLocation = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        this.town = new Town("Hamlet1", "hamlet2", townLocation, new HashMap<>(),
                300, 300, 2, 0, -1);
        TownManager.getInstance().addTown(town);
    }

    @After
    public void after() {
        TestUtil.world.setChunkLoaded(true);
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
        assertEquals(295, this.town.getPower());
    }

    @Test
    @Ignore // TODO fix this
    public void villagerShouldSpawnNewVillager() {
        CommonScheduler.getLastTown().put(TestUtil.player.getUniqueId(), this.town);
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
        CommonScheduler.getLastTown().put(TestUtil.player.getUniqueId(), this.town);

        villagerEffect.regionCreatedHandler(region);
        Villager villager = VillagerEffect.spawnVillager(region);
        assertNotNull(villager);
        VillagerEffect.townCooldowns.clear();
        villager = VillagerEffect.spawnVillager(region);
        assertNull(villager);
    }

    @Test
    public void warehouseShouldFindNeededItems() {
        TestUtil.world.setChunkLoaded(false);
        RegionsTests.loadRegionTypeCobble3();
        RegionType warehouseType = (RegionType) ItemManager.getInstance().getItemType("warehouse");
        Location location = new Location(Bukkit.getWorld("world"), 2,50,0);
        Region cobbleRegion = RegionsTests.createNewRegion("cobble", location);
        cobbleRegion.getFailingUpkeeps().add(0);
        Location location2 = new Location(Bukkit.getWorld("world"), 3,90,0);
        Region warehouse = RegionsTests.createNewRegion("warehouse", location2);

        WarehouseEffect warehouseEffect = new WarehouseEffect();
        CVInventory warehouseCVInventory = UnloadedInventoryHandler.getInstance().getChestInventory(TestUtil.blockUnique3.getLocation());
        warehouseCVInventory.addItem(new ItemStack(Material.IRON_PICKAXE));
        warehouseEffect.putInventoryLocation(warehouse, warehouseCVInventory);
        HashMap<String, CVInventory> chestMap = new HashMap<>();
        chestMap.put(Region.locationToString(TestUtil.blockUnique3.getLocation()), warehouseCVInventory);
        warehouseEffect.availableItems.put(warehouse, chestMap);

        Location townLocation = new Location(Bukkit.getWorld("world"), 2, 75, 0);
        TownTests.loadTown("test", "metropolis", townLocation);
        CVInventory regionInventory = UnloadedInventoryHandler.getInstance().getChestInventory(cobbleRegion.getLocation());

        RegionTickEvent regionTickEvent = new RegionTickEvent(warehouse, warehouseType, false, false);
        warehouseEffect.onCustomEvent(regionTickEvent);
        assertEquals(Material.IRON_PICKAXE, regionInventory.getItem(0).getType());
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

    @Test
    public void repairEffectShouldGetCorrectAmount() {
        int repairCost = RepairEffect.getRepairCost(Material.DIAMOND_PICKAXE, 1);
        assertEquals(1, repairCost);
    }

    @Test
    public void maxRepairEffectShouldGetCorrectAmount() {
        int repairCost = RepairEffect.getRepairCost(Material.DIAMOND_PICKAXE, Material.DIAMOND_PICKAXE.getMaxDurability() - 1);
        assertEquals(3, repairCost);
    }

    @After
    public void cleanUp() {
        ((WorldImpl) Bukkit.getWorld("world")).nearbyEntities.clear();
        VillagerEffect.townCooldowns.clear();
    }
}
