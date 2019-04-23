package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.BlockLogger;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
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
