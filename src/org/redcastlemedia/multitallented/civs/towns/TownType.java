package org.redcastlemedia.multitallented.civs.towns;

import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashSet;
import java.util.List;

public class TownType extends CivItem {

    private final List<String> reqs;
    private final HashSet<String> effects;
    private final int buildRadius;
    private final int buildRadiusY;
    private final List<String> criticalReqs;
    private final int power;
    private final int maxPower;

    public List<String> getReqs() {
        return reqs;
    }
    public HashSet<String> getEffects() {
        return effects;
    }
    public int getBuildRadius() {
        return buildRadius;
    }
    public int getBuildRadiusY() {
        return buildRadiusY;
    }
    public List<String> getCriticalReqs() {
        return criticalReqs;
    }
    public int getPower() {
        return power;
    }
    public int getMaxPower() {
        return maxPower;
    }

    public TownType(String name,
                    CVItem icon,
                    List<String> civReqs,
                    int civQty,
                    int civMin,
                    int civMax,
                    double price,
                    String permission,
                    List<String> reqs,
                    HashSet<String> effects,
                    int buildRadius,
                    int buildRadiusY,
                    List<String> criticalReqs,
                    List<String> description,
                    int power,
                    int maxPower) {
        super(civReqs,
                true,
                ItemType.TOWN,
                name,
                icon.getMat(),
                icon.getDamage(),
                civQty,
                civMin,
                civMax,
                price,
                permission,
                description);
        this.reqs = reqs;
        this.effects = effects;
        this.buildRadius = buildRadius;
        this.buildRadiusY = buildRadiusY;
        this.criticalReqs = criticalReqs;
        this.power = power;
        this.maxPower = maxPower;
    }
}
