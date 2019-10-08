package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.List;

public class TutorialChoosePathMenu extends Menu {
    static final String MENU_NAME = "CivsTutChoose";
    public TutorialChoosePathMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clickedStack = event.getCurrentItem();
        if (clickedStack.getItemMeta() == null) {
            return;
        }
        if (clickedStack.getItemMeta().getDisplayName() == null) {
            return;
        }
        ItemMeta im = clickedStack.getItemMeta();
        String pathName = im.getLore().get(0);
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        event.getWhoClicked().closeInventory();
        civilian.setTutorialIndex(0);
        civilian.setTutorialProgress(0);
        civilian.setTutorialPath(pathName);
        TutorialManager.getInstance().sendMessageForCurrentTutorialStep(civilian, true);
        CivilianManager.getInstance().saveCivilian(civilian);
    }

    public static Inventory createMenu(Civilian civilian) {

        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        clearHistory(civilian.getUuid());

        int i=0;
        List<CVItem> itemList = TutorialManager.getInstance().getPaths(civilian);

        for (CVItem item : itemList) {
            if (i > 7) {
                break;
            }
            inventory.setItem(i, item.createItemStack());
            i++;
        }

        // TODO add button to end tutorial

        return inventory;
    }
}
