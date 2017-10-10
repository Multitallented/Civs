package org.redcastlemedia.multitallented.civs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import org.redcastlemedia.multitallented.civs.regions.Region;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class BlockLogger {
    private static BlockLogger blockLogger = null;
    private HashMap<Location, UUID> blocks = new HashMap<>();
//    private long lastSave = 0;
//    private int intervalId = -1;

    public BlockLogger() {
        blockLogger = this;
        loadBlocks();
    }

    public UUID getBlock(Location location) {
        return blocks.get(location);
    }
    public void putBlock(Location location, UUID uuid) {
        blocks.put(location, uuid);
        saveBlocks();
    }
    public void removeBlock(Location location) {
        blocks.remove(location);
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

    public void saveBlocks() {
        Civs civs = Civs.getInstance();
        if (civs == null) {
            return;
        }
//        intervalId = -1;
        final File blockData = new File(civs.getDataFolder(), "block-data.yml");
        final HashMap<Location, UUID> finalBlocks = blocks;
        Runnable runMe = new Runnable() {
            @Override
            public void run() {
                FileConfiguration config = new YamlConfiguration();
                try {
                    //Don't load the file. Overwrite it
                    for (Location location : finalBlocks.keySet()) {
                        config.set(Region.locationToString(location), finalBlocks.get(location));
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
        Civs civs = Civs.getInstance();
        if (civs == null) {
            return;
        }
        File blockData = new File(civs.getDataFolder(), "block-data.yml");
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
                blocks.put(Region.idToLocation(s), UUID.fromString(config.getString(s)));
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
