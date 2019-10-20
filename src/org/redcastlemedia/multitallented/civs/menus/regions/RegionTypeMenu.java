package org.redcastlemedia.multitallented.civs.menus.regions;

import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionTypeMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("regionType")) {
            CivItem regionType = ItemManager.getInstance().getItemType(params.get("regionType"));
            data.put("regionType", regionType);
        }
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        LocaleManager localeManager = LocaleManager.getInstance();
        if ("icon".equals(menuIcon.getKey())) {
            RegionType regionType = (RegionType) MenuManager.getData(civilian.getUuid(), "regionType");
            CVItem shopIcon = regionType.getShopIcon().clone();
            List<String> lore = new ArrayList<>();
            shopIcon.setDisplayName(localeManager.getTranslation(civilian.getLocale(), regionType.getProcessedName() + "-name"));
            lore.add(localeManager.getTranslation(civilian.getLocale(), "size") +
                    ": " + (regionType.getBuildRadiusX() * 2 + 1) + "x" + (regionType.getBuildRadiusZ() * 2 + 1) + "x" + (regionType.getBuildRadiusY() * 2 + 1));
            if (regionType.getEffectRadius() != regionType.getBuildRadius()) {
                lore.add(localeManager.getTranslation(civilian.getLocale(), "range") +
                        ": " + regionType.getEffectRadius());
            }

            lore.addAll(Util.textWrap(regionType.getDescription(civilian.getLocale())));
            shopIcon.setLore(lore);
            return shopIcon.createItemStack();
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public String getFileName() {
        return "region-type";
    }
}
