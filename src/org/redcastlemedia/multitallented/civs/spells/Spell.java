package org.redcastlemedia.multitallented.civs.spells;

import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.spells.conditions.Condition;
import org.redcastlemedia.multitallented.civs.spells.effects.Effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Spell {
    public boolean preCast = true;
    private String type;
    public final HashMap<Integer, Integer> pendingConditionsSize = new HashMap<>();
    public final HashMap<Integer, HashSet<SpellResult>> pendingConditions = new HashMap<>();
    private HashMap<String, HashSet<Object>> targetMap;

    public Spell(String type) {
        this.type = type;
    }

    public void checkConditions() {
        SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(type);

        ArrayList<HashMap<Condition, String>> conditions = preCast ? spellType.getPreCastConditions() : spellType.getPostCastConditions();

        for (int i=0; i<conditions.size(); i++) {
            HashMap<Condition, String> currConditions = conditions.get(i);
            pendingConditionsSize.put(i, currConditions.size());
            for (Condition con : currConditions.keySet()) {
                con.testCondition(targetMap.get(currConditions.get(con)));
            }
        }
    }


    public void checkInCondition(int index, SpellResult result) {
        if (result != SpellResult.FAILED) {
            if (pendingConditionsSize.get(index) <= pendingConditions.get(index).size()) {
                executeEffects(index);
            } else {
                return;
            }
        } else if (!preCast) {
            return;
        } else {
            int difference = pendingConditionsSize.get(index) - pendingConditions.get(index).size();
            for (int i=0; i<difference ; i++) {
                pendingConditions.get(index).add(SpellResult.FAILED);
            }
        }
        for (int i : pendingConditions.keySet()) {
            if (pendingConditionsSize.get(i) > pendingConditions.get(i).size()) {
                return;
            }
        }
        pendingConditions.clear();
        pendingConditionsSize.clear();
        preCast = false;
        //TODO announce the skill cast?
        checkConditions();
    }

    private void executeEffects(int index) {
        SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(type);
        ArrayList<HashMap<Effect, String>> effects = preCast ? spellType.getPreCastEffects() : spellType.getPostCastEffects();
        for (Effect effect : effects.get(index).keySet()) {
            effect.execute(targetMap.get(effects.get(index).get(effect)));
        }
    }
}
