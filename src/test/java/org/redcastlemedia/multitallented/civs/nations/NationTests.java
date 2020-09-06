package org.redcastlemedia.multitallented.civs.nations;

import static org.junit.Assert.*;

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
import org.redcastlemedia.multitallented.civs.events.AllianceFormedEvent;
import org.redcastlemedia.multitallented.civs.events.TownCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.TownEvolveEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public class NationTests extends TestUtil {

    private Town town1;
    private Town town2;
    private Town town3;

    @Before
    public void setup() {
        NationManager.getInstance().reload();
        AllianceManager.getInstance().reload();
        TownManager.getInstance().reload();
        RegionManager.getInstance().reload();
        this.town1  = TownTests.loadTown("town1", "hamlet", new Location(TestUtil.world, 0, 0, 0));
        this.town2 = TownTests.loadTown("town2", "village",
                new Location(TestUtil.world, 1000, 0, 1000));
        this.town3 = TownTests.loadTown("town3", "settlement", new Location(TestUtil.world, -1000, 0, -1000));
    }

    @After
    public void cleanup() {
        ((PlayerInventoryImpl) TestUtil.player.getInventory()).init();
    }

    @Test
    public void nationShouldNotBeCreatedWhenTownReachesLevel3() {
        TownType hamlet = (TownType) ItemManager.getInstance().getItemType("hamlet");
        TownType village = (TownType) ItemManager.getInstance().getItemType("village");
        TownEvolveEvent evolveEvent = new TownEvolveEvent(this.town3, hamlet, village);
        NationManager.getInstance().onTownEvolve(evolveEvent);
        assertNull(NationManager.getInstance().getNation(this.town3.getName()));
    }

    @Test
    public void nationShouldBeCreatedWhenTownReachesLevel4() {
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        Town town = new Town("town3", "village",
                new Location(TestUtil.world, 0, 0, 0),
                people, 500, 5000, 25, 24, -1);
        TownManager.getInstance().addTown(town);
        TownCreatedEvent townCreatedEvent = new TownCreatedEvent(town, (TownType) ItemManager.getInstance().getItemType("village"));
        NationManager.getInstance().onTownCreated(townCreatedEvent);
        Nation nation = NationManager.getInstance().getNationByTownName("town3");
        assertNotNull(nation);
    }

    @Test
    public void townJoiningAllianceShouldFormNation() {
        AllianceManager.getInstance().allyTheseTowns(town1, town2);
        AllianceFormedEvent allianceFormedEvent = new AllianceFormedEvent(
                AllianceManager.getInstance().getAllAlliances().get(0), town2, true);
        NationManager.getInstance().onAllianceFormed(allianceFormedEvent);
        Nation nation = NationManager.getInstance().getNationByTownName("town2");
        assertNotNull(nation);
    }

    @Test
    public void townInAllianceEvolveShouldCreateNation() {
        AllianceManager.getInstance().allyTheseTowns(town1, town3);
        Nation nation = NationManager.getInstance().getNationByTownName("town3");
        assertNull(nation);
        TownType oldType = (TownType) ItemManager.getInstance().getItemType("settlement");
        TownType newType = (TownType) ItemManager.getInstance().getItemType("hamlet");
        TownEvolveEvent townEvolveEvent = new TownEvolveEvent(town3, oldType, newType);
        town3.setType("hamlet");
        NationManager.getInstance().onTownEvolve(townEvolveEvent);
        nation = NationManager.getInstance().getNationByTownName("town3");
        assertNotNull(nation);
    }

    @Test
    public void townInNationInAllianceEvolveShouldNotCreateNation() {
        AllianceManager.getInstance().allyTheseTowns(town1, town2);
        AllianceFormedEvent allianceFormedEvent = new AllianceFormedEvent(
                AllianceManager.getInstance().getAllAlliances().get(0), town2, true);
        NationManager.getInstance().createNation(town2);
        NationManager.getInstance().onAllianceFormed(allianceFormedEvent);
        Nation nation = NationManager.getInstance().getNationByTownName("town1");
        assertNull(nation);
    }

    @Test
    public void townShouldHave1000ClaimsMax() {
        NationManager.getInstance().createNation(this.town2);
        assertEquals(550, NationManager.getInstance().getMaxNationClaims(
                NationManager.getInstance().getNationByTownName(this.town2.getName())));
    }

    @Test
    public void nationShouldBeDestroyedWhenCapitolIsDestroyed() {
        NationManager.getInstance().createNation(this.town2);
        TownDestroyedEvent townDestroyedEvent = new TownDestroyedEvent(this.town2,
                (TownType) ItemManager.getInstance().getItemType(this.town2.getType()));
        NationManager.getInstance().onTownDestroyed(townDestroyedEvent);
        assertTrue(NationManager.getInstance().getAllNations().isEmpty());
    }

    @Test
    public void lastTownLeaveNationShouldDestroyNation() {
        NationManager.getInstance().createNation(town1);
        Nation nation = NationManager.getInstance().getNation(town1.getName());
        NationManager.getInstance().removeMemberFromNation(nation, town1);
        assertNull(NationManager.getInstance().getNation(town1.getName()));
    }

    @Test
    public void townShouldLeaveNation() {
        NationManager.getInstance().createNation(town1);
        Nation nation = NationManager.getInstance().getNation(town1.getName());
        nation.getMembers().add(town2.getName());
        NationManager.getInstance().removeMemberFromNation(nation, town2);
        assertFalse(nation.getMembers().contains(town2.getName()));
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

    @Test
    public void spiralClaimsShouldReturnCorrectXY() {
        AllianceManager.getInstance().allyTheseTowns(town1, town2);
        assertEquals(0, NationManager.getInstance().getSurroundTownClaim(0, town1.getLocation()).getX());
        assertEquals(0, NationManager.getInstance().getSurroundTownClaim(0, town1.getLocation()).getZ());
        assertEquals(1, NationManager.getInstance().getSurroundTownClaim(1, town1.getLocation()).getX());
        assertEquals(1, NationManager.getInstance().getSurroundTownClaim(1, town1.getLocation()).getZ());
        assertEquals(1, NationManager.getInstance().getSurroundTownClaim(2, town1.getLocation()).getX());
        assertEquals(0, NationManager.getInstance().getSurroundTownClaim(2, town1.getLocation()).getZ());
        assertEquals(1, NationManager.getInstance().getSurroundTownClaim(2, town1.getLocation()).getX());
        assertEquals(-1, NationManager.getInstance().getSurroundTownClaim(5, town1.getLocation()).getX());
        assertEquals(1, NationManager.getInstance().getSurroundTownClaim(9, town1.getLocation()).getX());
    }

    @Test
    public void allianceBecomingANationShouldBridgeTheTwoTowns() {

    }

    @Test
    public void townJoiningNationShouldAutoClaimBridge() {

    }

    @Test
    public void nationMemberShouldClaimLandIfHasItemAndClaimsAvailable() {

    }

    @Test
    public void nationMemberShouldNotClaimLandIfNoClaimsAvailable() {

    }

    @Test
    public void nationCapitolShouldOnlyBeChangedIfTownHasGreaterPopulation() {

    }

    @Test
    public void nationCapitolLeavingNationShouldSetNextBiggestTownAsCapitol() {
        town1.setVillagers(10);
        town2.setVillagers(8);
        town3.setVillagers(9);
        NationManager.getInstance().createNation(town1);
        Nation nation = NationManager.getInstance().getNation(town1.getName());
        nation.getMembers().add(town2.getName());
        nation.getMembers().add(town3.getName());
        NationManager.getInstance().removeMemberFromNation(nation, town1);
        assertEquals(nation.getCapitol(), town3.getName());
    }
}
