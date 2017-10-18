package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

import java.util.*;

public class Civilian {

    private final UUID uuid;
    private final HashMap<CivItem, Integer> exp;
    private Set<CivClass> civClasses;
    private String locale;
    private final ArrayList<CivItem> stashItems;

    public Civilian(UUID uuid, String locale, ArrayList<CivItem> stashItems, Set<CivClass> civClasses,
            HashMap<CivItem, Integer> exp) {
        this.uuid = uuid;
        this.locale = locale;
        this.stashItems = stashItems;
        this.civClasses = civClasses;
        this.exp = exp;
    }

    public UUID getUuid() {
        return uuid;
    }
    public Set<CivClass> getCivClasses() { return civClasses; }
    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
    public ArrayList<CivItem> getStashItems() {
        return stashItems;
    }
    public HashMap<CivItem, Integer> getExp() { return exp; }

    public boolean isAtMax(CivItem civItem) {
        String processedName = civItem.getProcessedName();
        boolean atMax = civItem.getCivMax() != -1 &&
                civItem.getCivMax() <= getCountStashItems(processedName) + getCountNonStashItems(processedName);
        if (atMax) {
            return true;
        }
        ConfigManager configManager = ConfigManager.getInstance();
        for (String group : civItem.getGroups()) {
            if (configManager.getGroups().get(group) != -1 &&
                    configManager.getGroups().get(group) <= getCountGroup(group)) {
                return true;
            }
        }
        return false;
    }

    public int getCountStashItems(String name) {
        for (CivItem civItem : stashItems) {
            if (civItem.getProcessedName().equals(name)) {
                return civItem.getCivQty();
            }
        }
        return 0;
    }

    public int getCountGroup(String group) {
        int count = 0;
        ItemManager itemManager = ItemManager.getInstance();
        for (CivItem item : stashItems) {
            if (item.getGroups().contains(group)) {
                count += item.getQty();
            }
        }
        for (ItemStack is : Bukkit.getPlayer(uuid).getInventory()) {
            if (is == null || !is.hasItemMeta()) {
                continue;
            }
            String displayName = is.getItemMeta().getDisplayName();
            if (displayName == null) {
                continue;
            }
            displayName = displayName.replace("Civs ", "").toLowerCase();
            CivItem item = itemManager.getItemType(displayName);
            if (item == null) {
                continue;
            }
            if (!item.getGroups().contains(group)) {
                continue;
            }
            count += is.getAmount();
        }

        for (Region region : RegionManager.getInstance().getAllRegions()) {
            CivItem item = itemManager.getItemType(region.getType());
            if (!item.getGroups().contains(group)) {

            }
            if (!region.getOwners().contains(uuid)) {
                continue;
            }
            count++;
        }
        return count;
    }

    public int getCountRegions(String name) {
        int count = 0;
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (region.getOwners().contains(uuid) && (name == null ||
                    region.getType().equalsIgnoreCase(name))) {
                count++;
            }
        }
        return count;
    }

    public int getCountNonStashItems(String name) {
        int count = 0;
        String itemName = "Civs " + name;
        for (ItemStack is : Bukkit.getPlayer(uuid).getInventory()) {
            if (is == null || !is.hasItemMeta()) {
                continue;
            }
            String displayName = is.getItemMeta().getDisplayName();
            if (displayName == null || !displayName.toLowerCase().equals(itemName)) {
                continue;
            }
            count += is.getAmount();
        }

        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (region.getOwners().contains(uuid) && region.getType().equalsIgnoreCase(name)) {
                count++;
            }
        }
        return count;
    }
}
