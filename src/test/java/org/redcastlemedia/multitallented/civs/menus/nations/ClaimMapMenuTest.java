package org.redcastlemedia.multitallented.civs.menus.nations;

import static org.junit.Assert.assertEquals;

import org.bukkit.block.BlockFace;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;

public class ClaimMapMenuTest extends TestUtil {

    private ClaimMapMenu claimMapMenu;

    @Before
    public void setup() {
        claimMapMenu = new ClaimMapMenu();
    }

    // South = +Z
    // East = +X

    @Test
    public void claimXShouldBeCorrect() {
        int x = claimMapMenu.getXClaim(45, 5, 10, BlockFace.NORTH);
        assertEquals(11, x);
        x = claimMapMenu.getXClaim(45, 5, 10, BlockFace.SOUTH);
        assertEquals(9, x);
        x = claimMapMenu.getXClaim(45, 14, 10, BlockFace.SOUTH);
        assertEquals(9, x);
        x = claimMapMenu.getXClaim(45, 3, 10, BlockFace.SOUTH);
        assertEquals(11, x);
        x = claimMapMenu.getXClaim(45, 13, 10, BlockFace.EAST);
        assertEquals(11, x);
        x = claimMapMenu.getXClaim(45, 22, 10, BlockFace.EAST);
        assertEquals(10, x);
        x = claimMapMenu.getXClaim(45, 22, 10, BlockFace.NORTH);
        assertEquals(10, x);
        x = claimMapMenu.getXClaim(45, 22, 10, BlockFace.SOUTH);
        assertEquals(10, x);
        x = claimMapMenu.getXClaim(45, 22, 10, BlockFace.WEST);
        assertEquals(10, x);
        x = claimMapMenu.getXClaim(45, 24, 10, BlockFace.EAST);
        assertEquals(10, x);
        x = claimMapMenu.getXClaim(45, 13, 10, BlockFace.WEST);
        assertEquals(9, x);
    }

    @Test
    public void claimZShouldBeCorrect() {
        int z = claimMapMenu.getZClaim(45, 3, 10, BlockFace.SOUTH);
        assertEquals(12, z);
        z = claimMapMenu.getZClaim(45, 23, 10, BlockFace.SOUTH);
        assertEquals(10, z);
        z = claimMapMenu.getZClaim(45, 13, 10, BlockFace.NORTH);
        assertEquals(9, z);
        z = claimMapMenu.getZClaim(45, 22, 10, BlockFace.EAST);
        assertEquals(10, z);
        z = claimMapMenu.getZClaim(45, 22, 10, BlockFace.WEST);
        assertEquals(10, z);
        z = claimMapMenu.getZClaim(45, 22, 10, BlockFace.NORTH);
        assertEquals(10, z);
        z = claimMapMenu.getZClaim(45, 22, 10, BlockFace.SOUTH);
        assertEquals(10, z);
        z = claimMapMenu.getZClaim(45, 13, 10, BlockFace.EAST);
        assertEquals(10, z);
        z = claimMapMenu.getZClaim(45, 23, 10, BlockFace.EAST);
        assertEquals(11, z);
        z = claimMapMenu.getZClaim(45, 23, 10, BlockFace.WEST);
        assertEquals(9, z);
        z = claimMapMenu.getZClaim(45, 26, -3, BlockFace.WEST);
        assertEquals(-7, z);
    }

}
