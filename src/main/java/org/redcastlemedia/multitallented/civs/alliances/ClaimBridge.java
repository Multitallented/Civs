package org.redcastlemedia.multitallented.civs.alliances;

import org.bukkit.World;

import lombok.Getter;

@Getter
public class ClaimBridge {
    private final double x1;
    private final double x2;
    private final double z1;
    private final double z2;
    private final double diffX;
    private final double diffZ;
    private final double slope;
    private final World world;

    public ClaimBridge(double x1, double x2, double z1, double z2, double diffX, double diffZ, double slope, World world) {
        this.x1 = x1;
        this.x2 = x2;
        this.z1 = z1;
        this.z2 = z2;
        this.diffX = diffX;
        this.diffZ = diffZ;
        this.slope = slope;
        this.world = world;
    }
}
