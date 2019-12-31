package org.redcastlemedia.multitallented.civs.alliances;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.bukkit.Bukkit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

public class AllianceMenuTests extends TestUtil {
    private Town town1;
    private Town town2;
    private Town town3;
    private Town town4;

    @Before
    public void setup() {
        TownManager.getInstance().reload();
        AllianceManager.getInstance().reload();
        town1 = TownTests.loadTown("town1", "hamlet", TestUtil.block.getLocation());
        town2 = TownTests.loadTown("town2", "hamlet", TestUtil.block14.getLocation());
        town3 = TownTests.loadTown("town3", "hamlet", TestUtil.block8.getLocation());
        town4 = TownTests.loadTown("town4", "hamlet", TestUtil.block6.getLocation());
    }

    @Test
    public void townsShouldBeAllied() {
        AllianceManager.getInstance().allyTheseTowns(town1, town2);
        assertTrue(AllianceManager.getInstance().isAllied(town1, town2));
    }

    @Test
    public void townsShouldNotBeAllied() {
        AllianceManager.getInstance().allyTheseTowns(town1, town2);
        AllianceManager.getInstance().unAlly(town1, town2);
        assertFalse(AllianceManager.getInstance().isAllied(town1, town2));
    }

    @Test
    public void mergeAlliances() {
        AllianceManager.getInstance().allyTheseTowns(town1, town2);
        AllianceManager.getInstance().allyTheseTowns(town3, town2);
        AllianceManager.getInstance().allyTheseTowns(town1, town3);
        assertEquals(1, AllianceManager.getInstance().getAllAlliances().size());
        assertEquals(3, AllianceManager.getInstance().getAllAlliances().get(0).getMembers().size());
    }

    @Test
    public void mergeAlliances2() {
        AllianceManager.getInstance().allyTheseTowns(town1, town2);
        AllianceManager.getInstance().allyTheseTowns(town1, town3);
        AllianceManager.getInstance().allyTheseTowns(town1, town4);
        AllianceManager.getInstance().allyTheseTowns(town2, town3);
        AllianceManager.getInstance().allyTheseTowns(town2, town4);
        AllianceManager.getInstance().allyTheseTowns(town3, town1);
        AllianceManager.getInstance().allyTheseTowns(town3, town2);
        AllianceManager.getInstance().allyTheseTowns(town3, town4);
        assertEquals(1, AllianceManager.getInstance().getAllAlliances().size());
        assertEquals(4, AllianceManager.getInstance().getAllAlliances().get(0).getMembers().size());
    }

    @Test
    public void unMergeAlliances() {
        AllianceManager.getInstance().allyTheseTowns(town1, town2);
        AllianceManager.getInstance().allyTheseTowns(town1, town3);
        AllianceManager.getInstance().allyTheseTowns(town1, town4);
        AllianceManager.getInstance().allyTheseTowns(town2, town3);
        AllianceManager.getInstance().allyTheseTowns(town2, town4);
        AllianceManager.getInstance().allyTheseTowns(town3, town1);
        AllianceManager.getInstance().allyTheseTowns(town3, town2);
        AllianceManager.getInstance().allyTheseTowns(town3, town4);
        AllianceManager.getInstance().unAlly(town1, town2);
        assertEquals(2, AllianceManager.getInstance().getAllAlliances().size());
        assertEquals(3, AllianceManager.getInstance().getAllAlliances().get(0).getMembers().size());
        assertEquals(3, AllianceManager.getInstance().getAllAlliances().get(1).getMembers().size());
    }

    @Test
    public void unAllyShouldRemoveAlliance() {
        AllianceManager.getInstance().allyTheseTowns(town1, town2);
        AllianceManager.getInstance().unAlly(town1, town2);
        assertEquals(0, AllianceManager.getInstance().getAllAlliances().size());
    }

    private void printAlliances() {
        for (Alliance alliance : AllianceManager.getInstance().getAllAlliances()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String townName : alliance.getMembers()) {
                stringBuilder.append(townName);
                stringBuilder.append(", ");
            }
            System.out.println(stringBuilder.toString());
        }
        System.out.println("----------");
    }
}
