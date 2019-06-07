package org.redcastlemedia.multitallented.civs.items;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashMap;
import java.util.List;

public abstract class CivItem extends CVItem {
    private final ItemType itemType;
    private final List<String> reqs;
    private final int qty;
    private final int min;
    private final int max;
    private final double price;
    private final String permission;
    private final boolean isInShop;
    private boolean isPlaceable = false;
    private final HashMap<String, String> description;
    private final List<String> groups;
    @Getter
    private final CVItem shopIcon;
    @Getter
    private final int level;

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
    public boolean getInShop() { return isInShop; }

    public double getPrice() {
        ConfigManager configManager = ConfigManager.getInstance();
        return price * configManager.getPriceMultiplier() + configManager.getPriceBase();
    }

    public String getPermission() { return permission; }
    public String getProcessedName() {
        return processItemName(getDisplayName());
    }
    public static String processItemName(String input) {
        input = ChatColor.stripColor(input);
        return input.replace(ConfigManager.getInstance().getCivsItemPrefix(), "").toLowerCase();
    }
    public String getDescription(String locale) {
        String localizedDescription = description.get(locale);
        if (localizedDescription == null) {
            localizedDescription = description.get(ConfigManager.getInstance().getDefaultLanguage());
            if (localizedDescription == null) {
                return "";
            }
        }
        return localizedDescription;
    }
    public List<String> getGroups() { return groups; }


    public CivItem(List<String> reqs,
                   boolean isPlaceable,
                   ItemType itemType,
                   String name,
                   Material material,
                   CVItem shopIcon,
                   int qty,
                   int min,
                   int max,
                   double price,
                   String permission,
                   HashMap<String, String> description,
                   List<String> groups,
                   boolean isInShop,
                   int level) {
        super(material, 1, 100, ConfigManager.getInstance().getCivsItemPrefix() + name);
        this.isPlaceable = isPlaceable;
        this.shopIcon = new CVItem(shopIcon.getMat(),
                shopIcon.getQty(),
                (int) shopIcon.getChance(),
                ConfigManager.getInstance().getCivsItemPrefix() + name);
        this.itemType = itemType;
        this.reqs = reqs;
        this.qty = qty;
        this.min = min;
        this.max = max;
        this.price = price;
        this.permission = permission;
        this.description = description;
        this.groups = groups;
        this.isInShop = isInShop;
        this.level = level;
    }

    public enum ItemType {
        REGION,
        SPELL,
        CLASS,
        FOLDER,
        TOWN
    }
}
