package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.items.ItemManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

public class TownTests {
    private TownManager townManager;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void onBefore() {
        townManager = new TownManager();
    }

    @Test
    public void findTownAtShouldReturnTown() {

        loadTownTypeHamlet();
        Town town = loadTown("BizRep", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        loadTown("Silverstone", new Location(Bukkit.getWorld("world"), 100, 0, 0));
        loadTown("Cupcake", new Location(Bukkit.getWorld("world"), -100, 0, 0));

        assertEquals(town, townManager.getTownAt(new Location(Bukkit.getWorld("world"), 0, 0,0)));
    }

    @Test
    public void shouldNotFindTown() {
        loadTownTypeHamlet();
        loadTown("BizRep", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        assertNull(townManager.getTownAt(new Location(Bukkit.getWorld("world"), 0, 55,0)));
    }
    @Test
    public void shouldFindTown() {
        loadTownTypeHamlet();
        Town town = loadTown("BizRep", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        assertEquals(town, townManager.getTownAt(new Location(Bukkit.getWorld("world"), 0, 0,0)));
    }

    @Test
    public void memberShouldBeAdded() {
        loadTownTypeHamlet();
        Town town = loadTown("Aeria", new Location(Bukkit.getWorld("world"), 0, 0, 20));
        UUID uuid = new UUID(1,5);
        townManager.addInvite(uuid, town);
        townManager.acceptInvite(uuid);
        assertEquals("member", town.getPeople().get(uuid));
    }

    @Test
    public void townsShouldIntersect() {
        loadTownTypeHamlet();
        loadTown("Summertown", new Location(Bukkit.getWorld("world"), 0, 0, 0));
        TownType townType = (TownType) ItemManager.getInstance().getItemType("hamlet");
        assertTrue(townManager.checkIntersect(new Location(Bukkit.getWorld("world"), 26, 0, 0), townType));
    }
    @Test
    public void townShouldNotIntersect() {
        loadTownTypeHamlet();
        loadTown("Summertown", new Location(Bukkit.getWorld("world"), 0, 0, 0));
        TownType townType = (TownType) ItemManager.getInstance().getItemType("hamlet");
        assertFalse(townManager.checkIntersect(new Location(Bukkit.getWorld("world"), 51, 0, 0), townType));
    }

    private Town loadTown(String name, Location location) {
        HashMap<UUID, String> owners = new HashMap<>();
        owners.put(TestUtil.player.getUniqueId(), "owner");
        Town town = new Town(name, "hamlet",
                location,
                owners);
        townManager.addTown(town);
        return town;
    }

    private void loadTownTypeHamlet() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "Hamlet");
        config.set("type", "town");
        config.set("build-radius", 25);
        ItemManager.getInstance().loadTownType(config, "hamlet");
    }
}
