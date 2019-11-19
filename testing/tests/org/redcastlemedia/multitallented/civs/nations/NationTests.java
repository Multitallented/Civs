package org.redcastlemedia.multitallented.civs.nations;

import org.bukkit.Bukkit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;

public class NationTests {
    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void setup() {
        new NationManager();
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
