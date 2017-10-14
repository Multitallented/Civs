package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CivilianManager {

    private HashMap<UUID, Civilian> onlineCivilians = new HashMap<>();

    private static CivilianManager civilianManager = null;

    public CivilianManager() {
        civilianManager = this;
    }

    public static CivilianManager getInstance() {
        if (civilianManager == null) {
            civilianManager = new CivilianManager();
            return civilianManager;
        } else {
            return civilianManager;
        }
    }

    void loadCivilian(Player player) {
        Civilian civilian = loadFromFileCivilian(player.getUniqueId());
        onlineCivilians.put(player.getUniqueId(), civilian);
    }
    public void createDefaultCivilian(Player player) {
        onlineCivilians.put(player.getUniqueId(), createDefaultCivilian(player.getUniqueId()));
    }
    void unloadCivilian(Player player) {
        onlineCivilians.remove(player.getUniqueId());
    }
    public Civilian getCivilian(UUID uuid) {
        Civilian civilian = onlineCivilians.get(uuid);
        if (civilian == null) {
            civilian = loadFromFileCivilian(uuid);
        }
        return civilian;
    }
    private Civilian loadFromFileCivilian(UUID uuid) {
        Civs civs = Civs.getInstance();
        if (civs == null) {
            Civilian civilian = createDefaultCivilian(uuid);
            saveCivilian(civilian);
            return civilian;
        }
        File civilianFolder = new File(civs.getDataFolder(), "players");
        if (!civilianFolder.exists()) {
            Civilian civilian = createDefaultCivilian(uuid);
            saveCivilian(civilian);
            return civilian;
        }
        File civilianFile = new File(civilianFolder, uuid + ".yml");
        if (!civilianFile.exists()) {
            Civilian civilian = createDefaultCivilian(uuid);
            saveCivilian(civilian);
            return civilian;
        }
        FileConfiguration civConfig = new YamlConfiguration();
        try {
            civConfig.load(civilianFile);

            //TODO load other civilian file properties

            ItemManager itemManager = ItemManager.getInstance();
            ArrayList<CivItem> items = itemManager.loadCivItems(civConfig, uuid);
            Set<CivClass> classes = new HashSet<>();
            ClassManager classManager = ClassManager.getInstance();
            for (int id : civConfig.getIntegerList("classes")) {
                classes.add(classManager.getCivClass(uuid, id));
            }
            HashMap<CivItem, Integer> exp = new HashMap<>();
            ConfigurationSection section = civConfig.getConfigurationSection("exp");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    CivItem item = itemManager.getItemType(key);
                    if (item == null) {
                        continue;
                    }
                    exp.put(item, civConfig.getInt("exp." + key, 0));
                }
            }

            return new Civilian(uuid, civConfig.getString("locale"), items, classes, exp);
        } catch (Exception ex) {
            Civs.logger.severe("Unable to read/write " + uuid + ".yml");
            ex.printStackTrace();
            return createDefaultCivilian(uuid);
        }
    }
    Civilian createDefaultCivilian(UUID uuid) {
        ConfigManager configManager = ConfigManager.getInstance();
        CivClass defaultClass = ClassManager.getInstance().createDefaultClass(uuid);
        Set<CivClass> classes = new HashSet<CivClass>();
        classes.add(defaultClass);
        return new Civilian(uuid,
                configManager.getDefaultLanguage(),
                ItemManager.getInstance().getNewItems(),
                classes,
                new HashMap<CivItem, Integer>());
    }
    public void saveCivilian(Civilian civilian) {
        Civs civs = Civs.getInstance();
        if (civs == null) {
            return;
        }
        File civilianFolder = new File(civs.getDataFolder(), "players");
        if (!civilianFolder.exists()) {
            if (civilianFolder.mkdir()) {
                Civs.logger.severe("Unable to create players folder");
                return;
            }
        }
        File civilianFile = new File(civilianFolder, civilian.getUuid() + ".yml");
        if (!civilianFile.exists()) {
            try {
                civilianFile.createNewFile();
            } catch (IOException ioexception) {
                Civs.logger.severe("Unable to create " + civilian.getUuid() + ".yml");
                return;
            }
        }
        FileConfiguration civConfig = new YamlConfiguration();
        try {
            civConfig.load(civilianFile);

            civConfig.set("locale", civilian.getLocale());
            //TODO save other civilian file properties
            for (CivItem civItem : civilian.getStashItems()) {
                civConfig.set("items." + civItem.getDisplayName().replace("Civs ", "").toLowerCase(), civItem.getQty());
            }
            List<Integer> classes = new ArrayList<>();
            for (CivClass civClass : civilian.getCivClasses()) {
                classes.add(civClass.getId());
            }
            civConfig.set("classes", classes);
            for (CivItem item : civilian.getExp().keySet()) {
                int exp = civilian.getExp().get(item);
                if (exp < 1) {
                    continue;
                }
                civConfig.set("exp." + item.getProcessedName(), exp);
            }

            civConfig.save(civilianFile);
        } catch (Exception ex) {
            Civs.logger.severe("Unable to read/write " + civilian.getUuid() + ".yml");
            return;
        }
    }
}
