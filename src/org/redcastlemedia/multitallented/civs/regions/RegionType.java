package org.redcastlemedia.multitallented.civs.regions;

import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashSet;

public class RegionType {

    private final String name;
    private final HashSet<CVItem> reqs;

    private final HashSet<String> effects;

    public RegionType(String name, HashSet<CVItem> reqs, HashSet<String> effects) {
        this.name = name;
        this.reqs = reqs;
        this.effects = effects;
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
}
