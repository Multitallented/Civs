package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;

import java.util.HashMap;

public class TemporaryEffect implements Listener {
    public static String KEY = "temporary";
    private HashMap<String, Long> created = new HashMap<>();

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
        long expiry = Long.parseLong(event.getRegion().getEffects().get(KEY));
        if (createdTime + expiry < System.currentTimeMillis()) {
            event.setShouldDestroy(true);
            created.remove(id);
        }
    }
}
