package org.redcastlemedia.multitallented.civs.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleUtil;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.skills.SkillManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

import lombok.Getter;

public abstract class CivItem extends CVItem {
    private final ItemType itemType;
    @Getter private final String key;
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
                   String key,
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
        this.key = key;
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

    public double getPrice(Civilian civilian) {
        return SkillManager.getInstance().getSkillDiscountedPrice(civilian, this);
    }

    @Deprecated
    public double getPrice() {
        return getRawPrice();
    }
    public double getRawPrice() {
        ConfigManager configManager = ConfigManager.getInstance();
        return price * configManager.getPriceMultiplier() + configManager.getPriceBase();
    }

    public String getPermission() { return permission; }
    public String getProcessedName() {
        return this.key;
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
        cvItem.setCivItemName(getProcessedName());
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
        if (getItemType().equals(ItemType.REGION)) {
            RegionType regionType = (RegionType) this;
            Region wonder = wonderAlreadyBuilt(regionType);
            if (wonder != null) {
                CVItem item = createCVItemFromString(Material.BARRIER.name());
                item.setDisplayName(getDisplayName(player));
                String ownerName = "Unowned";
                Set<UUID> wonderOwners = wonder.getOwners();
                if (!wonderOwners.isEmpty()) {
                    OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(wonderOwners.iterator().next());
                    if (ownerPlayer != null && ownerPlayer.hasPlayedBefore() && ownerPlayer.getName() != null) {
                        ownerName = ownerPlayer.getName();
                    }
                }
                item.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player, "cant-build-wonder")
                        .replace("$1", ownerName)));
                item.getLore().add(wonder.getLocation().getBlockX() + "x " + wonder.getLocation().getBlockZ() + "z");
                item.getLore().addAll(Util.textWrap(civilian, Util.parseColors(getDescription(civilian.getLocale()))));
                return item.createItemStack();
            }
        }
        if (!getItemType().equals(ItemType.FOLDER)) {
            cvItem.setDisplayName(getDisplayName(player));
            cvItem.getLore().clear();
            cvItem.setLore(getLore(player, true));
        }
        ItemStack itemStack = cvItem.createItemStack();
        if (cvItem.getMmoItemType() != null) {
            ItemMeta meta = itemStack.getItemMeta();
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    private Region wonderAlreadyBuilt(RegionType regionType) {
        if (regionType.getEffects().containsKey(Constants.WONDER)) {
            for (Region region : RegionManager.getInstance().getAllRegions()) {
                if (region.getEffects().containsKey(Constants.WONDER) &&
                        regionType.getProcessedName().equals(region.getType())) {
                    return region;
                }
            }
        }
        return null;
    }

    public enum ItemType {
        REGION,
        SPELL,
        CLASS,
        FOLDER,
        TOWN
    }

    public static CivItem getFromItemStack(ItemStack itemStack) {
        if (Civs.getInstance() != null && itemStack.getItemMeta() != null &&
                itemStack.getItemMeta().getPersistentDataContainer()
                        .has(new NamespacedKey(Civs.getInstance(), Civs.NAME), PersistentDataType.STRING)) {
            String itemTypeKey = itemStack.getItemMeta().getPersistentDataContainer()
                    .get(new NamespacedKey(Civs.getInstance(), Civs.NAME), PersistentDataType.STRING);
            if (itemTypeKey != null) {
                return ItemManager.getInstance().getItemType(itemTypeKey);
            }
        }
        if (itemStack.getItemMeta().getLore().size() < 2) {
            return null;
        }
        String processedName = ChatColor.stripColor(itemStack.getItemMeta().getLore().get(1));
        return ItemManager.getInstance().getItemType(processedName
                .replace(ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "").toLowerCase());
    }
    public static CivItem getFromItemStack(CVItem cvItem) {
        if (cvItem.getCivItemName() != null) {
            return ItemManager.getInstance().getItemType(cvItem.getCivItemName());
        }
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
        LocaleManager localeManager = LocaleManager.getInstance();
        if (player == null) {
            String defaultLocale = ConfigManager.getInstance().getDefaultLanguage();
            if (includePrice) {
                lore.add(localeManager.getTranslation(defaultLocale, "price")
                        .replace("$1", Util.getNumberFormat(getRawPrice(), defaultLocale)));
            }
        } else {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            if (includePrice) {
                lore.add(localeManager.getTranslation(player, "price")
                        .replace("$1", Util.getNumberFormat(getPrice(civilian), civilian.getLocale())));
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
        String displayName = getDisplayName(player);
        if (isTown) {
            lore.add(ChatColor.GREEN + Util.parseColors(LocaleManager.getInstance().getTranslation(player,
                    "town-instructions").replace("$1", displayName)));
        } else if (isRegion) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            lore.addAll(Util.textWrap(civilian, Util.parseColors(LocaleManager.getInstance().getTranslation(player,
                    getProcessedName() + LocaleConstants.DESC_SUFFIX))));
        }
        cvItem.setOwnerBound(player.getUniqueId());
        cvItem.setCivItemName(key);
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
        cvItem.setCivItemName(getProcessedName());
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
