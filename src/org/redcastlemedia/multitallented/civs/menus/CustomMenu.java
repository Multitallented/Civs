package org.redcastlemedia.multitallented.civs.menus;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;

public abstract class CustomMenu {
    protected HashMap<Integer, MenuIcon> itemIndexes;
    protected int size;

    public Inventory createMenu(Civilian civilian) {
        Inventory inventory = Bukkit.createInventory(null, this.size, Civs.NAME + getKey());
        for (Integer i : itemIndexes.keySet()) {
            inventory.setItem(i, itemIndexes.get(i).createCVItem(civilian.getLocale()).createItemStack());
        }
        return inventory;
    }
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon) {
        return menuIcon.createCVItem(civilian.getLocale()).createItemStack();
    }
    public void loadConfig(HashMap<Integer, MenuIcon> itemIndexes,
                    int size) {
        this.itemIndexes = itemIndexes;
        this.size = size;
    }
    public abstract String getKey();
    public abstract String getFileName();
    public abstract void doAction(Civilian civilian, MenuIcon menuIcon);
}
