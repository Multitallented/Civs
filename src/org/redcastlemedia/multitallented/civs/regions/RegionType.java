package org.redcastlemedia.multitallented.civs.regions;

import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashSet;
import java.util.List;

public class RegionType extends CivItem {

    private final String name;
    private final HashSet<CVItem> reqs;

    private final HashSet<String> effects;

    private final int buildRadius;
    private final int buildRadiusX;
    private final int buildRadiusY;
    private final int buildRadiusZ;
    private final int effectRadius;
    private final String rebuild;
    private final String description;

    public RegionType(String name,
                      CVItem icon,
                      List<String> civReqs,
                      int civQty,
                      int civMin,
                      int civMax,
                      double price,
                      String permission,
                      HashSet<CVItem> reqs,
                      HashSet<String> effects,
                      int buildRadius,
                      int buildRadiusX,
                      int buildRadiusY,
                      int buildRadiusZ,
                      int effectRadius,
                      String rebuild,
                      String description) {
        super(civReqs, true, ItemType.REGION, name, icon.getMat(), icon.getDamage(), civQty, civMin, civMax, price, permission);
        this.name = name;
        this.reqs = reqs;
        this.effects = effects;
        this.buildRadius = buildRadius;
        this.buildRadiusX = buildRadiusX;
        this.buildRadiusY = buildRadiusY;
        this.buildRadiusZ = buildRadiusZ;
        this.effectRadius = effectRadius;
        this.rebuild = rebuild;
        this.description = description;
    }
    public String getName() {
        return name;
    }
    public HashSet<CVItem> getReqs() {
        return reqs;
    }
    public HashSet<String> getEffects() {
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
    public String getDescription() { return description; }
}
