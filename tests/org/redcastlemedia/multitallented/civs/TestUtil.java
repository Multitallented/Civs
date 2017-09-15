package org.redcastlemedia.multitallented.civs;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.mockito.Matchers;

import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtil {
    public static Block block;
    public static Block block2;
    public static Block block3;
    public static Player player;

    public static void serverSetup() {
        Server server = mock(Server.class);
        Inventory inventory = mock(Inventory.class);
        when(server.getLogger()).thenReturn(mock(Logger.class));
        when(server.createInventory(Matchers.any(InventoryHolder.class), Matchers.anyInt(), Matchers.anyString())).thenReturn(inventory);

        ItemFactory itemFactory = mock(ItemFactory.class);
        when(server.getItemFactory()).thenReturn(itemFactory);
        ItemMeta im = mock(ItemMeta.class);
        when(itemFactory.getItemMeta(Matchers.any(Material.class))).thenReturn(im);
        when(im.getDisplayName()).thenReturn("Civs Cobble");

        World world = mock(World.class);
        UUID uuid = new UUID(1,2);
        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);

        block = mock(Block.class);
        when(block.getType()).thenReturn(Material.CHEST);
        block2 = mock(Block.class);
        when(block2.getType()).thenReturn(Material.COBBLESTONE);
        block3 = mock(Block.class);
        when(block3.getType()).thenReturn(Material.COBBLESTONE);
        when(block.getLocation()).thenReturn(new Location(world, 0, 0, 0));
        when(block2.getLocation()).thenReturn(new Location(world, 1, 0, 0));
        when(block3.getLocation()).thenReturn(new Location(world, 2, 0, 0));


        when(world.getBlockAt(0, 0,0)).thenReturn(block);
        when(world.getBlockAt(1, 0,0)).thenReturn(block2);
        when(world.getBlockAt(2, 0,0)).thenReturn(block3);
        when(server.getWorld("world")).thenReturn(world);

        Bukkit.setServer(server);
    }
}
