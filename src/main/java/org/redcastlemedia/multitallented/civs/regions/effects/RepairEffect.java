package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

@CivsSingleton @SuppressWarnings("unused")
public class RepairEffect implements Listener {

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new RepairEffect(), Civs.getInstance());
    }

    private static final String KEY = "repair";
    public static Set<Material> getRequiredReagent(Material material) {
        Set<Material> returnSet = new HashSet<>();
        switch (material) {
            case WOODEN_HOE:
            case WOODEN_PICKAXE:
            case WOODEN_SHOVEL:
            case WOODEN_SWORD:
                returnSet.add(Material.OAK_PLANKS);
                returnSet.add(Material.SPRUCE_PLANKS);
                returnSet.add(Material.BIRCH_PLANKS);
                returnSet.add(Material.JUNGLE_PLANKS);
                returnSet.add(Material.DARK_OAK_PLANKS);
                returnSet.add(Material.ACACIA_PLANKS);
                return returnSet;
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                returnSet.add(Material.LEATHER);
                return returnSet;
            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SHOVEL:
            case STONE_SWORD:
                returnSet.add(Material.COBBLESTONE);
                return returnSet;
            case IRON_PICKAXE:
            case IRON_SHOVEL:
            case IRON_SWORD:
            case IRON_AXE:
            case IRON_HOE:
            case IRON_CHESTPLATE:
            case IRON_HELMET:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case SHEARS:
                returnSet.add(Material.IRON_INGOT);
                return returnSet;
            case GOLDEN_PICKAXE:
            case GOLDEN_SHOVEL:
            case GOLDEN_SWORD:
            case GOLDEN_AXE:
            case GOLDEN_HOE:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_HELMET:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
                returnSet.add(Material.GOLD_INGOT);
                return returnSet;
            case DIAMOND_PICKAXE:
            case DIAMOND_SHOVEL:
            case DIAMOND_SWORD:
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
                returnSet.add(Material.DIAMOND);
                return returnSet;
            case BOW:
            case CROSSBOW:
            case FISHING_ROD:
                returnSet.add(Material.STRING);
                return returnSet;
            case ELYTRA:
                returnSet.add(Material.PHANTOM_MEMBRANE);
                return returnSet;
            default:
                return returnSet;
        }
    }

    public static int getRepairCost(Material mat, double damage) {
        int amt;
        switch (mat) {
            case WOODEN_SHOVEL:
            case STONE_SHOVEL:
            case GOLDEN_SHOVEL:
            case IRON_SHOVEL:
            case DIAMOND_SHOVEL:
                amt = (int) Math.ceil(damage / mat.getMaxDurability() * 1.0D);
                return Math.max(amt, 1);
            case WOODEN_HOE:
            case WOODEN_SWORD:
            case FISHING_ROD:
            case SHEARS:
            case STONE_HOE:
            case STONE_SWORD:
            case GOLDEN_SWORD:
            case GOLDEN_HOE:
            case IRON_HOE:
            case DIAMOND_SWORD:
            case DIAMOND_HOE:
            case IRON_SWORD:
                amt = (int) Math.ceil(damage / mat.getMaxDurability() * 2.0D);
                return Math.max(amt, 1);
            case BOW:
            case CROSSBOW:
            case STONE_PICKAXE:
            case WOODEN_PICKAXE:
            case GOLDEN_PICKAXE:
            case GOLDEN_AXE:
            case STONE_AXE:
            case DIAMOND_AXE:
            case IRON_AXE:
            case IRON_PICKAXE:
            case DIAMOND_PICKAXE:
                amt = (int) Math.ceil(damage / mat.getMaxDurability() * 3.0D);
                return Math.max(amt, 1);
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
            case DIAMOND_BOOTS:
                amt = (int) Math.ceil(damage / mat.getMaxDurability() * 4.0D);
                return Math.max(amt, 1);
            case DIAMOND_HELMET:
            case LEATHER_HELMET:
            case IRON_HELMET:
            case GOLDEN_HELMET:
                amt = (int) Math.ceil(damage / mat.getMaxDurability() * 5.0D);
                return Math.max(amt, 1);
            case DIAMOND_LEGGINGS:
            case LEATHER_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLDEN_LEGGINGS:
                amt = (int) Math.ceil(damage / mat.getMaxDurability() * 7.0D);
                return Math.max(amt, 1);
            case LEATHER_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
                amt = (int) Math.ceil(damage / mat.getMaxDurability() * 8.0D);
                return Math.max(amt, 1);
            default:
                return 0;
        }
    }

    public static boolean isArmor(Material mat) {
        switch (mat) {
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
            case DIAMOND_BOOTS:
            case CHAINMAIL_BOOTS:
            case DIAMOND_HELMET:
            case LEATHER_HELMET:
            case IRON_HELMET:
            case GOLDEN_HELMET:
            case CHAINMAIL_HELMET:
            case DIAMOND_LEGGINGS:
            case LEATHER_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case LEATHER_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
                return true;
            default:
                return false;
        }
    }
    public static boolean isHelmet(Material mat) {
        switch (mat) {
            case DIAMOND_HELMET:
            case LEATHER_HELMET:
            case IRON_HELMET:
            case GOLDEN_HELMET:
            case CHAINMAIL_HELMET:
                return true;
            default:
                return false;
        }
    }
    public static boolean isChestplate(Material mat) {
        switch (mat) {
            case LEATHER_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
                return true;
            default:
                return false;
        }
    }
    public static boolean isLeggings(Material mat) {
        switch (mat) {
            case DIAMOND_LEGGINGS:
            case LEATHER_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
                return true;
            default:
                return false;
        }
    }
    public static boolean isBoots(Material mat) {
        switch (mat) {
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
            case DIAMOND_BOOTS:
            case CHAINMAIL_BOOTS:
                return true;
            default:
                return false;
        }
    }

    public static boolean isWeapon(Material type) {
        switch (type) {
            case DIAMOND_SWORD:
            case IRON_SWORD:
            case GOLDEN_SWORD:
            case STONE_SWORD:
            case DIAMOND_AXE:
            case IRON_AXE:
            case GOLDEN_AXE:
            case STONE_AXE:
            case TRIDENT:
            case BOW:
            case CROSSBOW:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAxe(Material type) {
        switch (type) {
            case DIAMOND_AXE:
            case IRON_AXE:
            case GOLDEN_AXE:
            case STONE_AXE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isSword(Material type) {
        switch (type) {
            case DIAMOND_SWORD:
            case IRON_SWORD:
            case GOLDEN_SWORD:
            case STONE_SWORD:
                return true;
            default:
                return false;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getHand() == null) {
            return;
        }

        if ((!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getAction().equals(Action.LEFT_CLICK_BLOCK)) ||
                (!event.getClickedBlock().getType().equals(Material.IRON_BLOCK)) ||
                event.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }

        Region r = RegionManager.getInstance().getRegionAt(event.getClickedBlock().getLocation());

        if (r == null || !r.getEffects().containsKey(KEY)) {
            return;
        }

        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof Damageable)) {
            return;
        }
        Damageable damageable = (Damageable) itemMeta;

        if (getRequiredReagent(item.getType()).isEmpty()) {
            return;
        }
        if (item.getType() == Material.AIR) {
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslationWithPlaceholders(player, "hold-repair-item"));
            return;
        }

        if (!damageable.hasDamage()) {
            return;
        }
        repairItem(event, player, item);
    }

    private void repairItem(PlayerInteractEvent event, Player player, ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        Damageable damageable = (Damageable) itemMeta;
        if (damageable == null) {
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslationWithPlaceholders(player, "cant-repair-item"));
            event.setCancelled(true);
            return;
        }
        int repairCost = getRepairCost(item.getType(), damageable.getDamage());
        if (repairCost == 0) {
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslationWithPlaceholders(player, "cant-repair-item"));
            event.setCancelled(true);
            return;
        }
        Set<Material> reagents = getRequiredReagent(item.getType());
        if (!reagents.isEmpty()) {
            boolean hasReagent = false;
            Material firstMat = reagents.iterator().next();
            for (Material mat : reagents) {
                ItemStack cost = new ItemStack(mat, repairCost);
                if (!hasReagentCost(player, cost)) {
                    continue;
                }
                hasReagent = true;
                player.getInventory().removeItem(cost);
            }
            if (!hasReagent) {
                String message = LocaleManager.getInstance().getTranslationWithPlaceholders(player, "more-repair-items");
                message = message.replace("$1", firstMat.name().toLowerCase().replace("_", " "));
                player.sendMessage(Civs.getPrefix() + message);
                return;
            }
        }
        damageable.setDamage(0);
        item.setItemMeta(itemMeta);
    }

    protected boolean hasReagentCost(Player player, ItemStack itemStack) {
        int amount = 0;
        for (ItemStack stack : player.getInventory().all(itemStack.getType()).values()) {
            if (stack.getItemMeta() != null && stack.getItemMeta().getLore() != null &&
                    !stack.getItemMeta().getLore().isEmpty()) {
                continue;
            }
            amount += stack.getAmount();
            if (amount >= itemStack.getAmount()) {
                return true;
            }
        }
        return false;
    }
}
