package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class BuiltRegionMenu extends Menu {
    public static final String MENU_NAME = "CivsBuilt";
    public BuiltRegionMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

    }

    public static Inventory createMenu(Civilian civilian) {
        List<Region> regions = new ArrayList<>();
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (region.getPeople().containsKey(civilian.getUuid())) {
                regions.add(region);
            }
        }
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(regions.size()) + 9, MENU_NAME);
        //TODO finish this stub

        int i=9;
        for (Region region : regions) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            CVItem cvItem = new CVItem(regionType.getMat(), 1, regionType.getDamage());
            cvItem.setDisplayName(region.getType() + "@" + region.getId());
            List<String> lore = new ArrayList<>();

            //TODO set lore
            cvItem.setLore(lore);
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}
