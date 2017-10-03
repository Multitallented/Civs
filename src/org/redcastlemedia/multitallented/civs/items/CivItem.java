package org.redcastlemedia.multitallented.civs.items;

public class CivItem {
    private final ItemType itemType;
    private boolean isPlaceable = false;

    public ItemType getItemType() {
        return itemType;
    }
    public boolean isPlaceable() {
        return isPlaceable;
    }


    public CivItem(boolean isPlaceable, ItemType itemType) {
        this.isPlaceable = isPlaceable;
        this.itemType = itemType;
    }

    public enum ItemType {
        REGION,
        SPELL
    }
}