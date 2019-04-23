package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.BlockLogger;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

public class StartTutorialMenu extends Menu {
    static String MENU_NAME = "CivStartTut";
    public StartTutorialMenu() {
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

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();

            String name = event.getCurrentItem().getItemMeta().getDisplayName();
            if (name.equals(localeManager.getTranslation(civilian.getLocale(), "set-tutorial"))) {
                BlockLogger.getInstance().saveTutorialLocation(event.getWhoClicked().getLocation());

                Location regionLocation = new Location(player.getWorld(), player.getLocation().getX() + 3,
                        player.getLocation().getY(), player.getLocation().getZ());

                pasteExampleRegion(regionLocation);
                createExampleCoalShop(regionLocation);
            } else {
                civilian.setInTutorial(true);
                player.teleport(BlockLogger.getInstance().getTutorialLocation());
                civilianManager.saveCivilian(civilian);

                // TODO send player a message for what to do first in the tutorial
            }

            return;
        }
        if (event.getCurrentItem().getType().equals(Material.BARRIER)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            civilian.setInTutorial(false);
            civilian.setAskForTutorial(false);
            civilianManager.saveCivilian(civilian);
            event.getWhoClicked().openInventory(MainMenu.createMenu(civilian));
            return;
        }
    }

    private void pasteExampleRegion(Location location) {
        location.getBlock().setType(Material.CHEST, true);
        location.getBlock().getRelative(1, 0, 1).setType(Material.CHEST, true);
        location.getBlock().getRelative(-1, 0, 1).setType(Material.CHEST, true);

        for (int x=-2; x<3; x++) {
            for (int z=-2; z<3; z++) {
                location.getBlock().getRelative(x, 2, z).setType(Material.WHITE_WOOL, true);
            }
        }
        for (int x=-2; x<3; x++) {
            for (int z=-2; z<3; z++) {
                if (0==x || 0==z) {
                    continue;
                }
                if (2 != Math.abs(x) && 2 != Math.abs(z)) {
                    continue;
                }
                location.getBlock().getRelative(x, 0, z).setType(Material.OAK_FENCE, true);
                location.getBlock().getRelative(x, 1, z).setType(Material.OAK_FENCE, true);
            }
        }
    }

    private void createExampleCoalShop(Location location) {
        int[] radii = new int[6];
        radii[0] = 2;
        radii[1] = 2;
        radii[2] = 2;
        radii[3] = 2;
        radii[4] = 2;
        radii[5] = 2;
        RegionType coalShop = (RegionType) ItemManager.getInstance().getItemType("coal_shop");
        HashMap<String, String> effects = new HashMap<>();
        effects.put("block_break", "");
        effects.put("block_build", "");
        effects.put("block_explosion", "");
        Region region = new Region("coal_shop", new HashMap<UUID, String>(), location, radii, effects, 0);
        RegionManager.getInstance().addRegion(region);
    }

    public static Inventory createMenu(Civilian civilian, boolean setLocation) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        LocaleManager localeManager = LocaleManager.getInstance();

        CVItem cvItem = CVItem.createCVItemFromString("EMERALD");
        if (!setLocation) {
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "start-tutorial"));
        } else {
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "set-tutorial"));
        }
        List<String> lore = new ArrayList<>();
        if (!setLocation) {
            lore.add(localeManager.getTranslation(civilian.getLocale(), "start-tutorial-desc"));
        } else {
            lore.add(localeManager.getTranslation(civilian.getLocale(), "set-tutorial-desc"));
        }
        cvItem.setLore(lore);
        inventory.setItem(3, cvItem.createItemStack());

        CVItem cvItem1 = CVItem.createCVItemFromString("BARRIER");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "skip"));
        inventory.setItem(4, cvItem1.createItemStack());

        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}
