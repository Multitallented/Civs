package org.redcastlemedia.multitallented.civs.regions;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Biome;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.towns.GovTypeBuff;
import org.redcastlemedia.multitallented.civs.towns.Government;

import java.util.*;

public class RegionType extends CivItem {

    private final List<List<CVItem>> reqs;
    private final HashMap<String, String> effects;
    private final int buildRadius;
    private final int buildRadiusX;
    private final int buildRadiusY;
    private final int buildRadiusZ;
    private final int effectRadius;
    private final List<String> rebuild;
    private final boolean dailyPeriod;
    private final HashSet<String> towns;
    private final List<RegionUpkeep> upkeeps;
    private final long period;
    private final Set<Biome> biomes;
    @Getter
    private final HashSet<String> worlds;

    @Getter
    private final boolean rebuildRequired;
    @Getter
    private final List<String> commandsOnCreation;
    @Getter
    private final List<String> commandsOnDestruction;
    @Getter @Setter
    private String dynmapMarkerKey = "";
    @Getter @Setter
    private boolean startInInventory = false;

    public RegionType(String key, String name,
                      CVItem icon,
                      CVItem shopIcon,
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
                      List<String> rebuild,
                      HashSet<String> towns,
                      Set<Biome> biomes,
                      long period,
                      boolean dailyPeriod,
                      List<String> groups,
                      boolean isInShop,
                      boolean rebuildRequired,
                      int level,
                      HashSet<String> worlds) {
        super(civReqs,
                true,
                ItemType.REGION,
                key,
                name, icon.getMat(),
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
        this.worlds = worlds;
        this.commandsOnCreation = new ArrayList<>();
        this.commandsOnDestruction = new ArrayList<>();
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
    public List<String> getRebuild() { return rebuild; }
    public List<RegionUpkeep> getUpkeeps() { return upkeeps; }
    public long getPeriod() {
        return period;
    }
    public long getPeriod(Government government) {
        if (government == null) {
            return period;
        }
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
