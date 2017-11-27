package org.redcastlemedia.multitallented.civs.civclass;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClassManager {
    private static ClassManager classManager = null;
    private HashMap<UUID, Set<CivClass>> civClasses = new HashMap<>();

    public ClassManager() {
        this.classManager = this;
        loadClasses();
    }

    void loadClasses() {
        if (Civs.getInstance() == null) {
            return;
        }
        File classFolder = new File(Civs.getInstance().getDataFolder(), "class-data");
        if (!classFolder.exists()) {
            classFolder.mkdir();
        }
        try {
            for (File file : classFolder.listFiles()) {
                try {
                    FileConfiguration classConfig = new YamlConfiguration();
                    classConfig.load(file);
                    int id = classConfig.getInt("id");
                    UUID uuid = UUID.fromString(classConfig.getString("uuid"));
                    String className = classConfig.getString("type");
                    if (!civClasses.containsKey(uuid)) {
                        civClasses.put(uuid, new HashSet<CivClass>());
                    }
                    int manaPerSecond = classConfig.getInt("mana-per-second", 1);
                    int maxMana = classConfig.getInt("max-mana", 100);

                    civClasses.get(uuid).add(new CivClass(id, uuid, className, manaPerSecond, maxMana));
                } catch (Exception ex) {
                    Civs.logger.severe("Unable to load " + file.getName());
                    ex.printStackTrace();
                    continue;
                }
            }
        } catch (Exception e) {
            Civs.logger.severe("Unable to load class files");
            return;
        }
    }
    public void addClass(CivClass civClass) {
        if (civClasses.get(civClass.getUuid()) == null) {
            civClasses.put(civClass.getUuid(), new HashSet<CivClass>());
        }
        civClasses.get(civClass.getUuid()).add(civClass);
        saveClass(civClass);
    }
    public void saveClass(CivClass civClass) {
        if (Civs.getInstance() == null) {
            return;
        }
        File classFolder = new File(Civs.getInstance().getDataFolder(), "class-data");
        if (!classFolder.exists()) {
            classFolder.mkdir();
        }
        File classFile = new File(classFolder, civClass.getId() + ".yml");
        if (!classFile.exists()) {
            try {
                classFile.createNewFile();
            } catch (IOException io) {
                Civs.logger.severe("Unable to create class file " + civClass.getId() + ".yml");
                return;
            }
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.set("id", civClass.getId());
            config.set("type", civClass.getType());
            config.set("uuid", civClass.getUuid());
            //TODO save attributes here as needed

            config.save(classFile);
        } catch (Exception e) {
            Civs.logger.severe("Unable to save class file " + civClass.getId() + ".yml");
            return;
        }
    }
    public int getNextId() {
        int i=0;
        if (Civs.getInstance() == null) {
            return 0;
        }
        File classFolder = new File(Civs.getInstance().getDataFolder(), "class-data");
        if (!classFolder.exists()) {
            return 0;
        }
        File classFile = new File(classFolder, i + ".yml");
        while(classFile.exists()) {
            i++;
            classFile = new File(classFolder, i + ".yml");
        }
        return i;
    }

    public CivClass getCivClass(UUID uuid, String type) {
        if (civClasses.get(uuid) == null) {
            return null; //TODO perhaps fix this to be a default class?
        }
        for (CivClass civClass : civClasses.get(uuid)) {
            if (civClass.getType().equals(type)) {
                return civClass;
            }
        }
        return null;
    }
    public CivClass getCivClass(UUID uuid, int id) {
        if (civClasses.get(uuid) == null) {
            return null;
        }
        for (CivClass civClass : civClasses.get(uuid)) {
            if (civClass.getId() == id) {
                return civClass;
            }
        }
        return null;
    }
    public CivClass createClass(UUID uuid, String type, int manaPerSecond, int maxMana) {
        return new CivClass(getNextId(), uuid, type, manaPerSecond, maxMana);
    }
    public CivClass createDefaultClass(UUID uuid) {
        String className = ConfigManager.getInstance().getDefaultClass();
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(className);
        if (classType == null) {
            classType = new ClassType(new ArrayList<String>(),
                    "default",
                    CVItem.createCVItemFromString("STONE"),
                    0,
                    "",
                    new ArrayList<String>(),
                    new HashMap<String, String>(),
                    new ArrayList<String>(),
                    5, 100);
        }
        return new CivClass(getNextId(), uuid, className, classType.getManaPerSecond(), classType.getMaxMana());
    }

    public static ClassManager getInstance() {
        if (classManager == null) {
            classManager = new ClassManager();
        }
        return classManager;
    }
}
