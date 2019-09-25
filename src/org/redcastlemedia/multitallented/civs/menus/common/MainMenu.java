package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.menus.StartTutorialMenu;
import org.redcastlemedia.multitallented.civs.menus.TutorialChoosePathMenu;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.StructureUtil;

public class MainMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
//        clearHistory(civilian.getUuid());
        StructureUtil.removeBoundingBox(civilian.getUuid());
        if (civilian.isAskForTutorial() && ConfigManager.getInstance().isUseTutorial()) {
//            return StartTutorialMenu.createMenu(civilian);
        }
        if (!TutorialManager.getInstance().getPaths(civilian).isEmpty()) {
//            return TutorialChoosePathMenu.createMenu(civilian);
        }
        Map<String, Object> data = new HashMap<>();
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Region region = RegionManager.getInstance().getRegionAt(player.getLocation());
        if (region != null) {
            data.put("region", region);
        }
        Town town = TownManager.getInstance().getTownAt(player.getLocation());
        if (town != null) {
            data.put("town", town);
        }
        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("region")) {
            Region region = (Region) MenuManager.getData(civilian.getUuid(), "region");
            if (region == null) {
                return new ItemStack(Material.AIR);
            } else {
                CVItem regionIcon = menuIcon.createCVItem(civilian.getLocale());
                regionIcon.setDisplayName(region.getType());
                ItemStack itemStack = regionIcon.createItemStack();
                putActions(civilian, menuIcon, itemStack);
                return itemStack;
            }
        } else if (menuIcon.getKey().equals("town")) {
            Town town = (Town) MenuManager.getData(civilian.getUuid(), "town");
            if (town == null) {
                return new ItemStack(Material.AIR);
            } else {
                CVItem townIcon = menuIcon.createCVItem(civilian.getLocale());
                townIcon.setDisplayName(town.getName());
                ItemStack itemStack = townIcon.createItemStack();
                putActions(civilian, menuIcon, itemStack);
                return itemStack;
            }
        } else if (menuIcon.getKey().equals("blueprints")) {
            if (civilian.getStashItems().isEmpty()) {
                return new ItemStack(Material.AIR);
            }
        } else if (menuIcon.getKey().equals("regions")) {
            boolean showBuiltRegions = false;
            for (Region region : RegionManager.getInstance().getAllRegions()) {
                if (region.getRawPeople().containsKey(civilian.getUuid())) {
                    showBuiltRegions = true;
                    break;
                }
            }
            if (!showBuiltRegions) {
                return new ItemStack(Material.AIR);
            }
        } else if (menuIcon.getKey().equals("guide")) {
            if (!ConfigManager.getInstance().isUseGuide()) {
                return new ItemStack(Material.AIR);
            }
        } else if (menuIcon.getKey().equals("shop")) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            if (!player.isOp() &&
                    (Civs.perm == null || !Civs.perm.has(player, "civs.shop"))) {
                return new ItemStack(Material.AIR);
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public String getFileName() {
        return "main";
    }
}
