package org.redcastlemedia.multitallented.civs.civclass;

import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@CivsSingleton
public class ClassManager {
    private static ClassManager classManager = null;

    public ClassManager() {
        this.classManager = this;
        loadClasses();
    }

    public void reload() {
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
                    CivClass civClass = new CivClass(id, uuid, className);
                    civClass.setManaPerSecond(manaPerSecond);
                    civClass.setMaxMana(maxMana);
                    if (classConfig.getBoolean("selected", false)) {
                        civClass.setSelectedClass(true);
                        civilian.setCurrentClass(civClass);
                    }
                    civClass.setLevel(classConfig.getInt("level", 0));
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
                        civilian.setCurrentClass(createDefaultClass(civilian.getUuid(), civilian.getLocale()));
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
            config.set("uuid", civClass.getUuid().toString());
            config.set("mana-per-second", civClass.getManaPerSecond());
            config.set("max-mana", civClass.getMaxMana());
            config.set("selected", civClass.isSelectedClass());
            config.set("level", civClass.getLevel());
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

    public CivClass createDefaultClass(UUID uuid, String locale) {
        String className = ConfigManager.getInstance().getDefaultClass();
        CivClass civClass = new CivClass(getNextId(), uuid, className);
        civClass.resetSpellSlotOrder();
        return civClass;
    }

    public static ClassManager getInstance() {
        if (classManager == null) {
            classManager = new ClassManager();
        }
        return classManager;
    }

    public List<ClassType> getUnlockedClasses(Civilian civilian) {
        List<ClassType> unlockedClasses = new ArrayList<>();
        for (CivItem civItem : ItemManager.getInstance().getAllItemTypes().values()) {
            if (civItem.getItemType() != CivItem.ItemType.CLASS ||
                    !ItemManager.getInstance().hasItemUnlocked(civilian, civItem)) {
                continue;
            }
            unlockedClasses.add((ClassType) civItem);
        }
        return unlockedClasses;
    }

    public void loadPlayer(Player player, Civilian civilian) {
        CivClass civClass = civilian.getCurrentClass();
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civClass.getType());
        player.setHealthScale(20);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(classType.getMaxHealth());
    }

    public void deleteClass(CivClass civClass) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(civClass.getUuid());
        civilian.getCivClasses().remove(civClass);
        if (civilian.getCurrentClass().equals(civClass)) {
            civilian.setCurrentClass(civilian.getCivClasses().iterator().next());
        }
        File classFolder = new File(Civs.dataLocation, "class-data");
        if (!classFolder.exists()) {
            classFolder.mkdir();
        }
        File classFile = new File(classFolder, civClass.getId() + ".yml");
        if (classFile.exists()) {
            if (!classFile.delete()) {
                Civs.logger.log(Level.SEVERE, "Unable to delete class {0}", classFile.getName());
            }
        }
    }

    public void createNewClass(Civilian civilian, ClassType classType) {
        CivClass civClass = new CivClass(getNextId(), civilian.getUuid(), classType.getProcessedName());
        civClass.resetSpellSlotOrder();
        civilian.getCurrentClass().setSelectedClass(false);
        civClass.setSelectedClass(true);
        civilian.setCurrentClass(civClass);
        civilian.getCivClasses().add(civClass);
        saveClass(civClass);
    }
}
