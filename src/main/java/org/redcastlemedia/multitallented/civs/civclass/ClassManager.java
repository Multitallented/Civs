package org.redcastlemedia.multitallented.civs.civclass;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
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
import org.redcastlemedia.multitallented.civs.spells.SpellManager;
import org.redcastlemedia.multitallented.civs.spells.SpellType;
import org.redcastlemedia.multitallented.civs.spells.SpellUtil;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

@CivsSingleton
public class ClassManager {
    private static ClassManager classManager = null;
    private static Map<UUID, Set<CivClass>> classes = new HashMap<>();

    public ClassManager() {
        this.classManager = this;
        loadClasses();
    }

    public void reload() {
        loadClasses();
    }

    void loadClasses() {
        if (Civs.getInstance() == null) {
            return;
        }
        File classFolder = new File(Civs.dataLocation, "class-data");
        if (!classFolder.exists()) {
            classFolder.mkdir();
        }
        try {
            for (File file : classFolder.listFiles()) {
                try {
                    FileConfiguration classConfig = new YamlConfiguration();
                    classConfig.load(file);
                    UUID classId;
                    if (classConfig.isSet("classId")) {
                        classId = UUID.fromString(classConfig.getString("classId"));
                    } else {
                        classId = UUID.randomUUID();
                    }
                    UUID uuid = UUID.fromString(classConfig.getString("uuid"));
                    String className = classConfig.getString("type");
                    int manaPerSecond = classConfig.getInt("mana-per-second", 1);
                    int maxMana = classConfig.getInt("max-mana", 100);

                    CivClass civClass = new CivClass(classId, uuid, className);
                    civClass.setManaPerSecond(manaPerSecond);
                    civClass.setMaxMana(maxMana);
                    if (classConfig.getBoolean("selected", false)) {
                        civClass.setSelectedClass(true);
                    }
                    civClass.setLevel(classConfig.getInt("level", 0));
                    if (!classes.containsKey(uuid)) {
                        classes.put(uuid, new HashSet<>());
                    }
                    if (classConfig.isSet("slots")) {
                        for (String key : classConfig.getConfigurationSection("slots").getKeys(false)) {
                            int index = Integer.parseInt(key);
                            int mappedIndex = classConfig.getInt("slots." + key);
                            civClass.getSpellSlotOrder().put(index, mappedIndex);
                        }
                    }
                    if (classConfig.isSet("spells")) {
                        for (String key : classConfig.getConfigurationSection("spells").getKeys(false)) {
                            int index = Integer.parseInt(key);
                            String mappedSpell = classConfig.getString("spells." + key);
                            civClass.getSelectedSpells().put(index, mappedSpell);
                        }
                    }
                    classes.get(uuid).add(civClass);
                } catch (Exception ex) {
                    Civs.logger.severe("Unable to load " + file.getName());
                    ex.printStackTrace();
                    continue;
                }
            }
        } catch (Exception e) {
            Civs.logger.log(Level.SEVERE, "Unable to load class files", e);
            return;
        }
        try {
            for (Civilian civilian : CivilianManager.getInstance().getCivilians()) {
                if (civilian.getCurrentClass() == null) {
                    if (civilian.getCivClasses().isEmpty()) {
                        createDefaultClass(civilian.getUuid());
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
        if (!ConfigManager.getInstance().getUseClassesAndSpells() || Civs.getInstance() == null) {
            return;
        }
        File classFolder = new File(Civs.dataLocation, "class-data");
        if (!classFolder.exists()) {
            classFolder.mkdir();
        }
        File classFile = new File(classFolder, civClass.getId().toString() + ".yml");
        if (!classFile.exists()) {
            try {
                classFile.createNewFile();
            } catch (IOException io) {
                Civs.logger.severe("Unable to create class file " + civClass.getId().toString() + ".yml");
                return;
            }
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.set("id", civClass.getId().toString());
            config.set("type", civClass.getType());
            config.set("uuid", civClass.getUuid().toString());
            config.set("mana-per-second", civClass.getManaPerSecond());
            config.set("max-mana", civClass.getMaxMana());
            config.set("selected", civClass.isSelectedClass());
            for (Map.Entry<Integer, Integer> entry : civClass.getSpellSlotOrder().entrySet()) {
                config.set("slots." + entry.getKey(), entry.getValue());
            }
            for (Map.Entry<Integer, String> entry : civClass.getSelectedSpells().entrySet()) {
                config.set("spells." + entry.getKey(), entry.getValue());
            }
            config.set("level", civClass.getLevel());
            for (Map.Entry<Integer, String> entry : civClass.getSelectedSpells().entrySet()) {
                config.set("spells." + entry.getKey(), entry.getValue());
            }

            config.save(classFile);
        } catch (Exception e) {
            Civs.logger.severe("Unable to save class file " + civClass.getId().toString() + ".yml");
            return;
        }
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
        if (classes.containsKey(civilian.getUuid())) {
            for (CivClass civClass : classes.get(civilian.getUuid())) {
                if (civClass.isSelectedClass()) {
                    civilian.setCurrentClass(civClass);
                }
                civilian.getCivClasses().add(civClass);
            }
            if (civilian.getCurrentClass() == null) {
                ClassManager.getInstance().switchClass(civilian, civilian.getCivClasses().iterator().next());
            }
        } else {
            createDefaultClass(civilian.getUuid());
        }
        CivClass civClass = civilian.getCurrentClass();
        for (String spellName : civClass.getSelectedSpells().values()) {
            SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(spellName);
            SpellManager.initPassiveSpell(civilian, spellType, player);
        }
        if (ConfigManager.getInstance().getUseClassesAndSpells()) {
            ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civClass.getType());
            player.setHealthScale(20);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(classType.getMaxHealth());
        }
    }

    public void deleteClass(CivClass civClass) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(civClass.getUuid());
        for (CivClass civClass1 : new HashSet<>(civilian.getCivClasses())) {
            if (civClass1.getId() == civClass.getId()) {
                civilian.getCivClasses().remove(civClass1);
            }
        }
        civilian.getCivClasses().remove(civClass);
        if (civilian.getCurrentClass().equals(civClass)) {
            Player player = Bukkit.getPlayer(civClass.getUuid());
            if (!civilian.getCombatBar().isEmpty() && player != null) {
                SpellUtil.removeCombatBar(player, civilian);
            }
            CivClass newCurrentClass = civilian.getCivClasses().iterator().next();
            ClassManager.getInstance().switchClass(civilian, newCurrentClass);
        }
        CivilianManager.getInstance().saveCivilian(civilian);
        if (classes.containsKey(civilian.getUuid())) {
            classes.get(civilian.getUuid()).remove(civClass);
        }
        File classFolder = new File(Civs.dataLocation, "class-data");
        if (!classFolder.exists()) {
            classFolder.mkdir();
        }
        File classFile = new File(classFolder, civClass.getId().toString() + ".yml");
        if (classFile.exists()) {
            if (!classFile.delete()) {
                Civs.logger.log(Level.SEVERE, "Unable to delete class {0}", classFile.getName());
            }
        }
    }

    public CivClass createDefaultClass(UUID uuid) {
        String className = ConfigManager.getInstance().getDefaultClass();
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(className);
        return createNewClass(civilian, classType);
    }

    public CivClass createNewClass(Civilian civilian, ClassType classType) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return null;
        }
        if (!civilian.getCombatBar().isEmpty()) {
            SpellUtil.removeCombatBar(player, civilian);
        }
        CivClass civClass = new CivClass(UUID.randomUUID(), civilian.getUuid(), classType.getProcessedName());
        civClass.resetSpellSlotOrder();
        civClass.setMaxMana(classType.getMaxMana());
        civClass.setManaPerSecond(classType.getManaPerSecond());
        if (civilian.getRawCurrentClass() != null) {
            civilian.getRawCurrentClass().setSelectedClass(false);
            saveClass(civilian.getRawCurrentClass());
        }
        civClass.setSelectedClass(true);
        civilian.setCurrentClass(civClass);
        civilian.getCivClasses().add(civClass);
        if (!classes.containsKey(civilian.getUuid())) {
            classes.put(civilian.getUuid(), new HashSet<>());
        }
        classes.get(civilian.getUuid()).add(civClass);
        saveClass(civClass);
        return civClass;
    }

    public void switchClass(Civilian civilian, CivClass civClass) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return;
        }
        if (!civilian.getCombatBar().isEmpty()) {
            SpellUtil.removeCombatBar(player, civilian);
        }
        SpellManager.removePassiveSpells(civilian);
        civilian.setCurrentClass(civClass);
        for (CivClass civClass1 : civilian.getCivClasses()) {
            if (!civClass1.equals(civClass) && civClass1.isSelectedClass()) {
                civClass1.setSelectedClass(false);
                ClassManager.getInstance().saveClass(civClass1);
            }
        }
        civClass.setSelectedClass(true);
        ClassManager.getInstance().saveClass(civClass);
        ClassManager.getInstance().loadPlayer(player, civilian);
    }

    public void unloadPlayer(Player player) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (!civilian.getCombatBar().isEmpty()) {
            SpellUtil.removeCombatBar(player, civilian);
        }
        for (CivState state : new HashSet<>(civilian.getStates().values())) {
            state.remove(player);
        }
    }
}
