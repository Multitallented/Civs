package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionUpkeepEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

import java.util.HashMap;
import java.util.UUID;

@CivsSingleton
public class EvolveEffect implements Listener {

    public static String KEY = "evolve";

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new EvolveEffect(), Civs.getInstance());
    }

    @EventHandler
    public void onRegionUpkeep(RegionUpkeepEvent event) {
        Location l = event.getRegion().getLocation();
        Region r = event.getRegion();
        RegionType rt = (RegionType) ItemManager.getInstance().getItemType(r.getType());


        if (!r.getEffects().containsKey(KEY)) {
            return;
        }

        int evolve;
        try {
            evolve = Integer.parseInt(r.getEffects().get(KEY).split("\\.")[1]);
        } catch (Exception e) {
            Civs.logger.severe("Invalid evolve config in " + rt.getName() + ".yml");
            return;
        }

        if (r.getExp() < evolve) {
            return;
        }
        String evolveTarget = r.getEffects().get(KEY).split("\\.")[0].toLowerCase();


        for (UUID uuid : r.getOwners()) {
            Player player = Bukkit.getPlayer(uuid);
            Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
            String locationString = r.getLocation().getWorld().getName() + " " +
                    (int) r.getLocation().getX() +
                    (int) r.getLocation().getY() +
                    (int) r.getLocation().getZ();
            String typeName = LocaleManager.getInstance().getTranslation(civilian.getLocale(), r.getType().toLowerCase() + "-name");
            String evolveName = LocaleManager.getInstance().getTranslation(civilian.getLocale(), evolveTarget.toLowerCase() + "-name");
            if (player != null && player.isOnline()) {
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "region-evolved")
                        .replace("$1", typeName)
                        .replace("$2", evolveName));
            }
        }

        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(evolveTarget);
        if (regionType == null) {
            Civs.logger.severe("Invalid evolveTarget " + evolveTarget);
            return;
        }
        r.setExp(0);
        r.setType(regionType.getName());
        r.setEffects((HashMap<String, String>) regionType.getEffects().clone());
        RegionManager.getInstance().saveRegion(r);
    }
}
