package org.redcastlemedia.multitallented.civs.civclass;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.io.File;
import java.io.IOException;
import java.util.*;

@CivsSingleton
public class ClassManager {
    private static ClassManager classManager = null;

    public ClassManager() {
        this.classManager = this;
        loadClasses();
    }

    void loadClasses() {
        File classFolder = new File(Civs.dataLocation, "class-data");
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
                    int manaPerSecond = classConfig.getInt("mana-per-second", 1);
                    int maxMana = classConfig.getInt("max-mana", 100);

                    Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
                    CivClass civClass = new CivClass(id, uuid, className, manaPerSecond, maxMana);
                    if (classConfig.getBoolean("selected", false)) {
                        civClass.setSelectedClass(true);
                        civilian.setCurrentClass(civClass);
                    }
                    civilian.getCivClasses().add(civClass);
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
        try {
            for (Civilian civilian : CivilianManager.getInstance().getCivilians()) {
                if (civilian.getCurrentClass() == null) {
                    if (civilian.getCivClasses().isEmpty()) {
                        civilian.setCurrentClass(createDefaultClass(civilian.getUuid()));
                    } else {
                        civilian.setCurrentClass(civilian.getCivClasses().iterator().next());
                    }
                }
            }
        } catch (Exception e) {
            Civs.logger.severe("Unable to set currentClass for civilians");
        }
    }
    public void saveClass(CivClass civClass) {
        File classFolder = new File(Civs.dataLocation, "class-data");
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
            config.set("mana-per-second", civClass.getManaPerSecond());
            config.set("max-mana", civClass.getMaxMana());
            config.set("selected", civClass.isSelectedClass());
            for (Map.Entry<Integer, String> entry : civClass.getSelectedSpells().entrySet()) {
                config.set("spells." + entry.getKey(), entry.getValue());
            }

            config.save(classFile);
        } catch (Exception e) {
            Civs.logger.severe("Unable to save class file " + civClass.getId() + ".yml");
            return;
        }
    }
    public int getNextId() {
        int i=0;
        File classFolder = new File(Civs.dataLocation, "class-data");
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

    public CivClass createDefaultClass(UUID uuid) {
        String className = ConfigManager.getInstance().getDefaultClass();
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(className);
        if (classType == null) {
            classType = new ClassType(new ArrayList<>(),
                    "default",
                    CVItem.createCVItemFromString("STONE"),
                    CVItem.createCVItemFromString("STONE"),
                    0,
                    "",
                    new ArrayList<>(),
                    new ArrayList<>(),
                    5, 100, true, 1);
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
