package org.redcastlemedia.multitallented.civs.menus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.alliance.AllianceMenu;

public class MenuManager {
    private static MenuManager instance = null;
    private static HashMap<UUID, HashMap<String, Object>> data = new HashMap<>();
    private static HashMap<UUID, ArrayList<String>> history = new HashMap<>();
    private static HashMap<UUID, HashMap<ItemStack, String>> menuActionKeys = new HashMap<>();
    private static HashMap<String, CustomMenu> menus = new HashMap<>();

    private MenuIcon backButton;
    private MenuIcon prevButton;
    private MenuIcon nextButton;

    public MenuManager() {
        instance = this;
    }
    public static MenuManager getInstance() {
        if (instance != null) {
            new MenuManager();
        }
        return instance;
    }

    public void loadMenuConfigs() {
        File menuFolder = new File(Civs.getInstance().getDataFolder(), "menus");
        if (menuFolder.exists()) {
            menuFolder.mkdir();
        }
        File menuFile = new File(menuFolder, "default.yml");
        if (menuFile.exists()) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to load menu default.yml");
            return;
        }
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(menuFile);
            backButton = new MenuIcon("back", config.getConfigurationSection("back"));
            prevButton = new MenuIcon("prev", config.getConfigurationSection("prev"));
            nextButton = new MenuIcon("next", config.getConfigurationSection("next"));
        } catch (Exception e) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to load menu default.yml");
            return;
        }

        AllianceMenu allianceMenu = new AllianceMenu();
        loadConfig(allianceMenu);
        menus.put(allianceMenu.getKey(), allianceMenu);
    }

    private void loadConfig(CustomMenu customMenu) {
        File menuFolder = new File(Civs.getInstance().getDataFolder(), "menus");
        if (menuFolder.exists()) {
            menuFolder.mkdir();
        }
        File menuFile = new File(menuFolder, customMenu.getFileName() + ".yml");
        if (menuFile.exists()) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to load menu " + customMenu.getFileName());
            return;
        }

        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(menuFile);
        } catch (Exception e) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to load menu " + customMenu.getFileName());
        }
        int size = -1;
        int newSize = config.getInt("size", 36);
        size = getInventorySize(newSize);
        HashMap<Integer, MenuIcon> items = new HashMap<>();
        for (String key : config.getConfigurationSection("items").getKeys(false)) {
            MenuIcon menuIcon = new MenuIcon(key, config.getConfigurationSection("items." + key));
            if (menuIcon.getIndex().isEmpty() ||
                    menuIcon.getIndex().get(0) < 0) {
                continue;
            }
            for (Integer i : menuIcon.getIndex()) {
                items.put(i, menuIcon);
            }
        }
        customMenu.loadConfig(items, size);
    }

    public ItemStack getBackButton(Civilian civilian) {
        CVItem backButton = this.backButton.createCVItem(civilian.getLocale());
        if (history.get(civilian.getUuid()) == null) {
            history.put(civilian.getUuid(), new ArrayList<>());
        }
        return backButton.createItemStack();
    }

    public static int getInventorySize(int count) {
        int size = 9;
        if (count > size) {
            size = count + 9 - (count % 9);
            if (count % 9 == 0) {
                size -= 9;
            }
        }
        return Math.min(size, 54);
    }
    public static Object getData(UUID uuid, String key) {
        HashMap<String, Object> dataMap = data.get(uuid);
        if (dataMap == null) {
            return null;
        }
        return dataMap.get(key);
    }
    public static void setNewData(UUID uuid, HashMap<String, Object> newData) {
        data.put(uuid, newData);
    }
    public static void clearData(UUID uuid) {
        data.remove(uuid);
    }
}
