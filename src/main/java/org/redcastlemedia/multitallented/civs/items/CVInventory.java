package org.redcastlemedia.multitallented.civs.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class CVInventory {
    private List<Integer> indexes = new ArrayList<>();
    private Map<Integer, ItemStack> contents = new HashMap<>();
    private Location chest1;
    private Location chest2;

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

    public List<ItemStack> addItems(ItemStack... itemStackParams) {
        cleanUp();
        List<ItemStack> returnItems = new ArrayList<>();
        ArrayList<ItemStack> itemStacks = new ArrayList<>(Arrays.asList(itemStackParams));

        while(!itemStacks.isEmpty()) {
            boolean itemAdded = false;
            for (int i = 0; i < getSize(); i++) {
                if (itemStacks.isEmpty()) {
                    return returnItems;
                }
                ItemStack currentStack = itemStacks.get(0);
                if (!contents.containsKey(i)) {
                    contents.put(i, currentStack);
                    itemStacks.remove(0);
                    itemAdded = true;
                } else if (contents.get(i).isSimilar(currentStack)) {
                    if (contents.get(i).getAmount() + currentStack.getAmount() < currentStack.getMaxStackSize()) {
                        contents.get(i).setAmount(contents.get(i).getAmount() + currentStack.getAmount());
                        itemStacks.remove(0);
                        itemAdded = true;
                    } else if (contents.get(i).getMaxStackSize() < contents.get(i).getAmount() + currentStack.getAmount()) {
                        int difference = currentStack.getMaxStackSize() - contents.get(i).getAmount();
                        contents.get(i).setAmount(currentStack.getMaxStackSize());
                        currentStack.setAmount(currentStack.getAmount() - difference);

                    }
                }
            }
            if (!itemAdded) {
                returnItems.add(itemStacks.get(0));
                itemStacks.remove(0);
            }
            itemStacks.remove(0);
        }
        return returnItems;
    }
}
