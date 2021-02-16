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
    @Getter
    private final Location location;
    private Inventory inventory;
    @Getter
    private int size;
    @Getter
    private boolean valid = true;

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

    public void update() {
        setInventory();
    }

    public int firstEmpty() {
        update();
        if (!this.valid) {
            return -1;
        }
        return this.inventory.firstEmpty();
    }

    public ItemStack getItem(int i) {
        update();
        if (!this.valid) {
            return null;
        }
        return this.inventory.getItem(i);
    }

    public void setItem(int i, ItemStack itemStack) {
        update();
        if (!this.valid) {
            return;
        }
        this.inventory.setItem(i, itemStack);
    }

    public ItemStack[] getContents() {
        update();
        if (!this.valid) {
            return new ItemStack[0];
        }
        return this.inventory.getContents();
    }

    public Map<Integer, ItemStack> checkAddItems(ItemStack... itemStacks) {
        return addOrCheckItems(false, itemStacks);
    }
    public Map<Integer, ItemStack> addItem(ItemStack... itemStackParams) {
        return addOrCheckItems(true, itemStackParams);
    }
    private Map<Integer, ItemStack> addOrCheckItems(boolean modify, ItemStack... itemStackParams) {
        if (modify) {
            if (!this.valid) {
                return new HashMap<>();
            }
            update();
            return this.inventory.addItem(itemStackParams);
        } else {
            update();
        }
        HashMap<Integer, ItemStack> returnItems = new HashMap<>();
        ArrayList<ItemStack> itemStacks = new ArrayList<>(Arrays.asList(itemStackParams));
        Map<Integer, ItemStack> contentsToModify;
        contentsToModify = new HashMap<>();
        for (int i = 0; i < this.inventory.getSize(); i++) {
            ItemStack item = this.inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                contentsToModify.put(i, new ItemStack(item));
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
        if (!this.valid) {
            return new HashMap<>();
        }
        update();
        return this.inventory.removeItem(itemStackParams);
    }

    public boolean contains(Material material) {
        if (material == null) {
            return false;
        }
        update();
        if (!this.valid) {
            return false;
        }
        return this.inventory.contains(material);
    }
}
