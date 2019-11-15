package org.redcastlemedia.multitallented.civs.menus;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.TeleportEffect;

public class TeleportDestinationMenu extends Menu {
    private static String MENU_NAME = "CivTeleportDest";
    public TeleportDestinationMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());


        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            event.getWhoClicked().closeInventory();
            return;
        }
        if (!event.getCurrentItem().hasItemMeta() ||
                event.getCurrentItem().getItemMeta().getLore() == null ||
                event.getCurrentItem().getItemMeta().getLore().isEmpty()) {
            return;
        }

        String locationString = event.getCurrentItem().getItemMeta().getLore().get(0);
        Region region = (Region) getData(civilian.getUuid(), "region");
        region.getEffects().put(TeleportEffect.KEY, locationString);
        RegionManager.getInstance().saveRegion(region);
        // TODO send message
    }

    public static Inventory createMenu(Civilian civilian, Region region) {
        Set<Region> regions = RegionManager.getInstance().getAllRegions();
        Set<Region> teleportRegions = new HashSet<>();
        for (Region currentRegion : regions) {
            if (currentRegion.getOwners().contains(civilian.getUuid()) &&
                    currentRegion.getEffects().containsKey(TeleportEffect.KEY)) {
                teleportRegions.add(currentRegion);
            }
        }

        int inventorySize = getInventorySize(teleportRegions.size() + 9);
        Inventory inventory = Bukkit.createInventory(null, inventorySize, MENU_NAME);

        // TODO set icon

        inventory.setItem(8, getBackButton(civilian));

        int index = 9;
        for (Region currentRegion : teleportRegions) {
            if (index > inventorySize) {
                break;
            }
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(currentRegion.getType());
            CVItem regionIcon = regionType.getShopIcon(civilian.getLocale());
            regionIcon.getLore().clear();
            regionIcon.getLore().add(currentRegion.getId());
            inventory.setItem(index, regionIcon.createItemStack());
            index++;
        }

        return inventory;
    }
}
