package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

public class AllianceMenu extends Menu {
    public static final String MENU_NAME = "CivsAlliance";

    public AllianceMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());


        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                LocaleManager.getInstance().getTranslation(civilian.getLocale(), "leave-alliance"))) {
            Alliance alliance = (Alliance) getData(civilian.getUuid(), "alliance");
//            alliance.getMembers().remove()
            // TODO finish this
            return;
        }

        String townName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (townName.isEmpty()) {
            return;
        }
        Town town = TownManager.getInstance().getTown(townName);
        if (town == null) {
            return;
        }
        event.getWhoClicked().closeInventory();
        event.getWhoClicked().openInventory(TownActionMenu.createMenu(civilian, town));
    }

    public static Inventory createMenu(Civilian civilian, Alliance alliance) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(alliance.getMembers().size()) + 9, MENU_NAME);

        Map<String, Object> data = new HashMap<>();
        data.put("alliance", alliance);
        setNewData(civilian.getUuid(), data);

        //0 Icon
        {
            CVItem cvItem = CVItem.createCVItemFromString("GOLDEN_SWORD");
            cvItem.setDisplayName(alliance.getName());
            inventory.setItem(0, cvItem.createItemStack());
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

        //2 Rename
        if (isOwnerOfTown) {
            CVItem cvItem = CVItem.createCVItemFromString("PAPER");
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "rename-alliance"));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "rename-alliance-desc")
                    .replace("$1", alliance.getName()));
            cvItem.setLore(lore);
            inventory.setItem(2, cvItem.createItemStack());
        }
        //3 Last Rename
        if (alliance.getLastRenamedBy() != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(alliance.getLastRenamedBy());
            if (offlinePlayer.getName() != null) {
                ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta isMeta = (SkullMeta) is.getItemMeta();
                isMeta.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "last-renamed-by").replace("$1", offlinePlayer.getName()));
                isMeta.setOwningPlayer(offlinePlayer);
                is.setItemMeta(isMeta);
                inventory.setItem(3, is);
            }
        }
        //4 Claims
        {
            CVItem cvItem = CVItem.createCVItemFromString("GRASS_BLOCK");
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "claims"));
            ArrayList<String> lore = new ArrayList<>();
            int currentClaims = AllianceManager.getInstance().getNumberOfClaims(alliance);
            int maxClaims = AllianceManager.getInstance().getMaxAllianceClaims(alliance);
            lore.add(currentClaims + " / " + maxClaims);
            cvItem.setLore(lore);
            inventory.setItem(4, cvItem.createItemStack());
        }

        //6 Leave Alliance
        if (isOwnerOfTown) {
            CVItem cvItem = CVItem.createCVItemFromString("BARRIER");
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "leave-alliance"));
            inventory.setItem(6, cvItem.createItemStack());
        }

        //8 Back button
        inventory.setItem(8, getBackButton(civilian));

        int i=9;
        for (String townName : alliance.getMembers()) {
            Town town = TownManager.getInstance().getTown(townName);
            CVItem cvItem = ItemManager.getInstance().getItemType(town.getType()).clone();
            cvItem.setDisplayName(town.getName());
            cvItem.getLore().clear();
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }

        return inventory;
    }
}
