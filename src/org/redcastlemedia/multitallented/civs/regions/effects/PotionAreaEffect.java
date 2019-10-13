package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashMap;
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
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;

public class PotionAreaEffect implements Listener {
    public static String KEY = "potion";
    private static HashMap<String, Long> cooldowns = new HashMap<>();

    @EventHandler
    public void onPlayerInRegion(PlayerInRegionEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY)) {
            return;
        }
        String key = event.getRegion().getId();
        if (cooldowns.containsKey(key) && cooldowns.get(key) > System.currentTimeMillis()) {
            return;
        } else {
            cooldowns.remove(key);
        }
        String potionString = event.getRegion().getEffects().get(KEY);
        boolean isMember = event.getRegion().getPeople().containsKey(event.getUuid());
        applyPotion(potionString, event.getUuid(), event.getRegionType().getProcessedName(), isMember, key);
    }

    @EventHandler
    public void onPlayerInTown(PlayerInTownEvent event) {
        if (!event.getTown().getEffects().containsKey(KEY)) {
            return;
        }
        String key = event.getTown().getName();
        if (cooldowns.containsKey(key) && cooldowns.get(key) > System.currentTimeMillis()) {
            return;
        } else {
            cooldowns.remove(key);
        }
        String potionString = event.getTown().getEffects().get(KEY);
        boolean isMember = event.getTown().getPeople().containsKey(event.getUuid());
        applyPotion(potionString, event.getUuid(), event.getTownType().getProcessedName(), isMember, key);
    }

    @EventHandler
    public void onRegionDestroyed(RegionDestroyedEvent event) {
        cooldowns.remove(event.getRegion().getId());
    }

    @EventHandler
    public void onTownDestroyed(TownDestroyedEvent event) {
        cooldowns.remove(event.getTown().getName());
    }

    private void applyPotion(String potionString, UUID uuid, String typeName, boolean isMember, String key) {
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
            int cooldown = 0;
            try {
                potionType = PotionEffectType.getByName(potionTypeString);
                if (potionType == null) {
                    Civs.logger.severe("Invalid potion type for " + typeName);
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
                if (splitPotionString.length > 4) {
                    cooldown = Integer.parseInt(splitPotionString[4]);
                    cooldown = Math.max(cooldown, 0);
                }
            } catch (Exception e) {
                Civs.logger.severe("Invalid potion type for " + typeName);
                return;
            }
            if (Math.random() * 100 > chance) {
                return;
            }
            if (cooldown > 0) {
                cooldowns.put(key, System.currentTimeMillis() + (cooldown * 1000));
            }

            PotionEffect potionEffect = new PotionEffect(potionType, duration * 20, amplifier);

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                return;
            }
            player.addPotionEffect(potionEffect);
        }
    }
}
