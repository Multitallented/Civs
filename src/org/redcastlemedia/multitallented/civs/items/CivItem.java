package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Material;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;

public class CivItem extends CVItem {
    private final ItemType itemType;
    private boolean isPlaceable = false;

    public ItemType getItemType() {
        return itemType;
    }
    public boolean isPlaceable() {
        return isPlaceable;
    }


    public CivItem(boolean isPlaceable, ItemType itemType, String name, Material material, int damage) {
        super(material, 1, damage, 100, "Civs " + name);
        this.isPlaceable = isPlaceable;
        this.itemType = itemType;
    }

    public enum ItemType {
        REGION,
        SPELL
    }
}