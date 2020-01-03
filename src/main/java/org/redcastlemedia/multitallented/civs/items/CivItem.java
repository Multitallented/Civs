package org.redcastlemedia.multitallented.civs.items;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;

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
    private final List<String> groups;
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
    public CVItem getShopIcon(String locale) {
        CVItem returnItem =  shopIcon.clone();
        returnItem.setDisplayName(LocaleManager.getInstance().getTranslation(locale,
                this.getProcessedName() + "-name"));
        return returnItem;
    }

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
        return input.replace(ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "").toLowerCase();
    }
    public String getDescription(String locale) {
        return LocaleManager.getInstance().getTranslation(locale, getProcessedName().toLowerCase() + "-desc");
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
                   List<String> groups,
                   boolean isInShop,
                   int level) {
        super(material, 1, 100, ConfigManager.getInstance().getCivsItemPrefix() + name);
        this.isPlaceable = isPlaceable;
        if (shopIcon.getMmoItemType() == null) {
            this.shopIcon = new CVItem(shopIcon.getMat(),
                    shopIcon.getQty(),
                    (int) shopIcon.getChance(),
                    ConfigManager.getInstance().getCivsItemPrefix() + name);
        } else {
            this.shopIcon = shopIcon;
        }
        this.itemType = itemType;
        this.reqs = reqs;
        this.qty = qty;
        this.min = min;
        this.max = max;
        this.price = price;
        this.permission = permission;
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

    public static CivItem getFromItemStack(ItemStack itemStack) {
        String processedName = ChatColor.stripColor(itemStack.getItemMeta().getLore().get(1));
        return ItemManager.getInstance().getItemType(processedName
                .replace(ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "").toLowerCase());
    }
    public static CivItem getFromItemStack(CVItem cvItem) {
        String processedName = ChatColor.stripColor(cvItem.getLore().get(1));
        return ItemManager.getInstance().getItemType(processedName
                .replace(ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "").toLowerCase());
    }
}
