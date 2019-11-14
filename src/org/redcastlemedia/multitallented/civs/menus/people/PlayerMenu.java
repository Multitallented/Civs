package org.redcastlemedia.multitallented.civs.menus.people;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "player")
public class PlayerMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("uuid")) {
            data.put("uuid", UUID.fromString(params.get("uuid")));
        }
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        UUID uuid = UUID.fromString((String) MenuManager.getData(civilian.getUuid(), "uuid"));
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = new CVItem(Material.PLAYER_HEAD, 1);
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()));
            ItemStack itemStack = cvItem.createItemStack();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            ((SkullMeta) itemStack.getItemMeta()).setOwningPlayer(offlinePlayer);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("money".equals(menuIcon.getKey())) {
            if (Civs.econ == null) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            String money = Util.getNumberFormat(Civs.econ.getBalance(Bukkit.getOfflinePlayer(civilian.getUuid())),
                    civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()).replace("$1", money));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("towns".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            int i = 0;
            for (Town town : TownManager.getInstance().getTowns()) {
                if (!town.getRawPeople().containsKey(civilian.getUuid())) {
                    continue;
                }
                cvItem.getLore().add(town.getName());
                i++;
                if (i > 5) {
                    break;
                }
            }
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("bounty".equals(menuIcon.getKey())) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()).replace("$1", player.getName()));
            ArrayList<String> lore = new ArrayList<>();
            int i=0;
            for (Bounty bounty : civilian.getBounties()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(bounty.getIssuer());
                lore.add(op.getName() + ": $" + Util.getNumberFormat(bounty.getAmount(), civilian.getLocale()));
                if (i>5) {
                    break;
                }
                i++;
            }
            cvItem.setLore(lore);

            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("points".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()).replace("$1", "" + civilian.getPoints()));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("karma".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()).replace("$1", "" + civilian.getKarma()));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("deaths".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()).replace("$1", "" + civilian.getDeaths()));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("killstreak".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()).replace("$1", "" + civilian.getKillStreak()));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("highest-killstreak".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()).replace("$1", "" + civilian.getHighestKillStreak()));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("add-friend".equals(menuIcon.getKey())) {

        } else if ("remove-friend".equals(menuIcon.getKey())) {

        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        UUID uuid = ((UUID) MenuManager.getData(civilian.getUuid(), "uuid"));
        Player player = Bukkit.getPlayer(civilian.getUuid());
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String name = offlinePlayer.getName();
        if (name == null) {
            name = "Unknown";
        }
        if ("add-friend".equals(actionString)) {
            civilian.getFriends().add(uuid);
            CivilianManager.getInstance().saveCivilian(civilian);
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "friend-added").replace("$1", name));
            return true;
        } else if ("remove-friend".equals(actionString)) {
            civilian.getFriends().remove(uuid);
            CivilianManager.getInstance().saveCivilian(civilian);
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "friend-removed").replace("$1", name));
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }
}
