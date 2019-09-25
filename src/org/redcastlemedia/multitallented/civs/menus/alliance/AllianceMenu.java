package org.redcastlemedia.multitallented.civs.menus.alliance;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public class AllianceMenu extends CustomMenu {

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Alliance alliance = (Alliance) MenuManager.getData(civilian.getUuid(), "alliance");
        if (menuIcon.getKey().equals("members")) {
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            String[] memberNames = new String[alliance.getMembers().size()];
            memberNames = alliance.getMembers().toArray(memberNames);
            String townName = memberNames[startIndex + count];
            Town town = TownManager.getInstance().getTown(townName);
            CVItem cvItem = ItemManager.getInstance().getItemType(town.getType()).clone();
            cvItem.setDisplayName(town.getName());
            cvItem.getLore().clear();
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack);
            return itemStack;
        }
        if (menuIcon.getKey().equals("last-rename")) {
            if (alliance == null || alliance.getLastRenamedBy() == null) {
                return new ItemStack(Material.AIR);
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(alliance.getLastRenamedBy());
            if (offlinePlayer.getName() != null) {
                CVItem lastRenameCVItem = menuIcon.createCVItem(civilian.getLocale());
                lastRenameCVItem.setMat(Material.PLAYER_HEAD);
                ItemStack is = lastRenameCVItem.createItemStack();
                SkullMeta isMeta = (SkullMeta) is.getItemMeta();
                isMeta.setDisplayName(offlinePlayer.getName());
                isMeta.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "last-renamed-by").replace("$1", offlinePlayer.getName())));
                isMeta.setOwningPlayer(offlinePlayer);
                is.setItemMeta(isMeta);
                putActions(civilian, menuIcon, is);
                return is;
            }
        }
        if (menuIcon.getKey().equals("icon")) {
            CVItem icon = menuIcon.createCVItem(civilian.getLocale());
            icon.setDisplayName(alliance.getName());
            ItemStack itemStack = icon.createItemStack();
            putActions(civilian, menuIcon, itemStack);
            return itemStack;
        }

        if (menuIcon.getKey().equals("rename") ||
                menuIcon.getKey().equals("leave-alliance")) {
            Town selectedTown = (Town) MenuManager.getData(civilian.getUuid(), "selectedTown");
            if (selectedTown == null) {
                return new ItemStack(Material.AIR);
            }
            return super.createItemStack(civilian, menuIcon, count);
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public String getFileName() {
        return "alliance";
    }

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        String allianceName = params.get("alliance");
        Alliance alliance = AllianceManager.getInstance().getAlliance(allianceName);
        data.put("alliance", alliance);

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        int maxPage = (int) Math.ceil((double) alliance.getMembers().size() / (double) itemsPerPage.get("members"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);

        data.put("lastRename", alliance.getLastRenamedBy().toString());
        if (!params.containsKey("selectedTown")) {
            for (String townName : alliance.getMembers()) {
                Town town = TownManager.getInstance().getTown(townName);
                if (town.getPeople().containsKey(civilian.getUuid()) &&
                        town.getPeople().get(civilian.getUuid()).contains("owner")) {
                    data.put("selectedTown", town);
                    break;
                }
            }
        } else {
            data.put("selectedTown",
                    TownManager.getInstance().getTown(params.get("selectedTown")));
        }
        return data;
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, ItemStack cursorItem, ItemStack clickedItem) {
        if (!actions.containsKey(civilian.getUuid())) {
            return false;
        }
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return true;
        }
        Town town = (Town) MenuManager.getData(civilian.getUuid(), "selectedTown");
        if (town == null) {
            return true;
        }
        List<String> actionStrings = actions.get(civilian.getUuid()).get(clickedItem);
        for (String actionString : actionStrings) {
            if (actionString.equals("leave-alliance")) {
                Alliance alliance = (Alliance) MenuManager.getData(civilian.getUuid(), "alliance");
                for (String townName : new HashSet<>(alliance.getMembers())) {
                    if (townName.equals(town.getName())) {
                        continue;
                    }
                    Town currentTown = TownManager.getInstance().getTown(townName);
                    AllianceManager.getInstance().unAlly(town, currentTown);
                }
            }
        }
        return super.doActionAndCancel(civilian, cursorItem, clickedItem);
    }
}
