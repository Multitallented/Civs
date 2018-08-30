package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ViewMembersMenu extends Menu {
    public static final String MENU_NAME = "CivsMembers";
    public ViewMembersMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        RegionManager regionManager = RegionManager.getInstance();
        String locationString = event.getInventory().getItem(0).getItemMeta().getDisplayName().split("@")[1];
        Town town = TownManager.getInstance().getTown(locationString);
        Region region = null;
        if (town == null) {
            region = regionManager.getRegionAt(Region.idToLocation(locationString));
        }

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {

            Player player = Bukkit.getPlayer(event.getCurrentItem().getItemMeta().getDisplayName());
            if (player.getUniqueId().equals(civilian.getUuid())) {
                return;
            }

            appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);
            event.getWhoClicked().closeInventory();
            if (town != null) {
                event.getWhoClicked().openInventory(MemberActionMenu.createMenu(civilian, town, player.getUniqueId()));
            } else {
                event.getWhoClicked().openInventory(MemberActionMenu.createMenu(civilian, region, player.getUniqueId()));
            }
            return;
        }

    }

    public static Inventory createMenu(Civilian civilian, Town town) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(town.getPeople().size()) + 9, MENU_NAME);

        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        //0 Icon
        CVItem cvItem = new CVItem(townType.getMat(), 1);
        cvItem.setDisplayName(town.getType() + "@" + town.getName());
        //TODO set lore
        inventory.setItem(0, cvItem.createItemStack());

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));

        setInventoryItems(inventory, town.getPeople(), civilian);

        return inventory;
    }

    public static Inventory createMenu(Civilian civilian, Region region) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(region.getPeople().size()) + 9, MENU_NAME);

        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        //0 Icon
        CVItem cvItem = new CVItem(regionType.getMat(), 1);
        cvItem.setDisplayName(region.getType() + "@" + region.getId());
        inventory.setItem(0, cvItem.createItemStack());
        //TODO set lore?

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));

        setInventoryItems(inventory, region.getPeople(), civilian);

        return inventory;
    }

    private static void setInventoryItems(Inventory inventory, HashMap<UUID, String> people, Civilian civilian) {
        ArrayList<String> lore;
        int i=9;
        for (UUID uuid : people.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            im.setDisplayName(player.getName());
            lore = new ArrayList<>();
            lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), people.get(uuid)));
            im.setLore(lore);
            im.setOwningPlayer(player);
            playerItem.setItemMeta(im);
            inventory.setItem(i, playerItem);
            i++;
        }
    }
}
