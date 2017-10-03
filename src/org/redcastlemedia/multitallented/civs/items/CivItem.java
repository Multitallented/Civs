package org.redcastlemedia.multitallented.civs.items;

public class CivItem {
    private final ItemType itemType;

    public ItemType getItemType() {
        return itemType;
    }

    public boolean isPlaceable() {
        return isPlaceable;
    }

    private boolean isPlaceable = false;

    public CivItem(boolean isPlaceable, ItemType itemType) {
        this.isPlaceable = isPlaceable;
        this.itemType = itemType;
    }

    public enum ItemType {
        REGION,
        SPELL
    }
}