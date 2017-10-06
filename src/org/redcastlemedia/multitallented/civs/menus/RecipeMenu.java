package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RecipeMenu extends Menu {

    private static final String MENU_NAME = "CivRecipe";

    public RecipeMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        if (Menu.isBackButton(event.getCurrentItem(),
                CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId()).getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
    }


    public static Inventory createMenu(List<List<CVItem>> items, UUID uuid, ItemStack icon) {
        int index = 0;
        HashMap<Integer, CVItem> proxyInv = new HashMap<>();
        HashMap<Integer, List<CVItem>> cycleItems = new HashMap<>();

        for (List<CVItem> subItems : items) {
            if (subItems.size() == 1) {
                CVItem item = subItems.get(0);
                int qty = item.getQty();

                ItemStack is = new ItemStack(item.getMat());
                int maxStack = is.getMaxStackSize();

                while (qty > maxStack) {
                    CVItem tempItem = item.clone();
                    tempItem.setQty(maxStack);
                    proxyInv.put(index, tempItem);
                    index++;
                    qty -= maxStack;
                }

                CVItem tempItem = item.clone();
                tempItem.setQty(qty);
                proxyInv.put(index, tempItem);
                index++;
            } else if (!subItems.isEmpty()) {
                int baseIndex = index;
                int baseIndexOffset = 0;

                CVItem item = subItems.get(0);
                int qty = item.getQty();

                ItemStack is = new ItemStack(item.getMat());
                int maxStack = is.getMaxStackSize();
                int orMax = 0;
                while (qty > maxStack) {
                    CVItem tempItem = item.clone();
                    tempItem.setQty(maxStack);
                    proxyInv.put(index, tempItem);
                    List<CVItem> tempListItems = new ArrayList<>();
                    tempListItems.add(tempItem);
                    cycleItems.put(baseIndex + baseIndexOffset, tempListItems);
                    index++;
                    orMax++;
                    baseIndexOffset++;
                    qty -= maxStack;
                }

                CVItem tempItem = item.clone();
                tempItem.setQty(qty);
                proxyInv.put(index, tempItem);
                ArrayList<CVItem> tempListItems = new ArrayList<>();
                tempListItems.add(tempItem);
                cycleItems.put(baseIndex + baseIndexOffset, tempListItems);
                index++;
                orMax++;

                int reqIndex = 1;
                for (CVItem currItem : subItems) {
                    if (currItem.equals(item)) {
                        continue;
                    }
                    baseIndexOffset = 0;

                    ItemStack cis = new ItemStack(currItem.getMat());
                    int cqty = currItem.getQty();
                    int cMaxStack = cis.getMaxStackSize();
                    int cMax = 0;
                    while (cqty > cMaxStack) {
                        cMax++;
                        cqty -= cMaxStack;

                        if (!cycleItems.containsKey(baseIndex + baseIndexOffset)) {
                            cycleItems.put(baseIndex + baseIndexOffset, new ArrayList<CVItem>());
                        }
                        CVItem clone = currItem.clone();
                        clone.setQty(cMaxStack);

                        List<CVItem> subCycleList = cycleItems.get(baseIndex + baseIndexOffset);
                        while (subCycleList.size() < reqIndex + 1) {
                            subCycleList.add(new CVItem(Material.AIR, 0, 0));
                        }
                        subCycleList.set(reqIndex, clone);

                        baseIndexOffset++;
                    }
                    cMax++;
                    if (!cycleItems.containsKey(baseIndex + baseIndexOffset)) {
                        cycleItems.put(baseIndex + baseIndexOffset, new ArrayList<CVItem>());
                    }

                    List<CVItem> subCycleList = cycleItems.get(baseIndex + baseIndexOffset);
                    while (subCycleList.size() < reqIndex + 1) {
                        subCycleList.add(new CVItem(Material.AIR, 0, 0));
                    }

                    CVItem clone = currItem.clone();
                    clone.setQty(cqty);
                    subCycleList.set(reqIndex, clone);

                    if (cMax > orMax) {
                        index += cMax - orMax;
                        orMax = cMax;
                    }
                    reqIndex++;
                }
                for (int k = 0; k< orMax; k++) {
                    List<CVItem> subCycleList = cycleItems.get(baseIndex + k);
                    while (subCycleList.size() < reqIndex) {
                        subCycleList.add(new CVItem(Material.AIR, 0, 0));
                    }
                }
            }
        }


        Inventory inv = Bukkit.createInventory(null, getInventorySize(index) + 9, MENU_NAME);

        inv.setItem(0, icon);
        inv.setItem(8, getBackButton(CivilianManager.getInstance().getCivilian(uuid)));

        Menu.sanitizeGUIItems(proxyInv);
        Menu.sanitizeCycleItems(cycleItems);

        ArrayList<String> lore;
        for (Integer pIndex : proxyInv.keySet()) {
            CVItem nextItem = proxyInv.get(pIndex);
            ItemStack is;
            if (nextItem.isWildDamage()) {
                is = new ItemStack(nextItem.getMat(), nextItem.getQty());
                ItemMeta isMeta = is.getItemMeta();
                lore = new ArrayList<String>();
                lore.add("Any type acceptable");
                isMeta.setLore(lore);
                is.setItemMeta(isMeta);
            } else {
                is = new ItemStack(nextItem.getMat(), nextItem.getQty(), (short) nextItem.getDamage());
            }
            inv.setItem(pIndex + 9, is);
        }

        for (Integer cycleIndex : cycleItems.keySet()) {
            Menu.addCycleItems(uuid, inv, cycleIndex + 9, cycleItems.get(cycleIndex));
        }
        return inv;
    }

}
