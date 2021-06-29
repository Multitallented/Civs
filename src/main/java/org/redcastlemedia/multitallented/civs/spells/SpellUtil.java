package org.redcastlemedia.multitallented.civs.spells;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public final class SpellUtil {
    private SpellUtil() {

    }

    public static void enableCombatBar(Player player, Civilian civilian) {
        for (Map.Entry<Integer, String> entry : civilian.getCurrentClass().getSelectedSpells().entrySet()) {
            int index = entry.getKey();
            SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(entry.getValue());
            int mappedIndex = civilian.getCurrentClass().getSpellSlotOrder()
                    .getOrDefault(index, index);
            ItemStack itemStack = player.getInventory().getItem(mappedIndex - 1);
            if (itemStack == null) {
                itemStack = new ItemStack(Material.AIR);
            }
            civilian.getCombatBar().put(index, itemStack);
            CVItem cvItem = spellType.clone();
            String localSpellName = spellType.getDisplayName(player);
            cvItem.setDisplayName(localSpellName);
            cvItem.getLore().clear();
            cvItem.getLore().add(ChatColor.BLACK + civilian.getUuid().toString());
            cvItem.getLore().add(ChatColor.BLACK + spellType.getProcessedName());
            cvItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    "switch-spell-cast")));

            player.getInventory().setItem(mappedIndex - 1, cvItem.createItemStack());
        }
    }

    public static void removeCombatBar(Player player, Civilian civilian) {
        CivClass civClass = civilian.getCurrentClass();
        for (Integer index : civClass.getSelectedSpells().keySet()) {
            int mappedIndex = civClass.getSpellSlotOrder().getOrDefault(index, index);
            ItemStack itemStack = civilian.getCombatBar().getOrDefault(index, new ItemStack(Material.AIR));
            if (CVItem.isCivsItem(itemStack)) {
                CivItem civItem = CivItem.getFromItemStack(itemStack);
                if (civItem != null && civItem.getItemType() == CivItem.ItemType.SPELL) {
                    itemStack = new ItemStack(Material.AIR);
                }
            }
            ItemStack currentItem = player.getInventory().getItem(mappedIndex - 1);
            if (CVItem.isCivsItem(currentItem)) {
                player.getInventory().setItem(mappedIndex - 1, itemStack);
            }
        }
        civilian.getCombatBar().clear();
    }
}
