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
    private final List<List<CVItem>> reagents;
    private final List<List<CVItem>> input;
    private final double payout;
    private final long period;

    private final List<List<CVItem>> output;

    public RegionType(String name,
                      CVItem icon,
                      List<String> civReqs,
                      int civQty,
                      int civMin,
                      int civMax,
                      double price,
                      String permission,
                      List<List<CVItem>> reqs,
                      List<List<CVItem>> reagents,
                      List<List<CVItem>> input,
                      List<List<CVItem>> output,
                      double payout,
                      HashMap<String, String> effects,
                      int buildRadius,
                      int buildRadiusX,
                      int buildRadiusY,
                      int buildRadiusZ,
                      int effectRadius,
                      String rebuild,
                      HashMap<String, String> description,
                      long period,
                      List<String> groups) {
        super(civReqs,
                true,
                ItemType.REGION,
                name, icon.getMat(),
                icon.getDamage(),
                civQty,
                civMin,
                civMax,
                price,
                permission,
                description,
                groups);
        this.name = name;
        this.reqs = reqs;
        this.reagents = reagents;
        this.input = input;
        this.output = output;
        this.payout = payout;
        this.effects = effects;
        this.buildRadius = buildRadius;
        this.buildRadiusX = buildRadiusX;
        this.buildRadiusY = buildRadiusY;
        this.buildRadiusZ = buildRadiusZ;
        this.effectRadius = effectRadius;
        this.rebuild = rebuild;
        this.period = period;
    }
    public String getName() {
        return name;
    }
    public List<List<CVItem>> getReqs() {
        return reqs;
    }
    public List<List<CVItem>> getReagents() {
        return reagents;
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
    public double getPayout() {
        return payout;
    }
    public List<List<CVItem>> getInput() {
        return input;
    }
    public List<List<CVItem>> getOutput() {
        return output;
    }
    public long getPeriod() { return period; }
}
