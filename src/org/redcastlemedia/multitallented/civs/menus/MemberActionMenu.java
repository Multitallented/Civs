package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemberActionMenu extends Menu {
    public static final String MENU_NAME = "CivsMemAct";
    public MemberActionMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        String locationString = "";
        if (getData(civilian.getUuid(), "region") != null) {
            Region region = (Region) getData(civilian.getUuid(), "region");
            locationString = region.getId();
        } else {
            Town town = (Town) getData(civilian.getUuid(), "town");
            locationString = town.getName();
        }
        UUID uuid = (UUID) getData(civilian.getUuid(), "uuid");

        Player player = Bukkit.getPlayer(event.getInventory().getItem(1).getItemMeta().getDisplayName());
        Player cPlayer = Bukkit.getPlayer(civilian.getUuid());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

//        clearHistory(civilian.getUuid());
//        appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);

        if (event.getCurrentItem().getType().equals(Material.GOLD_BLOCK)) {
            cPlayer.performCommand("cv setowner " + player.getName() + " " + locationString);
            clickBackButton(cPlayer);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.IRON_BLOCK)) {
            cPlayer.performCommand("cv setmember " + player.getName() + " " + locationString);
            clickBackButton(cPlayer);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.DIORITE)) {
            cPlayer.performCommand("cv setguest " + player.getName() + " " + locationString);
            clickBackButton(cPlayer);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.REDSTONE_BLOCK)) {
            cPlayer.performCommand("cv removemember " + player.getName() + " " + locationString + " " + uuid);
            clickBackButton(cPlayer);
            return;
        }
    }

    private static void addItems(Inventory inventory, Civilian civilian, String role, boolean viewingSelf) {
        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));
        LocaleManager localeManager = LocaleManager.getInstance();
        ArrayList<String> lore;

        //9 set owner
        if (!viewingSelf && !role.equals("owner")) {
            CVItem cvItem1 = CVItem.createCVItemFromString("GOLD_BLOCK");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "set-owner"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "owner-description"));
            cvItem1.setLore(lore);
            inventory.setItem(9, cvItem1.createItemStack());
        }

        //10 set member
        if (!viewingSelf && !role.equals("member")) {
            CVItem cvItem1 = CVItem.createCVItemFromString("IRON_BLOCK");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "set-member"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "member-description"));
            cvItem1.setLore(lore);
            inventory.setItem(10, cvItem1.createItemStack());
        }

        //11 set guest
        if (!viewingSelf && !role.equals("guest")) {
            CVItem cvItem1 = CVItem.createCVItemFromString("DIORITE");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "set-guest"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "guest-description"));
            cvItem1.setLore(lore);
            inventory.setItem(11, cvItem1.createItemStack());
        }

        //12 remove member
        CVItem cvItem1 = CVItem.createCVItemFromString("REDSTONE_BLOCK");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "remove-member"));
        inventory.setItem(12, cvItem1.createItemStack());
    }

    public static Inventory createMenu(Civilian civilian, Town town, UUID uuid, boolean viewingSelf) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(town.getPeople().size()) + 9, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("town", town);
        data.put("uuid", uuid);
        setNewData(civilian.getUuid(), data);

        ArrayList<String> lore;

        //1 Player
        Player player = Bukkit.getPlayer(uuid);
        String role = town.getPeople().get(uuid);
        ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta im = (SkullMeta) playerItem.getItemMeta();
        im.setDisplayName(player.getName());
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), town.getPeople().get(uuid)));
        im.setLore(lore);
        im.setOwningPlayer(player);
        playerItem.setItemMeta(im);
        inventory.setItem(1, playerItem);

        addItems(inventory, civilian, role, viewingSelf);

        return inventory;
    }

    public static Inventory createMenu(Civilian civilian, Region region, UUID uuid, boolean viewingSelf) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(region.getPeople().size()) + 9, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("region", region);
        data.put("uuid", uuid);
        setNewData(civilian.getUuid(), data);

        ArrayList<String> lore;

        //1 Player
        Player player = Bukkit.getPlayer(uuid);
        String role = region.getPeople().get(uuid);
        ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta im = (SkullMeta) playerItem.getItemMeta();
        im.setDisplayName(player.getName());
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), region.getPeople().get(uuid)));
        im.setLore(lore);
        im.setOwningPlayer(player);
        playerItem.setItemMeta(im);
        inventory.setItem(1, playerItem);

        addItems(inventory, civilian, role, viewingSelf);

        return inventory;
    }
}
