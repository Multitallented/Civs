package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;
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

        LocaleManager localeManager = LocaleManager.getInstance();
        RegionManager regionManager = RegionManager.getInstance();
        String locationString = event.getInventory().getItem(0).getItemMeta().getDisplayName().split("@")[1];
        Region region = regionManager.getRegionAt(Region.idToLocation(locationString));

        Player player = Bukkit.getPlayer(event.getInventory().getItem(1).getItemMeta().getDisplayName());
        Player cPlayer = Bukkit.getPlayer(civilian.getUuid());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        clearHistory(civilian.getUuid());
//        appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);

        if (event.getCurrentItem().getType().equals(Material.GOLD_BLOCK)) {
            cPlayer.performCommand("cv setowner " + player.getName() + " " + locationString);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.IRON_BLOCK)) {
            cPlayer.performCommand("cv setmember " + player.getName() + " " + locationString);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.STONE)) {
            cPlayer.performCommand("cv setguest " + player.getName() + " " + locationString);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.REDSTONE_BLOCK)) {
            cPlayer.performCommand("cv removemember " + player.getName() + " " + locationString);
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, Region region, UUID uuid) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(region.getPeople().size()) + 9, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        //0 Icon
        CVItem cvItem = new CVItem(regionType.getMat(), 1, regionType.getDamage());
        cvItem.setDisplayName(region.getType() + "@" + region.getId());
        ArrayList<String> lore;
        //TODO set lore
        inventory.setItem(0, cvItem.createItemStack());

        //1 Player
        Player player = Bukkit.getPlayer(uuid);
        String role = region.getPeople().get(uuid);
        ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta im = (SkullMeta) playerItem.getItemMeta();
        im.setDisplayName(player.getName());
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), region.getPeople().get(uuid)));
        im.setLore(lore);
        im.setOwner(player.getName());
        playerItem.setItemMeta(im);
        inventory.setItem(1, playerItem);

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));

        //9 set owner
        if (!role.equals("owner")) {
            CVItem cvItem1 = CVItem.createCVItemFromString("GOLD_BLOCK");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "set-owner"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "owner-description"));
            cvItem1.setLore(lore);
            inventory.setItem(9, cvItem1.createItemStack());
        }

        //10 set member
        if (!role.equals("member")) {
            CVItem cvItem1 = CVItem.createCVItemFromString("IRON_BLOCK");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "set-member"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "member-description"));
            cvItem1.setLore(lore);
            inventory.setItem(10, cvItem1.createItemStack());
        }

        //11 set guest
        if (!role.equals("guest")) {
            CVItem cvItem1 = CVItem.createCVItemFromString("STONE.2");
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

        return inventory;
    }
}
