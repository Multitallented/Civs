package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Util {

    public static ArrayList<String> textWrap(String prefix, String input) {
        prefix = ChatColor.WHITE + prefix;
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
        String returnInput = new String(input);
        returnInput = returnInput.replaceAll("@\\{AQUA\\}", ChatColor.AQUA + "");
        returnInput = returnInput.replaceAll("@\\{BLACK\\}", ChatColor.BLACK + "");
        returnInput = returnInput.replaceAll("@\\{BLUE\\}", ChatColor.BLUE + "");
        returnInput = returnInput.replaceAll("@\\{BOLD\\}", ChatColor.BOLD + "");
        returnInput = returnInput.replaceAll("@\\{DARK_AQUA\\}", ChatColor.DARK_AQUA + "");
        returnInput = returnInput.replaceAll("@\\{DARK_BLUE\\}", ChatColor.DARK_BLUE + "");
        returnInput = returnInput.replaceAll("@\\{DARK_GRAY\\}", ChatColor.DARK_GRAY + "");
        returnInput = returnInput.replaceAll("@\\{DARK_GREEN\\}", ChatColor.DARK_GREEN + "");
        returnInput = returnInput.replaceAll("@\\{DARK_PURPLE\\}", ChatColor.DARK_PURPLE + "");
        returnInput = returnInput.replaceAll("@\\{DARK_RED\\}", ChatColor.DARK_RED + "");
        returnInput = returnInput.replaceAll("@\\{GOLD\\}", ChatColor.GOLD + "");
        returnInput = returnInput.replaceAll("@\\{GREEN\\}", ChatColor.GREEN + "");
        returnInput = returnInput.replaceAll("@\\{ITALIC\\}", ChatColor.ITALIC + "");
        returnInput = returnInput.replaceAll("@\\{LIGHT_PURPLE\\}", ChatColor.LIGHT_PURPLE + "");
        returnInput = returnInput.replaceAll("@\\{MAGIC\\}", ChatColor.MAGIC + "");
        returnInput = returnInput.replaceAll("@\\{RED\\}", ChatColor.RED + "");
        returnInput = returnInput.replaceAll("@\\{RESET\\}", ChatColor.RESET + "");
        returnInput = returnInput.replaceAll("@\\{STRIKETHROUGH\\}", ChatColor.STRIKETHROUGH + "");
        returnInput = returnInput.replaceAll("@\\{UNDERLINE\\}", ChatColor.UNDERLINE + "");
        returnInput = returnInput.replaceAll("@\\{WHITE\\}", ChatColor.WHITE + "");
        returnInput = returnInput.replaceAll("@\\{YELLOW\\}", ChatColor.YELLOW + "");
        return returnInput;
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

    public static ArrayList<Bounty> readBountyList(FileConfiguration config) {
        ArrayList<Bounty> bountyList = new ArrayList<>();
        ConfigurationSection section1 = config.getConfigurationSection("bounties");
        for (String key : section1.getKeys(false)) {
            Bounty bounty;
            if (section1.isSet(key + ".issuer")) {
                bounty = new Bounty(UUID.fromString(section1.getString(key + ".issuer")),
                        section1.getDouble(key + ".amount"));
            } else {
                bounty = new Bounty(null,
                        section1.getDouble(key + ".amount"));
            }
            bountyList.add(bounty);
        }
        return bountyList;
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

    public static boolean hasOverride(Region region, Civilian civilian) {
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        return hasOverride(region, civilian, town);
    }

    public static boolean hasOverride(Region region, Civilian civilian, Town town) {
        boolean override = false;
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        if (town != null && civilian != null) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            override = !regionType.getEffects().containsKey("cant_override") &&
                    townType.getEffects().containsKey("control_override") &&
                    town.getPeople().get(civilian.getUuid()) != null &&
                    town.getPeople().get(civilian.getUuid()).equals("owner");
        }
        return override;
    }
}
