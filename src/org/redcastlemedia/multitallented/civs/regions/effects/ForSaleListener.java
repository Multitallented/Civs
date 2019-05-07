package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.events.PlayerEnterRegionEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;

public class ForSaleListener implements Listener {

    @EventHandler
    public void onPlayerEnterRegionEvent(PlayerEnterRegionEvent event) {
        Region region = event.getRegion();
        if (region.getForSale() == -1) {
            return;
        }
        // TODO notify user that they can buy this region
    }
}
