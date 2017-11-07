package org.redcastlemedia.multitallented.civs.civclass;

import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashMap;
import java.util.List;

public class ClassType extends CivItem {
    private final List<CivItem> children;

    public ClassType(List<String> reqs,
                     String name,
                     CVItem icon,
                     double price,
                     String permission,
                     List<CivItem> children,
                     HashMap<String, String> description,
                     List<String> groups) {
        super(reqs,
                false,
                ItemType.CLASS,
                name,
                icon.getMat(),
                icon.getDamage(),
                children.isEmpty() ? 1 : children.size(),
                0,
                1,
                price,
                permission,
                description,
                groups);
        this.children = children;
    }

    public List<CivItem> getChildren() {
        return children;
    }
}
