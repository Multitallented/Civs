package org.redcastlemedia.multitallented.civs.nations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.PlayerInventoryImpl;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public class NationTests extends TestUtil {

    @Before
    public void setup() {
        NationManager.getInstance().reload();
        AllianceManager.getInstance().reload();
        TownManager.getInstance().reload();
        RegionManager.getInstance().reload();
    }

    @After
    public void cleanup() {
        ((PlayerInventoryImpl) TestUtil.player.getInventory()).init();
    }

    @Test
    public void nationShouldNotBeCreatedWhenTownReachesLevel3() {
        Town town1  = TownTests.loadTown("town1", "settlement", new Location(TestUtil.world, 0, 0, 0));
        town1.setVillagers(24);
        RegionsTests.createNewRegion("council_room");
        RegionsTests.createNewRegion("cobble_quarry");
        RegionsTests.createNewRegion("purifier");
        for (int i = 0; i < 8; i++) {
            RegionsTests.createNewRegion("shack");
        }
        ((PlayerInventoryImpl) TestUtil.player.getInventory())
                .setMainHandItem(TestUtil.createUniqueItemStack(Material.GLOWSTONE, "Civs Hamlet"));
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        Town town = new Town("town3", "hamlet",
                new Location(TestUtil.world, 0, 0, 0),
                people, 500, 5000, 25, 24, -1);
        try {
            TownManager.getInstance().placeTown(TestUtil.player, "town3", town);
        } catch (SuccessException se) {
            // Do nothing
        }
        assertTrue(NationManager.getInstance().getAllNations().isEmpty());
    }

    @Test
    public void nationShouldBeCreatedWhenTownReachesLevel4() {
        Town town1  = TownTests.loadTown("town1", "hamlet", new Location(TestUtil.world, 0, 0, 0));
        town1.setVillagers(24);
        RegionsTests.createNewRegion("council_room");
        RegionsTests.createNewRegion("cobble_quarry");
        RegionsTests.createNewRegion("purifier");
        RegionsTests.createNewRegion("purifier");
        RegionsTests.createNewRegion("potato_farm");
        for (int i = 0; i < 8; i++) {
            RegionsTests.createNewRegion("shack");
        }
        ((PlayerInventoryImpl) TestUtil.player.getInventory())
                .setMainHandItem(TestUtil.createUniqueItemStack(Material.GLOWSTONE, "Civs Village"));
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        Town town = new Town("town3", "village",
                new Location(TestUtil.world, 0, 0, 0),
                people, 500, 5000, 25, 24, -1);
        try {
            TownManager.getInstance().placeTown(TestUtil.player, "town3", town);
        } catch (SuccessException se) {
            // Do nothing
        }
        assertFalse(NationManager.getInstance().getAllNations().isEmpty());
    }

    @Test
    public void nationShouldBeDestroyedWhenCapitolIsDestroyed() {
        Town town = TownTests.loadTown("test", "village",
                new Location(TestUtil.world, 0, 0, 0));
        NationManager.getInstance().createNation(town);
        TownDestroyedEvent townDestroyedEvent = new TownDestroyedEvent(town,
                (TownType) ItemManager.getInstance().getItemType(town.getType()));
        NationManager.getInstance().onTownDestroyed(townDestroyedEvent);
        assertTrue(NationManager.getInstance().getAllNations().isEmpty());
    }

//    @Test
//    public void allianceShouldHaveCorrectMaxClaims() {
//        AllianceManager.getInstance().allyTheseTowns(town1, town2);
//        Alliance alliance = AllianceManager.getInstance().getAllAlliances().get(0);
//        assertEquals(200, AllianceManager.getInstance().getMaxAllianceClaims(alliance));
//    }

//    @Test
//    public void allianceClaimsShouldFill2Towns() {
//        AllianceManager.getInstance().allyTheseTowns(town1, town2);
//        Alliance alliance = AllianceManager.getInstance().getAllAlliances().get(0);
//        assertEquals(200, alliance.getNationClaims().get(TestUtil.world.getUID()).size());
//    }

//    @Test
//    public void spiralClaimsShouldReturnCorrectXY() {
//        AllianceManager.getInstance().allyTheseTowns(town1, town2);
//        assertEquals(0, AllianceManager.getInstance().getSurroundTownClaim(0, town1.getLocation()).getX());
//        assertEquals(0, AllianceManager.getInstance().getSurroundTownClaim(0, town1.getLocation()).getZ());
//        assertEquals(1, AllianceManager.getInstance().getSurroundTownClaim(1, town1.getLocation()).getX());
//        assertEquals(1, AllianceManager.getInstance().getSurroundTownClaim(1, town1.getLocation()).getZ());
//        assertEquals(1, AllianceManager.getInstance().getSurroundTownClaim(2, town1.getLocation()).getX());
//        assertEquals(0, AllianceManager.getInstance().getSurroundTownClaim(2, town1.getLocation()).getZ());
//        assertEquals(1, AllianceManager.getInstance().getSurroundTownClaim(2, town1.getLocation()).getX());
//        assertEquals(-1, AllianceManager.getInstance().getSurroundTownClaim(5, town1.getLocation()).getX());
//        assertEquals(1, AllianceManager.getInstance().getSurroundTownClaim(9, town1.getLocation()).getX());
//    }
}
