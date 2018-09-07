package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Multi
 */
public class CVItem {
    private Material mat;
    private int qty;
    private final double chance;
    private String displayName = null;
    private List<String> lore = new ArrayList<>();

    public CVItem(Material mat, int qty, int chance, String displayName, List<String> lore) {
        this.mat = mat;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
        this.displayName = displayName;
        this.lore = lore;
    }

    public CVItem(Material mat, int qty, int chance, String displayName) {
        this.mat = mat;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
        this.displayName = displayName;
    }

    public CVItem(Material mat, int qty, int chance) {
        this.mat = mat;
        this.qty = qty;
        this.chance = ((double) chance) / 100;
    }

    public CVItem(Material mat, int qty) {
        this.mat = mat;
        this.qty = qty;
        this.chance = 1;
    }

    public static CVItem createCVItemFromString(String materialString)  {
        String quantityString = "1";
        String chanceString = "100";
        Material mat;

        String[] splitString;


        for (;;) {
            int asteriskIndex = materialString.indexOf("*");
            int percentIndex = materialString.indexOf("%");
            if (asteriskIndex != -1 && asteriskIndex > percentIndex) {
                splitString = materialString.split("\\*");
                quantityString = splitString[splitString.length - 1];
                materialString = splitString[0];
            } else if (percentIndex != -1 && percentIndex > asteriskIndex) {
                splitString = materialString.split("%");
                chanceString = splitString[splitString.length - 1];
                materialString = splitString[0];
            } else {
                mat = getMaterialFromString(materialString);
                break;
            }
        }

        if (mat == null) {
            mat = Material.STONE;
            Civs.logger.severe(Civs.getPrefix() + "Unable to parse material " + materialString);
        }
        int quantity = Integer.parseInt(quantityString);
        int chance = Integer.parseInt(chanceString);
        return new CVItem(mat, quantity, chance);
    }

    private static Material getMaterialFromString(String materialString) {
        Material mat = Material.valueOf(materialString.replaceAll(" ", "_").toUpperCase());
//        if (mat == null) {
//            int id = Integer.parseInt(materialString);
//            mat = Material.getMaterial(id);
//        }
        return mat;
    }

//    public boolean damageMatches(short durability) {
//        int dur = (int) durability;
//        if (dur == damage) {
//            return true;
//        }
//        if ((mat == Material.OAK_LOG || mat == Material.BIRCH_LOG || mat == Material.SPRUCE_LOG
//                || mat == Material.JUNGLE_LOG || mat == Material.DARK_OAK_LOG || mat == Material.ACACIA_LOG) &&
//                ((damage + 4) == dur || (damage + 8) == dur || (damage + 12) == dur)) {
//            return true;
//        }
//        return false;
//    }

    public boolean equivalentItem(ItemStack iss) {
        return equivalentItem(iss, false);
    }

    public static boolean isCivsItem(ItemStack is) {
        if (is == null || !is.hasItemMeta()) {
            return false;
        }
        ItemMeta im = is.getItemMeta();
        if (im == null || im.getDisplayName() == null) {
            return false;
        }
        return im.getDisplayName().contains("Civs ");
    }

    public static CVItem createFromItemStack(ItemStack is) {
        if (is.hasItemMeta() && is.getItemMeta().getDisplayName() != null) {
            if (is.getItemMeta().getLore() != null) {
                return new CVItem(is.getType(),is.getAmount(), 100, is.getItemMeta().getDisplayName(), is.getItemMeta().getLore());
            } else {
                return new CVItem(is.getType(),is.getAmount(), 100, is.getItemMeta().getDisplayName());
            }
        }
        return new CVItem(is.getType(),is.getAmount());
    }
    public static List<CVItem> createListFromString(String input) {
        group: if (input.contains("g:")) {
            String key = null;
            String itemGroup = null;
            String params = null;
            for (String currKey : ConfigManager.getInstance().getItemGroups().keySet()) {
                if (input.matches("g:" + currKey + "\\*.*")) {
                    key = currKey;
                    itemGroup = ConfigManager.getInstance().getItemGroups().get(key);
                    params = input.replaceAll("g:" + currKey + "(?=\\*)", "");
                }
            }
            if (key == null || itemGroup == null || params == null) {
                break group;
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String chunk : itemGroup.split(",")) {
                stringBuilder.append(chunk);
                stringBuilder.append(params);
                stringBuilder.append(",");
            }
            stringBuilder.substring(stringBuilder.length() - 1);
            input = stringBuilder.toString();
        }
        List<CVItem> reqs = new ArrayList<>();
        for (String req : input.split(",")) {
            reqs.add(createCVItemFromString(req));
        }
        return reqs;
    }

    public ItemStack createItemStack() {
        ItemStack is = new ItemStack(mat, qty);
        if (!is.hasItemMeta()) {
            is.setItemMeta(Bukkit.getItemFactory().getItemMeta(is.getType()));
        }
        ItemMeta im = is.getItemMeta();
        if (displayName != null) {
            im.setDisplayName(displayName);
        }
        if (lore == null) {
            lore = new ArrayList<>();
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
            boolean hasItemMeta = iss.hasItemMeta();
            boolean issNullName = hasItemMeta && iss.getItemMeta().getDisplayName() == null;
            boolean nullName = !hasItemMeta || issNullName;

            boolean equivalentNames = (nullComparison && nullName) || ((!nullComparison && !nullName) && iss.getItemMeta().getDisplayName().equals(getDisplayName()));

            return iss.getType() == getMat() &&
                    equivalentNames;
        } else {
            return iss.getType() == getMat();
        }
    }

    public boolean equivalentCVItem(CVItem iss) {
        return equivalentCVItem(iss, false);
    }

    public boolean equivalentCVItem(CVItem iss, boolean useDisplayName) {
        if (useDisplayName) {
            return iss.getMat() == getMat() &&
                    ((getDisplayName() == null && iss.getDisplayName() == null) || getDisplayName().equals(iss.getDisplayName()));
        } else {
            return iss.getMat() == getMat();
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
    public int getQty() {
        return qty;
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
        return new CVItem(mat, qty, (int) chance, displayName, lore);
    }
}