package org.redcastlemedia.multitallented.civs.regions;

import lombok.Getter;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.towns.GovTypeBuff;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final HashSet<String> towns;
    private List<RegionUpkeep> upkeeps;
    private final long period;
    private final Set<Biome> biomes;

    @Getter
    private final boolean rebuildRequired;

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
                      HashSet<String> towns,
                      Set<Biome> biomes,
                      HashMap<String, String> description,
                      long period,
                      boolean dailyPeriod,
                      List<String> groups,
                      boolean isInShop,
                      boolean rebuildRequired,
                      int level) {
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
                groups,
                isInShop,
                level);
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
        this.towns = towns;
        this.biomes = biomes;
        this.rebuildRequired = rebuildRequired;
    }
    public String getName() {
        return name;
    }
    public List<List<CVItem>> getReqs() {
        return cloneReqMap(reqs);
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
    public long getPeriod() {
        return period;
    }
    public long getPeriod(Government government) {
        for (GovTypeBuff buff : government.getBuffs()) {
            if (buff.getBuffType() != GovTypeBuff.BuffType.COOLDOWN) {
                continue;
            }
            return (int) (period * (1 - (double) buff.getAmount() / 100));
        }
        return period;
    }
    public boolean isDailyPeriod() {
        return dailyPeriod;
    }
    public HashSet<String> getTowns() { return towns; }
    public Set<Biome> getBiomes() {
        return biomes;
    }

    static List<List<CVItem>> cloneReqMap(List<List<CVItem>> reqMap) {
        List<List<CVItem>> itemCheck = new ArrayList<>();
        for (List<CVItem> currentList : reqMap) {
            List<CVItem> currentReqMap = new ArrayList<>();
            for (CVItem currentItem : currentList) {
                CVItem clone = currentItem.clone();
                currentReqMap.add(clone);
            }
            itemCheck.add(currentReqMap);
        }
        return itemCheck;
    }
}
