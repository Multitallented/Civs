package org.redcastlemedia.multitallented.civs.items;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleUtil;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

public abstract class CivItem extends CVItem {
    private final ItemType itemType;
    private final String localName;
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
        super(material, 1, 100);
        this.localName = name.toLowerCase();
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
                this.getProcessedName() + LocaleConstants.NAME_SUFFIX));
        return returnItem;
    }
    public CVItem getShopIcon(Player player) {
        CVItem returnItem =  shopIcon.clone();
        returnItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                this.getProcessedName() + LocaleConstants.NAME_SUFFIX));
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        returnItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                this.getProcessedName() + LocaleConstants.DESC_SUFFIX)));
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
        return LocaleManager.getInstance().getTranslation(locale, localName + LocaleConstants.DESC_SUFFIX);
    }
    public List<String> getGroups() { return groups; }


    public ItemStack createShopItemStack(Player player) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        CVItem cvItem = getShopIcon(player);
        if (getItemType() == ItemType.FOLDER) {
            FolderType folderType = (FolderType) this;
            if (!folderType.getVisible() &&
                    (Civs.perm == null || !Civs.perm.has(player, Constants.ADMIN_PERMISSION))) {
                return new ItemStack(Material.AIR);
            }
            cvItem.setDisplayName(getDisplayName(player));
            cvItem.setLore(getLore(player, false));
        }
        String maxLimit = civilian.isAtMax(this, true);
        if (getItemType() != ItemType.FOLDER && maxLimit != null) {
            CVItem item = createCVItemFromString(Material.BARRIER.name());
            item.setDisplayName(getDisplayName(player));
            LocaleUtil.getTranslationMaxItem(maxLimit, this, player, item.getLore());
            item.getLore().addAll(Util.textWrap(civilian, Util.parseColors(getDescription(civilian.getLocale()))));
            return item.createItemStack();
        }
        if (!getItemType().equals(ItemType.FOLDER)) {
            cvItem.setDisplayName(getDisplayName(player));
            cvItem.getLore().clear();
            cvItem.setLore(getLore(player, true));
        }
        ItemStack itemStack = cvItem.createItemStack();
        if (cvItem.getMmoItemType() != null) {
            ItemMeta meta = itemStack.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(0, ChatColor.BLACK + getProcessedName());
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public enum ItemType {
        REGION,
        SPELL,
        CLASS,
        FOLDER,
        TOWN
    }

    public static CivItem getFromItemStack(ItemStack itemStack) {
        if (itemStack.getItemMeta().getLore().size() < 2) {
            return null;
        }
        String processedName = ChatColor.stripColor(itemStack.getItemMeta().getLore().get(1));
        return ItemManager.getInstance().getItemType(processedName
                .replace(ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "").toLowerCase());
    }
    public static CivItem getFromItemStack(CVItem cvItem) {
        if (cvItem.getLore().size() < 2) {
            return null;
        }
        String processedName = ChatColor.stripColor(cvItem.getLore().get(1));
        return ItemManager.getInstance().getItemType(processedName
                .replace(ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "").toLowerCase());
    }

    public String getDisplayName(Player player) {
        if (player == null) {
            return LocaleManager.getInstance().getTranslation(ConfigManager.getInstance().getDefaultLanguage(),
                    localName + LocaleConstants.NAME_SUFFIX);
        }
        return LocaleManager.getInstance().getTranslation(player, localName + LocaleConstants.NAME_SUFFIX);
    }

    public List<String> getLore(Player player, boolean includePrice) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.BLACK + getProcessedName());
        LocaleManager localeManager = LocaleManager.getInstance();
        if (player == null) {
            String defaultLocale = ConfigManager.getInstance().getDefaultLanguage();
            if (includePrice) {
                lore.add(localeManager.getTranslation(defaultLocale, "price")
                        .replace("$1", Util.getNumberFormat(getPrice(), defaultLocale)));
            }
        } else {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            if (includePrice) {
                lore.add(localeManager.getTranslation(player, "price")
                        .replace("$1", Util.getNumberFormat(getPrice(), civilian.getLocale())));
            }
            lore.addAll(Util.textWrap(civilian, Util.parseColors(getDescription(civilian.getLocale()))));
        }
        return lore;
    }

    public ItemStack createItemStack(Player player) {
        CVItem cvItem = clone();
        boolean isTown = itemType == CivItem.ItemType.TOWN;
        boolean isRegion = itemType == CivItem.ItemType.REGION;
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.BLACK + player.getUniqueId().toString());
        String displayName = getDisplayName(player);
        lore.add(displayName);
        if (isTown) {
            lore.add(ChatColor.GREEN + Util.parseColors(LocaleManager.getInstance().getTranslation(player,
                    "town-instructions").replace("$1", displayName)));
        } else if (isRegion) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            lore.addAll(Util.textWrap(civilian, Util.parseColors(LocaleManager.getInstance().getTranslation(player,
                    getProcessedName() + LocaleConstants.DESC_SUFFIX))));
        }
        cvItem.setLore(lore);
        cvItem.setDisplayName(displayName);
        return cvItem.createItemStack();
    }

    @Override
    public ItemStack createItemStack() {
        CVItem cvItem = clone();
        boolean isTown = itemType == CivItem.ItemType.TOWN;
        boolean isRegion = itemType == CivItem.ItemType.REGION;
        List<String> lore = new ArrayList<>();
        lore.add("");
        String displayName = getDisplayName();
        String defaultLocale = ConfigManager.getInstance().getDefaultLanguage();
        lore.add(displayName);
        if (isTown) {
            lore.add(ChatColor.GREEN + Util.parseColors(LocaleManager.getInstance().getTranslation(defaultLocale,
                    "town-instructions").replace("$1", displayName)));
        } else if (isRegion) {
            lore.addAll(Util.textWrap(Util.parseColors(LocaleManager.getInstance().getTranslation(defaultLocale,
                    getProcessedName() + LocaleConstants.DESC_SUFFIX))));
        }
        cvItem.setLore(lore);
        cvItem.setDisplayName(displayName);
        return cvItem.createItemStack();
    }

    @Override
    public String getDisplayName() {
        return getDisplayName(null);
    }
}
