package org.redcastlemedia.multitallented.civs.menus.regions;

import org.bukkit.Material;
import org.bukkit.block.Biome;
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
            return shopIcon.createItemStack();
        } else if ("price".equals(menuIcon.getKey())) {
            // TODO lock item conditionally
        } else if ("rebuild".equals(menuIcon.getKey())) {
            // TODO handle either 1 rebuild or multiple
        } else if ("evolve".equals(menuIcon.getKey())) {
            if (!regionType.getEffects().containsKey(EvolveEffect.KEY)) {
                return new ItemStack(Material.AIR);
            }
            String evolveName = regionType.getEffects().get(EvolveEffect.KEY);
            if (evolveName == null) {
                return new ItemStack(Material.AIR);
            }
            CVItem shopItem = ItemManager.getInstance().getItemType(evolveName.split("\\.")[0]).getShopIcon();
            return shopItem.createItemStack();
        } else if ("biome".equals(menuIcon.getKey())) {
            ItemStack itemStack = super.createItemStack(civilian, menuIcon, count);
            ArrayList<String> lore = new ArrayList<>();
            for (Biome biome : regionType.getBiomes()) {
                lore.add(biome.name());
            }
            itemStack.getItemMeta().setLore(lore);
            return itemStack;
        } else if ("towns".equals(menuIcon.getKey())) {
            ItemStack itemStack = super.createItemStack(civilian, menuIcon, count);
            for (String townTypeName : regionType.getTowns()) {
                String localizedTownTypeName = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        townTypeName + "-name");
                itemStack.getItemMeta().getLore().add(localizedTownTypeName);
            }
            return itemStack;
        } else if ("effects".equals(menuIcon.getKey())) {
            ItemStack itemStack = super.createItemStack(civilian, menuIcon, count);
            itemStack.getItemMeta().getLore().addAll(regionType.getEffects().keySet());
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
