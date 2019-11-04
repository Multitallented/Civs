package org.redcastlemedia.multitallented.civs.menus.regions;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionUpkeep;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public class RegionMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        if (!params.containsKey("region")) {
            return data;
        }
        Region region = RegionManager.getInstance().getRegionById(params.get("region"));
        data.put("region", region);
        StringBuilder failingUpkeeps = new StringBuilder();
        for (Integer i : region.getFailingUpkeeps()) {
            failingUpkeeps.append(i).append(",");
        }
        failingUpkeeps.substring(0, failingUpkeeps.length() - 1);
        data.put("failingUpkeeps", failingUpkeeps.toString());
        data.put("regionType", region.getType());
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Region region = (Region) MenuManager.getData(civilian.getUuid(), "region");
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(
                (String) MenuManager.getData(civilian.getUuid(), "regionType"));
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = regionType.getShopIcon().clone();
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    regionType.getProcessedName() + "-name"));
            cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    regionType.getProcessedName() + "-desc")));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("location".equals(menuIcon.getKey())) {
            CVItem cvItem = CVItem.createCVItemFromString(menuIcon.getIcon());
            cvItem.setDisplayName(region.getLocation().getWorld().getName() + " " +
                    (int) region.getLocation().getX() + "x, " +
                    (int) region.getLocation().getY() + "y, " +
                    (int) region.getLocation().getZ() + "z");
            Town town = TownManager.getInstance().getTownAt(region.getLocation());
            if (town != null) {
                cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        menuIcon.getDesc()).replace("$1", town.getName())));
            }
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("region-type".equals(menuIcon.getKey())) {
            CVItem cvItem = CVItem.createCVItemFromString(menuIcon.getIcon());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    regionType.getProcessedName() + "-name"));
            cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getDesc())));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("income".equals(menuIcon.getKey())) {
            String localRegionTypeName = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    regionType.getProcessedName() + "-name");
            CVItem cvItem = CVItem.createCVItemFromString(menuIcon.getIcon());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()));
            HashMap<Integer, Integer> upkeepsWithinLastDay = region.getNumberOfUpkeepsWithin24Hours();
            HashMap<Integer, Integer> upkeepsWithinLastWeek = region.getNumberOfUpkeepsWithin1Week();
            double lastDayIncome = 0;
            double lastWeekIncome = 0;
            int i = 0;
            for (RegionUpkeep regionUpkeep : regionType.getUpkeeps()) {
                if (regionUpkeep.getPayout() == 0) {
                    i++;
                    continue;
                }
                if (upkeepsWithinLastDay.containsKey(i)) {
                    lastDayIncome += (double) upkeepsWithinLastDay.get(i) * regionUpkeep.getPayout();
                }
                if (upkeepsWithinLastWeek.containsKey(i)) {
                    lastWeekIncome += (double) upkeepsWithinLastWeek.get(i) * regionUpkeep.getPayout();
                }
                i++;
            }
            cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getDesc()).replace("$1", localRegionTypeName)
                    .replace("$2", NumberFormat.getCurrencyInstance().format(lastDayIncome))
                    .replace("$3", NumberFormat.getCurrencyInstance().format(lastWeekIncome))));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, ItemStack cursorItem, ItemStack clickedItem) {
        if (!actions.containsKey(civilian.getUuid())) {
            return false;
        }
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return true;
        }
        List<String> actionStrings = actions.get(civilian.getUuid()).get(clickedItem);
        if (actionStrings == null) {
            return true;
        }
        for (String actionString : actionStrings) {
            if (actionString.equals("cancel-sale")) {
                Player player = Bukkit.getPlayer(civilian.getUuid());
                player.performCommand("cv sell");
                return true;
            }
        }
        return super.doActionAndCancel(civilian, cursorItem, clickedItem);
    }

    @Override
    public String getFileName() {
        return "region";
    }
}
