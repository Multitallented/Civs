package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;
import org.redcastlemedia.multitallented.civs.towns.*;
import org.redcastlemedia.multitallented.civs.util.Constants;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SchedulerTests extends TestUtil {

    @Test
    public void regionShouldTickWhenZeroPlayersAreOnline() {
        CommonScheduler commonScheduler = new CommonScheduler();
        RegionsTests.loadRegionTypeCobble();
        Region region = RegionsTests.createNewRegion("cobble");
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("cobble");
        Bukkit.getPluginManager().callEvent(null);
        for (int i=0; i<10; i++) {
            commonScheduler.run();
        }
        ArgumentCaptor<Event> tickCaptor = ArgumentCaptor.forClass(Event.class);
        verify(Bukkit.getPluginManager(), atLeast(4)).callEvent(
                tickCaptor.capture()
        );
        List<Event> capturedTickEvents = tickCaptor.getAllValues();
        for (Event event : capturedTickEvents) {
            if (event == null || !(event instanceof RegionTickEvent)) {
                continue;
            }
            RegionTickEvent regionTickEvent = (RegionTickEvent) event;
        }
    }

    @Test
    public void checkReagentsBeforeProvidingUpkeep() {
        RegionsTests.loadRegionTypeCobble3();
        RegionManager regionManager = RegionManager.getInstance();
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), Constants.OWNER);
        HashMap<String, String> effects = new HashMap<>();
        Region region = new Region("cobble", people,
                TestUtil.blockUnique2.getLocation(),
                RegionsTests.getRadii(), effects, 0);
        regionManager.addRegion(region);

        RegionTickUtil.runUpkeeps();
    }

    @Test(expected = SuccessException.class)
    public void messageShouldBeSentWhenEnterTown() {
        TownTests.loadTownTypeHamlet2();
        World world = mock(World.class);
        TownTests.loadTown("Moenia", "hamlet2", new Location(world, 0, 0, 0));
        CommonScheduler commonScheduler = new CommonScheduler();
        Player player = mock(Player.class);
        UUID uuid = new UUID(1, 8);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getLocation()).thenReturn(new Location(world, 1000,0,0));
        doThrow(new SuccessException()).when(player).sendTitle(Matchers.anyString(), Matchers.anyString(),
                Matchers.anyInt(), Matchers.anyInt(), Matchers.anyInt());
        commonScheduler.playerInTown(player);

        when(player.getLocation()).thenReturn(new Location(world, 0,0,0));
        commonScheduler.playerInTown(player);
    }

    @Test
    public void messageShouldNotBeRepeatedlySent() {
        TownTests.loadTownTypeHamlet2();
        World world = mock(World.class);
        TownTests.loadTown("Arcadia", "hamlet2", new Location(world, 0, 0, 0));
        CommonScheduler commonScheduler = new CommonScheduler();
        Player player = mock(Player.class);
        UUID uuid = new UUID(1, 8);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getLocation()).thenReturn(new Location(world, 1000,0,0));
        commonScheduler.playerInTown(player);

        when(player.getLocation()).thenReturn(new Location(world, 0,0,0));
        commonScheduler.playerInTown(player);

        doThrow(new SuccessException()).when(player).sendMessage(Matchers.anyString());
        commonScheduler.playerInTown(player);
    }
    @Test
    public void convertedManaShouldBeSetFractionally() {
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civilian.setMana(0);
        CommonScheduler commonScheduler = new CommonScheduler();
        commonScheduler.setConvertedMana(civilian, 120, 60);
        assertEquals(50, civilian.getMana());
    }
}
