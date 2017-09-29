package org.redcastlemedia.multitallented.civs;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginLogger;
import org.mockito.Matchers;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtil {
    public static Block block;
    public static Block block2;
    public static Block block3;
    public static Block block4;
    public static Block block5;
    public static Block block6;
    public static Block block7;
    public static Block block8;
    public static Player player;
    public static Block blockUnique;
    public static Block blockUnique2;
    public static Block blockUnique3;
    public static Block blockUnique4;

    public static void serverSetup() {
        Civs.logger = mock(PluginLogger.class);

        Server server = mock(Server.class);
        Inventory inventory = mock(Inventory.class);
        when(server.getLogger()).thenReturn(mock(Logger.class));
        when(server.createInventory(Matchers.any(InventoryHolder.class), Matchers.anyInt(), Matchers.anyString())).thenReturn(inventory);

        File mockConfigFile = mock(File.class);
        ConfigManager configManager = new ConfigManager(mockConfigFile);
        configManager.blackListWorlds = new ArrayList<String>();
        configManager.blackListWorlds.add("Hub");

        ItemFactory itemFactory = mock(ItemFactory.class);
        when(server.getItemFactory()).thenReturn(itemFactory);
        ItemMeta im = mock(ItemMeta.class);
        when(itemFactory.getItemMeta(Matchers.any(Material.class))).thenReturn(im);
        when(im.getDisplayName()).thenReturn("Civs Cobble");

        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        when(world.getMaxHeight()).thenReturn(255);
        World world2 = mock(World.class);
        when(world2.getName()).thenReturn("world2");
        when(world2.getMaxHeight()).thenReturn(255);
        UUID uuid = new UUID(1,2);
        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getLocation()).thenReturn(new Location(world, 0,0,0));

        block = mock(Block.class);
        when(block.getType()).thenReturn(Material.CHEST);
        when(block.getLocation()).thenReturn(new Location(world, 0, 0, 0));
        block2 = mock(Block.class);
        when(block2.getType()).thenReturn(Material.COBBLESTONE);
        when(block2.getLocation()).thenReturn(new Location(world, 1, 0, 0));
        block3 = mock(Block.class);
        when(block3.getType()).thenReturn(Material.COBBLESTONE);
        when(block3.getLocation()).thenReturn(new Location(world, 2, 0, 0));
        block4 = mock(Block.class);
        when(block4.getType()).thenReturn(Material.LOG);
        when(block4.getLocation()).thenReturn(new Location(world, 3, 0, 0));
        block5 = mock(Block.class);
        when(block5.getType()).thenReturn(Material.COBBLESTONE);
        when(block5.getLocation()).thenReturn(new Location(world, 1, 100, 0));
        block6 = mock(Block.class);
        when(block6.getType()).thenReturn(Material.COBBLESTONE);
        when(block6.getLocation()).thenReturn(new Location(world, 10, 100, 0));
        block7 = mock(Block.class);
        when(block7.getType()).thenReturn(Material.COBBLESTONE);
        when(block7.getLocation()).thenReturn(new Location(world, 1, 0,93));
        block8 = mock(Block.class);
        when(block8.getType()).thenReturn(Material.COBBLESTONE);
        when(block8.getLocation()).thenReturn(new Location(world, -1, 0,106));


        when(world.getBlockAt(0, 0,0)).thenReturn(block);
        when(world.getBlockAt(1, 0,0)).thenReturn(block2);
        when(world.getBlockAt(2, 0,0)).thenReturn(block3);
        when(world.getBlockAt(3, 0,0)).thenReturn(block4);
        when(world.getBlockAt(1, 100,0)).thenReturn(block5);
        when(world.getBlockAt(10, 100,0)).thenReturn(block6);
        when(world.getBlockAt(1, 0,93)).thenReturn(block7);
        when(world.getBlockAt(-1, 0,106)).thenReturn(block8);
        when(world.getBlockAt(4, 0,0)).thenReturn(blockUnique);
        when(world.getBlockAt(2, 50,0)).thenReturn(blockUnique2);
        when(world.getBlockAt(3, 100,0)).thenReturn(blockUnique3);
        when(world.getBlockAt(0, 0,100)).thenReturn(blockUnique4);
        when(server.getWorld("world")).thenReturn(world);
        when(server.getWorld("world2")).thenReturn(world2);

        Bukkit.setServer(server);
        blockUnique = createUniqueBlock(Material.CHEST, "Civs Cobble", new Location(world, 4,0,0));
        blockUnique2 = createUniqueBlock(Material.CHEST, "Civs Cobble", new Location(world, 2,50,0));
        blockUnique3 = createUniqueBlock(Material.CHEST, "Civs Cobble", new Location(world, 3,100,0));
        blockUnique4 = createUniqueBlock(Material.CHEST, "Civs Cobble", new Location(world, 0, 0,100));
    }

    private static Block createUniqueBlock(Material mat, String name, Location location) {
        CVItem cvItem = new CVItem(mat, 1, -1, 100, name);
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(mat);
        BlockState blockState = mock(BlockState.class);
        MaterialData materialData = mock(MaterialData.class);
        when(block.getState()).thenReturn(blockState);
        when(blockState.getData()).thenReturn(materialData);
        ItemStack is = cvItem.createItemStack();
        when(materialData.toItemStack()).thenReturn(is);
        when(block.getType()).thenReturn(mat);
        when(block.getLocation()).thenReturn(location);
        return block;
    }
}
