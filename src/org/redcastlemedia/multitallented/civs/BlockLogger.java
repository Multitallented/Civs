package org.redcastlemedia.multitallented.civs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class BlockLogger {
    private static BlockLogger blockLogger = null;
    private HashMap<String, CVItem> blocks = new HashMap<>();

//    private long lastSave = 0;
//    private int intervalId = -1;

    public BlockLogger() {
        loadBlocks();
    }

    public CVItem getBlock(Location location) {
        CVItem returnBlock = blocks.get(Region.locationToString(location));
        if (returnBlock == null) {
            Location newLocation = new Location(location.getWorld(), location.getX() + 0.5,
                    location.getY() + 0.5, location.getZ() + 0.5);
            returnBlock = blocks.get(Region.locationToString(newLocation));
        }
        return returnBlock;
    }
    public void putBlock(Location location, CVItem cvItem) {
        blocks.put(Region.locationToString(location), cvItem);
        saveBlocks();
    }
    public void removeBlock(Location location) {
        blocks.remove(Region.locationToString(location));
        Location newLocation = new Location(location.getWorld(), location.getX() + 0.5,
                location.getY() + 0.5, location.getZ() + 0.5);
        blocks.remove(Region.locationToString(newLocation));
        saveBlocks();
    }
//    private void shouldSave() {
//        if (System.currentTimeMillis() - 300000 > lastSave && intervalId == -1) {
//            intervalId = Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
//                @Override
//                public void run() {
//                    saveBlocks();
//                }
//            }, 600L);
//            lastSave = System.currentTimeMillis() + 300000;
//        } else if (intervalId == -1) {
//            saveBlocks();
//            lastSave = System.currentTimeMillis();
//        }
//    }

    private void saveBlocks() {
//        intervalId = -1;
        final File blockData = new File(Civs.dataLocation, "block-data.yml");
        final HashMap<String, CVItem> finalBlocks = blocks;
        Runnable runMe = new Runnable() {
            @Override
            public void run() {
                FileConfiguration config = new YamlConfiguration();
                try {
                    //Don't load the file. Overwrite it
                    for (String location : finalBlocks.keySet()) {
                        CVItem cvItem = finalBlocks.get(location);
                        String locationString = location.replaceAll("\\.", "^");
                        config.set(locationString + ".mat", cvItem.getMat().toString());
                        config.set(locationString + ".name", cvItem.getDisplayName());
                        config.set(locationString + ".lore", cvItem.getLore());
                    }
                    config.save(blockData);
                } catch (Exception e) {
                    Civs.logger.severe("Unable to save to block-data.yml");
                    return;
                }
            }
        };
        runMe.run();
    }

    private void loadBlocks() {
        File blockData = new File(Civs.dataLocation, "block-data.yml");
        if (!blockData.exists()) {
            try {
                blockData.createNewFile();
            } catch (IOException ioe) {
                Civs.logger.severe("Unable to write to block-data.yml");
                return;
            }
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(blockData);
            for (String s : config.getKeys(false)) {
                try {
                    CVItem cvItem = new CVItem(
                            Material.valueOf(config.getString(s + ".mat")),
                            1,
                            100,
                            config.getString(s + ".name"),
                            config.getStringList(s + ".lore")
                    );
                    blocks.put(s.replaceAll("\\^", "."), cvItem);
                } catch (Exception e) {
                    Civs.logger.severe("Unable to read line from block-data.yml");
                    e.printStackTrace();
                    continue;
                }
            }

        } catch (Exception e) {
            Civs.logger.severe("Unable to read from block-data.yml");
            return;
        }
    }

    public static BlockLogger getInstance() {
        if (blockLogger == null) {
            blockLogger = new BlockLogger();
        }
        return blockLogger;
    }
}
