package org.redcastlemedia.multitallented.civs;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import com.google.common.collect.ImmutableMap;

public class ItemStackImpl extends ItemStack {
    private Material type;
    private int amount;
    private MaterialData data;
    private ItemMetaImpl meta = new ItemMetaImpl();

    protected ItemStackImpl() {
        this.type = Material.AIR;
        this.amount = 0;
        this.data = null;
    }

    public ItemStackImpl(Material type) {
        this(type, 1);
    }

    public ItemStackImpl(Material type, int amount) {
        this(type, amount, (short)0);
    }

    /** @deprecated */
    public ItemStackImpl(Material type, int amount, short damage) {
        this(type, amount, damage, (Byte)null);
    }

    /** @deprecated */
    @Deprecated
    public ItemStackImpl(Material type, int amount, short damage, Byte data) {
        this.type = Material.AIR;
        this.amount = 0;
        this.data = null;
        Validate.notNull(type, "Material cannot be null");
        this.type = type;
        this.amount = amount;
        if (damage != 0) {
            this.setDurability(damage);
        }

        if (data != null) {
            this.createData(data);
        }

    }

    public ItemStackImpl(ItemStackImpl stack) throws IllegalArgumentException {
        this.type = Material.AIR;
        this.amount = 0;
        this.data = null;
        Validate.notNull(stack, "Cannot copy null stack");
        this.type = stack.getType();
        this.amount = stack.getAmount();
        this.data = stack.getData();
        if (stack.hasItemMeta()) {
            this.setItemMeta0(stack.getItemMeta(), this.type);
        }

    }

    public Material getType() {
        return this.type;
    }

    public void setType(Material type) {
        Validate.notNull(type, "Material cannot be null");
        this.type = type;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public MaterialData getData() {
        Material mat = Bukkit.getUnsafe().toLegacy(this.getType());
        if (this.data == null && mat != null && mat.getData() != null) {
            this.data = mat.getNewData((byte)this.getDurability());
        }

        return this.data;
    }

    public void setData(MaterialData data) {
        Material mat = Bukkit.getUnsafe().toLegacy(this.getType());
        if (data != null && mat != null && mat.getData() != null) {
            if (data.getClass() != mat.getData() && data.getClass() != MaterialData.class) {
                throw new IllegalArgumentException("Provided data is not of type " + mat.getData().getName() + ", found " + data.getClass().getName());
            }

            this.data = data;
        } else {
            this.data = data;
        }

    }

    /** @deprecated */
    @Deprecated
    public void setDurability(short durability) {
        ItemMeta meta = this.getItemMeta();
        if (meta != null) {
            ((Damageable)meta).setDamage(durability);
            this.setItemMeta(meta);
        }

    }

    /** @deprecated */
    @Deprecated
    public short getDurability() {
        ItemMeta meta = this.getItemMeta();
        return meta == null ? 0 : (short)((Damageable)meta).getDamage();
    }

    public int getMaxStackSize() {
        Material material = this.getType();
        return material != null ? material.getMaxStackSize() : -1;
    }

    private void createData(byte data) {
        this.data = this.type.getNewData(data);
    }

    public String toString() {
        StringBuilder toString = (new StringBuilder("ItemStack{")).append(this.getType().name()).append(" x ").append(this.getAmount());
        if (this.hasItemMeta()) {
            toString.append(", ").append(this.getItemMeta());
        }

        return toString.append('}').toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof ItemStack)) {
            return false;
        } else {
            ItemStack stack = (ItemStack)obj;
            return this.getAmount() == stack.getAmount() && this.isSimilar(stack);
        }
    }

    public boolean isSimilar(ItemStack stack) {
        if (stack == null) {
            return false;
        } else if (stack == this) {
            return true;
        } else {
            Material comparisonType = Bukkit.getUnsafe().fromLegacy(this.getType());
            return comparisonType == stack.getType() && this.getDurability() == stack.getDurability() && this.hasItemMeta() == stack.hasItemMeta() && (!this.hasItemMeta() || Bukkit.getItemFactory().equals(this.getItemMeta(), stack.getItemMeta()));
        }
    }

    public ItemStackImpl clone() {
        ItemStackImpl itemStack = (ItemStackImpl)super.clone();
        if (this.meta != null) {
            itemStack.meta = this.meta.clone();
        }

        if (this.data != null) {
            itemStack.data = this.data.clone();
        }

        return itemStack;
    }

    public boolean containsEnchantment(Enchantment ench) {
        return this.meta == null ? false : this.meta.hasEnchant(ench);
    }

    public int getEnchantmentLevel(Enchantment ench) {
        return this.meta == null ? 0 : this.meta.getEnchantLevel(ench);
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return (Map)(this.meta == null ? ImmutableMap.of() : this.meta.getEnchants());
    }

    public void addEnchantments(Map<Enchantment, Integer> enchantments) {
        Validate.notNull(enchantments, "Enchantments cannot be null");
        Iterator var3 = enchantments.entrySet().iterator();

        while(var3.hasNext()) {
            Entry<Enchantment, Integer> entry = (Entry)var3.next();
            this.addEnchantment((Enchantment)entry.getKey(), (Integer)entry.getValue());
        }

    }

    public void addEnchantment(Enchantment ench, int level) {
        Validate.notNull(ench, "Enchantment cannot be null");
        if (level >= ench.getStartLevel() && level <= ench.getMaxLevel()) {
            if (!ench.canEnchantItem(this)) {
                throw new IllegalArgumentException("Specified enchantment cannot be applied to this itemstack");
            } else {
                this.addUnsafeEnchantment(ench, level);
            }
        } else {
            throw new IllegalArgumentException("Enchantment level is either too low or too high (given " + level + ", bounds are " + ench.getStartLevel() + " to " + ench.getMaxLevel() + ")");
        }
    }

    public void addUnsafeEnchantments(Map<Enchantment, Integer> enchantments) {
        Iterator var3 = enchantments.entrySet().iterator();

        while(var3.hasNext()) {
            Entry<Enchantment, Integer> entry = (Entry)var3.next();
            this.addUnsafeEnchantment((Enchantment)entry.getKey(), (Integer)entry.getValue());
        }

    }

    public void addUnsafeEnchantment(Enchantment ench, int level) {

    }

    public int removeEnchantment(Enchantment ench) {
        int level = this.getEnchantmentLevel(ench);
        if (level != 0 && this.meta != null) {
            this.meta.removeEnchant(ench);
            return level;
        } else {
            return level;
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap();
        result.put("v", Bukkit.getUnsafe().getDataVersion());
        result.put("type", this.getType().name());
        if (this.getAmount() != 1) {
            result.put("amount", this.getAmount());
        }

        ItemMeta meta = this.getItemMeta();
        if (!Bukkit.getItemFactory().equals(meta, (ItemMeta)null)) {
            result.put("meta", meta);
        }

        return result;
    }

    public static ItemStack deserialize(Map<String, Object> args) {
        int version = args.containsKey("v") ? ((Number)args.get("v")).intValue() : -1;
        short damage = 0;
        int amount = 1;
        if (args.containsKey("damage")) {
            damage = ((Number)args.get("damage")).shortValue();
        }

        Material type;
        if (version < 0) {
            type = Material.getMaterial("LEGACY_" + (String)args.get("type"));
            byte dataVal = type.getMaxDurability() == 0 ? (byte)damage : 0;
            type = Bukkit.getUnsafe().fromLegacy(new MaterialData(type, dataVal), true);
            if (dataVal != 0) {
                damage = 0;
            }
        } else {
            type = Material.getMaterial((String)args.get("type"));
        }

        if (args.containsKey("amount")) {
            amount = ((Number)args.get("amount")).intValue();
        }

        ItemStack result = new ItemStack(type, amount, damage);
        Object raw;
        if (args.containsKey("enchantments")) {
            raw = args.get("enchantments");
            if (raw instanceof Map) {
                Map<?, ?> map = (Map)raw;
                Iterator var9 = map.entrySet().iterator();

                while(var9.hasNext()) {
                    Entry<?, ?> entry = (Entry)var9.next();
                    Enchantment enchantment = Enchantment.getByName(entry.getKey().toString());
                    if (enchantment != null && entry.getValue() instanceof Integer) {
                        result.addUnsafeEnchantment(enchantment, (Integer)entry.getValue());
                    }
                }
            }
        } else if (args.containsKey("meta")) {
            raw = args.get("meta");
            if (raw instanceof ItemMeta) {
                result.setItemMeta((ItemMeta)raw);
            }
        }

        if (version < 0 && args.containsKey("damage")) {
            result.setDurability(damage);
        }

        return result;
    }

    public ItemMetaImpl getItemMeta() {
        return this.meta;
    }

    public boolean hasItemMeta() {
        return true;
    }

    public boolean setItemMeta(ItemMetaImpl itemMeta) {
        return this.setItemMeta0(itemMeta, this.type);
    }

    private boolean setItemMeta0(ItemMetaImpl itemMeta, Material material) {
        this.meta = itemMeta;
        return true;
    }
}
