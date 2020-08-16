package org.redcastlemedia.multitallented.civs.items;

import java.util.ArrayList;
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
                      boolean visible,
                      int level) {
        super(reqs,
                false,
                ItemType.FOLDER,
                name,
                name,
                icon.getMat(),
                icon,
                children.isEmpty() ? 1 : children.size(),
                0,
                1,
                price,
                permission,
                new ArrayList<String>(),
                true,
                level);
        this.children = children;
        this.visible = visible;
    }

    public List<CivItem> getChildren() {
        return children;
    }
    public boolean getVisible() { return visible; }
}
