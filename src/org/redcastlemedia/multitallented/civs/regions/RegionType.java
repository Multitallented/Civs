package org.redcastlemedia.multitallented.civs.regions;

import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashSet;

public class RegionType {

    private final String name;
    private final HashSet<CVItem> reqs;

    public RegionType(String name, HashSet<CVItem> reqs) {
        this.name = name;
        this.reqs = reqs;
    }
    public String getName() {
        return name;
    }
    public HashSet<CVItem> getReqs() {
        return reqs;
    }
}
