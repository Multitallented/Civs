package org.redcastlemedia.multitallented.civs.towns;

import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class TownType extends CivItem {

    private final HashMap<String, Integer> reqs;
    private final HashMap<String, String> effects;
    private final int buildRadius;
    private final int buildRadiusY;
    private final List<String> criticalReqs;
    private final int power;
    private final int maxPower;
    private final String child;
    @Getter
    @Setter
    private String defaultGovType;
    @Getter
    private final int childPopulation;
    @Getter
    private final HashMap<String, Integer> regionLimits;

    public HashMap<String, Integer> getReqs() {
        return reqs;
    }
    public HashMap<String, String> getEffects() {
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
                    CVItem shopIcon,
                    List<String> civReqs,
                    int civQty,
                    int civMin,
                    int civMax,
                    double price,
                    String permission,
                    HashMap<String, Integer> reqs,
                    HashMap<String, Integer> regionLimits,
                    HashMap<String, String> effects,
                    int buildRadius,
                    int buildRadiusY,
                    List<String> criticalReqs,
                    int power,
                    int maxPower,
                    List<String> groups,
                    String child,
                    int childPopulation,
                    boolean isInShop,
                    int level) {
        super(civReqs,
                false,
                ItemType.TOWN,
                name,
                icon.getMat(),
                shopIcon,
                civQty,
                civMin,
                civMax,
                price,
                permission,
                groups,
                isInShop,
                level);
        this.reqs = reqs;
        this.regionLimits = regionLimits;
        this.effects = effects;
        this.buildRadius = buildRadius;
        this.buildRadiusY = buildRadiusY;
        this.criticalReqs = criticalReqs;
        this.power = power;
        this.maxPower = maxPower;
        this.child = child;
        this.childPopulation = childPopulation;
    }
}
