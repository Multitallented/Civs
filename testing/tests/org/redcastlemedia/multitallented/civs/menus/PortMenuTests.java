package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.common.PortMenu;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class PortMenuTests {
    private PortMenu portMenu = new PortMenu();

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void setup() {
        MenuManager.clearData(TestUtil.player.getUniqueId());
    }

    @Test @Ignore
    public void privatePortsShouldDisplay() {
        loadRegionTypePPort();
        loadRegion("pport");
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        portMenu.itemsPerPage.put("ports", 36);
        portMenu.createData(civilian, new HashMap<>());
        Inventory inventory = portMenu.createMenu(civilian);
        assertEquals(Material.IRON_BLOCK, inventory.getItem(9).getType());
    }

    private void loadRegionTypePPort() {
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "IRON_BLOCK");
        ArrayList<String> reqs = new ArrayList<>();
        reqs.add("cobblestone*2");
        config.set("build-reqs", reqs);
        ArrayList<String> effects = new ArrayList<>();
        effects.add("block_place");
        effects.add("block_break");
        effects.add("port:private");
        config.set("effects", effects);
        config.set("upkeep.0.power-input", 2);
        config.set("period", "daily");
        ItemManager.getInstance().loadRegionType(config, "pport");
    }
    public static Region loadRegion(String type) {
        HashMap<UUID, String> peopleMap = new HashMap<>();
        peopleMap.put(TestUtil.player.getUniqueId(), "owner");
        Location location = new Location(Bukkit.getWorld("world"), 0,0,0);
        int[] radii = new int[6];
        for (int i=0; i<6; i++) {
            radii[i] = 5;
        }
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(type);
        Region region = new Region(type, peopleMap, location, radii, regionType.getEffects(),0);

        RegionManager.getInstance().addRegion(region);

        return region;
    }
}
