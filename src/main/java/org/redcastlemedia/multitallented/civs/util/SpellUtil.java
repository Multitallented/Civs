package org.redcastlemedia.multitallented.civs.util;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.spells.SpellType;

public final class SpellUtil {
    private SpellUtil() {

    }

    public static void enableCombatBar(Player player, Civilian civilian) {
        for (Map.Entry<Integer, String> entry : civilian.getCurrentClass().getSelectedSpells().entrySet()) {
            SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(entry.getValue());
            int index = civilian.getCurrentClass().getSpellSlotOrder().get(entry.getKey());
            civilian.getCombatBar().put(index, player.getInventory().getItem(index - 1));
            CVItem cvItem = spellType.clone();
            cvItem.getLore().clear();
            cvItem.getLore().add(ChatColor.BLACK + civilian.getUuid().toString());
            cvItem.getLore().add(ChatColor.BLACK + spellType.getProcessedName());
            cvItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "switch-spell-cast")));

            player.getInventory().getContents()[index] = cvItem.createItemStack();
        }
    }

    public static void removeCombatBar(Player player, Civilian civilian) {
        CivClass civClass = civilian.getCurrentClass();
        for (Map.Entry<Integer, Integer> entry : civClass.getSpellSlotOrder().entrySet()) {
            if (civClass.getSelectedSpells().containsKey(entry.getKey())) {
                player.getInventory().getContents()[entry.getValue() - 1] =
                        civilian.getCombatBar().get(entry.getValue());
            }
        }
        civilian.getCombatBar().clear();
    }
}
