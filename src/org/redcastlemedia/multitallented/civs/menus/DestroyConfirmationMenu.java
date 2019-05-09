package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashMap;
import java.util.Map;

public class DestroyConfirmationMenu extends Menu {
    static String MENU_NAME = "CivDestroyConfirm";
    public DestroyConfirmationMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        LocaleManager localeManager = LocaleManager.getInstance();
        CivilianManager civilianManager = CivilianManager.getInstance();

        Civilian civilian = civilianManager.getCivilian(event.getWhoClicked().getUniqueId());

        if (Menu.isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Region region = null;
        Town town = null;
        if (getData(civilian.getUuid(), "region") != null) {
            region = (Region) getData(civilian.getUuid(), "region");
        } else {
            town = (Town) getData(civilian.getUuid(), "town");
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            clearHistory(civilian.getUuid());
            if (region != null) {
                if ((!region.getPeople().containsKey(civilian.getUuid()) ||
                        !region.getPeople().get(civilian.getUuid()).equals("owner")) &&
                        (Civs.perm == null || !Civs.perm.has(player, "civs.admin"))) {
                    clearHistory(civilian.getUuid());
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(Civs.getPrefix() +
                            localeManager.getTranslation(civilian.getLocale(), "no-permission"));
                    return;
                }
                RegionManager.getInstance().removeRegion(region, true, true);
                BlockBreakEvent blockBreakEvent = new BlockBreakEvent(region.getLocation().getBlock(), player);
                CivilianListener.getInstance().onCivilianBlockBreak(blockBreakEvent);
            } else if (town != null) {
                if ((!town.getPeople().containsKey(civilian.getUuid()) ||
                        !town.getPeople().get(civilian.getUuid()).equals("owner")) &&
                        (Civs.perm == null || !Civs.perm.has(player, "civs.admin"))) {
                    clearHistory(civilian.getUuid());
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(Civs.getPrefix() +
                            localeManager.getTranslation(civilian.getLocale(), "no-permission"));
                    return;
                }
                TownManager.getInstance().removeTown(town, true);
                if (ConfigManager.getInstance().getTownRings()) {
                    town.destroyRing(true, true);
                }
            }
            event.getWhoClicked().closeInventory();
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.BARRIER)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, Town town) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        LocaleManager localeManager = LocaleManager.getInstance();
        TownType regionType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        CVItem regionTypeIcon = regionType.clone();
        regionTypeIcon.setDisplayName(town.getName());
        inventory.setItem(0, regionTypeIcon.createItemStack());

        Map<String, Object> data = new HashMap<>();
        data.put("town", town);
        setNewData(civilian.getUuid(), data);

        CVItem cvItem = CVItem.createCVItemFromString("EMERALD");
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "destroy"));

        inventory.setItem(3, cvItem.createItemStack());

        CVItem cvItem1 = CVItem.createCVItemFromString("BARRIER");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "cancel"));
        inventory.setItem(4, cvItem1.createItemStack());

        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }

    public static Inventory createMenu(Civilian civilian, Region region) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        LocaleManager localeManager = LocaleManager.getInstance();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        CVItem regionTypeIcon = regionType.clone();
        inventory.setItem(0, regionTypeIcon.createItemStack());

        Map<String, Object> data = new HashMap<>();
        data.put("region", region);
        setNewData(civilian.getUuid(), data);

        CVItem cvItem = CVItem.createCVItemFromString("EMERALD");
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "destroy"));

        inventory.setItem(3, cvItem.createItemStack());

        CVItem cvItem1 = CVItem.createCVItemFromString("BARRIER");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "cancel"));
        inventory.setItem(4, cvItem1.createItemStack());

        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}
