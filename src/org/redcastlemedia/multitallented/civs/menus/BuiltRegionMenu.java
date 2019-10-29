package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuiltRegionMenu extends Menu {
    public static final String MENU_NAME = "CivsBuilt";
    public BuiltRegionMenu() {
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

        if (!event.getCurrentItem().hasItemMeta() ||
                event.getCurrentItem().getItemMeta().getDisplayName() == null) {
            return;
        }
        int index = Integer.parseInt(event.getCurrentItem().getItemMeta().getLore().get(0));
        Region region = ((ArrayList<Region>) getData(civilian.getUuid(), "regionList")).get(index);
        if (region == null) {
            Civs.logger.severe("Unable to find region");
            return;
        }
        appendHistory(civilian.getUuid(), MENU_NAME);
        event.getWhoClicked().closeInventory();
        event.getWhoClicked().openInventory(RegionActionMenu.createMenu(civilian, region));
    }

    public static Inventory createMenu(Civilian civilian) {
        List<Region> regions = new ArrayList<>();
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (region.getRawPeople().containsKey(civilian.getUuid())) {
                regions.add(region);
            }
        }
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(regions.size() + 9), MENU_NAME);

        ArrayList<Region> regionList = new ArrayList<>();
        int i=9;
        for (Region region : regions) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            CVItem cvItem = regionType.getShopIcon().clone();
            cvItem.setDisplayName(region.getType() + "@" + region.getLocation().getWorld().getName() + ":" +
                    (int) region.getLocation().getX() + "x, " +
                    (int) region.getLocation().getY() + "y, " +
                    (int) region.getLocation().getZ() + "z");
            List<String> lore = new ArrayList<>();
            regionList.add(region);
            Town town = TownManager.getInstance().getTownAt(region.getLocation());
            lore.add("" + (i - 9));
            if (town != null) {
                lore.add(town.getName());
            }
            cvItem.setLore(lore);
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("regionList", regionList);
        setNewData(civilian.getUuid(), data);

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}
