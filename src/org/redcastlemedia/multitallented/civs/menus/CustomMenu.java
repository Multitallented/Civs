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
        HashMap<String, Integer> duplicateCount = new HashMap<>();
        for (Integer i : itemIndexes.keySet()) {
            MenuIcon menuIcon = itemIndexes.get(i);
            if (duplicateCount.containsKey(menuIcon.getKey())) {
                duplicateCount.put(menuIcon.getKey(), duplicateCount.get(menuIcon.getKey()) + 1);
            } else {
                duplicateCount.put(menuIcon.getKey(), 0);
            }
            inventory.setItem(i, createItemStack(civilian, menuIcon, duplicateCount.get(menuIcon.getKey())));
        }
        return inventory;
    }
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
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
