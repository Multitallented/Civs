package org.redcastlemedia.multitallented.civs.skills;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SkillType {
    private final String name;
    private final String icon;
    private double expPerCategory;
    private double expRepeatDecay;
    private Map<String, Double> exceptions = new HashMap<>();
    private double maxChance;

    public SkillType(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public double getExp(String key, double count) {
        double baseExp = exceptions.getOrDefault(key, expPerCategory);
        return Math.max(baseExp - (expRepeatDecay * Math.max(count - 1, 0)), 0);
    }
}
