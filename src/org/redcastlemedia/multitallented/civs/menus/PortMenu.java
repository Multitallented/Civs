package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.*;

public class PortMenu extends Menu {
    public static String MENU_NAME = "CivsPortList";

    public PortMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() ||
                (event.getCurrentItem().getType() == Material.STONE &&
                event.getCurrentItem().getItemMeta().getDisplayName().startsWith("Icon"))) {
            return;
        }
        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        int page = (int) getData(civilian.getUuid(), "page");

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        LocaleManager localeManager = LocaleManager.getInstance();
        if (event.getCurrentItem().getType() == Material.EMERALD &&
                itemName.equals(ChatColor.stripColor(localeManager.getTranslation(civilian.getLocale(), "next-button")))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(PortMenu.createMenu(civilian, page + 1));
            return;
        }
        if (event.getCurrentItem().getType() == Material.REDSTONE &&
                itemName.equals(ChatColor.stripColor(localeManager.getTranslation(civilian.getLocale(), "prev-button")))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(PortMenu.createMenu(civilian, page - 1));
            return;
        }
        Region region = RegionManager.getInstance().getRegionAt(Region.idToLocation(itemName));
        if (region != null) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            event.getWhoClicked().closeInventory();
            Player player = (Player) event.getWhoClicked();
            String command = "/cv port " + itemName;
            PlayerCommandPreprocessEvent commandPreprocessEvent = new PlayerCommandPreprocessEvent(player, command);
            Bukkit.getPluginManager().callEvent(commandPreprocessEvent);
            if (!commandPreprocessEvent.isCancelled()) {
                player.performCommand("cv port " + itemName);
            }
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, int page) {
        List<Region> returnSet = new ArrayList<>();
        Set<Region> regionSet = RegionManager.getInstance().getAllRegions();
        for (Region region : regionSet) {
            if (returnSet.contains(region)) {
                continue;
            }
            if (!region.getEffects().containsKey("port")) {
                continue;
            }
            if (!region.getPeople().containsKey(civilian.getUuid())) {
                continue;
            }
            //Don't show private ports
            if (region.getEffects().get("port") != null &&
                    !region.getPeople().get(civilian.getUuid()).contains("member") &&
                    !region.getPeople().get(civilian.getUuid()).contains("owner")) {
                continue;
            }
            returnSet.add(region);
        }
        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Prev button
        if (page > 0) {
            CVItem cvItem = CVItem.createCVItemFromString("REDSTONE");
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "prev-button"));
            inventory.setItem(0, cvItem.createItemStack());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        setNewData(civilian.getUuid(), data);

        List<String> lore;

        //6 Back button
        inventory.setItem(6, getBackButton(civilian));

        int startIndex = page * 36;
        //8 Next button
        if (startIndex + 36 < returnSet.size()) {
            CVItem cvItem1 = CVItem.createCVItemFromString("EMERALD");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "next-button"));
            inventory.setItem(8, cvItem1.createItemStack());
        }

        HashSet<Region> regionsAdded = new HashSet<>();
        int i=9;
        for (int k=startIndex; k<returnSet.size() && k<startIndex+36; k++) {
            Region region = returnSet.get(k);
            if (regionsAdded.contains(region)) {
                continue;
            } else {
                regionsAdded.add(region);
            }
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            CVItem cvItem1 = regionType.clone();
            cvItem1.setDisplayName(region.getId());
            lore = new ArrayList<>();
            lore.add(regionType.getDisplayName());
            Town town = TownManager.getInstance().getTownAt(region.getLocation());
            if (town != null) {
                lore.add(town.getName());
            }
            cvItem1.setLore(lore);
            inventory.setItem(i, cvItem1.createItemStack());
            i++;
        }

        return inventory;
    }
}
