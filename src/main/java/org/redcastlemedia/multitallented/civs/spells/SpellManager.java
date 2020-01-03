package org.redcastlemedia.multitallented.civs.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class SpellManager {
    private static SpellManager spellManager = null;

    public static SpellManager getInstance() {
        if (spellManager == null) {
            spellManager = new SpellManager();
        }
        return spellManager;
    }

//    private HashMap<UUID, ArrayList<Spell>> spells = new HashMap<>();
//
//    public void putSpell(UUID uuid, Spell spell) {
//        if (!spells.containsKey(uuid)) {
//            spells.put(uuid, new ArrayList<Spell>());
//        }
//        spells.get(uuid).add(spell);
//    }
}
