package org.redcastlemedia.multitallented.civs.civilians;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.Ignore;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;

public class CivilianTests extends TestUtil {

    @Test(expected = SuccessException.class)
    @Ignore
    public void inventoryClickOnUnownedCivItemShouldBeCancelled() {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        ItemStack is = TestUtil.createUniqueItemStack(Material.CHEST, "Civs Cobble");
        when(event.getCurrentItem()).thenReturn(is);
        Inventory inventory = mock(Inventory.class);
        when(event.getClickedInventory()).thenReturn(inventory);
        Player player = mock(Player.class);
        UUID uuid = new UUID(1,8);
        when(player.getUniqueId()).thenReturn(uuid);
        when(event.getWhoClicked()).thenReturn(player);
        doThrow(new SuccessException()).when(event).setCancelled(true);

        CivilianListener civilianListener = new CivilianListener();
//        civilianListener.onInventoryMoveEvent(event);
        fail("set cancelled not called");
    }

    @Test
    public void civilianShouldNotBeOverMaxItems() {
        ItemManager itemManager = ItemManager.getInstance();
        RegionsTests.loadRegionTypeCobble();
        CivilianManager civilianManager = CivilianManager.getInstance();
        civilianManager.loadCivilian(TestUtil.player);
        Civilian civilian = civilianManager.getCivilian(TestUtil.player.getUniqueId());
        assertNull(civilian.isAtMax(itemManager.getItemType("cobble")));
    }

    @Test
    public void highestBountyShouldWork() {
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        ArrayList<Bounty> bountyArrayList = new ArrayList<>();
        UUID uuid = new UUID(2,6);
        bountyArrayList.add(new Bounty(new UUID(2,4),10));
        bountyArrayList.add(new Bounty(uuid,20));
        civilian.setBounties(bountyArrayList);
        Bounty bounty = civilian.getHighestBounty();
        assertEquals(20.0, bounty.getAmount(), 0.1);
    }

    @Test
    public void droppingAnItemShouldPutItInBlueprints() {
        RegionsTests.loadRegionTypeCobble();
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civilian.getStashItems().clear();
        ItemStack itemStack = TestUtil.createUniqueItemStack(Material.COBBLESTONE, "cobble");
        Item item = mock(Item.class);
        when(item.getItemStack()).thenReturn(itemStack);
        PlayerDropItemEvent playerDropItemEvent = new PlayerDropItemEvent(TestUtil.player, item);
        CivilianListener.getInstance().onCivilianDropItem(playerDropItemEvent);
        assertTrue(civilian.getStashItems().containsKey("cobble"));
    }

    public static void loadCivilian(Player player) {
        CivilianManager.getInstance().loadCivilian(player);
    }
}
