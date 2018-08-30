package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Util {

    public static ArrayList<String> textWrap(String prefix, String input) {
        ArrayList<String> lore = new ArrayList<>();
        String sendMe = new String(input);
        String[] sends = sendMe.split(" ");
        String outString = "";
        for (String s : sends) {
            if (outString.length() > 40) {
                lore.add(outString);
                outString = "";
            }
            if (!outString.equals("")) {
                outString += prefix + " ";
            } else {
                outString += prefix;
            }
            outString += s;
        }
        lore.add(outString);
        return lore;
    }
    public static List<String> parseColors(List<String> inputString) {
        for (int i=0; i<inputString.size(); i++) {
            inputString.set(i, parseColors(inputString.get(i)));
        }
        return inputString;
    }
    public static String parseColors(String input) {
        input = input.replaceAll("@\\{AQUA\\}", ChatColor.AQUA + "");
        input = input.replaceAll("@\\{BLACK\\}", ChatColor.BLACK + "");
        input = input.replaceAll("@\\{BLUE\\}", ChatColor.BLUE + "");
        input = input.replaceAll("@\\{BOLD\\}", ChatColor.BOLD + "");
        input = input.replaceAll("@\\{DARK_AQUA\\}", ChatColor.DARK_AQUA + "");
        input = input.replaceAll("@\\{DARK_BLUE\\}", ChatColor.DARK_BLUE + "");
        input = input.replaceAll("@\\{DARK_GRAY\\}", ChatColor.DARK_GRAY + "");
        input = input.replaceAll("@\\{DARK_GREEN\\}", ChatColor.DARK_GREEN + "");
        input = input.replaceAll("@\\{DARK_PURPLE\\}", ChatColor.DARK_PURPLE + "");
        input = input.replaceAll("@\\{DARK_RED\\}", ChatColor.DARK_RED + "");
        input = input.replaceAll("@\\{GOLD\\}", ChatColor.GOLD + "");
        input = input.replaceAll("@\\{GREEN\\}", ChatColor.GREEN + "");
        input = input.replaceAll("@\\{ITALIC\\}", ChatColor.ITALIC + "");
        input = input.replaceAll("@\\{LIGHT_PURPLE\\}", ChatColor.LIGHT_PURPLE + "");
        input = input.replaceAll("@\\{MAGIC\\}", ChatColor.MAGIC + "");
        input = input.replaceAll("@\\{RED\\}", ChatColor.RED + "");
        input = input.replaceAll("@\\{RESET\\}", ChatColor.RESET + "");
        input = input.replaceAll("@\\{STRIKETHROUGH\\}", ChatColor.STRIKETHROUGH + "");
        input = input.replaceAll("@\\{UNDERLINE\\}", ChatColor.UNDERLINE + "");
        input = input.replaceAll("@\\{WHITE\\}", ChatColor.WHITE + "");
        input = input.replaceAll("@\\{YELLOW\\}", ChatColor.YELLOW + "");
        return input;
    }
    public static boolean isSolidBlock(Material type) {
        return type != Material.AIR &&
                type != Material.LEVER &&
                type != Material.WALL_SIGN &&
                type != Material.TORCH &&
                type != Material.STONE_BUTTON &&
                type != Material.BIRCH_BUTTON &&
                type != Material.SPRUCE_BUTTON &&
                type != Material.JUNGLE_BUTTON &&
                type != Material.DARK_OAK_BUTTON &&
                type != Material.ACACIA_BUTTON &&
                type != Material.OAK_BUTTON;
    }
    public static boolean validateFileName(String fileName) {
        return fileName.matches("^[^.\\\\/:*?\"<>|]?[^\\\\/:*?\"<>|]*")
                && getValidFileName(fileName).length()>0;
    }

    public static String getValidFileName(String fileName) throws IllegalStateException {
        String newFileName = fileName.replaceAll("^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*", "");
        if(newFileName.length()==0)
            throw new IllegalStateException(
                    "File Name " + fileName + " results in a empty fileName!");
        return newFileName;
    }
    public static Locale getNumberFormatLocale(String locale) {
        Locale localeEnum = Locale.forLanguageTag(locale);
        if (localeEnum == null) {
            Civs.logger.severe("Unable to find locale " + locale);
            return Locale.getDefault();
        }
        return localeEnum;
    }
    public static String getNumberFormat(double number, String locale) {
        return NumberFormat.getInstance(getNumberFormatLocale(locale)).format(number);
    }
    public static boolean containsItems(List<List<CVItem>> req, Inventory inv) {
        if (req.isEmpty()) {
            return true;
        }
        if (inv == null) {
            return false;
        }

        outer: for (List<CVItem> orReqs : req) {
            for (CVItem orReq : orReqs) {

                int amount = 0;
                for (ItemStack iss : inv.getContents()) {
                    if (iss == null) {
                        continue;
                    }

                    if (orReq.equivalentItem(iss, true)) {
                        if ((iss.getAmount() + amount) >= orReq.getQty()) {
                            continue outer;
                        } else {
                            amount += iss.getAmount();
                        }
                    }
                }
            }
            return false;
        }
        return true;
    }

    public static boolean removeItems(List<List<CVItem>> req, Inventory inv) {
        if (inv == null) {
            return false;
        }

        //clone the list
        ArrayList<ArrayList<CVItem>> hsItemsList = new ArrayList<>();
        for (List<CVItem> hsItems : req) {
            ArrayList<CVItem> tempList = new ArrayList<>();
            for (CVItem hsItem : hsItems) {
                tempList.add(hsItem.clone());
            }
            hsItemsList.add(tempList);
        }


        ArrayList<Integer> removeItems = new ArrayList<>();
        HashMap<Integer, Integer> reduceItems = new HashMap<>();

        for (int i =0; i< inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) {
                continue;
            }

            int j=0;
            boolean removeIndex = false;
            outer1: for (ArrayList<CVItem> hsItems : hsItemsList) {
                for (CVItem hsItem : hsItems) {
                    if (hsItem.equivalentItem(item, true)) {

                        if (item.getAmount() > hsItem.getQty()) {
                            reduceItems.put(i, hsItem.getQty());
                            removeIndex = true;
                        } else if (item.getAmount() == hsItem.getQty()) {
                            removeItems.add(i);
                            removeIndex = true;
                        } else {
                            removeItems.add(i);
                            hsItem.setQty(hsItem.getQty() - item.getAmount());
                        }
                        break outer1;

                    }
                }
                j++;
            }
            if (removeIndex) {
                hsItemsList.remove(j);
            }
        }

        if (!hsItemsList.isEmpty()) {
            return false;
        }

        for (Integer i : reduceItems.keySet()) {
            inv.getItem(i).setAmount(inv.getItem(i).getAmount() - reduceItems.get(i));
        }

        for (Integer i : removeItems) {
            inv.setItem(i, null);
        }

        return true;
    }

    public static ArrayList<ItemStack> addItems(List<List<CVItem>> addItems, Inventory inv) {
        ArrayList<ItemStack> remainingItems = new ArrayList<>();

        outer: for (List<CVItem> tempItems : addItems) {
            double rand = Math.random();
            double prevChance = 0;
            for (CVItem item : tempItems) {
                if ((prevChance < rand) && (prevChance + item.getChance() > rand)) {
                    ItemStack is = new ItemStack(item.getMat(), 1);
                    if (item.getDisplayName() != null) {
                        ItemMeta im = is.getItemMeta();
                        im.setDisplayName(item.getDisplayName());
                        if (item.getLore() != null) {
                            im.setLore(item.getLore());
                        }
                        is.setItemMeta(im);
                    }
                    if (inv == null) {
                        remainingItems.add(is);
                        continue;
                    }
                    int amount = item.getQty();
                    int max = is.getMaxStackSize();
                    String displayName = is.hasItemMeta() ? is.getItemMeta().getDisplayName() : null;
                    List<String> lore = is.hasItemMeta() ? is.getItemMeta().getLore() : null;
                    for (ItemStack iss : inv) {
                        if (iss == null) {
                            ItemStack isa;
                            if (amount > max) {
                                isa = new ItemStack(is.getType(), max);
                            } else {
                                isa = new ItemStack(is.getType(), amount);
                            }
                            if (displayName != null) {
                                ItemMeta ima = isa.getItemMeta();
                                ima.setDisplayName(displayName);
                                if (lore != null) {
                                    ima.setLore(lore);
                                }
                                isa.setItemMeta(ima);
                            }
                            inv.addItem(isa);
                            if (amount > max) {
                                amount -= max;
                                continue;
                            } else {
                                continue outer;
                            }
                        }
                        if (iss.getType() == is.getType() &&
                                iss.getAmount() < iss.getMaxStackSize() &&
                                ((!iss.hasItemMeta() && !is.hasItemMeta()) ||
                                        (iss.hasItemMeta() && is.hasItemMeta() &&
                                                iss.getItemMeta().getDisplayName().equals(is.getItemMeta().getDisplayName())))) {
                            if (amount + iss.getAmount() > iss.getMaxStackSize()) {
                                amount = amount - (iss.getMaxStackSize() - iss.getAmount());
                                iss.setAmount(iss.getMaxStackSize());
                            } else {
                                iss.setAmount(amount + iss.getAmount());
                                continue outer;
                            }
                        }
                    }

                    if (amount > 0) {
                        is.setAmount(amount);
                        remainingItems.add(is);
                    }
                    continue outer;

                }
                prevChance += item.getChance();
            }
        }

        return remainingItems;
    }
}
