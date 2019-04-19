package org.redcastlemedia.multitallented.civs;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemMetaImpl implements ItemMeta {

    private String displayName = null;
    private List<String> lore = new ArrayList<>();
    public ItemMetaImpl() {

    }
    public ItemMetaImpl(String displayName, List<String> lore) {
        this.displayName = displayName;
        this.lore = lore;
    }

    @Override
    public boolean hasDisplayName() {
        return displayName == null;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String s) {
        this.displayName = s;
    }

    @Override
    public boolean hasLocalizedName() {
        return false;
    }

    @Override
    public String getLocalizedName() {
        return null;
    }

    @Override
    public void setLocalizedName(String s) {

    }

    @Override
    public boolean hasLore() {
        return !lore.isEmpty();
    }

    @Override
    public List<String> getLore() {
        return lore;
    }

    @Override
    public void setLore(List<String> list) {
        this.lore = list;
    }

    @Override
    public boolean hasEnchants() {
        return false;
    }

    @Override
    public boolean hasEnchant(Enchantment enchantment) {
        return false;
    }

    @Override
    public int getEnchantLevel(Enchantment enchantment) {
        return 0;
    }

    @Override
    public Map<Enchantment, Integer> getEnchants() {
        return null;
    }

    @Override
    public boolean addEnchant(Enchantment enchantment, int i, boolean b) {
        return false;
    }

    @Override
    public boolean removeEnchant(Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean hasConflictingEnchant(Enchantment enchantment) {
        return false;
    }

    @Override
    public void addItemFlags(ItemFlag... itemFlags) {

    }

    @Override
    public void removeItemFlags(ItemFlag... itemFlags) {

    }

    @Override
    public Set<ItemFlag> getItemFlags() {
        return null;
    }

    @Override
    public boolean hasItemFlag(ItemFlag itemFlag) {
        return false;
    }

    @Override
    public boolean isUnbreakable() {
        return false;
    }

    @Override
    public void setUnbreakable(boolean b) {

    }
    @Override
    public ItemMetaImpl clone() {
        return new ItemMetaImpl(displayName, lore);
    }

    @Override
    public Spigot spigot() {
        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
