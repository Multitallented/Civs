package org.redcastlemedia.multitallented.civs.spells;

import java.util.HashMap;
import java.util.HashSet;

public class SpellManager {
    private static SpellManager spellManager = null;

    public static SpellManager getInstance() {
        if (spellManager == null) {
            spellManager = new SpellManager();
        }
        return spellManager;
    }

    public void createSpell(String name, HashMap<String, HashSet<Object>> targetMap) {
        Spell spell = new Spell(name);
        spell.checkConditions();
    }
}
