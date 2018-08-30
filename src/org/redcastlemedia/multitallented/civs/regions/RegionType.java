package org.redcastlemedia.multitallented.civs.regions;

import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RegionType extends CivItem {

    private final String name;
    private final List<List<CVItem>> reqs;

    private final HashMap<String, String> effects;

    private final int buildRadius;
    private final int buildRadiusX;
    private final int buildRadiusY;
    private final int buildRadiusZ;
    private final int effectRadius;
    private final String rebuild;
    private final boolean dailyPeriod;
    private List<RegionUpkeep> upkeeps;
    private final long period;

    public RegionType(String name,
                      CVItem icon,
                      List<String> civReqs,
                      int civQty,
                      int civMin,
                      int civMax,
                      double price,
                      String permission,
                      List<List<CVItem>> reqs,
                      List<RegionUpkeep> upkeeps,
                      HashMap<String, String> effects,
                      int buildRadius,
                      int buildRadiusX,
                      int buildRadiusY,
                      int buildRadiusZ,
                      int effectRadius,
                      String rebuild,
                      HashMap<String, String> description,
                      long period,
                      boolean dailyPeriod,
                      List<String> groups) {
        super(civReqs,
                true,
                ItemType.REGION,
                name, icon.getMat(),
                civQty,
                civMin,
                civMax,
                price,
                permission,
                description,
                groups);
        this.name = name;
        this.reqs = reqs;
        this.upkeeps = upkeeps;
        this.effects = effects;
        this.buildRadius = buildRadius;
        this.buildRadiusX = buildRadiusX;
        this.buildRadiusY = buildRadiusY;
        this.buildRadiusZ = buildRadiusZ;
        this.effectRadius = effectRadius;
        this.rebuild = rebuild;
        this.period = period;
        this.dailyPeriod = dailyPeriod;
    }
    public String getName() {
        return name;
    }
    public List<List<CVItem>> getReqs() {
        return reqs;
    }
    public HashMap<String, String> getEffects() {
        return effects;
    }
    public int getBuildRadius() {
        return buildRadius;
    }
    public int getBuildRadiusX() {
        return buildRadiusX;
    }
    public int getBuildRadiusY() {
        return buildRadiusY;
    }
    public int getBuildRadiusZ() {
        return buildRadiusZ;
    }
    public int getEffectRadius() {
        return effectRadius;
    }
    public String getRebuild() { return rebuild; }
    public List<RegionUpkeep> getUpkeeps() { return upkeeps; }
    public long getPeriod() { return period; }
    public boolean isDailyPeriod() {
        return dailyPeriod;
    }
}
