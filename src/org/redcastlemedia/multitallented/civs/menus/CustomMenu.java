package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;

public interface CustomMenu {
    Inventory createMenu(Civilian civilian);
    void loadConfig(FileConfiguration config, int size);
    String getKey();
    String getFileName();
}
