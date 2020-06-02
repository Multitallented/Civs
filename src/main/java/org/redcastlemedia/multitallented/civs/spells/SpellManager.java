package org.redcastlemedia.multitallented.civs.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

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

    public static void removePassiveSpell(Civilian civilian, Player player, String spellName) {
        SpellType oldSpell = (SpellType) ItemManager.getInstance().getItemType(spellName);
        if (oldSpell != null && oldSpell.getConfig() != null && oldSpell.getConfig().isSet("passives")){
            for (Map.Entry<String, CivState> entry : new HashSet<>(civilian.getStates().entrySet())) {
                String currentAbilityName = entry.getKey().split("\\.")[0];
                if (oldSpell.getProcessedName().equalsIgnoreCase(currentAbilityName)) {
                    civilian.getStates().remove(entry.getKey());
                    entry.getValue().remove(player);
                }
            }
        }
    }

    public static void removePassiveSpells(Civilian civilian) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        for (Map.Entry<String, CivState> entry : new HashSet<>(civilian.getStates().entrySet())) {
            String currentAbilityName = entry.getKey().split("\\.")[0];
            SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(currentAbilityName);
            if (spellType.getConfig().isSet("passives")) {
                civilian.getStates().remove(entry.getKey());
                entry.getValue().remove(player);
            }
        }
    }

    public static void initPassiveSpell(Civilian civilian, SpellType spellType, Player player) {
        if (spellType != null && spellType.getConfig() != null && spellType.getConfig().isSet("passives")) {
            Spell spell = new Spell(spellType.getProcessedName(),
                    Bukkit.getPlayer(civilian.getUuid()), civilian.getLevel(spellType));
            Map<String, Set<?>> mappedTargets = new HashMap<>();
            Spell.addSelfToTargetMapping(mappedTargets, player);
            spell.useAbility(mappedTargets, true,
                    spellType.getConfig().getConfigurationSection("passives"));
        }
    }
}
