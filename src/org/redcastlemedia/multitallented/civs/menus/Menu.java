package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.*;

public abstract class Menu implements Listener {
    private final String MENU_NAME;
    private volatile static HashMap<UUID, GUI> guis = new HashMap<>();
    private volatile static boolean running = false;

    public Menu(String menuName) {
        this.MENU_NAME = menuName;
    }

    abstract void handleInteract(InventoryClickEvent event);

    @EventHandler
    public void onMenuInteract(InventoryClickEvent event) {
        if (event.getClickedInventory() == null ||
                event.getClickedInventory().getTitle() == null ||
                !event.getClickedInventory().getTitle().equals(MENU_NAME)) {
            return;
        }
        handleInteract(event);
    }
    static int getInventorySize(int count) {
        int size = 9;
        if (count > size) {
            size = count + 9 - (count % 9);
            if (count % 9 == 0) {
                size -= 9;
            }
        }
        size += 9;
        return size;
    }

    public static void sanitizeCycleItems(HashMap<Integer, List<CVItem>> items) {
        for (Integer i : items.keySet()) {
            sanitizeGUIItems(items.get(i));
        }
    }
    public static void sanitizeGUIItems(HashMap<Integer, CVItem> items) {
        sanitizeGUIItems(items.values());
    }
    public static void sanitizeGUIItems(Collection<CVItem> items) {
        for (CVItem item : items) {
            Material mat = item.getMat();
            if (mat == Material.BED_BLOCK) {
                if (item.getQty() > 1) {
                    item.setQty(Math.round(item.getQty() / 2));
                }
                item.setQty(Math.round(item.getQty() / 2));
                item.setMat(Material.BED);
            } else if (mat == Material.WOODEN_DOOR) {
                if (item.getQty() > 1) {
                    item.setQty(Math.round(item.getQty() / 2));
                }
                item.setMat(Material.WOOD_DOOR);
            } else if (mat == Material.REDSTONE_WIRE) {
                item.setMat(Material.REDSTONE);
            } else if (mat == Material.IRON_DOOR_BLOCK) {
                if (item.getQty() > 1) {
                    item.setQty(Math.round(item.getQty() / 2));
                }
                item.setMat(Material.IRON_DOOR);
            } else if (mat == Material.DARK_OAK_DOOR) {
                if (item.getQty() > 1) {
                    item.setQty(Math.round(item.getQty() / 2));
                }
                item.setMat(Material.DARK_OAK_DOOR_ITEM);
            } else if (mat == Material.ACACIA_DOOR) {
                if (item.getQty() > 1) {
                    item.setQty(Math.round(item.getQty() / 2));
                }
                item.setMat(Material.ACACIA_DOOR_ITEM);
            } else if (mat == Material.SPRUCE_DOOR) {
                if (item.getQty() > 1) {
                    item.setQty(Math.round(item.getQty() / 2));
                }
                item.setMat(Material.SPRUCE_DOOR_ITEM);
            } else if (mat == Material.BIRCH_DOOR) {
                if (item.getQty() > 1) {
                    item.setQty(Math.round(item.getQty() / 2));
                }
                item.setMat(Material.BIRCH_DOOR_ITEM);
            } else if (mat == Material.JUNGLE_DOOR) {
                if (item.getQty() > 1) {
                    item.setQty(Math.round(item.getQty() / 2));
                }
                item.setMat(Material.JUNGLE_DOOR_ITEM);
            } else if (mat == Material.WALL_SIGN) {
                item.setMat(Material.SIGN);
            } else if (mat == Material.CROPS) {
                item.setMat(Material.WHEAT);
            } else if (mat == Material.CARROT) {
                item.setMat(Material.CARROT_ITEM);
            } else if (mat == Material.POTATO) {
                item.setMat(Material.POTATO_ITEM);
            } else if (mat == Material.SUGAR_CANE_BLOCK) {
                item.setMat(Material.SUGAR_CANE);
            } else if (mat == Material.FLOWER_POT) {
                item.setMat(Material.FLOWER_POT_ITEM);
            } else if (mat == Material.BURNING_FURNACE) {
                item.setMat(Material.FURNACE);
            } else if (mat == Material.REDSTONE_LAMP_ON) {
                item.setMat(Material.REDSTONE_LAMP_OFF);
            } else if (mat == Material.REDSTONE_TORCH_OFF) {
                item.setMat(Material.REDSTONE_TORCH_ON);
            } else if (mat == Material.STATIONARY_WATER) {
                item.setMat(Material.WATER_BUCKET);
            } else if (mat == Material.STATIONARY_LAVA) {
                item.setMat(Material.LAVA_BUCKET);
            } else if (mat == Material.CAULDRON) {
                item.setMat(Material.CAULDRON_ITEM);
            } else if (mat == Material.NETHER_WARTS) {
                item.setMat(Material.NETHER_STALK);
            } else if (mat == Material.REDSTONE_COMPARATOR_OFF ||
                    mat == Material.REDSTONE_COMPARATOR_ON) {
                item.setMat(Material.REDSTONE_COMPARATOR);
            } else if (mat == Material.DIODE_BLOCK_OFF ||
                    mat == Material.DIODE_BLOCK_ON) {
                item.setMat(Material.DIODE);
            }
        }
    }

    public synchronized static void addCycleItems(UUID uuid, Inventory inv, int index, List<CVItem> items) {

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
    private synchronized static HashMap<UUID, GUI> getGuis() {
        return guis;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity he = event.getPlayer();
        if (he == null) {
            return;
        }
        clearCycleItems(he.getUniqueId());
    }

    public synchronized static void clearCycleItems(UUID uuid) {
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

    private static class GUI {
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
            for (GUIItemSet guiItemSet : cycleItems) {
                int position = guiItemSet.getPosition();
                int pos = position;
                if (guiItemSet.getItems().size() - 2 < position) {
                    pos = 0;
                } else {
                    pos++;
                }
                CVItem nextItem = guiItemSet.getItems().get(pos);
                ItemStack is;
                if (nextItem.isWildDamage()) {
                    is = new ItemStack(nextItem.getMat(), nextItem.getQty());
                    ItemMeta isMeta = is.getItemMeta();
                    if (isMeta != null) {
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add("Any type acceptable");
                        isMeta.setLore(lore);
                        is.setItemMeta(isMeta);
                    }
                } else {
                    is = new ItemStack(nextItem.getMat(), nextItem.getQty(), (short) nextItem.getDamage());
                }

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
