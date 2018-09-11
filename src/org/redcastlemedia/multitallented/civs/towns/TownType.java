package org.redcastlemedia.multitallented.civs.towns;

import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TownType extends CivItem {

    private final HashMap<String, Integer> reqs;
    private final HashSet<String> effects;
    private final int buildRadius;
    private final int buildRadiusY;
    private final List<String> criticalReqs;
    private final int power;
    private final int maxPower;
    private final String child;
    private final HashMap<String, Integer> regionLimits;

    public HashMap<String, Integer> getReqs() {
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
    public String getChild() { return child; }
    public int getRegionLimit(String regionTypeName) {
        return regionLimits.get(regionTypeName) == null ? 0 : regionLimits.get(regionTypeName);
    }

    public TownType(String name,
                    CVItem icon,
                    List<String> civReqs,
                    int civQty,
                    int civMin,
                    int civMax,
                    double price,
                    String permission,
                    HashMap<String, Integer> reqs,
                    HashMap<String, Integer> regionLimits,
                    HashSet<String> effects,
                    int buildRadius,
                    int buildRadiusY,
                    List<String> criticalReqs,
                    HashMap<String, String> description,
                    int power,
                    int maxPower,
                    List<String> groups,
                    String child) {
        super(civReqs,
                false,
                ItemType.TOWN,
                name,
                icon.getMat(),
                civQty,
                civMin,
                civMax,
                price,
                permission,
                description,
                groups);
        this.reqs = reqs;
        this.regionLimits = regionLimits;
        this.effects = effects;
        this.buildRadius = buildRadius;
        this.buildRadiusY = buildRadiusY;
        this.criticalReqs = criticalReqs;
        this.power = power;
        this.maxPower = maxPower;
        this.child = child;
    }
}
