package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.events.RegionUpkeepEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

@CivsSingleton
public class EvolveEffect implements Listener {

    public static final String KEY = "evolve";

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new EvolveEffect(), Civs.getInstance());
    }

    @EventHandler @SuppressWarnings("unchecked")
    public void onRegionUpkeep(RegionUpkeepEvent event) {
        Region r = event.getRegion();
        RegionType rt = (RegionType) ItemManager.getInstance().getItemType(r.getType());


        if (!r.getEffects().containsKey(KEY)) {
            return;
        }

        int evolve;
        try {
            evolve = Integer.parseInt(r.getEffects().get(KEY).split("\\.")[1]);
        } catch (Exception e) {
            Civs.logger.severe("Invalid evolve config in " + rt.getProcessedName() + ".yml");
            return;
        }

        if (r.getExp() < evolve) {
            return;
        }
        String evolveTarget = r.getEffects().get(KEY).split("\\.")[0].toLowerCase();


        for (UUID uuid : r.getOwners()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(r.getType());
            String typeName = regionType.getDisplayName(player);
            RegionType evolveType = (RegionType) ItemManager.getInstance().getItemType(evolveTarget.toLowerCase());
            String evolveName = evolveType.getDisplayName(player);
            if (player.isOnline()) {
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(player,
                        "region-evolved")
                        .replace("$1", typeName)
                        .replace("$2", evolveName));
            }
        }

        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(evolveTarget);
        if (regionType == null) {
            Civs.logger.log(Level.SEVERE, "Invalid evolveTarget {0}", evolveTarget);
            return;
        }
        r.setExp(0);
        r.setType(regionType.getProcessedName());
        r.setEffects((HashMap<String, String>) regionType.getEffects().clone());
        RegionManager.getInstance().saveRegion(r);
    }
}
