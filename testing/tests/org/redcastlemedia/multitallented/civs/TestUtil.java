package org.redcastlemedia.multitallented.civs;

import net.minecraft.server.v1_13_R2.TileEntityChest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_13_R2.block.CraftChest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class TestUtil {
    public static Block block;
    public static Block block2;
    public static Block block3;
    public static Block block4;
    public static Block block5;
    public static Block block6;
    public static Block block7;
    public static Block block8;
    public static Block block9;
    public static Block block10;
    public static Block block11;
    public static Block block12;
    public static Player player;
    public static Block blockUnique;
    public static Block blockUnique2;
    public static Block blockUnique3;
    public static Block blockUnique4;
    public static Block blockUnique5;
    public static Block blockUnique6;
    public static Block blockUnique7;

    public static void serverSetup() {
        Civs.logger = mock(PluginLogger.class);

        Server server = mock(Server.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        Logger logger = mock(Logger.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                System.out.println(args[0]);
                return args[0];
            }
        }).when(logger).severe(Matchers.anyString());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                System.out.println(args[0]);
                return args[0];
            }
        }).when(logger).warning(Matchers.anyString());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                System.out.println(args[0]);
                return args[0];
            }
        }).when(logger).info(Matchers.anyString());
        when(server.getLogger()).thenReturn(logger);
        when(server.createInventory(Matchers.any(InventoryHolder.class), Matchers.anyInt(), Matchers.anyString())).thenReturn(inventory);

        createDefaultClass();
        File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        ConfigManager configManager = new ConfigManager(file);
        configManager.blackListWorlds = new ArrayList<>();
        configManager.blackListWorlds.add("Hub");
        configManager.itemGroups = new HashMap<>();
        configManager.itemGroups.put("glass", "GLASS_PANE,GLASS");
        configManager.itemGroups.put("door", "OAK_DOOR,ACACIA_DOOR");
        configManager.useStarterBook = false;

        LocaleManager localeManager = new LocaleManager();
        HashMap<String, String> mockLanguageMap = new HashMap<>();
        mockLanguageMap.put("no-region-type-found", "No se encontró ningún tipo de región");
        localeManager.languageMap.put("es", mockLanguageMap);

        ItemFactory itemFactory = mock(ItemFactory.class);
        when(server.getItemFactory()).thenReturn(itemFactory);
        ItemMeta im = mock(ItemMeta.class);
        when(itemFactory.getItemMeta(Matchers.any(Material.class))).thenReturn(im);
//        when(im.getDisplayName()).thenReturn("Civs Cobble");

        WorldImpl world = new WorldImpl("world");
        WorldImpl world2 = new WorldImpl("world2");

        UUID uuid = new UUID(1,2);
        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getLocation()).thenReturn(new Location(world, 0,0,0));
        List<ItemStack> items = new ArrayList<>();
        ListIterator<ItemStack> listIterator = items.listIterator();
        when(inventory.iterator()).thenReturn(listIterator);
        when(player.getInventory()).thenReturn(inventory);
        when(server.getPlayer(Matchers.any(UUID.class))).thenReturn(player);

        CivilianManager civilianManager = new CivilianManager();
        civilianManager.createDefaultCivilian(player);
        when(server.getScheduler()).thenReturn(mock(BukkitScheduler.class));

        block = createBlock(Material.CHEST, new Location(world, 0, 0, 0));
        block2 = createBlock(Material.COBBLESTONE, new Location(world, 1, 0, 0));
        block3 = createBlock(Material.COBBLESTONE, new Location(world, 2, 0, 0));
        block4 = createBlock(Material.OAK_LOG, new Location(world, 3, 0, 0));
        block5 = createBlock(Material.COBBLESTONE, new Location(world, 1, 100, 0));
        block6 = createBlock(Material.COBBLESTONE, new Location(world, 10, 100, 0));
        block7 = createBlock(Material.COBBLESTONE, new Location(world, 1, 0,93));
        block8 = createBlock(Material.COBBLESTONE, new Location(world, -1, 0,106));
        block9 = createBlock(Material.GLASS_PANE, new Location(world, 1, 1,1));
        block10 = createBlock(Material.GOLD_BLOCK, new Location(world, 0, 1,1));
        block11 = createBlock(Material.GOLD_BLOCK, new Location(world, 4, 101,1));
        block12 = createBlock(Material.OAK_DOOR, new Location(world, 2, 0,1));


        world.putBlock(0,0,0,block);
        world.putBlock(1,0,0,block2);
        world.putBlock(2,0,0,block3);
        world.putBlock(3,0,0,block4);
        world.putBlock(1,100,0,block5);
        world.putBlock(10,100,0, block6);
        world.putBlock(1,0,93, block7);
        world.putBlock(-1, 0, 106, block8);
        world.putBlock(1,1,1, block9);
        world.putBlock(0,1,1, block10);
        world.putBlock(4,101,1,block11);
        world.putBlock(2,0,1,block12);
        when(server.getWorld("world")).thenReturn(world);
        when(server.getWorld("world2")).thenReturn(world2);
        when(server.getPlayer(Matchers.any(UUID.class))).thenReturn(player);
        blockUnique = createUniqueBlock(Material.CHEST, null, new Location(world, 4,0,0), true);
        blockUnique2 = createUniqueBlock(Material.CHEST, null, new Location(world, 2,50,0), false);
        blockUnique3 = createUniqueBlock(Material.CHEST, null, new Location(world, 3,100,0), true);
        blockUnique4 = createUniqueBlock(Material.CHEST, null, new Location(world, 0, 0,100), false);
        blockUnique5 = createUniqueBlock(Material.CHEST, null, new Location(world, 500, 0,0), true);
        blockUnique6 = createUniqueBlock(Material.CHEST, null, new Location(world, 509, 0,0), false);
        blockUnique7 = createUniqueBlock(Material.CHEST, null, new Location(world, 511, 0,0), true);

        world.putBlock(4,0,0,blockUnique);
        world.putBlock(2,50,0,blockUnique2);
        world.putBlock(3,100,0,blockUnique3);
        world.putBlock(0,0,100,blockUnique4);
        world.putBlock(500,0,0,blockUnique5);
        world.putBlock(509,0,0,blockUnique6);
        world.putBlock(511,0,0,blockUnique7);

        Bukkit.setServer(server);
    }

    public static ItemStack createItemStack(Material mat) {
        ItemStack is = mock(ItemStack.class);
        when(is.hasItemMeta()).thenReturn(false);
        when(is.getType()).thenReturn(mat);
        when(is.getDurability()).thenReturn((short) 0);
        return is;
    }

    public static ItemStack createUniqueItemStack(Material mat, String name) {
        ItemStack is = mock(ItemStack.class);
        ItemMeta im = mock(ItemMeta.class);
        when(is.hasItemMeta()).thenReturn(true);
        when(is.getItemMeta()).thenReturn(im);
        when(im.getDisplayName()).thenReturn(name);
        when(is.getType()).thenReturn(mat);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(TestUtil.player.getUniqueId().toString());
        when(im.getLore()).thenReturn(lore);
        return is;
    }

    public static void createDefaultClass() {
        FileConfiguration config = new YamlConfiguration();
        config.set("name", "default");
        ItemManager.getInstance().loadClassType(config);
    }

    public static Block createBlock(Material mat, Location location) {
        Block block = mock(Block.class);
        BlockState state = mock(BlockState.class);
        when(block.getState()).thenReturn(state);
        when(block.getType()).thenReturn(mat);
        when(block.getLocation()).thenReturn(location);
        return block;
    }

    public static Block createUniqueBlock(Material mat, String name, Location location, boolean containsPickaxe) {
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(mat);
        Chest chest = mock(Chest.class);
        Inventory inventory1 = new InventoryImpl();
        if (containsPickaxe) {
            inventory1.addItem(mockItemStack(Material.IRON_PICKAXE, 1, null, new ArrayList<>()));
        }
        when(chest.getBlockInventory()).thenReturn(inventory1);
        when(chest.getInventory()).thenReturn(inventory1);
        when(block.getState()).thenReturn(chest);
        when(block.getType()).thenReturn(mat);
        when(block.getLocation()).thenReturn(location);
        return block;
    }

    public static ItemStack mockItemStack(Material mat, int qty, String name, List<String> lore) {
        ItemStack itemStack = mock(ItemStack.class);
        when(itemStack.getType()).thenReturn(mat);
        when(itemStack.getAmount()).thenReturn(qty);

        ItemMeta itemMeta = mock(ItemMeta.class);
        when(itemStack.hasItemMeta()).thenReturn(true);
        when(itemMeta.getDisplayName()).thenReturn(name);
        when(itemMeta.getLore()).thenReturn(lore);
        when(itemStack.getItemMeta()).thenReturn(itemMeta);
        return itemStack;
    }

//    private static Location getMockLocation(Location location) {
//        Location mockLocation = mock(Location.class);
//        when(mockLocation.getX()).thenReturn(location.getX());
//        when(mockLocation.getY()).thenReturn(location.getY());
//        when(mockLocation.getZ()).thenReturn(location.getZ());
//        when(mockLocation.getBlock()).thenReturn(location.getWorld()
//                .getBlockAt((int) location.getX(), (int) location.getY(), (int) location.getZ()));
//        when(mockLocation.getWorld()).thenReturn(location.getWorld());
//        return mockLocation;
//    }
}
