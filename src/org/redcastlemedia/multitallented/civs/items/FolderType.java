package org.redcastlemedia.multitallented.civs.items;

import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.List;

public class FolderType extends CivItem {
    private final List<CivItem> children;

    public FolderType(List<String> reqs, String name, CVItem icon, double price, String permission, List<CivItem> children) {
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
                permission);
        this.children = children;
    }

    public List<CivItem> getChildren() {
        return children;
    }
}
