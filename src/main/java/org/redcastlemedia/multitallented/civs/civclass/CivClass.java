package org.redcastlemedia.multitallented.civs.civclass;


import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.effects.RepairEffect;
import org.redcastlemedia.multitallented.civs.spells.SpellType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CivClass {

    @Getter
    private final String type;
    @Getter
    private final UUID uuid;
    @Deprecated
    private int id;
    private UUID classId = UUID.randomUUID();
    @Setter
    private int manaPerSecond;
    @Setter
    private int maxMana;
    @Getter @Setter
    private boolean selectedClass;
    @Getter @Setter
    private int level = 0;
    @Getter
    private final Map<Integer, String> selectedSpells = new HashMap<>();
    @Getter
    private final Map<Integer, Integer> spellSlotOrder = new HashMap<>();

    public CivClass(UUID id, UUID uuid, String type) {
        this.classId = id;
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

    @NotNull
    public UUID getId() {
        if (this.classId == null) {
            this.classId = UUID.randomUUID();
        }
        return this.classId;
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
        return getAllowedLevel(enchantment.getKey().getKey().toUpperCase());
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
        return getAllowedLevel(potionEffectType.getName().toUpperCase());
    }

    private int getAllowedLevel(String key) {
        if (!ConfigManager.getInstance().getUseClassesAndSpells()) {
            return 999999;
        }
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(type);
        int allowedLevel = classType.getAllowedActions().getOrDefault(key, -1);
        for (String spellName : selectedSpells.values()) {
            SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(spellName);
            int spellLevel = spellType.getAllowedActions().getOrDefault(key, -1);
            allowedLevel = Math.max(allowedLevel, spellLevel);
        }
        return allowedLevel;
    }

    public void resetSpellSlotOrder() {
        for (int i = 1; i < 7; i++) {
            spellSlotOrder.put(i, i);
        }
    }
}
