package org.redcastlemedia.multitallented.civs.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CVInventory {
    private List<Integer> indexes = new ArrayList<>();
    private Map<Integer, ItemStack> contents = new HashMap<>();
    private Location chest1;
    private Location chest2;

    public CVInventory(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                this.contents.put(i, new ItemStack(itemStack));
            }
        }
    }

    public CVInventory(Location location) {
        // TODO get block at location and find double chest if it exists
    }

    private void cleanUp() {
        indexes.removeIf(index -> !contents.containsKey(index));
        Collections.sort(indexes);
    }

    public int getSize() {
        return chest2 == null ? 27 : 54;
    }

    public int firstEmpty() {
        cleanUp();
        for (int i = 0; i < getSize(); i++) {
            if (!indexes.contains(i)) {
                return i;
            }
        }
        return -1;
    }

    public ItemStack getIndex(int i) {
        return contents.get(i);
    }

    public void setIndex(int i, ItemStack itemStack) {
        if (i > 0 && i < getSize()) {
            contents.put(i, itemStack);
        }
    }

    public Map<Integer, ItemStack> getContents() {
        return this.contents;
    }

    public List<ItemStack> checkAddItems(ItemStack... itemStacks) {
        return addOrCheckItems(false, itemStacks);
    }
    public List<ItemStack> addItems(ItemStack... itemStackParams) {
        return addOrCheckItems(true, itemStackParams);
    }
    private List<ItemStack> addOrCheckItems(boolean modify, ItemStack... itemStackParams) {
        cleanUp();
        List<ItemStack> returnItems = new ArrayList<>();
        ArrayList<ItemStack> itemStacks = new ArrayList<>(Arrays.asList(itemStackParams));
        Map<Integer, ItemStack> contentsToModify;
        if (modify) {
            contentsToModify = this.contents;
        } else {
            contentsToModify = new HashMap<>();
            for (Map.Entry<Integer, ItemStack> entry : this.contents.entrySet()) {
                contentsToModify.put(entry.getKey().intValue(), new ItemStack(entry.getValue()));
            }
        }

        while(!itemStacks.isEmpty()) {
            boolean itemAdded = false;
            for (int i = 0; i < getSize(); i++) {
                if (itemStacks.isEmpty()) {
                    return returnItems;
                }
                itemAdded = adjustItemToAdd(itemStacks, contentsToModify, itemAdded, i);
            }
            if (!itemAdded) {
                returnItems.add(itemStacks.get(0));
                itemStacks.remove(0);
            }
            itemStacks.remove(0);
        }
        return returnItems;
    }

    private boolean adjustItemToAdd(ArrayList<ItemStack> itemStacks,
                                    Map<Integer, ItemStack> contentsToModify,
                                    boolean itemAdded, int i) {
        ItemStack currentStack = itemStacks.get(0);
        if (!contentsToModify.containsKey(i)) {
            contentsToModify.put(i, currentStack);
            itemStacks.remove(0);
            itemAdded = true;
        } else if (contentsToModify.get(i).isSimilar(currentStack)) {
            if (contentsToModify.get(i).getAmount() + currentStack.getAmount() < currentStack.getMaxStackSize()) {
                contentsToModify.get(i).setAmount(contentsToModify.get(i).getAmount() + currentStack.getAmount());
                itemStacks.remove(0);
                itemAdded = true;
            } else if (contentsToModify.get(i).getMaxStackSize() < contentsToModify.get(i).getAmount() + currentStack.getAmount()) {
                int difference = currentStack.getMaxStackSize() - contentsToModify.get(i).getAmount();
                contentsToModify.get(i).setAmount(currentStack.getMaxStackSize());
                currentStack.setAmount(currentStack.getAmount() - difference);

            }
        }
        return itemAdded;
    }
}
