package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownTests;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class UtilTests {

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void setup() {
        new TownManager();
    }

    @Test
    public void cvItemShouldCreateItemStack() {
        CVItem cvItem = new CVItem(Material.COBBLESTONE,1, 100, "CustomCobble");
        ItemStack is = cvItem.createItemStack();
        assertEquals(Material.COBBLESTONE, is.getType());
    }

    @Test
    public void cvItemFromStringShouldSetValuesProperly() {
        CVItem cvItem = CVItem.createCVItemFromString("COBBLESTONE*2%50");
        assertTrue(cvItem.getMat() == Material.COBBLESTONE && cvItem.getChance() == .5 && cvItem.getQty() == 2);
        assertNull(cvItem.getDisplayName());
    }
    @Test
    @Ignore
    public void cvItemFromStringWithNameShouldSetValuesProperly() {
        CVItem cvItem = CVItem.createCVItemFromString("PRISMARINE.Jade*2");
        assertTrue(cvItem.getMat() == Material.PRISMARINE && cvItem.getQty() == 2 &&
                cvItem.getDisplayName().equals("Jade"));
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
        Inventory inventory = mock(Inventory.class);
        List<ItemStack> inventoryContents = new ArrayList<>();
        inventoryContents.add(new ItemStack(Material.COBBLESTONE, 6));
        inventoryContents.add(new ItemStack(Material.WOODEN_AXE));
        inventoryContents.add(new ItemStack(Material.STONE_SWORD));
        inventoryContents.add(null);
        inventoryContents.add(null);
        inventoryContents.add(null);
        inventoryContents.add(null);
        inventoryContents.add(null);
        inventoryContents.add(null);
        ListIterator<ItemStack> itemStacks = inventoryContents.listIterator();
        when(inventory.iterator()).thenReturn(itemStacks);
        ArgumentCaptor<ItemStack> itemStackArgumentCaptor = ArgumentCaptor.forClass(ItemStack.class);
        List<CVItem> tempList = new ArrayList<>();
        tempList.add(CVItem.createCVItemFromString("GRASS"));
        List<List<CVItem>> returnList = new ArrayList<>();
        returnList.add(tempList);
        Util.addItems(returnList, inventory);
        verify(inventory).addItem(itemStackArgumentCaptor.capture());
        List<ItemStack> stacks = itemStackArgumentCaptor.getAllValues();
        assertEquals(Material.GRASS, stacks.get(0).getType());
    }

    @Test
    public void placeHookShouldReportOwnerName() {
        CivilianManager.getInstance().createDefaultCivilian(TestUtil.player);
        TownTests.loadTownTypeHamlet();
        Location location = new Location(Bukkit.getWorld("world"), 0,0,0);
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "owner");
        Town town = new Town("mytown", "hamlet", location, people, 100, 100,
                1, 0, -1);
        TownManager.getInstance().addTown(town);
        PlaceHook placeHook = new PlaceHook();
        assertEquals("mytown", placeHook.onPlaceholderRequest(TestUtil.player, "townname"));
    }

    @Test
    @Ignore
    public void placeHookShouldReportHighestPop() {
        CivilianManager.getInstance().createDefaultCivilian(TestUtil.player);
        TownTests.loadTownTypeHamlet();
        Location location = new Location(Bukkit.getWorld("world"), 0,0,0);
        HashMap<UUID, String> people = new HashMap<>();
        people.put(TestUtil.player.getUniqueId(), "member");
        Town town = new Town("mytown1", "hamlet", location, people, 100, 100,
                3, 0, -1);
        TownManager.getInstance().addTown(town);
        Town town1 = new Town("mytown2", "hamlet", location, people, 100, 100,
                8, 0, -1);
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

    @Test
    public void formatTimeShouldReturnCorrectFormat() {
        assertEquals("54s", AnnouncementUtil.formatTime(54));
        assertEquals("1m 22s", AnnouncementUtil.formatTime(82));
        assertEquals("1h 20m 30s", AnnouncementUtil.formatTime(4830));
        assertEquals("2h 20m 30s", AnnouncementUtil.formatTime(8430));
    }

    @Test
    public void getDefaultColorShouldReturnRed() {
        String message = ChatColor.RED + "[Civs] Something";
        assertEquals("" + ChatColor.RED, Util.getDefaultColor(message));
    }
}
