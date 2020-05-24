package org.redcastlemedia.multitallented.civs.spells;

import java.util.ArrayList;
import java.util.List;

import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
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

    public List<SpellType> getSpellsForSlot(CivClass selectedClass, int selectedSlot) {
        List<SpellType> spellTypeList = new ArrayList<>();
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(selectedClass.getType());

        for (String spellKey : classType.getSpellSlots().get(selectedSlot)) {
            if (spellKey.startsWith("g:")) {
                for (CivItem civItem : ItemManager.getInstance()
                        .getItemGroup(spellKey.replace("g:", ""))) {
                    spellTypeList.add((SpellType) civItem);
                }
            } else {
                spellTypeList.add((SpellType) ItemManager.getInstance().getItemType(spellKey));
            }
        }
        return spellTypeList;
    }

    public List<SpellType> getSpellsForClass(CivClass selectedClass) {
        List<SpellType> spellTypeList = new ArrayList<>();
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(selectedClass.getType());
        for (Integer i : classType.getSpellSlots().keySet()) {
            spellTypeList.addAll(getSpellsForSlot(selectedClass, i));
        }
        return spellTypeList;
    }
}
