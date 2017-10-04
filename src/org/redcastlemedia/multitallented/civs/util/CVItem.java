package org.redcastlemedia.multitallented.civs.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Multi
 */
public class CVItem {
    private Material mat;
    private final int damage;
    private int qty;
    private final double chance;
    private final boolean wildDamage;
    private String displayName = null;
    private List<String> lore = new ArrayList<String>();

    public CVItem(Material mat, int qty, int damage, int chance, String displayName, List<String> lore) {
        this.mat = mat;
        this.damage = damage;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
        wildDamage = damage == -1;
        this.displayName = displayName;
        this.lore = lore;
    }

    public CVItem(Material mat, int qty, int damage, int chance, String displayName) {
        this.mat = mat;
        this.damage = damage;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
        wildDamage = damage == -1;
        this.displayName = displayName;
    }

    public CVItem(Material mat, int qty, int damage, int chance) {
        this.mat = mat;
        this.damage = damage;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
        wildDamage = damage == -1;
    }

    public CVItem(Material mat, int qty, int damage) {
        this.mat = mat;
        this.damage = damage;
        this.qty = qty;
        this.chance = 1;
        wildDamage = damage == -1;
    }
    public CVItem(Material mat, int qty) {
        this.mat = mat;
        this.damage = -1;
        this.qty = qty;
        this.chance = 1;
        wildDamage = true;
    }

    public static CVItem createCVItemFromString(String materialString) {
        String quantityString = "1";
        String chanceString = "100";
        String damageString = "-1";
        Material mat;

        String[] splitString;


        for (;;) {
            int asteriskIndex = materialString.indexOf("*");
            int percentIndex = materialString.indexOf("%");
            int dotIndex = materialString.indexOf(".");
            if (asteriskIndex != -1 && asteriskIndex > percentIndex && asteriskIndex > dotIndex) {
                splitString = materialString.split("\\*");
                quantityString = splitString[splitString.length - 1];
                materialString = splitString[0];
            } else if (dotIndex != -1 && dotIndex > percentIndex && dotIndex > asteriskIndex) {
                splitString = materialString.split("\\.");
                damageString = splitString[splitString.length - 1];
                materialString = splitString[0];
            } else if (percentIndex != -1 && percentIndex > asteriskIndex && percentIndex > dotIndex) {
                splitString = materialString.split("%");
                chanceString = splitString[splitString.length - 1];
                materialString = splitString[0];
            } else {
                mat = getMaterialFromString(materialString);
                break;
            }
        }

        if (mat == null) {
            return null;
        }
        int quantity = Integer.parseInt(quantityString);
        int damage = Integer.parseInt(damageString);
        int chance = Integer.parseInt(chanceString);
        return new CVItem(mat, quantity, damage, chance);
    }

    private static Material getMaterialFromString(String materialString) {
        Material mat = Material.valueOf(materialString.replaceAll(" ", "_").toUpperCase());
        if (mat == null) {
            int id = Integer.parseInt(materialString);
            mat = Material.getMaterial(id);
        }
        return mat;
    }

    public boolean damageMatches(short durability) {
        int dur = (int) durability;
        if (dur == damage) {
            return true;
        }
        if ((mat == Material.LOG || mat == Material.LOG_2) && ((damage + 4) == dur || (damage + 8) == dur || (damage + 12) == dur)) {
            return true;
        }
        return false;
    }

    public boolean equivalentItem(ItemStack iss) {
        return equivalentItem(iss, false);
    }

    public static boolean isCivsItem(ItemStack is) {
        if (is == null) {
            return false;
        }
        ItemMeta im = is.getItemMeta();
        if (im == null || im.getDisplayName() == null) {
            return false;
        }
        return im.getDisplayName().contains("Civs ");
    }

    public static CVItem createFromItemStack(ItemStack is) {
        if (is.hasItemMeta() && !is.getItemMeta().getDisplayName().equals("")) {
            return new CVItem(is.getType(),is.getAmount(), is.getDurability(), 100, is.getItemMeta().getDisplayName(), is.getItemMeta().getLore());
        }
        if (is.getDurability() > 0) {
            return new CVItem(is.getType(),is.getAmount(), is.getDurability());
        }
        return new CVItem(is.getType(),is.getAmount());
    }
    public ItemStack createItemStack() {
        ItemStack is;
        if (isWildDamage()) {
            is = new ItemStack(mat, qty);
        } else {
            is = new ItemStack(mat, qty, (short) damage);
        }
        if (!is.hasItemMeta()) {
            is.setItemMeta(Bukkit.getItemFactory().getItemMeta(is.getType()));
        }
        ItemMeta im = is.getItemMeta();
        if (displayName != null) {
            im.setDisplayName(displayName);
        }
        if (!lore.isEmpty()) {
            im.setLore(lore);
        }
        im.removeItemFlags();
        is.setItemMeta(im);
        return is;
    }

    public boolean equivalentItem(ItemStack iss, boolean useDisplayName) {
        if (useDisplayName) {
            boolean nullComparison = getDisplayName() == null;
            boolean nullName = !iss.hasItemMeta() || iss.getItemMeta().getDisplayName() == null;

            boolean equivalentNames = (nullComparison && nullName) || ((!nullComparison && !nullName) && iss.getItemMeta().getDisplayName().equals(getDisplayName()));

            return iss.getType() == getMat() &&
                    (isWildDamage() || getDamage() == (int) (iss.getDurability())) &&
                    equivalentNames;
        } else {
            return iss.getType() == getMat() &&
                    (isWildDamage() || getDamage() == (int) (iss.getDurability()));
        }
    }

    public boolean equivalentTOItem(CVItem iss) {
        return equivalentTOItem(iss, false);
    }

    public boolean equivalentTOItem(CVItem iss, boolean useDisplayName) {
        if (useDisplayName) {
            return iss.getMat() == getMat() &&
                    (isWildDamage() || iss.isWildDamage() || getDamage() == iss.getDamage()) &&
                    ((getDisplayName() == null && iss.getDisplayName() == null) || getDisplayName().equals(iss.getDisplayName()));
        } else {
            return iss.getMat() == getMat() &&
                    (isWildDamage() || iss.isWildDamage() || getDamage() == iss.getDamage());
        }
    }

    public List<String> getLore() {
        return lore;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setMat(Material mat) {
        this.mat = mat;
    }

    public Material getMat() {
        return mat;
    }
    public int getDamage() {
        return damage;
    }
    public int getQty() {
        return qty;
    }
    public boolean isWildDamage() {
        return wildDamage;
    }
    public double getChance() {
        return chance;
    }
    public void setQty(int qty) {
        this.qty = qty;
    }
    public void setDisplayName(String name) {
        this.displayName = name;
    }
    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    @Override
    public CVItem clone() {
        return new CVItem(mat, qty, damage, (int) chance, displayName, lore);
    }
}