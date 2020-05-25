package org.redcastlemedia.multitallented.civs.civclass;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.effects.RepairEffect;
import org.redcastlemedia.multitallented.civs.spells.SpellType;

import lombok.Getter;
import lombok.Setter;

public class CivClass {

    @Getter
    private final String type;
    @Getter
    private final UUID uuid;
    @Getter
    private final int id;
    @Setter
    private int manaPerSecond;
    @Setter
    private int maxMana;
    @Getter @Setter
    private boolean selectedClass;
    @Getter @Setter
    private int level = 0;
    @Getter
    private Map<Integer, String> selectedSpells = new HashMap<>();
    @Getter
    private Map<Integer, Integer> spellSlotOrder = new HashMap<>();

    public CivClass(int id, UUID uuid, String type) {
        this.id = id;
        this.uuid = uuid;
        this.type = type;
        this.manaPerSecond = -1;
        this.maxMana = -1;
    }

    public int getMaxMana() {
        if (maxMana == -1) {
            ClassType classType = (ClassType) ItemManager.getInstance().getItemType(type);
            return classType.getMaxMana();
        }
        return maxMana;
    }

    public int getManaPerSecond() {
        if (manaPerSecond == -1) {
            ClassType classType = (ClassType) ItemManager.getInstance().getItemType(type);
            return classType.getManaPerSecond();
        }
        return manaPerSecond;
    }

    public int getMaxEnchantLevel(Enchantment enchantment) {
        if (!RepairEffect.isCombatEnchantment(enchantment)) {
            return 999999;
        }
        return getAllowedLevel(enchantment.getKey().getKey());
    }

    public boolean isItemAllowed(Material material) {
        if (!RepairEffect.isArmor(material) && !RepairEffect.isWeapon(material)) {
            return true;
        }
        int level = getAllowedLevel(material.getKey().getKey().toUpperCase());
        return level > 0;
    }

    public int isPotionEffectAllowed(PotionEffectType potionEffectType) {
        if (potionEffectType == PotionEffectType.GLOWING) {
            return 999999;
        }
        return getAllowedLevel(potionEffectType.getName());
    }

    private int getAllowedLevel(String key) {
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(type);
        int level = classType.getAllowedActions().getOrDefault(key.toUpperCase(), -1);
        for (String spellName : selectedSpells.values()) {
            SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(spellName);
            level = Math.max(level, spellType.getAllowedActions().getOrDefault(key, -1));
        }
        return level;
    }

    public void resetSpellSlotOrder() {
        for (int i = 1; i < 7; i++) {
            spellSlotOrder.put(i, i);
        }
    }
}
