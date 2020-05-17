package org.redcastlemedia.multitallented.civs.skills;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SkillType {
    private final String name;
    private final String icon;
    private double expPerCategory;
    private double expRepeatDecay;
    private List<String> exceptions = new ArrayList<>();
    private double maxChance;

    public SkillType(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}
