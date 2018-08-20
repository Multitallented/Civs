package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDeathEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
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
        this.townLocation = new Location(Bukkit.getWorld("world"), 0, 20, 0);
        this.town = new Town("Hamlet1", "hamlet", townLocation, new HashMap<>(), 300, 300, 2, 1);
        townManager.addTown(town);
    }

    @Test
    public void villagerEffectShouldBumpPopulation() {
        VillagerEffect villagerEffect = new VillagerEffect();
        Player player = mock(Player.class);
        Block block = TestUtil.createBlock(Material.CHEST, townLocation);
        doReturn(TestUtil.createBlock(Material.AIR, townLocation.add(0, 1,0))).when(block).getRelative(any(), anyInt());
        when(block.getWorld()).thenReturn(mock(World.class));
        villagerEffect.createRegionHandler(block, player);
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
}
