package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;

public class PotionAreaEffect implements Listener {
    public static String KEY = "potion";

    @EventHandler
    public void onPlayerInRegion(PlayerInRegionEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY)) {
            return;
        }
        String potionString = event.getRegion().getEffects().get(KEY);
        String[] splitPotionString = potionString.split("\\.");
        String potionTypeString = splitPotionString[0];

        PotionEffectType potionType;
        int duration = 40;
        int amplifier = 1;
        try {
            potionType = PotionEffectType.getByName(potionTypeString);
            if (potionType == null) {
                Civs.logger.severe("Invalid potion type for " + event.getRegionType().getName());
                return;
            }
            if (splitPotionString.length > 1) {
                duration = Integer.parseInt(splitPotionString[1]);
            }
            if (splitPotionString.length > 2) {
                amplifier = Integer.parseInt(splitPotionString[2]);
            }
        } catch (Exception e) {
            Civs.logger.severe("Invalid potion type for " + event.getRegionType().getName());
            return;
        }
        PotionEffect potionEffect = new PotionEffect(potionType, duration, amplifier);

        Player player = Bukkit.getPlayer(event.getUuid());
        if (player == null) {
            return;
        }
        player.addPotionEffect(potionEffect);
    }
}
