package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.FolderType;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleUtil;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "shop") @SuppressWarnings("unused")
public class ShopMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return data;
        }

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        CivItem parent;
        if (params.get("parent") == null) {
            parent = null;
        } else {
            parent = ItemManager.getInstance().getItemType(params.get("parent"));
            data.put("parent", parent);
        }
        String sortType = params.get("sort");
        if (sortType == null) {
            sortType = "category";
        }
        int level = -1;
        if (params.containsKey("level")) {
            level = Integer.parseInt(params.get("level"));
        }
        List<CivItem> shopItems = null;
        ArrayList<CVItem> levelList = new ArrayList<>();
        if (sortType.equals("level")) {
            if (level < 0) {
                int currentLevel = 1;
                for (String matString : ConfigManager.getInstance().getLevelList()) {
                    CVItem cvItem = CVItem.createCVItemFromString(matString);
                    cvItem.setDisplayName(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                            "level").replace("$1", "" + currentLevel));
                    ArrayList<String> lore = new ArrayList<>();
                    lore.add("" + currentLevel);
                    cvItem.setLore(lore);
                    levelList.add(cvItem);
                    currentLevel++;
                }

            } else {
                shopItems = createLevelList(civilian, level);
            }
        } else {
            shopItems = ItemManager.getInstance().getShopItems(civilian, parent);
            shopItems.removeIf(new Predicate<CivItem>() {
                @Override
                public boolean test(CivItem civItem) {
                    if (!(civItem instanceof FolderType)) {
                        return false;
                    }
                    FolderType folderType = (FolderType) civItem;
                    return !folderType.getVisible();
                }
            });
        }
        if (shopItems != null) {
            data.put("shopItems", shopItems);
            int maxPage = (int) Math.ceil((double) shopItems.size() / (double) itemsPerPage.get("items"));
            maxPage = maxPage > 0 ? maxPage - 1 : 0;
            data.put("maxPage", maxPage);
        } else if (!levelList.isEmpty()) {
            data.put("levelList", levelList);
            int maxPage = (int) Math.ceil((double) levelList.size() / (double) itemsPerPage.get("items"));
            maxPage = maxPage > 0 ? maxPage - 1 : 0;
            data.put("maxPage", maxPage);
        }

        for (String key : params.keySet()) {
            if (key.equals("page") || key.equals("maxPage") ||
                    key.equals("parent")) {
                continue;
            }
            data.put(key, params.get(key));
        }

        return data;
    }

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if (menuIcon.getKey().equals("parent")) {
            CivItem parent = (CivItem) MenuManager.getData(civilian.getUuid(), "parent");
            if (parent == null) {
                return new ItemStack(Material.AIR);
            }
            CVItem icon = parent.getShopIcon(civilian.getLocale());
            icon.setDisplayName(LocaleManager.getInstance()
                    .getTranslationWithPlaceholders(player, parent.getProcessedName() + LocaleConstants.NAME_SUFFIX));
            icon.getLore().clear();
            icon.getLore().add(ChatColor.BLACK + parent.getProcessedName());
            icon.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance()
                    .getTranslationWithPlaceholders(player,
                    parent.getProcessedName() + "-desc")));
            putActions(civilian, menuIcon, icon.createItemStack(), count);
            return icon.createItemStack();
        } else if (menuIcon.getKey().equals("items")) {
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int index = count + menuIcon.getIndex().size() * page;
            ArrayList<CivItem> shopItems = (ArrayList<CivItem>) MenuManager.getData(civilian.getUuid(), "shopItems");
            ArrayList<CVItem> levelList = (ArrayList<CVItem>) MenuManager.getData(civilian.getUuid(), "levelList");
            if (shopItems != null) {
                if (shopItems.size() <= index) {
                    return new ItemStack(Material.AIR);
                }
                CivItem civItem = shopItems.get(index);
                if (civItem instanceof FolderType) {
                    FolderType folderType = (FolderType) civItem;
                    if (!folderType.getVisible()) {
                        return new ItemStack(Material.AIR);
                    }
                }
                ItemStack itemStack = createShopItem(civItem, civilian);
                if (itemStack.getType() == Material.AIR) {
                    return itemStack;
                }
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else if (levelList != null) {
                if (levelList.size() <= index) {
                    return new ItemStack(Material.AIR);
                }
                ItemStack itemStack = levelList.get(index).createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                return new ItemStack(Material.AIR);
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if (actionString.equals("view-item")) {
            String key = clickedItem.getItemMeta().getLore().get(0);
            Player player = Bukkit.getPlayer(civilian.getUuid());
            String sortType = (String) MenuManager.getData(civilian.getUuid(), "sort");
            HashMap<String, String> params = new HashMap<>();
            String name = ChatColor.stripColor(key).toLowerCase();
            CivItem civItem = ItemManager.getInstance().getItemType(name);
            if (civItem != null) {
                if (civItem.getItemType() == CivItem.ItemType.REGION) {
                    params.put("regionType", name);
                    params.put("showPrice", "true");
                    MenuManager.getInstance().openMenu(player, "region-type", params);
                    return true;
                } else if (civItem.getItemType() == CivItem.ItemType.TOWN) {
                    params.put("townType", name);
                    params.put("showPrice", "true");
                    MenuManager.getInstance().openMenu(player, "town-type", params);
                    return true;
                }
            } else if (clickedItem.getType() == Material.BARRIER) {
                return true;
            }
            if ("level".equals(sortType)) {
                int level = Integer.parseInt(name);
                params.put("level", "" + level);
                params.put("sort", "level");
                MenuManager.getInstance().openMenu(player, "shop", params);
                return true;
            } else if ("category".equals(sortType)) {
                params.put("sort", "category");
                params.put("parent", ChatColor.stripColor(key).toLowerCase());
                MenuManager.getInstance().openMenu(player, "shop", params);
                return true;
            }
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }

    private ItemStack createShopItem(CivItem civItem, Civilian civilian) {
        LocaleManager localeManager = LocaleManager.getInstance();
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        CVItem civItem1 = civItem.getShopIcon(civilian.getLocale());
        if (civItem.getItemType() == CivItem.ItemType.FOLDER) {
            FolderType folderType = (FolderType) civItem;
            if (!folderType.getVisible() &&
                    (Civs.perm == null || !Civs.perm.has(player, Constants.ADMIN_PERMISSION))) {
                return new ItemStack(Material.AIR);
            }
            civItem1.setDisplayName(localeManager.getTranslationWithPlaceholders(player, folderType.getProcessedName() + "-name"));
            civItem1.getLore().add(ChatColor.BLACK + folderType.getProcessedName());
            civItem1.getLore().addAll(Util.textWrap(civilian, localeManager.getTranslationWithPlaceholders(player, folderType.getProcessedName() + "-desc")));
        }
        String maxLimit = civilian.isAtMax(civItem);
        if (civItem.getItemType() != CivItem.ItemType.FOLDER && maxLimit != null) {
            CVItem item = CVItem.createCVItemFromString(Material.BARRIER.name());
            item.setDisplayName(localeManager.getTranslationWithPlaceholders(player,
                    civItem.getProcessedName() + LocaleConstants.NAME_SUFFIX));
            LocaleUtil.getTranslationMaxItem(maxLimit, civItem, player, item.getLore());
            item.getLore().addAll(Util.textWrap(civilian, Util.parseColors(civItem.getDescription(civilian.getLocale()))));
            return item.createItemStack();
        }
        if (!civItem.getItemType().equals(CivItem.ItemType.FOLDER)) {
            civItem1.setDisplayName(localeManager.getTranslationWithPlaceholders(player,
                    civItem.getProcessedName() + LocaleConstants.NAME_SUFFIX));
            civItem1.getLore().clear();
            civItem1.getLore().add(ChatColor.BLACK + civItem.getProcessedName());
            civItem1.getLore().add(localeManager.getTranslationWithPlaceholders(player, "price")
                    .replace("$1", Util.getNumberFormat(civItem.getPrice(), civilian.getLocale())));
            civItem1.getLore().addAll(Util.textWrap(civilian, Util.parseColors(civItem.getDescription(civilian.getLocale()))));
        }
        ItemStack itemStack = civItem1.createItemStack();
        if (civItem1.getMmoItemType() != null) {
            List<String> lore = itemStack.getItemMeta().getLore();
            lore.add(0, ChatColor.BLACK + civItem.getProcessedName());
        }
        return itemStack;
    }

    private ArrayList<CivItem> createLevelList(Civilian civilian, int level) {
        ArrayList<CivItem> levelList = new ArrayList<>();
        for (CivItem civItem : ItemManager.getInstance().getItemsByLevel(level)) {
            if (civItem.getItemType() == CivItem.ItemType.FOLDER ||
                    !civItem.getInShop()) {
                continue;
            }
            if (civilian.isAtMax(civItem) != null) {
                continue;
            }
            levelList.add(civItem);
        }
        return levelList;
    }
}
