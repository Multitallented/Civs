package org.redcastlemedia.multitallented.civs.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.util.Util;

import lombok.Getter;
import lombok.NonNull;

public class CVInventory {
    private List<Integer> indexes = new ArrayList<>();
    private Map<Integer, ItemStack> contents = new HashMap<>();
    @Getter
    private Location location;
    private Inventory inventory;
    @Getter
    private int size;
    @Getter
    private boolean valid = true;
    @Getter
    private long lastUnloadedModification = -1;

    protected CVInventory(@NonNull Location location) {
        this.location = location;
        setInventory();
        if (this.valid) {
            update();
        } else {
            this.size = 27;
        }
    }

    public void setInventory() {
        if (location.getWorld() == null ||
                Bukkit.getWorld(location.getWorld().getUID()) == null) {
            this.valid = false;
            return;
        }
        Block block = this.location.getBlock();
        if (block.getType() != Material.CHEST) {
            this.valid = false;
            return;
        }
        try {
            Chest chest = (Chest) block.getState();
            this.inventory = chest.getInventory();
            this.size = this.inventory.getSize();
            this.valid = true;
        } catch (Exception e) {
            this.valid = false;
        }
    }

    // This method assumes the chunk is loaded
    public void update() {
        setInventory();
        if (!this.valid) {
            return;
        }
        this.contents.clear();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                this.contents.put(i, new ItemStack(itemStack));
            }
        }
    }

    public void sync() {
        if (!this.valid) {
            return;
        }
        this.size = this.inventory.getSize();
        this.lastUnloadedModification = -1;
        for (int i = 0; i < getSize(); i++) {
            if (this.contents.containsKey(i)) {
                if (this.contents.get(i) == null) {
                    this.inventory.setItem(i, new ItemStack(Material.AIR));
                } else {
                    this.inventory.setItem(i, this.contents.get(i));
                }
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
            update();
            if (!this.valid) {
                return -1;
            }
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

    public ItemStack getItem(int i) {
        if (Util.isChunkLoadedAt(this.location)) {
            update();
            if (!this.valid) {
                return null;
            }
            return this.inventory.getItem(i);
        } else {
            return contents.get(i);
        }
    }

    public void setItem(int i, ItemStack itemStack) {
        if (Util.isChunkLoadedAt(this.location)) {
            update();
            if (!this.valid) {
                return;
            }
            this.inventory.setItem(i, itemStack);
        } else {
            if (i >= 0 && i < getSize()) {
                if (itemStack != null) {
                    contents.put(i, itemStack);
                } else {
                    contents.remove(i);
                }
            }
        }
    }

    public ItemStack[] getContents() {
        if (Util.isChunkLoadedAt(this.location)) {
            update();
            if (!this.valid) {
                return new ItemStack[0];
            }
            return this.inventory.getContents();
        } else {
            int biggestIndex = 0;
            for (Integer i : this.contents.keySet()) {
                if (i > biggestIndex) {
                    biggestIndex = i;
                }
            }
            ItemStack[] itemStacks = new ItemStack[Math.max(getSize(), biggestIndex)];
            for (Map.Entry<Integer, ItemStack> entry : this.contents.entrySet()) {
                itemStacks[entry.getKey()] = entry.getValue();
            }
            return itemStacks;
        }
    }

    public Map<Integer, ItemStack> checkAddItems(ItemStack... itemStacks) {
        return addOrCheckItems(false, itemStacks);
    }
    public Map<Integer, ItemStack> addItem(ItemStack... itemStackParams) {
        return addOrCheckItems(true, itemStackParams);
    }
    private Map<Integer, ItemStack> addOrCheckItems(boolean modify, ItemStack... itemStackParams) {
        boolean isChunkLoaded = Util.isChunkLoadedAt(this.location);
        if (isChunkLoaded && modify) {
            if (!this.valid) {
                return new HashMap<>();
            }
            Map<Integer, ItemStack> returnMap = this.inventory.addItem(itemStackParams);
            update();
            return returnMap;
        } else if (isChunkLoaded) {
            update();
        }
        if (!isChunkLoaded && modify) {
            this.lastUnloadedModification = System.currentTimeMillis();
        }
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
                itemAdded = adjustItemToAdd(itemStacks, contentsToModify, i);
                if (itemAdded) {
                    break;
                }
            }
            if (!itemAdded) {
                returnItems.put(index, itemStacks.get(0));
                index++;
                itemStacks.remove(0);
            }
        }
        return returnItems;
    }

    private boolean adjustItemToAdd(ArrayList<ItemStack> itemStacks,
                                    Map<Integer, ItemStack> contentsToModify, int i) {
        boolean itemAdded = false;
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

    public Map<Integer, ItemStack> removeItem(ItemStack... itemStackParams) {
        if (Util.isChunkLoadedAt(this.location)) {
            if (!this.valid) {
                return new HashMap<>();
            }
            Map<Integer, ItemStack> returnMap = this.inventory.removeItem(itemStackParams);
            update();
            return returnMap;
        } else {
            this.lastUnloadedModification = System.currentTimeMillis();
            HashMap<Integer, ItemStack> returnItems = new HashMap<>();
            ArrayList<ItemStack> itemStacks = new ArrayList<>(Arrays.asList(itemStackParams));

            int index = 0;
            while(!itemStacks.isEmpty()) {
                boolean itemRemoved = false;
                for (int i = 0; i < getSize(); i++) {
                    if (itemStacks.isEmpty()) {
                        return returnItems;
                    }
                    itemRemoved = adjustItemToRemove(itemStacks, this.contents, i);
                    if (itemRemoved) {
                        break;
                    }
                }
                if (!itemRemoved) {
                    returnItems.put(index, itemStacks.get(0));
                    index++;
                    itemStacks.remove(0);
                }
            }
            return returnItems;
        }
    }

    private boolean adjustItemToRemove(ArrayList<ItemStack> itemStacks,
                                    Map<Integer, ItemStack> contentsToModify, int i) {
        boolean itemRemoved = false;
        ItemStack currentStack = itemStacks.get(0);
        if (!contentsToModify.containsKey(i)) {
            return false;
        } else if (contentsToModify.get(i).isSimilar(currentStack)) {
            if (contentsToModify.get(i).getAmount() > currentStack.getAmount()) {
                contentsToModify.get(i).setAmount(contentsToModify.get(i).getAmount() - currentStack.getAmount());
                itemStacks.remove(0);
                itemRemoved = true;
            } else if (contentsToModify.get(i).getAmount() < currentStack.getAmount()) {
                int amount = contentsToModify.get(i).getAmount();
                contentsToModify.remove(i);
                currentStack.setAmount(currentStack.getAmount() - amount);
            } else {
                itemStacks.remove(0);
                contentsToModify.remove(i);
                itemRemoved = true;
            }
        }
        return itemRemoved;
    }

    public boolean contains(Material material) {
        if (material == null) {
            return false;
        }
        if (Util.isChunkLoadedAt(this.location)) {
            update();
            if (!this.valid) {
                return false;
            }
            return this.inventory.contains(material);
        } else {
            for (Map.Entry<Integer, ItemStack> entry : this.contents.entrySet()) {
                if (material.equals(entry.getValue().getType())) {
                    return true;
                }
            }
            return false;
        }
    }
}
