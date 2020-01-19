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

    public CVInventory(Inventory inventory, Location location) {
        this.inventory = inventory;
        this.size = inventory.getSize();
        this.location = location;
    }

    protected CVInventory(@NonNull Location location) {
        this.location = location;
        setInventory();
        if (this.valid) {
            this.size = this.inventory.getSize();
            update();
        }
    }

    public void setInventory() {
        Block block = this.location.getBlock();
        if (block.getType() != Material.CHEST) {
            this.valid = false;
            return;
        }
        Chest chest = (Chest) block.getState();
        this.inventory = chest.getBlockInventory();
    }

    // This method assumes the chunk is loaded
    public void update() {
        if (!this.valid) {
            setInventory();
            if (!this.valid) {
                return;
            }
        }
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
            update();
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
            return this.inventory.getItem(i);
        } else {
            return contents.get(i);
        }
    }

    public void setItem(int i, ItemStack itemStack) {
        if (Util.isChunkLoadedAt(this.location)) {
            update();
            this.inventory.setItem(i, itemStack);
        } else {
            if (i > 0 && i < getSize()) {
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
            return this.inventory.getContents();
        } else {
            ItemStack[] itemStacks = new ItemStack[getSize()];
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
            Map<Integer, ItemStack> returnMap = this.inventory.addItem(itemStackParams);
            update();
            return returnMap;
        } else if (isChunkLoaded) {
            update();
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
            Map<Integer, ItemStack> returnMap = this.inventory.removeItem(itemStackParams);
            update();
            return returnMap;
        } else {
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
