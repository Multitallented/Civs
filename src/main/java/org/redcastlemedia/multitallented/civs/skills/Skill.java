package org.redcastlemedia.multitallented.civs.skills;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Skill {
    private Map<String, Integer> accomplishments = new HashMap<>();
    private final String type;

    public Skill(String type) {
        this.type = type;
    }

    public double getExp() {
        double exp = 0;
        for (Map.Entry<String, Integer> accomplishment : accomplishments.entrySet()) {
            exp += getExpInCategory(accomplishment.getKey(), accomplishment.getValue());
        }
        return exp;
    }

    public int getLevel() {
        SkillType skillType = SkillManager.getInstance().getSkillType(type);
        return (int) Math.round(10 * getExp() / skillType.getMaxExp());
    }

    private double getExpInCategory(String category, int count) {
        if (count < 1) {
            return 0;
        }
        SkillType skillType = SkillManager.getInstance().getSkillType(type);
        double exp = 0;
        do {
            double baseExp = skillType.getExceptions()
                    .getOrDefault(category, skillType.getExpPerCategory());
            exp += Math.max(0, baseExp - (skillType.getExpRepeatDecay() * (count - 1)));
            count--;
        } while (count > 0);
        return exp;
    }
}
