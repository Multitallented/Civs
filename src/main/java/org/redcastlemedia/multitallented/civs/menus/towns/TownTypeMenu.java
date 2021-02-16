package org.redcastlemedia.multitallented.civs.menus.towns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleUtil;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "town-type") @SuppressWarnings("unused")
public class TownTypeMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey(Constants.TOWN_TYPE)) {
            CivItem regionType = ItemManager.getInstance().getItemType(params.get(Constants.TOWN_TYPE));
            data.put(Constants.TOWN_TYPE, regionType);
        }
        if (params.containsKey(Constants.SHOW_PRICE) && "true".equals(params.get(Constants.SHOW_PRICE))) {
            data.put(Constants.SHOW_PRICE, true);
        } else {
            data.put(Constants.SHOW_PRICE, false);
        }
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        LocaleManager localeManager = LocaleManager.getInstance();
        TownType townType = (TownType) MenuManager.getData(civilian.getUuid(), Constants.TOWN_TYPE);
        if (townType == null) {
            return new ItemStack(Material.AIR);
        }
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = townType.clone();
            cvItem.setDisplayName(townType.getDisplayName(player));
            List<String> lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(player, "size") +
                    ": " + (townType.getBuildRadius() * 2 + 1) + "x" + (townType.getBuildRadius() * 2 + 1) + "x" + (townType.getBuildRadiusY() * 2 + 1));
            lore.addAll(Util.textWrap(civilian, Util.parseColors(townType.getDescription(civilian.getLocale()))));
            cvItem.setLore(lore);
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("price".equals(menuIcon.getKey())) {
            boolean hasShopPerms = Civs.perm != null && Civs.perm.has(player, "civs.shop");
            String maxLimit = civilian.isAtMax(townType);
            if (hasShopPerms && maxLimit == null) {
                CVItem priceItem = CVItem.createCVItemFromString(menuIcon.getIcon());
                priceItem.setDisplayName(localeManager.getTranslation(player, menuIcon.getName()));
                ArrayList<String> lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(player, "price")
                        .replace("$1", Util.getNumberFormat(townType.getPrice(civilian), civilian.getLocale())));
                priceItem.setLore(lore);
                ItemStack itemStack = priceItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else if (hasShopPerms) {
                CVItem priceItem = CVItem.createCVItemFromString("BARRIER");
                priceItem.setDisplayName(localeManager.getTranslation(player, menuIcon.getName()));
                ArrayList<String> lore = new ArrayList<>();
                LocaleUtil.getTranslationMaxItem(maxLimit, townType, player, lore);
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
            lore.add(localeManager.getTranslation(player, menuIcon.getDesc())
                    .replace("$1", townType.getProcessedName())
                    .replace("$2", townType.getChild()));
            rebuildItem.setLore(lore);
            ItemStack itemStack = rebuildItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("build-reqs".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            String localizedName = townType.getDisplayName(player);
            cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc()).replace("$1", localizedName)));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("effects".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.getLore().addAll(townType.getEffects().keySet());
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("population".equals(menuIcon.getKey())) {
            if (townType.getChild() == null || townType.getChildPopulation() < 1) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(player, count);
            CivItem childItem = ItemManager.getInstance().getItemType(townType.getChild().toLowerCase());
            String childName = childItem.getDisplayName(player);
            cvItem.getLore().clear();
            cvItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc()).replace("$1", childName)
                    .replace("$2", "" + townType.getChildPopulation())));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}
