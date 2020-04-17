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
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
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
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "player") @SuppressWarnings("unused")
public class PlayerMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        UUID uuid = UUID.fromString(params.get(Constants.UUID));
        Civilian civilian1 = CivilianManager.getInstance().getCivilian(uuid);
        if (params.containsKey(Constants.UUID)) {
            data.put(Constants.UUID, uuid);
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.getName() != null) {
            data.put("name", offlinePlayer.getName());
        }
        if (Civs.econ != null) {
            data.put("money", Util.getNumberFormat(Civs.econ.getBalance(offlinePlayer), civilian.getLocale()));
        }
        data.put("points", "" + civilian1.getPoints());
        data.put("kills", "" + civilian1.getKills());
        data.put("deaths", "" + civilian1.getDeaths());
        data.put("karma", "" + civilian1.getKarma());
        data.put("killstreak", "" + civilian1.getKillStreak());
        data.put("highest-killstreak", "" + civilian1.getHighestKillStreak());
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        UUID uuid = (UUID) MenuManager.getData(civilian.getUuid(), Constants.UUID);
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = new CVItem(Material.PLAYER_HEAD, 1);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            ItemStack itemStack = cvItem.createItemStack();
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(offlinePlayer);
                itemStack.setItemMeta(skullMeta);
            }
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("friends".equals(menuIcon.getKey())) {
            Civilian civilian1 = CivilianManager.getInstance().getCivilian(uuid);
            if (civilian1.getFriends().isEmpty()) {
                return new ItemStack(Material.AIR);
            }
        } else if ("towns".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            int i = 0;
            for (Town town : TownManager.getInstance().getTowns()) {
                if (!town.getRawPeople().containsKey(uuid)) {
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
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
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
        } else if ("add-friend".equals(menuIcon.getKey())) {
            if (civilian.getUuid().equals(uuid) || civilian.getFriends().contains(uuid)) {
                return new ItemStack(Material.AIR);
            }
        } else if ("remove-friend".equals(menuIcon.getKey())) {
            if (civilian.getUuid().equals(uuid) || !civilian.getFriends().contains(uuid)) {
                return new ItemStack(Material.AIR);
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        UUID uuid = ((UUID) MenuManager.getData(civilian.getUuid(), Constants.UUID));
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null || uuid == null) {
            return true;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        String name = offlinePlayer.getName();
        if (name == null) {
            name = "Unknown";
        }
        if ("add-friend".equals(actionString)) {
            civilian.getFriends().add(uuid);
            CivilianManager.getInstance().saveCivilian(civilian);
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "friend-added").replace("$1", name));
            return true;
        } else if ("remove-friend".equals(actionString)) {
            civilian.getFriends().remove(uuid);
            CivilianManager.getInstance().saveCivilian(civilian);
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "friend-removed").replace("$1", name));
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }
}
