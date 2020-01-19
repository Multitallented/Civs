package org.redcastlemedia.multitallented.civs.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.util.Util;

import lombok.Getter;

public class CVInventory {
    private List<Integer> indexes = new ArrayList<>();
    private Map<Integer, ItemStack> contents = new HashMap<>();
    private Location location;
    private Inventory inventory;
    @Getter
    private int size;

    public CVInventory(Inventory inventory) {
        this.inventory = inventory;
        this.size = this.inventory.getSize();
        update();
    }

    public CVInventory(Location location) {
        this.location = location;
        Block block = location.getBlock();
        if (block.getType() != Material.CHEST) {
            return;
        }
        Chest chest = (Chest) block.getState();
        this.inventory = chest.getBlockInventory();
        this.size = this.inventory.getSize();
        update();
    }

    public void update() {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                this.contents.put(i, new ItemStack(itemStack));
            }
        }
    }

    public void sync() {
        for (int i = 0; i < getSize(); i++) {
            if (this.contents.containsKey(i)) {
                this.inventory.setItem(i, this.contents.get(i));
            } else {
                this.inventory.setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    private void cleanUp() {
        indexes.removeIf(index -> !contents.containsKey(index));
        Collections.sort(indexes);
    }

    public int firstEmpty() {
        if (Util.isChunkLoadedAt(this.location)) {
            return this.inventory.firstEmpty();
        } else {
            cleanUp();
            for (int i = 0; i < getSize(); i++) {
                if (!indexes.contains(i)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public ItemStack getIndex(int i) {
        if (Util.isChunkLoadedAt(this.location)) {
            return this.inventory.getItem(i);
        } else {
            return contents.get(i);
        }
    }

    public void setIndex(int i, ItemStack itemStack) {
        if (Util.isChunkLoadedAt(this.location)) {
            this.inventory.setItem(i, itemStack);
        } else {
            if (i > 0 && i < getSize()) {
                contents.put(i, itemStack);
            }
        }
    }

    public ItemStack[] getContents() {
        if (Util.isChunkLoadedAt(this.location)) {
            return this.inventory.getContents();
        } else {
            ItemStack[] itemStacks = new ItemStack[getSize()];
            for (Map.Entry<Integer, ItemStack> entry : this.contents.entrySet()) {
                itemStacks[entry.getKey()] = entry.getValue();
            }
            return itemStacks;
        }
    }

    public HashMap<Integer, ItemStack> checkAddItems(ItemStack... itemStacks) {
        return addOrCheckItems(false, itemStacks);
    }
    public HashMap<Integer, ItemStack> addItems(ItemStack... itemStackParams) {
        return addOrCheckItems(true, itemStackParams);
    }
    private HashMap<Integer, ItemStack> addOrCheckItems(boolean modify, ItemStack... itemStackParams) {
        boolean isChunkLoaded = Util.isChunkLoadedAt(this.location);
        if (isChunkLoaded && modify) {
            return this.inventory.addItem(itemStackParams);
        } else if (isChunkLoaded) {
            update();
        }
        cleanUp();
        HashMap<Integer, ItemStack> returnItems = new HashMap<>();
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

        int index = 0;
        while(!itemStacks.isEmpty()) {
            boolean itemAdded = false;
            for (int i = 0; i < getSize(); i++) {
                if (itemStacks.isEmpty()) {
                    return returnItems;
                }
                itemAdded = adjustItemToAdd(itemStacks, contentsToModify, itemAdded, i);
            }
            if (!itemAdded) {
                returnItems.put(index, itemStacks.get(0));
                index++;
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
