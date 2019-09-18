package org.redcastlemedia.multitallented.civs.menus.alliance;


import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class AllianceMenu extends CustomMenu {

    @Override
    public Inventory createMenu(Civilian civilian) {
        Alliance alliance = (Alliance) MenuManager.getData(civilian.getUuid(), "alliance");
        int page = (int) MenuManager.getData(civilian.getUuid(), "page");

        int instanceSize = size;
        if (size == -1) {
            instanceSize = MenuManager.getInventorySize(alliance.getMembers().size()) + 9;
        }
        Inventory inventory = Bukkit.createInventory(null, instanceSize, getKey());

        /*if (menuIcon.getIndex().get(0) != -1) {
            inventory.setItem(menuIcon.getIndex().get(0), menuIcon.createCVItem(civilian.getLocale()).createItemStack());
        }

        if (renameIcon.getIndex().get(0) != -1) {
            inventory.setItem(renameIcon.getIndex().get(0), renameIcon.createCVItem(civilian.getLocale()).createItemStack());
        }

        if (alliance.getLastRenamedBy() != null &&
                lastRenameIcon.getIndex().get(0) != -1) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(alliance.getLastRenamedBy());
            if (offlinePlayer.getName() != null) {
                CVItem lastRenameCVItem = lastRenameIcon.createCVItem(civilian.getLocale());
                lastRenameCVItem.setMat(Material.PLAYER_HEAD);
                ItemStack is = lastRenameCVItem.createItemStack();
                SkullMeta isMeta = (SkullMeta) is.getItemMeta();
                isMeta.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "last-renamed-by").replace("$1", offlinePlayer.getName()));
                isMeta.setOwningPlayer(offlinePlayer);
                is.setItemMeta(isMeta);
                inventory.setItem(lastRenameIcon.getIndex().get(0), is);
            }
        }

        boolean isOwnerOfTown = false;
        for (String townName : alliance.getMembers()) {
            Town town = TownManager.getInstance().getTown(townName);
            if (town.getPeople().containsKey(civilian.getUuid()) &&
                    town.getPeople().get(civilian.getUuid()).contains("owner")) {
                isOwnerOfTown = true;
                break;
            }
        }

        if (isOwnerOfTown && leaveAllianceIcon.getIndex().get(0) != -1) {
            inventory.setItem(leaveAllianceIcon.getIndex().get(0), leaveAllianceIcon.createCVItem(civilian.getLocale()).createItemStack());
        }

        if (backIndex > -1) {
            inventory.setItem(backIndex, MenuManager.getInstance().getBackButton(civilian));
        }

        int startIndex = (page - 1) * memberStartIndex.size();
        int endIndex = startIndex + memberStartIndex.size();
        String[] memberNames = new String[alliance.getMembers().size()];
        memberNames = alliance.getMembers().toArray(memberNames);

        for (int j = startIndex; j < memberNames.length && j <= endIndex; j++) {
            int index = memberStartIndex.get(j);
            String townName = memberNames[j];
            Town town = TownManager.getInstance().getTown(townName);
            CVItem cvItem = ItemManager.getInstance().getItemType(town.getType()).clone();
            cvItem.setDisplayName(town.getName());
            cvItem.getLore().clear();
            inventory.setItem(index, cvItem.createItemStack());
        }*/

        // TODO set actions

        return inventory;
    }

    @Override
    public String getKey() {
        return "Alliance";
    }

    @Override
    public String getFileName() {
        return "alliance";
    }

    @Override
    public void doAction(Civilian civilian, MenuIcon menuIcon) {

    }
}
