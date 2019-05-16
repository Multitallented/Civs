package org.redcastlemedia.multitallented.civs.util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public final class Util {

    private Util() {

    }

    public static boolean equivalentLocations(Location location1, Location location2) {
        if (location1 == null || location2 == null ||
                location1.getWorld() == null || location2.getWorld() == null) {
            return location1 == null && location2 == null;
        }
        if (!location1.getWorld().getUID().equals(location2.getWorld().getUID())) {
            return false;
        }
        boolean xEq = Math.abs(location1.getX() - location2.getX()) < 1;
        boolean yEq = Math.abs(location1.getY() - location2.getY()) < 1;
        boolean zEq = Math.abs(location1.getZ() - location2.getZ()) < 1;
        return xEq && yEq && zEq;
    }

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

    public static ItemStack createStarterBook(String locale) {
        CVItem cvItem = CVItem.createCVItemFromString("WRITTEN_BOOK");
        cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(locale, "starter-book"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("starter-book");
        cvItem.setLore(lore);
        return cvItem.createItemStack();
    }

    public static boolean isStarterBook(ItemStack itemStack) {
        if (itemStack.getType() != Material.WRITTEN_BOOK) {
            return false;
        }
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        if (itemStack.getItemMeta().getLore() == null ||
                itemStack.getItemMeta().getLore().isEmpty()) {
            return false;
        }
        return itemStack.getItemMeta().getLore().get(0).equals("starter-book");
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

    public static int createPageButtons(Inventory inventory, int page, Civilian civilian, int totalSize) {
        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Prev button
        if (page > 0) {
            CVItem cvItem = CVItem.createCVItemFromString("REDSTONE");
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "prev-button"));
            inventory.setItem(0, cvItem.createItemStack());
        }

        int startIndex = page * 36;
        //8 Next button
        if (startIndex + 36 < totalSize) {
            CVItem cvItem1 = CVItem.createCVItemFromString("EMERALD");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "next-button"));
            inventory.setItem(8, cvItem1.createItemStack());
        }
        return startIndex;
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
                    town.getPeople().get(civilian.getUuid()).contains("owner");
        }
        return override;
    }

    public static void spawnRandomFirework(Player player) {
        Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        //Our random generator
        Random random = new Random();

        //Get the type
        int rt = random.nextInt(4) + 1;
        FireworkEffect.Type type = null;
        if (rt == 1) type = FireworkEffect.Type.BALL;
        if (rt == 2) type = FireworkEffect.Type.BALL_LARGE;
        if (rt == 3) type = FireworkEffect.Type.BURST;
        if (rt == 4) type = FireworkEffect.Type.CREEPER;
        if (rt == 5) type = FireworkEffect.Type.STAR;

        if (type == null) {
            return;
        }

        //Get our random colours
        int r1i = random.nextInt(17) + 1;
        int r2i = random.nextInt(17) + 1;
        Color c1 = getColor(r1i);
        Color c2 = getColor(r2i);

        //Create our effect with this
        FireworkEffect effect = FireworkEffect.builder()
                .flicker(random.nextBoolean()).withColor(c1).withFade(c2)
                .with(type).trail(random.nextBoolean()).build();

        //Then apply the effect to the meta
        fireworkMeta.addEffect(effect);

        //Generate some random power and set it
        int rp = random.nextInt(2) + 1;
        fireworkMeta.setPower(rp);

        //Then apply this to our rocket
        firework.setFireworkMeta(fireworkMeta);
    }
    private static Color getColor(int i) {
        Color c = null;
        if(i==1){
            c=Color.AQUA;
        }
        if(i==2){
            c=Color.BLACK;
        }
        if(i==3){
            c=Color.BLUE;
        }
        if(i==4){
            c=Color.FUCHSIA;
        }
        if(i==5){
            c=Color.GRAY;
        }
        if(i==6){
            c=Color.GREEN;
        }
        if(i==7){
            c=Color.LIME;
        }
        if(i==8){
            c=Color.MAROON;
        }
        if(i==9){
            c=Color.NAVY;
        }
        if(i==10){
            c=Color.OLIVE;
        }
        if(i==11){
            c=Color.ORANGE;
        }
        if(i==12){
            c=Color.PURPLE;
        }
        if(i==13){
            c=Color.RED;
        }
        if(i==14){
            c=Color.SILVER;
        }
        if(i==15){
            c=Color.TEAL;
        }
        if(i==16){
            c=Color.WHITE;
        }
        if(i==17){
            c=Color.YELLOW;
        }

        return c;
    }

    public static CVItem getGovermentTypeIcon(Civilian civilian, GovernmentType governmentType) {
        CVItem cvItem;
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Gov Type: " + governmentType.name());
        switch (governmentType) {
            case ANARCHY:
                cvItem = CVItem.createCVItemFromString("NETHERRACK");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "anarchy"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "anarchy-desc")));
                break;
            case DEMOCRACY:
                cvItem = CVItem.createCVItemFromString("WHITE_STAINED_GLASS");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "democracy"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "democracy-desc")));
                break;
            case OLIGARCHY:
                cvItem = CVItem.createCVItemFromString("LIME_GLAZED_TERRACOTTA");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "oligarchy"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "oligarchy-desc")));
                break;
            case COMMUNISM:
                cvItem = CVItem.createCVItemFromString("RED_GLAZED_TERRACOTTA");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "communism"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "communism-desc")));
                break;
            case COOPERATIVE:
                cvItem = CVItem.createCVItemFromString("RED_STAINED_GLASS");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "cooperative"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "cooperative-desc")));
                break;
            case LIBERTARIAN:
                cvItem = CVItem.createCVItemFromString("YELLOW_GLAZED_TERRACOTTA");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "libertarian"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "libertarian-desc")));
                break;
            case LIBERTARIAN_SOCIALISM:
                cvItem = CVItem.createCVItemFromString("ORANGE_GLAZED_TERRACOTTA");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "libertarian_socialism"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "libertarian_socialism-desc")));
                break;
            case SOCIALISM:
                cvItem = CVItem.createCVItemFromString("CYAN_GLAZED_TERRACOTTA");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "socialism"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "socialism-desc")));
                break;
            case DEMOCRATIC_SOCIALISM:
                cvItem = CVItem.createCVItemFromString("CYAN_STAINED_GLASS");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "democratic_socialism"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "democratic_socialism-desc")));
                break;
            case KRATEROCRACY:
                cvItem = CVItem.createCVItemFromString("BLACK_GLAZED_TERRACOTTA");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "kraterocracy"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "kraterocracy-desc")));
                break;
            case MERITOCRACY:
                cvItem = CVItem.createCVItemFromString("PURPLE_GLAZED_TERRACOTTA");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "meritocracy"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "meritocracy-desc")));
                break;
            case CYBERSYNACY:
                cvItem = CVItem.createCVItemFromString("LIGHT_BLUE_GLAZED_TERRACOTTA");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "cybersynacy"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "cybersynacy-desc")));
                break;
            case TRIBALISM:
                cvItem = CVItem.createCVItemFromString("GRASS_BLOCK");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "tribalism"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "tribalism-desc")));
                break;
            case FEUDALISM:
                cvItem = CVItem.createCVItemFromString("LIGHT_GRAY_CONCRETE");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "feudalism"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "feudalism-desc")));
                break;
            case COLONIALISM:
                cvItem = CVItem.createCVItemFromString("GRAY_GLAZED_TERRACOTTA");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "colonialism"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "colonialism-desc")));
                break;
            case CAPITALISM:
                cvItem = CVItem.createCVItemFromString("LIME_STAINED_GLASS");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "capitalism"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "capitalism-desc")));
                break;
            case DICTATORSHIP:
            default:
                cvItem = CVItem.createCVItemFromString("GLOWSTONE");
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "dictatorship"));
                lore.addAll(Util.textWrap("", LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "dictatorship-desc")));
                break;
        }
        cvItem.setLore(lore);
        return cvItem;
    }
}
