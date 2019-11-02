package org.redcastlemedia.multitallented.civs.menus.regions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
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
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.EvolveEffect;
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
        RegionType regionType = (RegionType) MenuManager.getData(civilian.getUuid(), "regionType");
        if ("icon".equals(menuIcon.getKey())) {
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
            ItemStack itemStack = shopIcon.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("price".equals(menuIcon.getKey())) {
            boolean showPrice = (boolean) MenuManager.getData(civilian.getUuid(), "showPrice");
            Player player = Bukkit.getPlayer(civilian.getUuid());
            boolean isCivsAdmin = Civs.perm != null && Civs.perm.has(player, "civs.admin");
            boolean hasShopPerms = Civs.perm != null && Civs.perm.has(player, "civs.shop");
            String maxLimit = civilian.isAtMax(regionType);
            boolean isInShop = regionType.getInShop();
            ArrayList<String> lore = new ArrayList<>();
            boolean hasItemUnlocked = ItemManager.getInstance().hasItemUnlocked(civilian, regionType);
            if (showPrice && (isCivsAdmin || (hasShopPerms && maxLimit == null && isInShop))) {
                CVItem priceItem;
                if (hasItemUnlocked || isCivsAdmin) {
                    priceItem = CVItem.createCVItemFromString(menuIcon.getIcon());
                } else {
                    priceItem = CVItem.createCVItemFromString("IRON_BARS");
                    lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "item-locked"));
                }
                priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), menuIcon.getName()));
                lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + regionType.getPrice());
                priceItem.setLore(lore);
                ItemStack itemStack = priceItem.createItemStack();
                if (hasItemUnlocked || isCivsAdmin) {
                    putActions(civilian, menuIcon, itemStack, count);
                }
                return itemStack;
            } else if (showPrice && hasShopPerms && isInShop) {
                CVItem priceItem = CVItem.createCVItemFromString("BARRIER");
                priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
                int max = maxLimit.equals(regionType.getProcessedName()) ? regionType.getCivMax() :
                        ConfigManager.getInstance().getGroups().get(maxLimit);
                lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "max-item")
                        .replace("$1", maxLimit)
                        .replace("$2", "" + max));
                return priceItem.createItemStack();
            }
        } else if ("rebuild-required-single".equals(menuIcon.getKey())) {

        } else if ("rebuild-required-multiple".equals(menuIcon.getKey())) {

        } else if ("rebuild-optional-single".equals(menuIcon.getKey())) {

        } else if ("rebuild-optional-multiple".equals(menuIcon.getKey())) {

        } else if ("evolve".equals(menuIcon.getKey())) {
            if (!regionType.getEffects().containsKey(EvolveEffect.KEY)) {
                return new ItemStack(Material.AIR);
            }
            String evolveName = regionType.getEffects().get(EvolveEffect.KEY);
            if (evolveName == null) {
                return new ItemStack(Material.AIR);
            }
            CVItem shopItem = ItemManager.getInstance().getItemType(evolveName.split("\\.")[0]).getShopIcon();
            ItemStack itemStack = shopItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("biome".equals(menuIcon.getKey())) {
            ItemStack itemStack = super.createItemStack(civilian, menuIcon, count);
            ArrayList<String> lore = new ArrayList<>();
            for (Biome biome : regionType.getBiomes()) {
                lore.add(biome.name());
            }
            itemStack.getItemMeta().setLore(lore);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("towns".equals(menuIcon.getKey())) {
            ItemStack itemStack = super.createItemStack(civilian, menuIcon, count);
            for (String townTypeName : regionType.getTowns()) {
                String localizedTownTypeName = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        townTypeName + "-name");
                itemStack.getItemMeta().getLore().add(localizedTownTypeName);
            }
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("effects".equals(menuIcon.getKey())) {
            ItemStack itemStack = super.createItemStack(civilian, menuIcon, count);
            itemStack.getItemMeta().getLore().addAll(regionType.getEffects().keySet());
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("reagents".equals(menuIcon.getKey()) ||
                   "output".equals(menuIcon.getKey()) ||
                   "payout".equals(menuIcon.getKey()) ||
                   "power-input".equals(menuIcon.getKey()) ||
                   "power-output".equals(menuIcon.getKey()) ||
                   "input".equals(menuIcon.getKey())) {
            if (count >= regionType.getUpkeeps().size()) {
                return new ItemStack(Material.AIR);
            }
            switch (menuIcon.getKey()) {
                case "reagents":
                    if (regionType.getUpkeeps().get(count).getReagents().isEmpty()) {
                        return new ItemStack(Material.AIR);
                    }
                    break;
                case "output":
                    if (regionType.getUpkeeps().get(count).getOutputs().isEmpty()) {
                        return new ItemStack(Material.AIR);
                    }
                    break;
                case "payout":
                    if (regionType.getUpkeeps().get(count).getPayout() <= 0) {
                        return new ItemStack(Material.AIR);
                    }
                    break;
                case "input":
                    if (regionType.getUpkeeps().get(count).getInputs().isEmpty()) {
                        return new ItemStack(Material.AIR);
                    }
                    break;
                case "power-input":
                    if (regionType.getUpkeeps().get(count).getPowerInput() <= 0) {
                        return new ItemStack(Material.AIR);
                    }
                    break;
                case "power-output":
                    if (regionType.getUpkeeps().get(count).getPowerOutput() <= 0) {
                        return new ItemStack(Material.AIR);
                    }
                    break;
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public String getFileName() {
        return "region-type";
    }
}
