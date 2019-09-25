package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.spells.SpellType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.*;

public abstract class Menu implements Listener {
    private final String MENU_NAME;
    private volatile static HashMap<UUID, GUI> guis = new HashMap<>();
    private volatile static boolean running = false;
    private final static Map<UUID, List<String>> history = new HashMap<>();
    private final static Map<UUID, Map<String, Object>> currentMenuStorage = new HashMap<>();

    public Menu(String menuName) {
        this.MENU_NAME = menuName;
    }

    abstract void handleInteract(InventoryClickEvent event);

    static Object getData(UUID uuid, String key) {
        Map<String, Object> data = currentMenuStorage.get(uuid);
        if (data == null) {
            return null;
        }
        return data.get(key);
    }
    static void setNewData(UUID uuid, Map<String, Object> data) {
        currentMenuStorage.put(uuid, data);
    }
    static void clearData(UUID uuid) {
        currentMenuStorage.remove(uuid);
    }

//    @EventHandler
//    public void onMenuInteract(InventoryClickEvent event) {
//        if (event.getClickedInventory() == null ||
//                event.getClickedInventory().getTitle() == null ||
//                !event.getClickedInventory().getTitle().equals(MENU_NAME)) {
//            return;
//        }
//        handleInteract(event);
//    }
    static int getInventorySize(int count) {
        int size = 9;
        if (count > size) {
            size = count + 9 - (count % 9);
            if (count % 9 == 0) {
                size -= 9;
            }
        }
        return size > 54 ? 54 : size;
    }

    static ItemStack getBackButton(Civilian civilian) {
        CVItem backButton = CVItem.createCVItemFromString("REDSTONE_BLOCK");
        backButton.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "back-button"));
        if (history.get(civilian.getUuid()) == null) {
            history.put(civilian.getUuid(), new ArrayList<String>());
        } else {
            backButton.setLore(history.get(civilian.getUuid()));
        }
        return backButton.createItemStack();
    }
    static void clickBackButton(HumanEntity humanEntity) {
        if (history.get(humanEntity.getUniqueId()) == null ||
                history.get(humanEntity.getUniqueId()).isEmpty()) {
            return;
        }
        ItemManager itemManager = ItemManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(humanEntity.getUniqueId());
        String[] lastHistory = history.get(civilian.getUuid()).get(history.get(civilian.getUuid()).size() - 1).split(",");
        history.get(civilian.getUuid()).remove(history.get(civilian.getUuid()).size() - 1);
        if (lastHistory[0].equals(ConfirmationMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            humanEntity.openInventory(ConfirmationMenu.createMenu(civilian, itemManager.getItemType(lastHistory[1])));
            return;
        }
        if (lastHistory[0].equals(MainMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            humanEntity.openInventory(MainMenu.createMenu(civilian));
            return;
        }
        if (lastHistory[0].equals(ShopLevelMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            humanEntity.openInventory(ShopLevelMenu.createMenu(civilian));
            return;
        }
        if (lastHistory[0].equals(ShopMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            if (lastHistory.length > 1) {
                humanEntity.openInventory(ShopMenu.createMenu(civilian, itemManager.getItemType(lastHistory[1])));
            } else {
                humanEntity.openInventory(ShopMenu.createMenu(civilian, null));
            }
            return;
        }
        if (lastHistory[0].equals(RegionTypeInfoMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            humanEntity.openInventory(RegionTypeInfoMenu.createMenu(civilian, (RegionType) itemManager.getItemType(lastHistory[1])));
            return;
        }
        if (lastHistory[0].equals(AllianceListMenu.MENU_NAME)) {
            int page = 0;
            if (lastHistory.length > 1) {
                page = Integer.parseInt(lastHistory[1]);
            }
            humanEntity.closeInventory();
            humanEntity.openInventory(AllianceListMenu.createMenu(civilian, page));
            return;
        }
        if (lastHistory[0].equals(ClassTypeInfoMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            humanEntity.openInventory(ClassTypeInfoMenu.createMenu(civilian, (ClassType) itemManager.getItemType(lastHistory[1])));
            return;
        }
        if (lastHistory[0].equals(SpellTypeInfoMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            humanEntity.openInventory(SpellTypeInfoMenu.createMenu(civilian, (SpellType) itemManager.getItemType(lastHistory[1])));
            return;
        }
        if (lastHistory[0].equals(BuiltRegionMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            humanEntity.openInventory(BuiltRegionMenu.createMenu(civilian));
            return;
        }
        if (lastHistory[0].equals(RegionActionMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            if (lastHistory.length > 1) {
                Region region = RegionManager.getInstance().getRegionAt(Region.idToLocation(lastHistory[1]));
                humanEntity.openInventory(RegionActionMenu.createMenu(civilian, region));
            } else {
                clearHistory(humanEntity.getUniqueId());
                humanEntity.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), "no-permission"
                ));
            }
            return;
        }
        if (lastHistory[0].equals(ViewMembersMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            if (lastHistory.length > 1) {
                Region region = RegionManager.getInstance().getRegionAt(Region.idToLocation(lastHistory[1]));
                if (region == null) {
                    clearHistory(humanEntity.getUniqueId());
                } else {
                    humanEntity.openInventory(ViewMembersMenu.createMenu(civilian, region));
                }
            } else {
                clearHistory(humanEntity.getUniqueId());
                humanEntity.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), "no-permission"
                ));
            }
            return;
        }
        if (lastHistory[0].equals(TownListMenu.MENU_NAME)) {
            if (lastHistory.length > 2) {
                UUID uuid = UUID.fromString(lastHistory[2]);
                humanEntity.openInventory(TownListMenu.createMenu(civilian, Integer.parseInt(lastHistory[1]), uuid));
            } else if (lastHistory.length > 1) {
                humanEntity.openInventory(TownListMenu.createMenu(civilian, Integer.parseInt(lastHistory[1])));
            } else {
                clearHistory(humanEntity.getUniqueId());
                humanEntity.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), "no-permission"
                ));
            }
            return;
        }
        if (lastHistory[0].equals(GovLeaderBoardMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            humanEntity.openInventory(GovLeaderBoardMenu.createMenu(civilian));
            return;
        }
        if (lastHistory[0].equals(TownInviteMenu.MENU_NAME)) {
            if (lastHistory.length > 2) {
                humanEntity.openInventory(TownInviteMenu.createMenu(civilian, Integer.parseInt(lastHistory[1]), lastHistory[2]));
            } else {
                clearHistory(humanEntity.getUniqueId());
                humanEntity.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), "no-permission"
                ));
            }
            return;
        }
        if (lastHistory[0].equals(TownActionMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            if (lastHistory.length > 1) {
                Town town = TownManager.getInstance().getTown(lastHistory[1]);
                humanEntity.openInventory(TownActionMenu.createMenu(civilian, town));
            } else {
                clearHistory(humanEntity.getUniqueId());
                humanEntity.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), "no-permission"
                ));
            }
            return;
        }
        if (lastHistory[0].equals(ListAllPlayersMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            if (lastHistory.length > 1) {
                humanEntity.openInventory(ListAllPlayersMenu.createMenu(civilian, 0, UUID.fromString(lastHistory[1])));
            } else {
                humanEntity.openInventory(ListAllPlayersMenu.createMenu(civilian, 0));
            }
            return;
        }
        if (lastHistory[0].equals(CommunityMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            humanEntity.openInventory(CommunityMenu.createMenu(civilian));
            return;
        }
        if (lastHistory[0].equals(LeaderboardMenu.MENU_NAME)) {
            humanEntity.closeInventory();
            humanEntity.openInventory(LeaderboardMenu.createMenu(civilian, 0));
            return;
        }
    }
    static boolean isBackButton(ItemStack is, String locale) {
        return is != null &&
                is.getType() == Material.REDSTONE_BLOCK &&
                is.hasItemMeta() &&
                is.getItemMeta().getDisplayName() != null &&
                ChatColor.stripColor(is.getItemMeta().getDisplayName())
                        .equals(ChatColor.stripColor(LocaleManager.getInstance().getTranslation(locale, "back-button")));
    }
    static void appendHistory(UUID uuid, String params) {
        if (history.get(uuid) == null) {
            history.put(uuid, new ArrayList<String>());
        }
        history.get(uuid).add(params);
    }
    static void setHistory(UUID uuid, List<String> newHistory) {
        history.put(uuid, newHistory);
    }
    public static void clearHistory(UUID uuid) {
        history.remove(uuid);
    }
    static List<String> getHistory(UUID uuid) {
        List<String> returnHistory = history.get(uuid);
        return returnHistory == null ? new ArrayList<String>() : returnHistory;
    }

    static void sanitizeCycleItems(HashMap<Integer, List<CVItem>> items) {
        for (Integer i : items.keySet()) {
            sanitizeGUIItems(items.get(i));
        }
    }
    static void sanitizeGUIItems(HashMap<Integer, CVItem> items) {
        sanitizeGUIItems(items.values());
    }
    private static void sanitizeGUIItems(Collection<CVItem> items) {
        for (CVItem item : items) {
            Material mat = item.getMat();
            if (mat == Material.RED_BED || mat == Material.BLACK_BED || mat == Material.BLUE_BED
                    || mat == Material.BROWN_BED || mat == Material.CYAN_BED
                    || mat == Material.GRAY_BED || mat == Material.GREEN_BED || mat == Material.LIGHT_BLUE_BED
                    || mat == Material.LIGHT_GRAY_BED || mat == Material.LIME_BED || mat == Material.MAGENTA_BED
                    || mat == Material.ORANGE_BED || mat == Material.PINK_BED || mat == Material.PURPLE_BED
                    || mat == Material.WHITE_BED || mat == Material.YELLOW_BED) {
                divideByTwo(item);
            } else if (mat == Material.OAK_DOOR || mat == Material.IRON_DOOR || mat == Material.DARK_OAK_DOOR
                    || mat == Material.BIRCH_DOOR || mat == Material.ACACIA_DOOR || mat == Material.SPRUCE_DOOR
                    || mat == Material.JUNGLE_DOOR) {
                divideByTwo(item);
            } else if (mat == Material.REDSTONE_WIRE) {
                item.setMat(Material.REDSTONE);
            } else if (mat == Material.WALL_SIGN) {
                item.setMat(Material.SIGN);
            } else if (mat == Material.WATER) {
                item.setMat(Material.WATER_BUCKET);
            } else if (mat == Material.LAVA) {
                item.setMat(Material.LAVA_BUCKET);
            } else if (mat == Material.POTATOES) {
                item.setMat(Material.POTATO);
            }
        }
    }
    private static void divideByTwo(CVItem item) {
        if (item.getQty() > 1) {
            item.setQty(Math.round(item.getQty() / 2));
        }
    }

    synchronized static void addCycleItems(UUID uuid, Inventory inv, int index, List<CVItem> items) {

        boolean startCycleThread = guis.isEmpty();
        if (guis.containsKey(uuid)) {
            guis.get(uuid).addCycleItems(index, items);
        } else {
            GUI currentGUI = new GUI(uuid, inv);
            currentGUI.addCycleItems(index, items);
            guis.put(uuid, currentGUI);
        }

        if (startCycleThread && !running) {
            running = true;
            ItemCycleThread ict = new ItemCycleThread();
            ict.start();
        }
    }
    synchronized static HashMap<UUID, GUI> getGuis() {
        return guis;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity he = event.getPlayer();
        if (he == null) {
            return;
        }
        clearCycleItems(he.getUniqueId());
//        history.remove(he.getUniqueId());
    }

    private synchronized static void clearCycleItems(UUID uuid) {
        guis.remove(uuid);
    }
    private static class ItemCycleThread extends Thread {
        @Override
        public void run() {
            while (!getGuis().isEmpty()) {

                HashMap<UUID, GUI> theGuis = getGuis();
                for (GUI gui : theGuis.values()) {
                    gui.advanceItemPositions();
                }


                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
            }
            running = false;
        }
    }

    static class GUI {
        private final UUID uuid;
        private final Inventory inventory;
        private ArrayList<GUIItemSet> cycleItems;

        private class GUIItemSet {
            private final int index;
            private int position;
            private final List<CVItem> items;

            public GUIItemSet(int index, int position, List<CVItem> items) {
                this.index = index;
                this.position = position;
                this.items = items;
            }

            public void setPosition(int position) {
                this.position = position;
            }
            public int getIndex() {
                return index;
            }
            public int getPosition() {
                return position;
            }
            public List<CVItem> getItems() {
                return items;
            }
        }

        public synchronized void advanceItemPositions() {
            ArrayList<GUIItemSet> clonedCycleItems = new ArrayList<>(cycleItems);
            for (GUIItemSet guiItemSet : clonedCycleItems) {
                int position = guiItemSet.getPosition();
                int pos = position;
                if (guiItemSet.getItems().size() - 2 < position) {
                    pos = 0;
                } else {
                    pos++;
                }
                CVItem nextItem = guiItemSet.getItems().get(pos).clone();
                if (nextItem.getGroup() != null) {
                    nextItem.getLore().add("g:" + nextItem.getGroup());
                }
                ItemStack is = nextItem.createItemStack();
                inventory.setItem(guiItemSet.getIndex(), is);
                guiItemSet.setPosition(pos);
            }
        }

        public GUI(UUID uuid, Inventory inv) {
            this.uuid=uuid;
            this.inventory = inv;
            this.cycleItems = new ArrayList<>();
        }

        public void addCycleItems(int index, List<CVItem> items) {
            cycleItems.add(new GUIItemSet(index, 0, items));
        }
    }
}
