package org.redcastlemedia.multitallented.civs.menus.nations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.Civs;
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
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = Constants.NATION) @SuppressWarnings("unused")
public class NationMenu extends CustomMenu {

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        if (!params.containsKey(Constants.NATION)) {
            return data;
        }
        Nation nation = NationManager.getInstance().getNation(params.get(Constants.NATION));
        data.put(Constants.NATION, nation);
        data.put("lastRenamed", nation.getLastRenamedBy().toString());
        List<Town> townList = new ArrayList<>();
        for (String memberName : nation.getMembers()) {
            Town town = TownManager.getInstance().getTown(memberName);
            if (town == null) {
                continue;
            }
            townList.add(town);
        }
        data.put("townList", townList);
        data.put("power", nation.getPower());
        data.put("maxPower", nation.getMaxPower());

        return data;
    }


    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Nation nation = (Nation) MenuManager.getData(civilian.getUuid(), Constants.NATION);
        if (player == null || nation == null) {
            return new ItemStack(Material.AIR);
        }
        boolean isAuthorized = OwnershipUtil.isAuthorized(player, nation);
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = nation.getIconAsCVItem(civilian);
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("last-rename".equals(menuIcon.getKey())) {
            if (!isAuthorized || nation.getLastRenamedBy() == null) {
                return new ItemStack(Material.AIR);
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nation.getLastRenamedBy());
            if (offlinePlayer.getName() != null) {
                CVItem lastRenameCVItem = menuIcon.createCVItem(player, count);
                lastRenameCVItem.setMat(Material.PLAYER_HEAD);
                ItemStack is = lastRenameCVItem.createItemStack();
                SkullMeta isMeta = (SkullMeta) is.getItemMeta();
                isMeta.setDisplayName(offlinePlayer.getName());
                isMeta.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "last-renamed-by").replace("$1", offlinePlayer.getName())));
                isMeta.setOwningPlayer(offlinePlayer);
                is.setItemMeta(isMeta);
                putActions(civilian, menuIcon, is, count);
                return is;
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("rename".equals(menuIcon.getKey())) {
            if (!isAuthorized) {
                return new ItemStack(Material.AIR);
            }
        } else if ("capitol".equals(menuIcon.getKey())) {
            Town capitol = TownManager.getInstance().getTown(nation.getCapitol());
            if (capitol == null) {
                return new ItemStack(Material.AIR);
            }
            TownType townType = (TownType) ItemManager.getInstance().getItemType(capitol.getType());
            CVItem cvItem = townType.clone();
            cvItem.setDisplayName(capitol.getName());
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("members".equals(menuIcon.getKey())) {
            List<Town> townList = (List<Town>) MenuManager.getData(civilian.getUuid(), "townList");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (townList.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Town town = townList.get(startIndex + count);
            CVItem cvItem = ItemManager.getInstance().getItemType(town.getType()).clone();
            cvItem.setDisplayName(town.getName());
            cvItem.setLore(Util.textWrap(civilian, town.getSummary(player)));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }


    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Nation nation = (Nation) MenuManager.getData(civilian.getUuid(), Constants.NATION);
        if (player == null || nation == null) {
            return true;
        }
        if ("get-lore".equals(actionString)) {
            ItemStack itemStack = nation.getLore();
            if (!player.getInventory().contains(itemStack)) {
                player.getInventory().addItem(itemStack);
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "item-received"));
            }
            return true;
        } else if ("set-capitol".equals(actionString)) {
            // TODO open select town menu
            return true;
        } else if ("leave-nation".equals(actionString)) {
            // TODO open select town menu
            return true;
        } else if ("join-nation".equals(actionString)) {
            // TODO open select town menu
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }
}
