package org.redcastlemedia.multitallented.civs;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class InventoryImpl implements Inventory {
    private HashMap<Integer, ItemStack> contents = new HashMap<>();

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setMaxStackSize(int i) {

    }

    @Override
    public ItemStack getItem(int i) {
        return contents.get(i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        if (itemStack == null) {
            contents.remove(i);
        } else {
            contents.put(i, itemStack);
        }
    }

    public int firstPartial(Material material) {
        Validate.notNull(material, "Material cannot be null");
        ItemStack[] inventory = this.getStorageContents();

        for(int i = 0; i < inventory.length; ++i) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == material && item.getAmount() < item.getMaxStackSize()) {
                return i;
            }
        }

        return -1;
    }

    private int firstPartial(ItemStack item) {
        ItemStack[] inventory = this.getStorageContents();
        ItemStack filteredItem = item.clone();
        if (item == null) {
            return -1;
        } else {
            for(int i = 0; i < inventory.length; ++i) {
                ItemStack cItem = inventory[i];
                if (cItem != null && cItem.getAmount() < cItem.getMaxStackSize() && cItem.isSimilar(filteredItem)) {
                    return i;
                }
            }

            return -1;
        }
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
        Validate.noNullElements(items, "Item cannot be null");
        HashMap<Integer, ItemStack> leftover = new HashMap();

        label35:
        for(int i = 0; i < items.length; ++i) {
            ItemStack item = items[i];

            while(true) {
                while(true) {
                    int firstPartial = this.firstPartial(item);
                    if (firstPartial == -1) {
                        int firstFree = this.firstEmpty();
                        if (firstFree == -1) {
                            leftover.put(i, item);
                            continue label35;
                        }

                        if (item.getAmount() <= 64) {
                            this.setItem(firstFree, item);
                            continue label35;
                        }

                        ItemStack stack = item.clone();
                        stack.setAmount(64);
                        this.setItem(firstFree, stack);
                        item.setAmount(item.getAmount() - 64);
                    } else {
                        ItemStack partialItem = this.getItem(firstPartial);
                        int amount = item.getAmount();
                        int partialAmount = partialItem.getAmount();
                        int maxAmount = partialItem.getMaxStackSize();
                        if (amount + partialAmount <= maxAmount) {
                            partialItem.setAmount(amount + partialAmount);
                            this.setItem(firstPartial, partialItem);
                            continue label35;
                        }

                        partialItem.setAmount(maxAmount);
                        this.setItem(firstPartial, partialItem);
                        item.setAmount(amount + partialAmount - maxAmount);
                    }
                }
            }
        }

        return leftover;
    }

    @Override
    public int first(Material material) {
        Validate.notNull(material, "Material cannot be null");
        ItemStack[] inventory = this.getStorageContents();

        for(int i = 0; i < inventory.length; ++i) {
            ItemStack item = inventory[i];
            if (item != null && item.getType() == material) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int first(ItemStack item) {
        return this.first(item, true);
    }


    private int first(ItemStack item, boolean withAmount) {
        if (item == null) {
            return -1;
        } else {
            ItemStack[] inventory = this.getStorageContents();
            int i = 0;

            while(true) {
                if (i >= inventory.length) {
                    return -1;
                }

                if (inventory[i] != null) {
                    if (withAmount) {
                        if (item.equals(inventory[i])) {
                            break;
                        }
                    } else if (item.isSimilar(inventory[i])) {
                        break;
                    }
                }

                ++i;
            }

            return i;
        }
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(ItemStack... items) throws IllegalArgumentException {
        Validate.notNull(items, "Items cannot be null");
        HashMap<Integer, ItemStack> leftover = new HashMap();

        for(int i = 0; i < items.length; ++i) {
            ItemStack item = items[i];
            int toDelete = item.getAmount();

            while(true) {
                int first = this.first(item, false);
                if (first == -1) {
                    item.setAmount(toDelete);
                    leftover.put(i, item);
                    break;
                }

                ItemStack itemStack = this.getItem(first);
                int amount = itemStack.getAmount();
                if (amount <= toDelete) {
                    toDelete -= amount;
                    this.clear(first);
                } else {
                    itemStack.setAmount(amount - toDelete);
                    this.setItem(first, itemStack);
                    toDelete = 0;
                }

                if (toDelete <= 0) {
                    break;
                }
            }
        }

        return leftover;
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] itemStacks = new ItemStack[contents.keySet().size()];
        for (Integer i : contents.keySet()) {
            itemStacks[i] = new ItemStack(contents.get(i).getType(), contents.get(i).getAmount());
        }
        return itemStacks;
    }

    @Override
    public void setContents(ItemStack[] itemStacks) throws IllegalArgumentException {
        contents.clear();
        for (int i = 0; i < itemStacks.length; i++) {
            contents.put(i, itemStacks[i]);
        }
    }

    @Override
    public ItemStack[] getStorageContents() {
        return getContents();
    }

    @Override
    public void setStorageContents(ItemStack[] itemStacks) throws IllegalArgumentException {
        setContents(itemStacks);
    }

    @Override
    public boolean contains(Material material) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean contains(Material material, int i) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(ItemStack itemStack, int i) {
        return false;
    }

    @Override
    public boolean containsAtLeast(ItemStack itemStack, int i) {
        return false;
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException {
        return null;
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(ItemStack itemStack) {
        return null;
    }

    @Override
    public int firstEmpty() {
        for (int i = 0; i < getSize(); i++) {
            if (contents.get(i) == null) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void remove(Material material) throws IllegalArgumentException {

    }

    @Override
    public void remove(ItemStack itemStack) {

    }

    @Override
    public void clear(int i) {

    }

    @Override
    public void clear() {

    }

    @Override
    public List<HumanEntity> getViewers() {
        return null;
    }

    @Override
    public InventoryType getType() {
        return null;
    }

    @Override
    public InventoryHolder getHolder() {
        return null;
    }

    @Override
    public ListIterator<ItemStack> iterator() {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < 27; i++) {
            items.add(null);
        }
        for (Integer i : contents.keySet()) {
            items.set(i, contents.get(i));
        }

        return items.listIterator();
    }

    @Override
    public ListIterator<ItemStack> iterator(int i) {
        return null;
    }

    @Override
    public Location getLocation() {
        return null;
    }
}
