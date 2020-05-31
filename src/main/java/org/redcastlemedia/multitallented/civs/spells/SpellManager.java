package org.redcastlemedia.multitallented.civs.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;

public class SpellManager {
    private static SpellManager spellManager = null;

    public static SpellManager getInstance() {
        if (spellManager == null) {
            spellManager = new SpellManager();
        }
        return spellManager;
    }

    public List<SpellType> getSpellsForSlot(CivClass selectedClass, int selectedSlot, boolean checkUnlock) {
        List<SpellType> spellTypeList = new ArrayList<>();
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(selectedClass.getType());

        Civilian civilian = CivilianManager.getInstance().getCivilian(selectedClass.getUuid());
        List<String> spells = classType.getSpellSlots().get(selectedSlot);
        if (spells != null) {
            for (String spellKey : spells) {
                try {
                    if (spellKey != null && spellKey.startsWith("g:")) {
                        for (CivItem civItem : ItemManager.getInstance()
                                .getItemGroup(spellKey.replace("g:", ""))) {
                            if (!(civItem instanceof SpellType)) {
                                continue;
                            }
                            SpellType spellType = (SpellType) civItem;
                            if (!checkUnlock || ItemManager.getInstance().hasItemUnlocked(civilian, spellType)) {
                                spellTypeList.add(spellType);
                            }
                        }
                    } else if (spellKey != null) {
                        SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(spellKey);
                        if (spellType != null &&
                                (!checkUnlock || ItemManager.getInstance().hasItemUnlocked(civilian, spellType))) {
                            spellTypeList.add(spellType);
                        }
                    }
                } catch (Exception e) {
                    Civs.logger.log(Level.SEVERE, "Unable to find spell " + spellKey, e);
                }
            }
        }
        return spellTypeList;
    }

    public List<SpellType> getSpellsForClass(CivClass selectedClass) {
        List<SpellType> spellTypeList = new ArrayList<>();
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(selectedClass.getType());
        for (Integer i : classType.getSpellSlots().keySet()) {
            spellTypeList.addAll(getSpellsForSlot(selectedClass, i, false));
        }
        return spellTypeList;
    }
}
