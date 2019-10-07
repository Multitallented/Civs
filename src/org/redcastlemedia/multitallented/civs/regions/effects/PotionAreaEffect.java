package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerInTownEvent;

public class PotionAreaEffect implements Listener {
    public static String KEY = "potion";

    @EventHandler
    public void onPlayerInRegion(PlayerInRegionEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY)) {
            return;
        }
        String potionString = event.getRegion().getEffects().get(KEY);
        boolean isMember = event.getRegion().getPeople().containsKey(event.getUuid());
        applyPotion(potionString, event.getUuid(), event.getRegionType().getProcessedName(), isMember);
    }

    @EventHandler
    public void onPlayerInTown(PlayerInTownEvent event) {
        if (!event.getTownType().getEffects().containsKey(KEY)) {
            return;
        }
        String potionString = event.getTownType().getEffects().get(KEY);
        boolean isMember = event.getTown().getPeople().containsKey(event.getUuid());
        applyPotion(potionString, event.getUuid(), event.getTownType().getProcessedName(), isMember);
    }

    private void applyPotion(String potionString, UUID uuid, String name, boolean isMember) {
        for (String currentPotionString : potionString.split(",")) {
            String[] splitPotionString = currentPotionString.split("\\.");
            String potionTypeString = splitPotionString[0];

            boolean invert = potionTypeString.startsWith("^");

            if (invert) {
                potionTypeString = potionTypeString.substring(1);
            }

            if (invert == isMember) {
                return;
            }

            PotionEffectType potionType;
            int duration = 40;
            int amplifier = 1;
            int chance = 100;
            try {
                potionType = PotionEffectType.getByName(potionTypeString);
                if (potionType == null) {
                    Civs.logger.severe("Invalid potion type for " + name);
                    return;
                }
                if (splitPotionString.length > 1) {
                    duration = Integer.parseInt(splitPotionString[1]);
                }
                if (splitPotionString.length > 2) {
                    amplifier = Integer.parseInt(splitPotionString[2]);
                }
                if (splitPotionString.length > 3) {
                    chance = Integer.parseInt(splitPotionString[3]);
                    chance = Math.max(Math.min(chance, 100), 0);
                }
            } catch (Exception e) {
                Civs.logger.severe("Invalid potion type for " + name);
                return;
            }
            if (Math.random() * 100 > chance) {
                return;
            }
            PotionEffect potionEffect = new PotionEffect(potionType, duration, amplifier);

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                return;
            }
            player.addPotionEffect(potionEffect);
        }
    }
}
