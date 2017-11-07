package org.redcastlemedia.multitallented.civs.items;

import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FolderType extends CivItem {
    private final List<CivItem> children;
    private final boolean visible;

    public FolderType(List<String> reqs,
                      String name,
                      CVItem icon,
                      double price,
                      String permission,
                      List<CivItem> children,
                      boolean visible) {
        super(reqs,
                false,
                ItemType.FOLDER,
                name,
                icon.getMat(),
                icon.getDamage(),
                children.isEmpty() ? 1 : children.size(),
                0,
                1,
                price,
                permission,
                new HashMap<String, String>(),
                new ArrayList<String>());
        this.children = children;
        this.visible = visible;
    }

    public List<CivItem> getChildren() {
        return children;
    }
    public boolean getVisible() { return visible; }
}
