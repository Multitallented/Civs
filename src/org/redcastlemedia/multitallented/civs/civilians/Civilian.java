package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

import java.util.*;

public class Civilian {

    private final UUID uuid;
    private final HashMap<CivItem, Integer> exp;
    private Set<CivClass> civClasses;
    private String locale;
    private final ArrayList<CivItem> stashItems;
    private final HashMap<String, CivState> states;
    private Location respawnPoint = null;
    private long lastJail = 0;
    private int kills;
    private int killStreak;
    private int deaths;
    private long lastDeath = 0;
    private int highestKillStreak;
    private double points;
    private int karma;
    private int mana;
    private float expOrbs;

    public Civilian(UUID uuid, String locale, ArrayList<CivItem> stashItems, Set<CivClass> civClasses,
            HashMap<CivItem, Integer> exp, int kills, int killStreak, int deaths, int highestKillStreak,
            double points, int karma) {
        this.uuid = uuid;
        this.locale = locale;
        this.stashItems = stashItems;
        this.civClasses = civClasses;
        this.exp = exp;
        this.states = new HashMap<>();
        this.kills = kills;
        this.killStreak = killStreak;
        this.deaths = deaths;
        this.highestKillStreak = highestKillStreak;
        this.points = points;
        this.karma = karma;
        this.mana = 0;
//        Player player;
//        try {
//            player = Bukkit.getPlayer(uuid);
//            if (player != null) {
//                expOrbs = player.getExp();
//            }
//        } catch (NullPointerException npe) {
//
//        }
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
    public HashMap<String, CivState> getStates() { return states; }
    public Location getRespawnPoint() { return respawnPoint; }
    public void setRespawnPoint(Location location) { this.respawnPoint = location; }
    public long getLastJail() { return lastJail; }
    public void refreshJail() { lastJail = System.currentTimeMillis(); }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public int getKillStreak() { return killStreak; }
    public void setKillStreak(int killStreak) { this.killStreak = killStreak; }
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void refreshDeath() { this.lastDeath = System.currentTimeMillis(); }
    public long getLastDeath() { return lastDeath; }
    public int getHighestKillStreak() { return highestKillStreak; }
    public void setHighestKillStreak(int highestKillStreak) { this.highestKillStreak = highestKillStreak; }
    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }
    public int getKarma() { return karma; }
    public void setKarma(int karma) { this.karma = karma; }
    public int getMana() { return mana; }
    public void setExpOrbs(float expOrbs) { this.expOrbs = expOrbs; }
    public void setMana(int mana) {
        this.mana = mana < 0 ? 0 : mana > 100 ? 100 : mana;
        updateExpBar();
    }

    private void updateExpBar() {
        Player player = Bukkit.getPlayer(uuid);
        if (mana > 99 || mana < 1) {
            player.setExp(expOrbs);
            return;
        }
        player.setExp(mana + 1288 + (mana > 33 ? 1 : 0) + (mana > 66 ? 1 : 0));
    }

    public int getLevel(CivItem civItem) {
        if (civItem == null || exp == null || exp.get(civItem) == null) {
            return 1;
        }
        double experience = exp.get(civItem);
        if (experience == 0) {
            return 1;
        }
        ConfigManager configManager = ConfigManager.getInstance();
        double modifier = configManager.getExpModifier();
        double base = configManager.getExpBase();
        int level = 1;
        for (;;) {
            experience -= base + (level - 1) * modifier * base;
            if (experience < 0) {
                break;
            }
            level++;
        }
        return level;
    }

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
