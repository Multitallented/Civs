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
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.civilians.TutorialManager;
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

        CivilianManager civilianManager = CivilianManager.getInstance();
        Civilian civilian = civilianManager.getCivilian(event.getWhoClicked().getUniqueId());

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();

            civilian.setTutorialIndex(0);
            civilian.setAskForTutorial(false);
            CivilianManager.getInstance().saveCivilian(civilian);
            TutorialManager.getInstance().sendMessageForCurrentTutorialStep(civilian);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.BARRIER)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            civilian.setAskForTutorial(false);
            civilianManager.saveCivilian(civilian);
            event.getWhoClicked().openInventory(MainMenu.createMenu(civilian));
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        LocaleManager localeManager = LocaleManager.getInstance();

        CVItem cvItem = CVItem.createCVItemFromString("EMERALD");
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "start-tutorial"));
        List<String> lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "start-tutorial-desc"));
        cvItem.setLore(lore);
        inventory.setItem(3, cvItem.createItemStack());

        CVItem cvItem1 = CVItem.createCVItemFromString("BARRIER");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "skip"));
        inventory.setItem(4, cvItem1.createItemStack());

        return inventory;
    }
}
