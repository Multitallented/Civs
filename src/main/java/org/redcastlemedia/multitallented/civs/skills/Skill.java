package org.redcastlemedia.multitallented.civs.skills;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;

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

    public double addAccomplishment(String key) {
        SkillType skillType = SkillManager.getInstance().getSkillType(type);
        if (!accomplishments.containsKey(key)) {
            double exp = skillType.getExp(key, 1);
            accomplishments.put(key, 1);
            return exp;
        }
        int count = accomplishments.get(key);
        double exp = skillType.getExp(key, count + 1.0);
        if (exp > 0) {
            accomplishments.put(key, count + 1);
        }
        return exp;
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
