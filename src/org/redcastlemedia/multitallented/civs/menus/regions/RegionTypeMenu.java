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
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(params.get("regionType"));
            data.put("regionType", regionType);
            if (!regionType.getRebuild().isEmpty()) {
                data.put("rebuildRegion", regionType.getRebuild().get(0));
                StringBuilder regionList = new StringBuilder();
                for (String rebuildRegionString : regionType.getRebuild()) {
                    regionList.append(rebuildRegionString);
                    regionList.append(",");
                }
                data.put("rebuildRegions", regionList.substring(0, regionList.length() - 1));
            }
            if (regionType.getEffects().containsKey(EvolveEffect.KEY)) {
                data.put("evolveRegion", regionType.getEffects().get(EvolveEffect.KEY)
                        .split(":")[1].split("\\.")[0]);
            }
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
        String localizedRegionTypeName = LocaleManager.getInstance().getTranslation(civilian.getLocale(), regionType.getProcessedName() + "-name");
        if ("icon".equals(menuIcon.getKey())) {
            CVItem shopIcon = regionType.getShopIcon(civilian.getLocale());
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
        } else if ("build-reqs".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getDesc()).replace("$1", localizedRegionTypeName)));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("limits".equals(menuIcon.getKey())) {
            int lowestDiff = 99999;
            int amount = -1;
            int limit = -1;
            String limitType = null;
            if (regionType.getCivMax() != -1) {
                int currentAmount = civilian.getCountRegions(regionType.getProcessedName());
                int currentDiff = regionType.getCivMax() - currentAmount;
                if (lowestDiff > currentDiff) {
                    lowestDiff = currentDiff;
                    limitType = regionType.getProcessedName();
                    amount = currentAmount;
                    limit = regionType.getCivMax();
                }
            }
            for (String groupName : regionType.getGroups()) {
                if (!ConfigManager.getInstance().getGroups().containsKey(groupName)) {
                    continue;
                }
                int currentLimit = ConfigManager.getInstance().getGroups().get(groupName);
                int currentAmount = civilian.getCountGroup(groupName);
                int currentDiff = currentLimit - currentAmount;
                if (lowestDiff > currentDiff) {
                    lowestDiff = currentDiff;
                    limitType = groupName;
                    amount = currentAmount;
                    limit = currentLimit;
                }
            }
            if (limitType == null) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = CVItem.createCVItemFromString(menuIcon.getIcon());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()));
            if (regionType.getProcessedName().equals(limitType)) { // TODO translate group names
                limitType = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        limitType + "-name");
            }
            cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getDesc()).replace("$1", "" + amount)
                    .replace("$2", "" + limit).replace("$3", limitType)));

            ItemStack itemStack = cvItem.createItemStack();
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
        } else if ("rebuild-required-single".equals(menuIcon.getKey()) ||
                "rebuild-optional-single".equals(menuIcon.getKey())) {
            if (regionType.getRebuild().size() != 1) {
                return new ItemStack(Material.AIR);
            }
            RegionType rebuildType = (RegionType) ItemManager.getInstance().getItemType(regionType.getRebuild().get(0));
            CVItem shopIcon = rebuildType.getShopIcon(civilian.getLocale());
            shopIcon.getLore().clear();
            shopIcon.getLore().addAll(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getDesc())));
            ItemStack itemStack = shopIcon.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("rebuild-required-multiple".equals(menuIcon.getKey()) ||
                "rebuild-optional-multiple".equals(menuIcon.getKey())) {
            if (regionType.getRebuild().size() < 2) {
                return new ItemStack(Material.AIR);
            }
            return super.createItemStack(civilian, menuIcon, count);
        } else if ("evolve".equals(menuIcon.getKey())) {
            if (!regionType.getEffects().containsKey(EvolveEffect.KEY)) {
                return new ItemStack(Material.AIR);
            }
            String evolveName = regionType.getEffects().get(EvolveEffect.KEY);
            if (evolveName == null) {
                return new ItemStack(Material.AIR);
            }
            CVItem shopItem = ItemManager.getInstance().getItemType(evolveName.split("\\.")[0]).getShopIcon(civilian.getLocale());
            ItemStack itemStack = shopItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("biome".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            for (Biome biome : regionType.getBiomes()) {
                cvItem.getLore().add(biome.name());
            }
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("towns".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            for (String townTypeName : regionType.getTowns()) {
                String localizedTownTypeName = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        townTypeName + "-name");
                cvItem.getLore().add(localizedTownTypeName);
            }
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("effects".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.getLore().addAll(regionType.getEffects().keySet());
            ItemStack itemStack = cvItem.createItemStack();
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
                return replaceItemStackWithRegionTypeName(civilian, menuIcon, localizedRegionTypeName, count);

                case "output":
                if (regionType.getUpkeeps().get(count).getOutputs().isEmpty()) {
                    return new ItemStack(Material.AIR);
                }
                return replaceItemStackWithRegionTypeName(civilian, menuIcon, localizedRegionTypeName, count);

                case "payout":
                if (regionType.getUpkeeps().get(count).getPayout() <= 0) {
                    return new ItemStack(Material.AIR);
                }
                {
                    CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
                    String payout = Util.getNumberFormat(regionType.getUpkeeps().get(count).getPayout(), civilian.getLocale());
                    cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            menuIcon.getDesc()).replace("$1", payout)));
                    ItemStack itemStack = cvItem.createItemStack();
                    putActions(civilian, menuIcon, itemStack, count);
                    return itemStack;
                }

                case "input":
                if (regionType.getUpkeeps().get(count).getInputs().isEmpty()) {
                    return new ItemStack(Material.AIR);
                }
                return replaceItemStackWithRegionTypeName(civilian, menuIcon, localizedRegionTypeName, count);

                case "power-input":
                if (regionType.getUpkeeps().get(count).getPowerInput() <= 0) {
                    return new ItemStack(Material.AIR);
                }
                {
                    CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
                    String powerInput = "" + regionType.getUpkeeps().get(count).getPowerInput();
                    cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            menuIcon.getDesc()).replace("$1", powerInput)));
                    ItemStack itemStack = cvItem.createItemStack();
                    putActions(civilian, menuIcon, itemStack, count);
                    return itemStack;
                }

                case "power-output":
                if (regionType.getUpkeeps().get(count).getPowerOutput() <= 0) {
                    return new ItemStack(Material.AIR);
                }
                {
                    CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
                    String powerOutput = "" + regionType.getUpkeeps().get(count).getPowerOutput();
                    cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            menuIcon.getDesc()).replace("$1", powerOutput)));
                    ItemStack itemStack = cvItem.createItemStack();
                    putActions(civilian, menuIcon, itemStack, count);
                    return itemStack;
                }
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    private ItemStack replaceItemStackWithRegionTypeName(Civilian civilian, MenuIcon menuIcon, String localizedRegionTypeName, int count) {
        CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
        cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                menuIcon.getDesc()).replace("$1", localizedRegionTypeName)));
        ItemStack itemStack = cvItem.createItemStack();
        putActions(civilian, menuIcon, itemStack, count);
        return itemStack;
    }

    @Override
    public String getFileName() {
        return "region-type";
    }
}
