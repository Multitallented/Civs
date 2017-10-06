package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

import java.util.ArrayList;
import java.util.UUID;

public class Civilian {

    private final UUID uuid;
    private String locale;
    private final ArrayList<CivItem> stashItems;

    public Civilian(UUID uuid, String locale, ArrayList<CivItem> stashItems) {
        this.uuid = uuid;
        this.locale = locale;
        this.stashItems = stashItems;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
    public ArrayList<CivItem> getStashItems() {
        return stashItems;
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
