package org.redcastlemedia.multitallented.civs.spells.civstate;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.spells.effects.CivPotionEffect;

@CivsSingleton
public class CivStateListener implements Listener {

    private static CivStateListener instance = null;

    public CivStateListener() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, Civs.getInstance());
    }

    public static CivStateListener getInstance() {
        if (instance == null) {
            new CivStateListener();
        }
        return instance;
    }


    @EventHandler(ignoreCancelled = true)
    public void instantDrinkListener(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) ||
                event.getItem() == null) {
            return;
        }
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        boolean hasInstantDrink = false;
        for (CivState civState : civilian.getStates().values()) {
            if (civState.getBuiltInCivStates().contains(BuiltInCivStates.INSTANT_DRINK)) {
                hasInstantDrink = true;
                break;
            }
        }
        if (!hasInstantDrink) {
            return;
        }
        if (item.getType() == Material.POTION) {
            PotionMeta pm = (PotionMeta) event.getItem().getItemMeta();
            if (pm == null) {
                return;
            }
            boolean drankPotion = false;
            PotionEffect potionEffect = CivPotionEffect.getPotionEffect(pm);
            if (potionEffect != null && pm.getBasePotionData().getType().getEffectType() != null) {
                PotionEffectType potionEffectType = pm.getBasePotionData().getType().getEffectType();
                player.removePotionEffect(potionEffectType);

                player.addPotionEffect(potionEffect);
                drankPotion = true;
            }
            for (PotionEffect potionEffect1 : pm.getCustomEffects()) {
                player.removePotionEffect(potionEffect1.getType());
                player.addPotionEffect(potionEffect1);
                drankPotion = true;
            }
            if (drankPotion) {
                player.playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_DRINK, 1, 1);
                player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
                event.setCancelled(true);
            }
        } else if (item.getType() == Material.MILK_BUCKET) {
            for (PotionEffect potionEffect : new ArrayList<>(player.getActivePotionEffects())) {
                player.removePotionEffect(potionEffect.getType());
            }
            player.playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_DRINK, 1, 1);
            player.getInventory().setItemInMainHand(new ItemStack(Material.BUCKET));
            event.setCancelled(true);
        }
    }
}
