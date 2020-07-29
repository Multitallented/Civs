package org.redcastlemedia.multitallented.civs.regions;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class RegionBlockCheckResponse {
    private final RegionPoints regionPoints;
    private final List<HashMap<Material, Integer>> missingItems;
}
