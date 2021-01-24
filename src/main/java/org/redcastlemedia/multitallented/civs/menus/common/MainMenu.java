package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.commands.PortCommand;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.regions.StructureUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "main") @SuppressWarnings("unused")
public class MainMenu extends CustomMenu {
    @Override
    public String beforeOpenMenu(Civilian civilian) {
        MenuManager.clearHistory(civilian.getUuid());
        MenuManager.clearData(civilian.getUuid());
        StructureUtil.removeBoundingBox(civilian.getUuid());
        if (!TutorialManager.getInstance().getPathIcons(civilian).isEmpty()) {
            return "tutorial-choose-path";
        }
        return null;
    }

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
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
        data.put("class", civilian.getCurrentClass());
        data.put("uuid", civilian.getUuid().toString());
        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if (menuIcon.getKey().equals(Constants.REGION)) {
            Region region = (Region) MenuManager.getData(civilian.getUuid(), Constants.REGION);
            if (region == null) {
                return new ItemStack(Material.AIR);
            } else {
                RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                CVItem regionIcon = regionType.getShopIcon(civilian.getLocale());
                ItemStack itemStack = regionIcon.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            }
        } else if (menuIcon.getKey().equals(Constants.TOWN)) {
            Town town = (Town) MenuManager.getData(civilian.getUuid(), Constants.TOWN);
            if (town == null) {
                return new ItemStack(Material.AIR);
            } else {
                TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
                CVItem townIcon = townType.getShopIcon(civilian.getLocale());
                townIcon.setDisplayName(town.getName());
                ItemStack itemStack = townIcon.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            }
        } else if (menuIcon.getKey().equals(Constants.REGIONS)) {
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
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setLore(TutorialManager.getInstance().getNextTutorialStepMessage(civilian, false));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if (menuIcon.getKey().equals("shop")) {
            if (!player.isOp() &&
                    (Civs.perm == null || !Civs.perm.has(player, "civs.shop"))) {
                return new ItemStack(Material.AIR);
            }
        } else if ("chat".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc()).replace("$1", civilian.getChatChannel().getName(player))));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("player".equals(menuIcon.getKey())) {
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            if (ConfigManager.getInstance().isSkinsInMenu()) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(civilian.getUuid()));
            }
            skullMeta.setDisplayName(player.getDisplayName());
            if (ConfigManager.getInstance().getUseClassesAndSpells()) {
                CivItem civItem = ItemManager.getInstance().getItemType(civilian.getCurrentClass().getType());
                skullMeta.setLore(Util.textWrap(civilian, civItem.getDisplayName(player)));
            }
            itemStack.setItemMeta(skullMeta);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("class".equals(menuIcon.getKey())) {
            if (ConfigManager.getInstance().getUseClassesAndSpells()) {
                CivClass civClass = civilian.getCurrentClass();
                ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civClass.getType());
                CVItem cvItem = classType.getShopIcon(civilian.getLocale());
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if (menuIcon.getKey().equals("your-towns")) {
            boolean isInATown = false;
            for (Town town : TownManager.getInstance().getTowns()) {
                if (town.getRawPeople().containsKey(civilian.getUuid())) {
                    isInATown = true;
                    break;
                }
            }
            if (!isInATown) {
                return new ItemStack(Material.AIR);
            }
        } else if (menuIcon.getKey().equals("alliances")) {
            if (AllianceManager.getInstance().getAllAlliances().isEmpty()) {
                return new ItemStack(Material.AIR);
            }
        } else if (menuIcon.getKey().equals("regions-for-sale")) {
            boolean hasRegionsForSale = false;
            for (Region r : RegionManager.getInstance().getAllRegions()) {
                if (r.getForSale() != -1 && (!r.getRawPeople().containsKey(civilian.getUuid()) ||
                        r.getRawPeople().get(civilian.getUuid()).contains("ally"))) {
                    hasRegionsForSale = true;
                    break;
                }
            }
            if (!hasRegionsForSale) {
                return new ItemStack(Material.AIR);
            }
        } else if (menuIcon.getKey().equals("ports")) {
            boolean hasPort = false;
            for (Region region : RegionManager.getInstance().getAllRegions()) {
                if (PortCommand.canPort(region, player.getUniqueId(), null)) {
                    hasPort = true;
                    break;
                }
            }
            if (!hasPort) {
                return new ItemStack(Material.AIR);
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}
