package org.redcastlemedia.multitallented.civs.civclass;

import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;

import java.io.File;
import java.util.*;

public class ClassManager {
    private static ClassManager classManager = null;
    private HashMap<UUID, Set<CivClass>> civClasses = new HashMap<>();

    public ClassManager() {
        this.classManager = this;
        loadClasses();
    }

    void loadClasses() {
        //TODO load civ classes from storage
    }
    public void addClass(CivClass civClass) {
        if (civClasses.get(civClass.getUuid()) == null) {
            civClasses.put(civClass.getUuid(), new HashSet<CivClass>());
        }
        civClasses.get(civClass.getUuid()).add(civClass);
        saveClass(civClass);
    }
    public void saveClass(CivClass civClass) {
        //TODO finish this stub
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
    public CivClass createDefaultClass(UUID uuid) {
        return new CivClass(getNextId(), uuid, ConfigManager.getInstance().getDefaultClass());
    }

    public static ClassManager getInstance() {
        if (classManager == null) {
            classManager = new ClassManager();
        }
        return classManager;
    }
}
