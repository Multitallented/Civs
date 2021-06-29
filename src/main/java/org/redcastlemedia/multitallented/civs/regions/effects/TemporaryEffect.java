package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;

import java.util.HashMap;

@CivsSingleton
public class TemporaryEffect implements Listener {
    public static String KEY = "temporary";
    private final HashMap<String, Long> created = new HashMap<>();

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new TemporaryEffect(), Civs.getInstance());
    }

    @EventHandler
    public void onRegionTick(RegionTickEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY)) {
            return;
        }
        String id = event.getRegion().getId();
        if (!created.containsKey(id)) {
            created.put(id, System.currentTimeMillis());
            return;
        }
        long createdTime = created.get(id);
        long expiry = Long.parseLong(event.getRegion().getEffects().get(KEY)) * 1000;
        if (createdTime + expiry < System.currentTimeMillis()) {
            event.setShouldDestroy(true);
            created.remove(id);
        }
    }
}
