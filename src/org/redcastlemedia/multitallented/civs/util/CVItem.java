package org.redcastlemedia.multitallented.civs.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
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
    @Getter
    @Setter
    private String group = null;
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
        String nameString = null;
        Material mat;

        String[] splitString;


        for (;;) {
            int asteriskIndex = materialString.indexOf("*");
            int percentIndex = materialString.indexOf("%");
            int nameIndex = materialString.indexOf(".");
            if (asteriskIndex != -1 && asteriskIndex > percentIndex && asteriskIndex > nameIndex) {
                splitString = materialString.split("\\*");
                quantityString = splitString[splitString.length - 1];
                materialString = splitString[0];
            } else if (percentIndex != -1 && percentIndex > asteriskIndex && percentIndex > nameIndex) {
                splitString = materialString.split("%");
                chanceString = splitString[splitString.length - 1];
                materialString = splitString[0];
            } else if (nameIndex != -1 && nameIndex > percentIndex && nameIndex > asteriskIndex) {
                splitString = materialString.split("\\.");
                nameString = splitString[splitString.length -1];
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
        if (nameString == null) {
            return new CVItem(mat, quantity, chance);
        } else {
            List<String> lore = ConfigManager.getInstance().getCustomItemDescription(nameString);
            return new CVItem(mat, quantity, chance, nameString, lore);
        }
    }

    private static Material getMaterialFromString(String materialString) {
        return Material.valueOf(materialString.replaceAll(" ", "_").toUpperCase());
    }

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
        if (im.getLore() == null || im.getLore().size() < 2 || !im.getLore().get(1).equals(im.getDisplayName())) {
            return false;
        }
        return im.getDisplayName().contains(ConfigManager.getInstance().getCivsItemPrefix());
    }

    public static CVItem createFromItemStack(ItemStack is) {
        if (is.hasItemMeta() && is.getItemMeta().getDisplayName() != null &&
                !"".equals(is.getItemMeta().getDisplayName())) {
            if (is.getItemMeta().getLore() != null) {
                return new CVItem(is.getType(),is.getAmount(), 100, is.getItemMeta().getDisplayName(), is.getItemMeta().getLore());
            } else {
                return new CVItem(is.getType(),is.getAmount(), 100, is.getItemMeta().getDisplayName());
            }
        }
        return new CVItem(is.getType(),is.getAmount());
    }
    public static List<CVItem> createListFromString(String input) {
        String groupName = null;
        group: if (input.contains("g:")) {
            String itemGroup = null;
            String params = null;
            for (String currKey : ConfigManager.getInstance().getItemGroups().keySet()) {
                if (input.matches("g:" + currKey + "\\*.*")) {
                    groupName = currKey;
                    itemGroup = ConfigManager.getInstance().getItemGroups().get(groupName);
                    params = input.replaceAll("g:" + currKey + "(?=\\*)", "");
                }
            }
            if (groupName == null || itemGroup == null || params == null) {
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
            CVItem cvItem = createCVItemFromString(req);
            if (groupName != null) {
                cvItem.setGroup(groupName);
            }
            reqs.add(cvItem);
        }
        return reqs;
    }

    public ItemStack createItemStack() {
        ItemStack is = new ItemStack(mat, qty);
        if (displayName != null || (lore != null && !lore.isEmpty())) {
            if (!is.hasItemMeta()) {
                is.setItemMeta(Bukkit.getItemFactory().getItemMeta(is.getType()));
            }
            ItemMeta im = is.getItemMeta();
            if (displayName != null) {
                im.setDisplayName(displayName);
            }
            if (lore == null) {
                lore = new ArrayList<>();
            } else if (!lore.isEmpty()) {
                im.setLore(lore);
            }
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            is.setItemMeta(im);
        }
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
        CVItem cvItem = new CVItem(mat, qty, (int) chance, displayName, new ArrayList<>(lore));
        cvItem.setGroup(group);
        return cvItem;
    }
}
