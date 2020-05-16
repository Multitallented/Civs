package org.redcastlemedia.multitallented.civs.civclass;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
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
    @Getter @Setter
    private int manaPerSecond;
    @Getter @Setter
    private int maxMana;
    @Getter
    private Map<Integer, String> selectedSpells = new HashMap<>();

    public CivClass(int id, UUID uuid, String type, int manaPerSecond, int maxMana) {
        this.id = id;
        this.uuid = uuid;
        this.type = type;
        this.manaPerSecond = manaPerSecond;
        this.maxMana = maxMana;
    }

    public int getMaxEnchantLevel(Enchantment enchantment) {
        return getAllowedLevel(enchantment.getKey().getKey());
    }

    public boolean isItemAllowed(Material material) {
        int level = getAllowedLevel(material.getKey().getKey());
        return level > 0;
    }

    private int getAllowedLevel(String key) {
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(type);
        int level = classType.getAllowedActions().getOrDefault(key, -1);
        for (String spellName : selectedSpells.values()) {
            SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(spellName);
            level = Math.max(level, spellType.getAllowedActions().getOrDefault(key, -1));
        }
        return level;
    }
}
