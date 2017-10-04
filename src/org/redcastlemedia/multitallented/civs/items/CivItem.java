package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Material;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.List;

public class CivItem extends CVItem {
    private final ItemType itemType;
    private final List<String> reqs;
    private final int qty;
    private final int min;
    private final int max;
    private boolean isPlaceable = false;

    public ItemType getItemType() {
        return itemType;
    }
    public boolean isPlaceable() {
        return isPlaceable;
    }
    public List<String> getCivReqs() {
        return reqs;
    }
    public int getCivQty() { return qty; }
    public int getCivMin() { return min; }
    public int getCivMax() { return max; }


    public CivItem(List<String> reqs,
                   boolean isPlaceable,
                   ItemType itemType,
                   String name,
                   Material material,
                   int damage,
                   int qty,
                   int min,
                   int max) {
        super(material, 1, damage, 100, "Civs " + name);
        this.isPlaceable = isPlaceable;
        this.itemType = itemType;
        this.reqs = reqs;
        this.qty = qty;
        this.min = min;
        this.max = max;
    }

    public enum ItemType {
        REGION,
        SPELL,
        CLASS
    }
}