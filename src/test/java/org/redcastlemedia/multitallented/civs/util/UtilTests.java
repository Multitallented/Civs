package org.redcastlemedia.multitallented.civs.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVInventory;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;
import org.redcastlemedia.multitallented.civs.placeholderexpansion.PlaceHook;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class UtilTests extends TestUtil {

    @Before
    public void setup() {
        TownManager.getInstance().reload();
    }

    @Test
    public void cvItemShouldCreateItemStack() {
        CVItem cvItem = new CVItem(Material.COBBLESTONE,1, 100, "CustomCobble");
        ItemStack is = cvItem.createItemStack();
        assertEquals(Material.COBBLESTONE, is.getType());
    }

    @Test
    public void hexColorShouldTranslateProperly() {
        String parsedColors = Util.parseColors("@{#FF0000}Whatever");
        assertEquals(ChatColor.of("#FF0000") + "Whatever", parsedColors);
        String parsedColors2 = Util.parseColors("@{#FF0000}test@{#00FF00}test2");
        assertEquals(ChatColor.of("#FF0000") + "test" + ChatColor.of("#00FF00") + "test2", parsedColors2);
    }

    @Test
    public void cvItemFromStringShouldSetValuesProperly() {
        CVItem cvItem = CVItem.createCVItemFromString("COBBLESTONE*2%50");
        assertTrue(cvItem.getMat() == Material.COBBLESTONE && cvItem.getChance() == .5 && cvItem.getQty() == 2);
        assertNull(cvItem.getDisplayName());
    }
    @Test
    public void cvItemFromStringWithNameShouldSetValuesProperly() {
        CVItem cvItem = CVItem.createCVItemFromString("PRISMARINE.Jade*2");
        assertEquals(2, cvItem.getQty());
//        assertEquals("Jade", cvItem.getDisplayName()); TODO fix this
        assertEquals(Material.PRISMARINE, cvItem.getMat());
    }
    @Test
    public void cvItemFromStringShouldSetValuesProperly2() {
        CVItem cvItem = CVItem.createCVItemFromString("dark oak log*64");
        assertTrue(cvItem.getMat() == Material.DARK_OAK_LOG && cvItem.getQty() == 64);
    }
    @Test
    public void itemGroupShouldReturnProperCVItems() {
        List<CVItem> cvItems = CVItem.createListFromString("g:glass*12");
        assertEquals(12, cvItems.get(0).getQty());
    }

    @Test
    public void equivalentRegions() {
        World world = Bukkit.getWorld("world");
        Location location = new Location(world, 0, 0, 0);
        Location location2 = new Location(world, 0.4, 0.5, -0.2);
        assertTrue(Util.equivalentLocations(location, location2));
        Location location3 = new Location(world, 1, 1, 1);
        assertFalse(Util.equivalentLocations(location, location3));
        Location location4 = new Location(world, 5, 3, -8);
        assertFalse(Util.equivalentLocations(location, location4));
    }

    @Test
    public void addItemsShouldAddProperItems() {
        TestUtil.world.setChunkLoaded(false);
        List<CVItem> tempList = new ArrayList<>();
        tempList.add(CVItem.createCVItemFromString("GRASS"));
        List<List<CVItem>> returnList = new ArrayList<>();
        returnList.add(tempList);
        CVInventory cvInventory = UnloadedInventoryHandler.getInstance().getChestInventory(new Location(TestUtil.world, 0, 0, 0));
        Util.addItems(returnList, cvInventory);
        for (ItemStack itemStack : cvInventory.getContents()) {
            System.out.println(itemStack.getType().name());
            if (itemStack.getType() == Material.GRASS) {
                return;
            }
        }
        fail("No Grass found in inventory");
    }

    @Test
    public void placeHookShouldReportOwnerName() {
        CivilianManager.getInstance().createDefaultCivilian(TestUtil.player);
        TownTests.loadTownTypeHamlet2();
        Location location = new Location(Bukkit.getWorld("world"), 0,0,0);
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), Constants.OWNER);
        Town town = new Town("mytown", "hamlet2", location, people, 100, 100,
                1, 0, -1);
        TownManager.getInstance().addTown(town);
        PlaceHook placeHook = new PlaceHook();
        assertEquals("mytown", placeHook.onPlaceholderRequest(TestUtil.player, "townname"));
    }

    @Test
    public void placeHookShouldReportHighestPop() {
        CivilianManager.getInstance().createDefaultCivilian(TestUtil.player);
        TownTests.loadTownTypeHamlet2();
        Location location = new Location(Bukkit.getWorld("world"), 0,0,0);
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "member");
        Town town = new Town("mytown1", "hamlet2", location, people, 100, 100,
                3, 0, -1);
        TownManager.getInstance().addTown(town);
        Town town1 = new Town("mytown2", "hamlet2", location, people, 100, 100,
                8, 1, -1);
        TownManager.getInstance().addTown(town1);
        PlaceHook placeHook = new PlaceHook();
        assertEquals("mytown2", placeHook.onPlaceholderRequest(TestUtil.player, "townname"));
    }

    @Test
    public void placeholderKarmaShouldReportCivKarma() {
        CivilianManager.getInstance().createDefaultCivilian(TestUtil.player);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civilian.setKarma(6);
        PlaceHook placeHook = new PlaceHook();
        assertEquals("6", placeHook.onPlaceholderRequest(TestUtil.player, "karma"));
    }

    @Test
    public void placeholderKillsShouldReportCivKills() {
        CivilianManager.getInstance().createDefaultCivilian(TestUtil.player);
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civilian.setKills(4);
        PlaceHook placeHook = new PlaceHook();
        assertEquals("4", placeHook.onPlaceholderRequest(TestUtil.player, "kills"));
    }

    @Test
    public void parseColorComponentShouldHandleAllIndexes() {
        String input = "@{RED}Red @{YELLOW}Yellow @{LIGHT_PURPLE} Light Blue";
        TextComponent component = Util.parseColorsComponent(input);
        assertEquals(2, component.getExtra().size());
        assertEquals("Red ", component.getText());
        assertEquals(ChatColor.RED, component.getColor());
        assertEquals("Red Yellow  Light Blue", component.toPlainText());
        assertEquals("Yellow ", component.getExtra().get(0).toPlainText());
        assertEquals(" Light Blue", component.getExtra().get(1).toPlainText());
    }

    @Test
    public void parseColorComponentShouldHandleSillyIndexes() {
        String input = "@{RED}Red @{YELLOW}@{BLUE}Yellow @{LIGHT_PURPLE} Light Blue";
        TextComponent component = Util.parseColorsComponent(input);
        assertEquals(2, component.getExtra().size());
        assertEquals("Red ", component.getText());
        assertEquals(ChatColor.RED, component.getColor());
        assertEquals(ChatColor.BLUE, component.getExtra().get(0).getColor());
    }

    @Test @Ignore
    public void formatTimeShouldReturnCorrectFormat() {
        assertEquals("54s", Util.formatTime(player, 54));
        assertEquals("1m 22s", Util.formatTime(player, 82));
        assertEquals("1h 20m 30s", Util.formatTime(player, 4830));
        assertEquals("2h 20m 30s", Util.formatTime(player, 8430));
    }

    @Test
    public void getDefaultColorShouldReturnRed() {
        String message = ChatColor.RED + "[Civs] Something";
        assertEquals("" + ChatColor.RED, Util.getDefaultColor(message));
    }

    @Test
    public void textWrapSpaces() {
        String wrapThis = "0123456789 0123456789 0123456789 0123456789 01234567890 1234567890";
        assertEquals("§r0123456789 0123456789 0123456789", Util.textWrap(wrapThis).get(0));
    }

    @Test
    public void textWrapLongNoSpaces() {
        String wrapThis = "0123456789012345678901234567890123456789012345678901234567890";
        assertEquals("§r012345678901234567890123", Util.textWrap(wrapThis).get(0));
        assertEquals("§r456789012345678901234567", Util.textWrap(wrapThis).get(1));
    }

    @Test
    public void textWrapWithColors() {
        String wrapThis = "§c0123456789 0123456789 0123456789 0123456789 01234567890 1234567890";
        assertEquals("§c0123456789 0123456789 0123456789", Util.textWrap(wrapThis).get(0));
        assertEquals("§c01", Util.textWrap(wrapThis).get(1).substring(0, 4));
    }

    @Test
    public void performCommandShouldExecuteCorrectCommand() {
        Player player = mock(Player.class);
        when(player.isOnline()).thenReturn(true);
        when(player.isOp()).thenReturn(false);
        when(player.getName()).thenReturn("Multitallented");
        CommandUtil.performCommand(player, "cv invite $name$ Moenia");
        verify(player, times(1)).performCommand("cv invite Multitallented Moenia");
    }

    @Test
    public void performCommandOfflineShouldExecuteCorrectCommand() {
        Player player = mock(Player.class);
        when(player.isOnline()).thenReturn(false);
        when(player.isOp()).thenReturn(false);
        when(player.isValid()).thenReturn(true);
        when(player.getPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("Multitallented");
        CommandUtil.performCommand(player, "^!cv invite $name$ Moenia");
        verify(Bukkit.getServer(), times(1)).dispatchCommand(null,"cv invite Multitallented Moenia");
    }

    @Test
    public void performCommandOpShouldExecuteCorrectCommand() {
        Player player = mock(Player.class);
        when(player.isOnline()).thenReturn(true);
        when(player.isOp()).thenReturn(false);
        when(player.getName()).thenReturn("Multitallented");
        CommandUtil.performCommand(player, "^cv invite $name$ Moenia");
        verify(player, times(1)).performCommand("cv invite Multitallented Moenia");
        verify(player, times(1)).setOp(true);
        verify(player, times(1)).setOp(false);
    }

    @Test
    public void ownershipShouldBeDenied() {
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        Civilian civilian2 = CivilianManager.getInstance().getCivilian(TestUtil.player2.getUniqueId());
        Town town = TownTests.loadTown("Biznatch Republic", "hamlet", new Location(TestUtil.world, 0, 0, 0));
        assertFalse(OwnershipUtil.shouldDenyOwnershipOverSomeone(town, civilian, civilian2, TestUtil.player));
    }

    @Test
    public void returnCharacterShouldCreateNewLine() {
        String testString = "something\nsomething else";
        assertEquals(2, Util.textWrap(testString).size());
    }

    @Test
    public void returnCharacterShouldCreateNewLinePlusExtra() {
        String testString = "something with a really long line that should be returned for being long\nsomething";
        assertEquals(3, Util.textWrap(testString).size());
    }

    @Test
    public void numberFormatShouldNotBeEmpty() {
        assertEquals("100", Util.getNumberFormat(100, "zh"));
        assertEquals("1 000,1", Util.getNumberFormat(1000.1, "ru"));
    }

    @Test
    public void nameShouldBeValid() {
        assertFalse(Util.validateFileName("something&something"));
        assertTrue(Util.validateFileName("something_something"));
    }
}
