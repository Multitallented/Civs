package org.redcastlemedia.multitallented.civs.menus.regions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleUtil;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.EvolveEffect;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.regions.StructureUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CivsMenu(name = "region-type") @SuppressWarnings("unused")
public class RegionTypeMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey(Constants.REGION_TYPE)) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(params.get(Constants.REGION_TYPE));
            data.put(Constants.REGION_TYPE, regionType);
            if (regionType.getRebuild() != null && !regionType.getRebuild().isEmpty()) {
                data.put("rebuildRegion", regionType.getRebuild().get(0));
                StringBuilder regionList = new StringBuilder();
                for (String rebuildRegionString : regionType.getRebuild()) {
                    regionList.append(rebuildRegionString);
                    regionList.append(",");
                }
                data.put("rebuildRegions", regionList.substring(0, regionList.length() - 1));
            }
            if (regionType.getEffects().containsKey(EvolveEffect.KEY)) {
                data.put("evolveRegion", regionType.getEffects().get(EvolveEffect.KEY).split("\\.")[0]);
            }

            Player player = Bukkit.getPlayer(civilian.getUuid());
            if (player != null && !StructureUtil.hasBoundingBoxShown(civilian.getUuid())) {
                Region region = RegionManager.getInstance().getRegionAt(player.getLocation());
                boolean infiniteBoundingBox = params.containsKey(Constants.INFINITE_BOUNDING_BOX);
                if (region == null) {
                    StructureUtil.showGuideBoundingBox(player, player.getLocation(), regionType, infiniteBoundingBox);
                } else {
                    StructureUtil.showGuideBoundingBox(player, region.getLocation(), regionType, infiniteBoundingBox);
                }
            }
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
        LocaleManager localeManager = LocaleManager.getInstance();
        RegionType regionType = (RegionType) MenuManager.getData(civilian.getUuid(), Constants.REGION_TYPE);
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null || regionType == null) {
            return new ItemStack(Material.AIR);
        }
        if ("icon".equals(menuIcon.getKey())) {
            CVItem shopIcon = regionType.getShopIcon(civilian.getLocale());
            List<String> lore = new ArrayList<>();
            shopIcon.setDisplayName(regionType.getDisplayName(player));
            lore.add(localeManager.getTranslation(player, "size") +
                    ": " + (regionType.getBuildRadiusX() * 2 + 1) + "x" + (regionType.getBuildRadiusZ() * 2 + 1) + "x" + (regionType.getBuildRadiusY() * 2 + 1));
            if (regionType.getEffectRadius() != regionType.getBuildRadius()) {
                lore.add(localeManager.getTranslation(player, "range") +
                        ": " + regionType.getEffectRadius());
            }

            lore.addAll(Util.textWrap(civilian, regionType.getDescription(civilian.getLocale())));
            shopIcon.setLore(lore);
            ItemStack itemStack = shopIcon.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("build-reqs".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc()).replace("$1", regionType.getDisplayName(player))));
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
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getName()));
            if (regionType.getProcessedName().equals(limitType)) {
                limitType = regionType.getDisplayName(player);
            } else {
                limitType = LocaleManager.getInstance().getTranslation(player,
                        limitType + LocaleConstants.GROUP_SUFFIX);
            }
            cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc()).replace("$1", "" + amount)
                    .replace("$2", "" + limit).replace("$3", limitType)));

            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("price".equals(menuIcon.getKey())) {
            boolean showPrice = (boolean) MenuManager.getData(civilian.getUuid(), Constants.SHOW_PRICE);
            boolean isCivsAdmin = Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_PERMISSION);
            boolean hasShopPerms = Civs.perm != null && Civs.perm.has(player, "civs.shop");
            String maxLimit = civilian.isAtMax(regionType, true);
            boolean isInShop = regionType.getInShop();
            ArrayList<String> lore = new ArrayList<>();
            boolean hasItemUnlocked = ItemManager.getInstance().hasItemUnlocked(civilian, regionType);
            if (showPrice && (isCivsAdmin || (hasShopPerms && maxLimit == null && isInShop))) {
                CVItem priceItem;
                if (hasItemUnlocked || isCivsAdmin) {
                    priceItem = CVItem.createCVItemFromString(menuIcon.getIcon());
                } else {
                    priceItem = CVItem.createCVItemFromString("IRON_BARS");
                    lore.add(LocaleManager.getInstance().getTranslation(player, "item-locked"));
                }
                priceItem.setDisplayName(localeManager.getTranslation(player, menuIcon.getName()));
                lore.add(localeManager.getTranslation(player, "price")
                        .replace("$1", Util.getNumberFormat(regionType.getPrice(civilian), civilian.getLocale())));
                priceItem.setLore(lore);
                ItemStack itemStack = priceItem.createItemStack();
                if (hasItemUnlocked || isCivsAdmin) {
                    putActions(civilian, menuIcon, itemStack, count);
                }
                return itemStack;
            } else if (showPrice && hasShopPerms && isInShop) {
                CVItem priceItem = CVItem.createCVItemFromString("BARRIER");
                priceItem.setDisplayName(localeManager.getTranslation(player, "buy-item"));
                LocaleUtil.getTranslationMaxItem(maxLimit, regionType, player, priceItem.getLore());
                return priceItem.createItemStack();
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("rebuild-required-single".equals(menuIcon.getKey()) ||
                "rebuild-optional-single".equals(menuIcon.getKey())) {
            if (regionType.getRebuild().size() != 1) {
                return new ItemStack(Material.AIR);
            }
            RegionType rebuildType = (RegionType) ItemManager.getInstance().getItemType(regionType.getRebuild().get(0));
            CVItem shopIcon;
            if (rebuildType != null) {
                shopIcon = rebuildType.getShopIcon(civilian.getLocale());
                shopIcon.getLore().clear();
                shopIcon.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                        menuIcon.getDesc())));
            } else {
                String groupName = regionType.getRebuild().get(0).toLowerCase();
                shopIcon = new CVItem(Material.CHEST, 1);
                shopIcon.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                        groupName + LocaleConstants.GROUP_SUFFIX));
                int i = 0;
                for (CivItem groupItem : ItemManager.getInstance().getItemGroup(groupName)) {
                    shopIcon.getLore().add(groupItem.getDisplayName(player));
                    i++;
                    if (i > 5) {
                        shopIcon.getLore().add(LocaleManager.getInstance().getTranslation(player, "and-more"));
                        break;
                    }
                }
            }
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
            if (regionType.getBiomes().isEmpty()) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale(), count);
            for (Biome biome : regionType.getBiomes()) {
                cvItem.getLore().add(biome.name());
            }
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("towns".equals(menuIcon.getKey())) {
            if (regionType.getTowns().isEmpty()) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale(), count);
            for (String townTypeName : regionType.getTowns()) {
                String localizedTownTypeName = LocaleManager.getInstance().getTranslation(player,
                        townTypeName + LocaleConstants.NAME_SUFFIX);
                cvItem.getLore().add(localizedTownTypeName);
            }
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("effects".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale(), count);
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
                    return replaceItemStackWithRegionTypeName(civilian, menuIcon, regionType.getDisplayName(player), count, player);

                case "output":
                    if (regionType.getUpkeeps().get(count).getOutputs().isEmpty()) {
                        return new ItemStack(Material.AIR);
                    }
                    return replaceItemStackWithRegionTypeName(civilian, menuIcon, regionType.getDisplayName(player), count, player);

                case "payout":
                    if (regionType.getUpkeeps().get(count).getPayout() <= 0 &&
                            regionType.getUpkeeps().get(count).getPowerInput() <= 0 &&
                            regionType.getUpkeeps().get(count).getPowerOutput() <= 0) {
                        return new ItemStack(Material.AIR);
                    }
                    return getPayoutItemStack(civilian, menuIcon, count, regionType, player);

                case "input":
                    if (regionType.getUpkeeps().get(count).getInputs().isEmpty()) {
                        return new ItemStack(Material.AIR);
                    }
                    return replaceItemStackWithRegionTypeName(civilian, menuIcon, regionType.getDisplayName(player), count, player);
                default:
                    return new ItemStack(Material.AIR);
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    private ItemStack getPayoutItemStack(Civilian civilian, MenuIcon menuIcon, int count, RegionType regionType, Player player) {
        CVItem cvItem = menuIcon.createCVItem(player, count);
        List<String> lore = new ArrayList<>();
        if (regionType.getUpkeeps().get(count).getPayout() <= 0) {
            String payout = Util.getNumberFormat(regionType.getUpkeeps().get(count).getPayout(), civilian.getLocale());
            lore.addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc()).replace("$1", payout)));
        }
        if (regionType.getUpkeeps().get(count).getPowerInput() <= 0) {
            String powerInput = "" + regionType.getUpkeeps().get(count).getPowerInput();
            lore.addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc()).replace("$1", powerInput)));
        }
        if (regionType.getUpkeeps().get(count).getPowerOutput() <= 0) {
            String powerInput = "" + regionType.getUpkeeps().get(count).getPowerInput();
            lore.addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc()).replace("$1", powerInput)));
        }
        cvItem.setLore(lore);
        ItemStack itemStack = cvItem.createItemStack();
        putActions(civilian, menuIcon, itemStack, count);
        return itemStack;
    }

    private ItemStack replaceItemStackWithRegionTypeName(Civilian civilian, MenuIcon menuIcon,
                                                         String localizedRegionTypeName, int count, Player player) {
        CVItem cvItem = menuIcon.createCVItem(player, count);
        cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                menuIcon.getDesc()).replace("$1", localizedRegionTypeName)));
        ItemStack itemStack = cvItem.createItemStack();
        putActions(civilian, menuIcon, itemStack, count);
        return itemStack;
    }
}
