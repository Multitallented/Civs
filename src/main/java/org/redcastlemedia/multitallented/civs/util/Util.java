package org.redcastlemedia.multitallented.civs.util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.items.CVInventory;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.*;

import net.md_5.bungee.api.chat.TextComponent;

public final class Util {

    private Util() {

    }

    public static void promoteWhoeverHasMostNoise(Town town, boolean save) {
        int highestScore = 0;
        UUID newOwner = null;
        for (UUID uuid : town.getIdiocracyScore().keySet()) {
            if (town.getIdiocracyScore().get(uuid) > highestScore) {
                highestScore = town.getIdiocracyScore().get(uuid);
                newOwner = uuid;
            }
        }
        if (newOwner == null) {
            return;
        }
        HashMap<UUID, String> people = new HashMap<>(town.getRawPeople());
        for (UUID uuid : people.keySet()) {
            if (people.get(uuid).contains(Constants.OWNER)) {
                town.getRawPeople().put(uuid, "member");
            }
        }
        town.getRawPeople().put(newOwner, Constants.OWNER);
        town.getIdiocracyScore().clear();
        if (save) {
            TownManager.getInstance().saveTown(town);
        }
    }

    public static void promoteWhoeverHasMostMerit(Town town, boolean save) {
        UUID lowestOwner = null;
        int lowestOwnerScore = 99999999;
        UUID highestMember = null;
        int highestMemberScore = 0;
        for (UUID uuid : town.getRawPeople().keySet()) {
            String role = town.getRawPeople().get(uuid);
            if (role.contains("member")) {
                int score = Util.calculateMerit(uuid, town);
                if (score > highestMemberScore) {
                    highestMember = uuid;
                    highestMemberScore = score;
                }
            } else if (role.contains(Constants.OWNER)) {
                int score = Util.calculateMerit(uuid, town);
                if (score < lowestOwnerScore) {
                    lowestOwnerScore = score;
                    lowestOwner = uuid;
                }
            }
        }
        if (lowestOwner != null && highestMember != null && lowestOwnerScore < highestMemberScore) {
            town.setPeople(lowestOwner, "member");
            town.setPeople(highestMember, Constants.OWNER);
            if (save) {
                TownManager.getInstance().saveTown(town);
            }
        }
    }

    public static void checkNoise(Town town, Player player) {
        if (town == null) {
            return;
        }
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        if (government.getGovernmentType() != GovernmentType.IDIOCRACY) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        int score = town.getIdiocracyScore().getOrDefault(civilian.getUuid(), 0);
        UUID demoteMe = null;
        for (UUID uuid : town.getRawPeople().keySet()) {
            if (town.getRawPeople().get(uuid).contains(Constants.OWNER)) {
                if (town.getIdiocracyScore().getOrDefault(uuid, 0) < score) {
                    demoteMe = uuid;
                    break;
                }
            }
        }
        if (demoteMe != null) {
            town.setPeople(demoteMe, "member");
            town.setPeople(player.getUniqueId(), Constants.OWNER);
            TownManager.getInstance().saveTown(town);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(demoteMe);
            String name = offlinePlayer.getName() == null ? "???" : offlinePlayer.getName();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                    civilian.getLocale(), "merit-new-owner"
            ).replace("$1", name));
            spawnRandomFirework(player);
        }
    }

    public static void checkMerit(Town town, Player player) {
        if (town == null) {
            return;
        }
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        if (government.getGovernmentType() != GovernmentType.MERITOCRACY) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        int score = Util.calculateMerit(player.getUniqueId(), town);
        UUID demoteMe = null;
        for (UUID uuid : town.getRawPeople().keySet()) {
            if (town.getRawPeople().get(uuid).contains(Constants.OWNER)) {
                if (Util.calculateMerit(uuid, town) < score) {
                    demoteMe = uuid;
                    break;
                }
            }
        }
        if (demoteMe != null) {
            town.setPeople(demoteMe, "member");
            town.setPeople(player.getUniqueId(), Constants.OWNER);
            TownManager.getInstance().saveTown(town);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(demoteMe);
            String name = offlinePlayer.getName() == null ? "???" : offlinePlayer.getName();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                    civilian.getLocale(), "merit-new-owner"
            ).replace("$1", name));
            spawnRandomFirework(player);
        }
    }

    public static int calculateMerit(UUID uuid, Town forTown) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        int basePoints = (int) (civilian.getPoints() - (double) civilian.getDeaths() / 4);

        for (Town town : TownManager.getInstance().getTowns()) {
            if (!town.equals(forTown) || !town.getRawPeople().containsKey(civilian.getUuid())) {
                continue;
            }
            int townPoints = 0;
            for (Region region : TownManager.getInstance().getContainingRegions(town.getName())) {
                if (region.getRawPeople().containsKey(civilian.getUuid()) &&
                        region.getRawPeople().get(civilian.getUuid()).contains(Constants.OWNER)) {
                    RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                    townPoints += 4 * regionType.getLevel();
                }
            }
            return basePoints + townPoints;
        }

        return 0;
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

    public static List<String> textWrap(String input) {
        int lineLength = ConfigManager.getInstance().getLineBreakLength();
        return textWrap(lineLength, input);
    }

    public static List<String> textWrap(Civilian civilian, String input) {
        return textWrap(ConfigManager.getInstance().getLineBreakLength(civilian.getLocale()), input);
    }

    private static List<String> textWrap(int lineLength, String input) {
        int longestLength = (int) Math.ceil((double) lineLength * 0.625);
        int longestSection = 0;
        for (String subString : input.split(" ")) {
            int length = subString.length();
            if (longestSection < length) {
                longestSection = length;
            }
        }
        if (longestSection > longestLength) {
            lineLength = longestLength;
        }
        String prefix = getDefaultColor(input);
        ArrayList<String> lore = new ArrayList<>();
        String sendMe = new String(input);
        String addMe = prefix.equals("§r") ? prefix : "";
        for (String line : sendMe.split("\n")) {
            for (String s : line.split(" ")) {
                do {
                    if (s.length() < lineLength) {
                        if (!addMe.equals(prefix) && addMe.length() > 0 && s.length() + addMe.length() > lineLength) {
                            lore.add("" + addMe.trim());
                            addMe = prefix;
                        }
                        addMe += s + " ";
                        s = "";
                    } else {
                        if (!addMe.equals(prefix) && addMe.length() > 0) {
                            lore.add("" + addMe.trim());
                            addMe = prefix;
                        }
                        addMe += s.substring(0, lineLength - 1);
                        s = s.substring(lineLength - 1);
                    }
                } while (s.length() > 0);
            }
            if (!addMe.equals(prefix) && addMe.length() > 0) {
                lore.add("" + addMe.trim());
                addMe = prefix;
            }
        }
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
        return ChatColor.stripColor(itemStack.getItemMeta().getLore().get(0)).equals("starter-book");
    }

    public static boolean isChunkLoadedAt(Location location) {
        int x = (int) Math.floor(location.getX() / 16);
        int z = (int) Math.floor(location.getZ() / 16);
        return location.getWorld().isChunkLoaded(x, z);
    }

    public static boolean isWithinChunk(Chunk chunk, Location location) {
        return chunk.getX() * 16 <= location.getX() &&
                chunk.getX() * 16 + 16 >= location.getX() &&
                chunk.getZ() * 16 <= location.getZ() &&
                chunk.getZ() * 16 + 16 >= location.getZ();
    }

    public static String getDefaultColor(String message) {
        String beginningColor = "" + ChatColor.RESET;
        if (message.startsWith("" + ChatColor.COLOR_CHAR)) {
            beginningColor = message.substring(0, 2);
        }
        return beginningColor;
    }

    public static List<String> parseColors(List<String> inputString) {
        for (int i=0; i<inputString.size(); i++) {
            inputString.set(i, parseColors(inputString.get(i)));
        }
        return inputString;
    }
    public static String parseColors(String input) {
        if (input == null) {
            return null;
        }
        String returnInput = new String(input);
        for (ChatColor color : ChatColor.values()) {
            returnInput = returnInput.replaceAll("@\\{" + color.name() + "\\}", color + "");
        }
        return returnInput;
    }

    public static TextComponent parseColorsComponent(String input) {
        TextComponent message = new TextComponent();
        HashMap<Integer, net.md_5.bungee.api.ChatColor> colorMap = new HashMap<>();
        input = findColorIndexes(input, colorMap);
        ArrayList<Integer> indexes = new ArrayList<>(colorMap.keySet());
        Collections.sort(indexes);
        int prevIndex = 0;
        for (Integer index : indexes) {
            if (index == prevIndex) {
                continue;
            }
            if (prevIndex == 0) {
                message.setText(input.substring(prevIndex, index));
                if (colorMap.containsKey(0)) {
                    message.setColor(colorMap.get(0));
                }
            } else {
                TextComponent newComponent = new TextComponent(input.substring(prevIndex, index));
                newComponent.setColor(colorMap.get(prevIndex));
                message.addExtra(newComponent);
            }
            prevIndex = index;
        }
        if (colorMap.isEmpty()) {
            message.setText(input);
        } else {
            TextComponent newComponent = new TextComponent(input.substring(prevIndex));
            newComponent.setColor(colorMap.get(prevIndex));
            message.addExtra(newComponent);
        }

        return message;
    }

    private static String findColorIndexes(String input, HashMap<Integer, net.md_5.bungee.api.ChatColor> colorMap) {
        String patternStr = "@\\{[A-z]*\\}";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {

            String key = input.substring(matcher.start() + 2, matcher.end() - 1);
            net.md_5.bungee.api.ChatColor color;
            try {
                color = net.md_5.bungee.api.ChatColor.valueOf(key);
            } catch (Exception e) {
                continue;
            }
            colorMap.put(matcher.start(), color);
            input = input.replaceFirst("@\\{[A-z]*\\}", "");
            matcher = pattern.matcher(input);
        }
        return input;
    }

    public static boolean isLocationWithinSightOfPlayer(Location location) {
        final int RENDER_RANGE_SQUARED = 25600;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (location.getWorld() != null &&
                    location.getWorld().equals(player.getWorld()) &&
                    player.getLocation().distanceSquared(location) < RENDER_RANGE_SQUARED) {
                return true;
            }
        }
        return false;
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

    public static String getValidFileName(String fileName) {
        return fileName.replaceAll("^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*", "");
    }
    public static Locale getNumberFormatLocale(String locale) {
        Locale localeEnum = Locale.forLanguageTag(locale);
        if (localeEnum == null) {
            Civs.logger.log(Level.SEVERE, "Unable to find locale {0}", locale);
            return Locale.getDefault();
        }
        return localeEnum;
    }
    public static String getNumberFormat(double number, String locale) {
        String numberFormat = NumberFormat.getInstance(getNumberFormatLocale(locale)).format(number);
        if (numberFormat.isEmpty()) {
            return NumberFormat.getCurrencyInstance().format(number);
        } else {
            return numberFormat;
        }
    }

    public static int createPageButtons(Inventory inventory, int page, Civilian civilian, int totalSize) {
        LocaleManager localeManager = LocaleManager.getInstance();
        Player player = Bukkit.getPlayer(civilian.getUuid());

        //0 Prev button
        if (page > 0) {
            CVItem cvItem = CVItem.createCVItemFromString("REDSTONE");
            cvItem.setDisplayName(localeManager.getTranslation(player,
                    "prev-button"));
            inventory.setItem(0, cvItem.createItemStack());
        }

        int startIndex = page * 36;
        //8 Next button
        if (startIndex + 36 < totalSize) {
            CVItem cvItem1 = CVItem.createCVItemFromString("EMERALD");
            cvItem1.setDisplayName(localeManager.getTranslation(player,
                    "next-button"));
            inventory.setItem(8, cvItem1.createItemStack());
        }
        return startIndex;
    }

    public static boolean containsItems(List<List<CVItem>> req, CVInventory inv) {
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

                    if (orReq.equivalentItem(iss, orReq.getDisplayName() != null, !orReq.getLore().isEmpty())) {
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

    public static boolean removeItems(List<List<CVItem>> req, CVInventory inv) {
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
                    if (hsItem.equivalentItem(item, hsItem.getDisplayName() != null, !hsItem.getLore().isEmpty())) {

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

    public static ItemStack[] getItems(List<List<CVItem>> addItems) {
        List<ItemStack> output = new ArrayList<>();
        outer: for (List<CVItem> tempItems : addItems) {
            double rand = Math.random();
            double prevChance = 0;
            for (CVItem item : tempItems) {
                if ((prevChance < rand) && (prevChance + item.getChance() > rand)) {
                    ItemStack is = item.createItemStack();
                    is.setAmount(1);
                    int amount = item.getQty();
                    int max = is.getMaxStackSize();
                    for (;;) {
                        ItemStack isa;
                        if (amount > max) {
                            isa = item.createItemStack();
                            isa.setAmount(max);
                        } else {
                            isa = item.createItemStack();
                            isa.setAmount(amount);
                        }
                        output.add(isa);
                        if (amount > max) {
                            amount -= max;
                        } else {
                            continue outer;
                        }
                    }
                }
                prevChance += item.getChance();
            }
        }
        return output.toArray(new ItemStack[0]);
    }

    public static ArrayList<ItemStack> addItems(List<List<CVItem>> addItems, CVInventory inv) {
        ArrayList<ItemStack> remainingItems = new ArrayList<>();

        outer: for (List<CVItem> tempItems : addItems) {
            double rand = Math.random();
            double prevChance = 0;
            for (CVItem item : tempItems) {
                if ((prevChance < rand) && (prevChance + item.getChance() > rand)) {
                    ItemStack is = item.createItemStack();
                    is.setAmount(1);
                    if (inv == null) {
                        remainingItems.add(is);
                        continue;
                    }
                    int amount = item.getQty();
                    int max = is.getMaxStackSize();
                    for (ItemStack iss : inv.getContents()) {
                        if (iss == null) {
                            ItemStack isa;
                            if (amount > max) {
                                isa = item.createItemStack();
                                isa.setAmount(max);
                            } else {
                                isa = item.createItemStack();
                                isa.setAmount(amount);
                            }
                            inv.addItem(isa);
                            if (amount > max) {
                                amount -= max;
                                continue;
                            } else {
                                continue outer;
                            }
                        }
                        if (item.equivalentItem(iss, item.getDisplayName() != null, !item.getLore().isEmpty())) {
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

    public static boolean isChestEmpty(CVInventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item != null) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasOverride(Region region, Civilian civilian) {
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        return hasOverride(region, civilian, town);
    }

    public static boolean hasOverride(Region region, Civilian civilian, Town town) {
        boolean override = false;
        Player player = Bukkit.getPlayer(civilian.getUuid());
        boolean isAdmin = player != null && (player.isOp() || Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_PERMISSION));
        if (isAdmin) {
            return true;
        }
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        if (town != null && civilian != null) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            override = !regionType.getEffects().containsKey("cant_override") &&
                    townType.getEffects().containsKey("control_override") &&
                    town.getPeople().get(civilian.getUuid()) != null &&
                    town.getPeople().get(civilian.getUuid()).contains(Constants.OWNER);
        }
        return override;
    }

    public static void spawnRandomFirework(Player player) {
        if (Civs.getInstance() == null) {
            return;
        }
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

    public static void sendMessageToPlayerOrConsole(CommandSender commandSender, String key, String message) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        if (player != null) {
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(player, key));
        } else {
            commandSender.sendMessage(message);
        }
    }

    private static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
    private static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST,
            BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    public static BlockFace getFacing(double yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections)
            return radial[(int) Math.round(yaw / 45f) & 0x7].getOppositeFace();

        return axis[(int) Math.round(yaw / 90f) & 0x3].getOppositeFace();
    }

    public static String formatTime(Player player, long duration) {
        if (duration < 60) {
            return LocaleManager.getInstance().getTranslation(player, "time-seconds")
                    .replace("$1", "" + duration);
        } else if (duration < 3600) {
            return LocaleManager.getInstance().getTranslation(player, "time-minutes")
                    .replace("$1", "" + (int) (duration / 60))
                    .replace("$2", "" + (int) (duration % 60));
        } else {
            int hours = (int) (duration / 3600);
            return LocaleManager.getInstance().getTranslation(player, "time-hours")
                    .replace("$1", "" + hours)
                    .replace("$2", "" + (int) ((duration - hours * 3600) / 60))
                    .replace("$3", "" + (int) (duration % 60));
        }
    }
}
