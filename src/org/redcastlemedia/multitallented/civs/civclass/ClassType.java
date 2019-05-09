package org.redcastlemedia.multitallented.civs.civclass;

import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashMap;
import java.util.List;

public class ClassType extends CivItem {
    private final List<String> children;
    private final int manaPerSecond;
    private final int maxMana;

    public ClassType(List<String> reqs,
                     String name,
                     CVItem icon,
                     double price,
                     String permission,
                     List<String> children,
                     HashMap<String, String> description,
                     List<String> groups,
                     int manaPerSecond,
                     int maxMana,
                     boolean isInShop,
                     int level) {
        super(reqs,
                false,
                ItemType.CLASS,
                name,
                icon.getMat(),
                children.isEmpty() ? 1 : children.size(),
                0,
                1,
                price,
                permission,
                description,
                groups,
                isInShop,
                level);
        this.children = children;
        this.manaPerSecond = manaPerSecond;
        this.maxMana = maxMana;
    }

    public List<String> getChildren() {
        return children;
    }
    public int getManaPerSecond() {
        return manaPerSecond;
    }
    public int getMaxMana() {
        return maxMana;
    }
}
