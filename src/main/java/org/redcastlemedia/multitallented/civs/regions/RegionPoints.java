package org.redcastlemedia.multitallented.civs.regions;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegionPoints {
    private int radiusXP;
    private int radiusXN;
    private int radiusYP;
    private int radiusYN;
    private int radiusZP;
    private int radiusZN;
    private boolean valid;

    public RegionPoints() {
        this.valid = false;
    }

    public RegionPoints(int modifierXP, int modifierXN, int modifierYP, int modifierYN, int modifierZP, int modifierZN) {
        this.radiusXP = modifierXP;
        this.radiusXN = modifierXN;
        this.radiusYP = modifierYP;
        this.radiusYN = modifierYN;
        this.radiusZP = modifierZP;
        this.radiusZN = modifierZN;
        this.valid = true;
    }

    public boolean isEquivalentTo(RegionPoints regionPoints) {
        return valid == regionPoints.isValid() &&
                radiusXP == regionPoints.getRadiusXP() &&
                radiusXN == regionPoints.getRadiusXN() &&
                radiusZP == regionPoints.getRadiusZP() &&
                radiusZN == regionPoints.getRadiusZN() &&
                radiusYP == regionPoints.getRadiusYP() &&
                radiusYN == regionPoints.getRadiusYN();
    }
}
