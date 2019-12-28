package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

import java.util.HashSet;

@CivsSingleton
public class RepairEffect implements Listener {

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new RepairEffect(), Civs.getInstance());
    }

    private final String KEY = "repair";
    private HashSet<Material> getRequiredReagent(Material material) {
        HashSet<Material> returnSet = new HashSet<>();
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
            case FISHING_ROD:
                returnSet.add(Material.STRING);
                return returnSet;
        } return null;
    }

    private int getRepairCost(Material mat, ItemStack is)
    {
        int amt = 1;
//        System.out.println("Durability: " + mat.getMaxDurability() + ":" + is.getDurability());
        switch (mat) {
            case WOODEN_HOE:
            case WOODEN_PICKAXE:
            case WOODEN_SHOVEL:
            case WOODEN_SWORD:
            case BOW:
            case FISHING_ROD:
                amt = (int)((double) is.getDurability() / mat.getMaxDurability() * 1.0D);
                return amt < 1 ? 1 : amt;
            case SHEARS:
            case GOLDEN_PICKAXE:
            case GOLDEN_SHOVEL:
            case GOLDEN_SWORD:
            case GOLDEN_AXE:
            case GOLDEN_HOE:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_HELMET:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
                amt = (int)((double) is.getDurability() / mat.getMaxDurability() * 3.0D);
                return amt < 1 ? 1 : amt;
            case IRON_PICKAXE:
            case IRON_SHOVEL:
            case IRON_SWORD:
            case IRON_AXE:
            case IRON_HOE:
            case IRON_CHESTPLATE:
            case IRON_HELMET:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
                amt = (int)((double) is.getDurability() / mat.getMaxDurability() * 4.0D);
                return amt < 1 ? 1 : amt;
            case DIAMOND_PICKAXE:
            case DIAMOND_SHOVEL:
            case DIAMOND_SWORD:
            case DIAMOND_AXE:
            case DIAMOND_HOE:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_HELMET:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
                amt = (int)((double) is.getDurability() / mat.getMaxDurability() * 7.0D);
                return amt < 1 ? 1 : amt;
            case STONE_AXE:
            case STONE_HOE:
            case STONE_PICKAXE:
            case STONE_SHOVEL:
            case STONE_SWORD:
            case LEATHER_CHESTPLATE:
            case LEATHER_HELMET:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                amt = (int)((double) is.getDurability() / mat.getMaxDurability() * 2.0D);
                return amt < 1 ? 1 : amt;
        } return 0;
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
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        ItemStack item = player.getInventory().getItemInMainHand();

        if (getRequiredReagent(item.getType()) == null) {
            return;
        }
        if (item.getType() == Material.AIR) {
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "hold-repair-item"));
            return;
        }

        if (item.getDurability() >= item.getType().getMaxDurability()) {
            return;
        }
        int repairCost = getRepairCost(item.getType(), item);
        if (repairCost == 0) {
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "cant-repair-item"));
            event.setCancelled(true);
            return;
        }
        HashSet<Material> reagents = getRequiredReagent(item.getType());
        if (reagents != null && !reagents.isEmpty()) {
            boolean hasReagent = false;
            Material firstMat = null;
            for (Material mat : reagents) {
                if (firstMat == null) {
                    firstMat = mat;
                }
                ItemStack cost = new ItemStack(mat, repairCost);
                if (!hasReagentCost(player, cost)) {
                    continue;
                }
                hasReagent = true;
                player.getInventory().removeItem(cost);
            }
            if (!hasReagent) {
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "more-repair-items")
                        .replace("$1", firstMat.name().toLowerCase().replace("_", " ")));
                return;
            }
        }
        item.setDurability((short) 0);
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
