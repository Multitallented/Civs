package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Material;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.List;

public class CivItem extends CVItem {
    private final ItemType itemType;
    private final List<String> reqs;
    private final int qty;
    private final int min;
    private final int max;
    private final double price;
    private final String permission;
    private boolean isPlaceable = false;
    private final List<String> description;

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
    public double getPrice() { return price * ConfigManager.getInstance().getPriceMultiplier(); }
    public String getPermission() { return permission; }
    public String getProcessedName() {
        return getDisplayName().replace("Civs ", "").toLowerCase();
    }
    public List<String> getDescription() { return description; }


    public CivItem(List<String> reqs,
                   boolean isPlaceable,
                   ItemType itemType,
                   String name,
                   Material material,
                   int damage,
                   int qty,
                   int min,
                   int max,
                   double price,
                   String permission,
                   List<String> description) {
        super(material, 1, damage, 100, "Civs " + name);
        this.isPlaceable = isPlaceable;
        this.itemType = itemType;
        this.reqs = reqs;
        this.qty = qty;
        this.min = min;
        this.max = max;
        this.price = price;
        this.permission = permission;
        this.description = description;
    }

    @Override
    public CivItem clone() {
        return new CivItem(reqs,
                isPlaceable,
                itemType,
                getDisplayName().replace("Civs ",""),
                getMat(),
                getDamage(),
                qty,
                min,
                max,
                price,
                permission,
                description);
    }

    public enum ItemType {
        REGION,
        SPELL,
        CLASS,
        FOLDER
    }
}