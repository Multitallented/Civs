package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.StructureUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "main") @SuppressWarnings("unused")
public class MainMenu extends CustomMenu {
    @Override
    public String beforeOpenMenu(Civilian civilian) {
        MenuManager.clearHistory(civilian.getUuid());
        StructureUtil.removeBoundingBox(civilian.getUuid());
        if (civilian.isAskForTutorial() && ConfigManager.getInstance().isUseTutorial()) {
            return "confirmation?type=tutorial";
        }
        if (!TutorialManager.getInstance().getPaths(civilian).isEmpty()) {
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
        ChunkClaim chunkClaim = ChunkClaim.fromLocation(player.getLocation());
        Nation nation = chunkClaim.getNation();
        if (nation != null) {
            data.put("nation", nation);
        }
        data.put("claim", chunkClaim);
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
        } else if (menuIcon.getKey().equals("nation")) {
            Nation nation = (Nation) MenuManager.getData(civilian.getUuid(), "nation");
            if (nation == null) {
                return new ItemStack(Material.AIR);
            }
            return nation.getIcon();
        } else if (menuIcon.getKey().equals("claim")) {
            ChunkClaim chunkClaim = (ChunkClaim) MenuManager.getData(civilian.getUuid(), "claim");
            ItemStack itemStack;
            if (chunkClaim.getNation() != null) {
                itemStack = chunkClaim.getNation().getIcon();
                itemStack.getItemMeta().setLore(Util.textWrap(civilian,
                        LocaleManager.getInstance().getTranslationWithPlaceholders(player, menuIcon.getDesc())));
            } else {
                CVItem cvItem = CVItem.createCVItemFromString(Material.GLASS.name());
                cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        menuIcon.getDesc())));
                itemStack = cvItem.createItemStack();
            }
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
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
        } else if (menuIcon.getKey().equals("shop")) {
            if (!player.isOp() &&
                    (Civs.perm == null || !Civs.perm.has(player, "civs.shop"))) {
                return new ItemStack(Material.AIR);
            }
        } else if ("chat".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    menuIcon.getDesc()).replace("$1", civilian.getChatChannel().getName(player))));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}
