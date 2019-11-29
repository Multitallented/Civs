package org.redcastlemedia.multitallented.civs.menus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.TwoSecondEvent;
import org.redcastlemedia.multitallented.civs.util.FallbackConfigUtil;
import org.reflections.Reflections;

import lombok.Getter;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class MenuManager implements Listener {
    private static MenuManager instance = null;
    private static Map<UUID, CycleGUI> cycleGuis = new HashMap<>();
    private static Map<UUID, Map<String, Object>> data = new HashMap<>();
    private static Map<UUID, ArrayList<MenuHistoryState>> history = new HashMap<>();
    private static Map<UUID, String> openMenus = new HashMap<>();
    protected static Map<String, CustomMenu> menus = new HashMap<>();

    @Getter
    private MenuIcon backButton;
    @Getter
    private MenuIcon prevButton;
    @Getter
    private MenuIcon nextButton;

    public static MenuManager getInstance() {
        if (instance == null) {
            instance = new MenuManager();
            instance.loadMenuConfigs();
            Bukkit.getPluginManager().registerEvents(instance, Civs.getInstance());
        }
        return instance;
    }
    public void reload() {
        menus.clear();
        loadMenuConfigs();
    }

    public boolean hasMenuOpen(UUID uuid) {
        return openMenus.containsKey(uuid);
    }
    public boolean hasMenuOpen(UUID uuid, String menuName) {
        return menuName.equals(openMenus.get(uuid));
    }

    @EventHandler @SuppressWarnings("unused")
    public void onTwoSecondEvent(TwoSecondEvent event) {
        try {
            for (CycleGUI gui : new HashSet<>(cycleGuis.values())) {
                gui.advanceItemPositions();
            }
        } catch (Exception e) {
            Civs.logger.warning(Arrays.toString(e.getStackTrace()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!openMenus.containsKey(uuid)) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        menus.get(openMenus.get(uuid)).onCloseMenu(civilian, event.getInventory());
        clearCycleItems(uuid);
        String dataMenuName = (String) getData(civilian.getUuid(), "menuName");
        if (openMenus.get(uuid).equals(dataMenuName) &&
                dataMenuName.equals(getData(civilian.getUuid(), "menuName"))) {
            clearData(uuid);
        }
        openMenus.remove(uuid);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        UUID uuid = event.getWhoClicked().getUniqueId();
        if (!openMenus.containsKey(uuid)) {
            return;
        }
        menus.get(openMenus.get(uuid)).onInventoryDrag(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        UUID uuid = event.getWhoClicked().getUniqueId();
        if (!openMenus.containsKey(uuid)) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        if (event.getCurrentItem() != null) {
            if (backButton.createCVItem(civilian.getLocale(), 0)
                    .equivalentItem(event.getCurrentItem(), true, true)) {
                goBack(civilian.getUuid());
                event.setCancelled(true);
                return;
            } else if (prevButton.createCVItem(civilian.getLocale(), 0)
                    .equivalentItem(event.getCurrentItem(), true, true)) {
                int page = (Integer) getData(civilian.getUuid(), "page");
                putData(civilian.getUuid(), "page", page < 1 ? 0 : page - 1);
                refreshMenu(civilian);
                event.setCancelled(true);
                return;
            } else if (nextButton.createCVItem(civilian.getLocale(), 0)
                    .equivalentItem(event.getCurrentItem(), true, true)) {
                int page = (Integer) getData(civilian.getUuid(), "page");
                int maxPage = (Integer) getData(civilian.getUuid(), "maxPage");
                putData(civilian.getUuid(), "page", page >= maxPage ? maxPage : page + 1);
                refreshMenu(civilian);
                event.setCancelled(true);
                return;
            }
        }

        menus.get(openMenus.get(uuid)).onInventoryClick(event);
    }

    public void goBack(UUID uuid) {
        popLastMenu(uuid);
        MenuHistoryState menuHistoryState = popLastMenu(uuid);
        Player player = Bukkit.getPlayer(uuid);
        openMenuFromHistory(player, menuHistoryState.getMenuName(), menuHistoryState.getData());
    }

    public void loadMenuConfigs() {
        File menuFolder = new File(Civs.dataLocation, "menus");
        if (menuFolder.exists()) {
            menuFolder.mkdir();
        }
        File menuFile = new File(menuFolder, "default.yml");
        try {
            FileConfiguration config = FallbackConfigUtil.getConfig(menuFile, "menus/default.yml");
            backButton = new MenuIcon("back",
                    config.getString("back.icon", "REDSTONE_BLOCK"),
                    config.getString("back.name", "back-button"),
                    config.getString("back.desc", ""));
            prevButton = new MenuIcon("prev",
                    config.getString("prev.icon", "REDSTONE"),
                    config.getString("prev.name", "prev-button"),
                    config.getString("prev.desc", ""));
            nextButton = new MenuIcon("next",
                    config.getString("next.icon", "EMERALD"),
                    config.getString("next.name", "next-button"),
                    config.getString("next.desc", ""));
        } catch (Exception e) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to load menu default.yml");
            Civs.logger.severe(Arrays.toString(e.getStackTrace()));
            return;
        }

        Reflections reflections = new Reflections("org.redcastlemedia.multitallented.civs.menus");
        Set<Class<? extends CustomMenu>> menuClasses = reflections.getSubTypesOf(CustomMenu.class);
        for (Class<? extends CustomMenu> menuClass : menuClasses) {
            try {
                CustomMenu currentMenu = menuClass.getConstructor().newInstance();
                loadConfig(currentMenu);
                menus.put(menuClass.getAnnotation(CivsMenu.class).name(), currentMenu);
            } catch (Exception e) {
                Civs.logger.severe(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    private void loadConfig(CustomMenu customMenu) {
        String menuName = customMenu.getClass().getAnnotation(CivsMenu.class).name();
        File menuFolder = new File(Civs.dataLocation, "menus");
        File menuFile = new File(menuFolder, menuName + ".yml");

        FileConfiguration config = FallbackConfigUtil.getConfig(menuFile, "menus/" + menuName + ".yml");
        int newSize = config.getInt("size", 36);
        int size = MenuUtil.getInventorySize(newSize);
        String name = config.getString("name", "Unnamed");
        HashSet<MenuIcon> items = new HashSet<>();
        ConfigurationSection section = config.getConfigurationSection("items");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                MenuIcon menuIcon = new MenuIcon(key, config.getConfigurationSection("items." + key));
                if (menuIcon.getIndex().isEmpty() ||
                        menuIcon.getIndex().get(0) < 0) {
                    continue;
                }
                items.add(menuIcon);
            }
        }
        customMenu.loadConfig(items, size, name);
    }

    private static synchronized void clearCycleItems(UUID uuid) {
        cycleGuis.remove(uuid);
    }

    public void openMenuFromHistory(Player player, String menuName, Map<String, Object> data) {
        if (!menus.containsKey(menuName)) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (openMenus.containsKey(civilian.getUuid()) &&
                menuName.equals(openMenus.get(civilian.getUuid()))) {
            menus.get(menuName).onCloseMenu(civilian, player.getOpenInventory().getTopInventory());
            openMenus.remove(civilian.getUuid());
        }
        player.openInventory(menus.get(menuName).createMenuFromHistory(civilian, data));
        if (menus.get(menuName).cycleItems.containsKey(civilian.getUuid())) {
            cycleGuis.put(civilian.getUuid(), menus.get(menuName).cycleItems.get(civilian.getUuid()));
            menus.get(menuName).cycleItems.remove(civilian.getUuid());
        }
        openMenus.put(player.getUniqueId(), menuName);
        if (!history.containsKey(player.getUniqueId())) {
            history.put(player.getUniqueId(), new ArrayList<>());
        }
        MenuHistoryState menuHistoryState = new MenuHistoryState(menuName, getAllData(player.getUniqueId()));
        history.get(player.getUniqueId()).add(menuHistoryState);
    }

    public void openMenu(Player player, String menuName, Map<String, String> params) {
        if (!menus.containsKey(menuName)) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String redirectMenu = menus.get(menuName).beforeOpenMenu(civilian);
        if (redirectMenu != null) {
            openMenuFromString(civilian, redirectMenu);
            return;
        }
        if (openMenus.containsKey(civilian.getUuid()) &&
                menuName.equals(openMenus.get(civilian.getUuid()))) {
            menus.get(menuName).onCloseMenu(civilian, player.getOpenInventory().getTopInventory());
            openMenus.remove(civilian.getUuid());
        }
        player.openInventory(menus.get(menuName).createMenu(civilian, params));
        if (menus.get(menuName).cycleItems.containsKey(civilian.getUuid())) {
            cycleGuis.put(civilian.getUuid(), menus.get(menuName).cycleItems.get(civilian.getUuid()));
            menus.get(menuName).cycleItems.remove(civilian.getUuid());
        }
        openMenus.put(player.getUniqueId(), menuName);
        if (!history.containsKey(player.getUniqueId())) {
            history.put(player.getUniqueId(), new ArrayList<>());
        }
        MenuHistoryState menuHistoryState = new MenuHistoryState(menuName, getAllData(player.getUniqueId()));
        history.get(player.getUniqueId()).add(menuHistoryState);
    }

    public static void openMenuFromString(Civilian civilian, String menuString) {
        String[] menuSplit = menuString.split("\\?");
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Map<String, String> params = new HashMap<>();
        if (menuSplit.length > 1) {
            String[] queryString = menuSplit[1].split("&");
            for (String queryParams : queryString) {
                String[] splitParams = queryParams.split("=");
                params.put(splitParams[0], splitParams[1]);
            }
        }
        MenuManager.getInstance().openMenu(player, menuSplit[0], params);
    }

    public void refreshMenu(Civilian civilian) {
        if (!openMenus.containsKey(civilian.getUuid())) {
            return;
        }
        Player player = Bukkit.getPlayer(civilian.getUuid());
        String menuName = openMenus.get(civilian.getUuid());

        menus.get(menuName).onCloseMenu(civilian, player.getOpenInventory().getTopInventory());
        openMenus.remove(civilian.getUuid());
        player.openInventory(menus.get(menuName).createMenu(civilian));
        openMenus.put(civilian.getUuid(), menuName);
    }

    public static void addHistory(UUID uuid, CustomMenu customMenu, Map<String, Object> data) {
        if (!history.containsKey(uuid)) {
            history.put(uuid, new ArrayList<>());
        }
        String name = customMenu.getClass().getAnnotation(CivsMenu.class).name();
        MenuHistoryState menuHistoryState = new MenuHistoryState(name, data);
        history.get(uuid).add(menuHistoryState);
    }
    public static MenuHistoryState popLastMenu(UUID uuid) {
        if (!history.containsKey(uuid) ||
                history.get(uuid).isEmpty()) {
            return new MenuHistoryState("main", new HashMap<>());
        }
        MenuHistoryState menuHistoryState = history.get(uuid).get(history.get(uuid).size() - 1);
        history.get(uuid).remove(history.get(uuid).size() - 1);
        return menuHistoryState;
    }
    public static void clearHistory(UUID uuid) {
        history.remove(uuid);
    }
    public static Map<String, Object> getAllData(UUID uuid) {
        return data.get(uuid);
    }
    public static Object getData(UUID uuid, String key) {
        Map<String, Object> dataMap = data.get(uuid);
        if (dataMap == null) {
            return null;
        }
        return dataMap.get(key);
    }
    public static void putData(UUID uuid, String key, Object value) {
        data.get(uuid).put(key, value);
    }
    public static void setNewData(UUID uuid, Map<String, Object> newData) {
        data.put(uuid, newData);
    }
    public static void clearData(UUID uuid) {
        data.remove(uuid);
    }
}
