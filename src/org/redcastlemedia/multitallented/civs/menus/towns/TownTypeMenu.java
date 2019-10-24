package org.redcastlemedia.multitallented.civs.menus.towns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Util;

public class TownTypeMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("townType")) {
            CivItem regionType = ItemManager.getInstance().getItemType(params.get("regionType"));
            data.put("townType", regionType);
        }
        if (params.containsKey("showPrice") && "true".equals(params.get("showPrice"))) {
            data.put("showPrice", true);
        } else {
            data.put("showPrice", false);
        }
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        LocaleManager localeManager = LocaleManager.getInstance();
        TownType townType = (TownType) MenuManager.getData(civilian.getUuid(), "townType");
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = townType.clone();
            List<String> lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "size") +
                    ": " + (townType.getBuildRadius() * 2 + 1) + "x" + (townType.getBuildRadius() * 2 + 1) + "x" + (townType.getBuildRadiusY() * 2 + 1));
            lore.addAll(Util.textWrap(Util.parseColors(townType.getDescription(civilian.getLocale()))));
            cvItem.setLore(lore);
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("price".equals(menuIcon.getKey())) {
            boolean hasShopPerms = Civs.perm != null && Civs.perm.has(Bukkit.getPlayer(civilian.getUuid()), "civs.shop");
            String maxLimit = civilian.isAtMax(townType);
            if (hasShopPerms && maxLimit == null) {
                CVItem priceItem = CVItem.createCVItemFromString(menuIcon.getIcon());
                priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), menuIcon.getName()));
                ArrayList<String> lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + townType.getPrice());
                priceItem.setLore(lore);
                ItemStack itemStack = priceItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else if (hasShopPerms) {
                CVItem priceItem = CVItem.createCVItemFromString("BARRIER");
                priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), menuIcon.getName()));
                int max = maxLimit.equals(townType.getProcessedName()) ? townType.getCivMax() :
                        ConfigManager.getInstance().getGroups().get(maxLimit);
                ArrayList<String> lore = new ArrayList<>();
                lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "max-item")
                        .replace("$1", maxLimit)
                        .replace("$2", "" + max));
                priceItem.setLore(lore);
                return priceItem.createItemStack();
            }
        } else if ("rebuild".equals(menuIcon.getKey())) {
            if (townType.getChild() == null) {
                return new ItemStack(Material.AIR);
            }
            CVItem rebuildItem = ItemManager.getInstance()
                    .getItemType(townType.getChild().toLowerCase()).clone();
            List<String> lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), menuIcon.getDesc())
                    .replace("$1", townType.getProcessedName())
                    .replace("$2", townType.getChild()));
            rebuildItem.setLore(lore);
            ItemStack itemStack = rebuildItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("effects".equals(menuIcon.getKey())) {
            ItemStack itemStack = super.createItemStack(civilian, menuIcon, count);
            List<String> lore = new ArrayList<>(townType.getEffects().keySet());
            itemStack.getItemMeta().setLore(lore);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public String getFileName() {
        return "town-type";
    }
}
