package org.redcastlemedia.multitallented.civs.civclass;

import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ClassType extends CivItem {
    private final List<String> children;
    private final int manaPerSecond;
    private final int maxMana;
    private final int maxHealth;
    private final Map<String, Integer> allowedActions = new HashMap<>();
    private final String manaTitle;
    private final Map<Integer, List<String>> spellSlots = new HashMap<>();
    @Setter
    private int maxLevel;
    private final Set<String> classPermissions = new HashSet<>();

    public ClassType(List<String> reqs,
                     String name,
                     CVItem icon,
                     CVItem shopIcon,
                     double price,
                     String permission,
                     List<String> children,
                     List<String> groups,
                     int manaPerSecond,
                     int maxMana,
                     boolean isInShop,
                     int level,
                     int maxHealth,
                     String manaTitle) {
        super(reqs,
                false,
                ItemType.CLASS,
                name,
                icon.getMat(),
                shopIcon,
                children.isEmpty() ? 1 : children.size(),
                0,
                1,
                price,
                permission,
                groups,
                isInShop,
                level);
        this.children = children;
        this.manaPerSecond = manaPerSecond;
        this.maxMana = maxMana;
        this.maxHealth = maxHealth;
        this.manaTitle = manaTitle;
    }

}
