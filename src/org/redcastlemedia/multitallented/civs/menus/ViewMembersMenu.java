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
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
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

        LocaleManager localeManager = LocaleManager.getInstance();
        RegionManager regionManager = RegionManager.getInstance();
        String locationString = event.getInventory().getItem(0).getItemMeta().getDisplayName().split("@")[1];
        Region region = regionManager.getRegionAt(Region.idToLocation(locationString));

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        if (event.getCurrentItem().getType() == Material.SKULL_ITEM &&
                event.getCurrentItem().getDurability() == (short) 3) {

            Player player = Bukkit.getPlayer(event.getCurrentItem().getItemMeta().getDisplayName());
            if (player.getUniqueId().equals(civilian.getUuid())) {
                return;
            }

            appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(MemberActionMenu.createMenu(civilian, region, player.getUniqueId()));
            return;
        }

    }

    public static Inventory createMenu(Civilian civilian, Region region) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(region.getPeople().size()) + 9, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        //0 Icon
        CVItem cvItem = new CVItem(regionType.getMat(), 1, regionType.getDamage());
        cvItem.setDisplayName(region.getType() + "@" + region.getId());
        ArrayList<String> lore;
        //TODO set lore
        inventory.setItem(0, cvItem.createItemStack());

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));

        int i=9;
        for (UUID uuid : region.getPeople().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            ItemStack playerItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            im.setDisplayName(player.getName());
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), region.getPeople().get(uuid)));
            im.setLore(lore);
            im.setOwner(player.getName());
            playerItem.setItemMeta(im);
            inventory.setItem(i, playerItem);
            i++;
        }

        return inventory;
    }
}
