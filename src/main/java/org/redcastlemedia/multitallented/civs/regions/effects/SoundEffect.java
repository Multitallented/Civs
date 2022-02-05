package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.events.RegionUpkeepEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;

@CivsSingleton
public class SoundEffect implements Listener {

    public static final String KEY = "sound";

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new SoundEffect(), Civs.getInstance());
    }

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onRegionUpkeep(RegionUpkeepEvent event) {
        Region region = event.getRegion();
        Location regionLocation = event.getRegion().getLocation();
        World currentWorld = event.getRegion().getLocation().getWorld();

        Sound sound;
        float volume;
        float pitch;

        if (!region.getEffects().containsKey(KEY) || currentWorld == null) {
            return;
        }

        String[] vars = region.getEffects().get(KEY).split("\\s*,\\s*");

        if (vars.length != 3) {
            return;
        }

        try {
            sound = Sound.valueOf(vars[0]);
            volume = Float.parseFloat(vars[1]);
            pitch = Float.parseFloat(vars[2]);
        } catch (IllegalArgumentException | NullPointerException ex1) {
            Civs.logger.log(Level.WARNING, "Invalid sound effect config for {0}", region.getType());
            return;
        }

        currentWorld.playSound(regionLocation.add(0.5, 0.5, 0.5), sound, volume, pitch);
    }
}
