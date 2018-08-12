package org.redcastlemedia.multitallented.civs;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.protections.ProtectionHandler;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import sun.security.krb5.Config;

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


        when(world.getBlockAt(0, 0,0)).thenReturn(block);
        when(world.getBlockAt(1, 0,0)).thenReturn(block2);
        when(world.getBlockAt(2, 0,0)).thenReturn(block3);
        when(world.getBlockAt(3, 0,0)).thenReturn(block4);
        when(world.getBlockAt(1, 100,0)).thenReturn(block5);
        when(world.getBlockAt(10, 100,0)).thenReturn(block6);
        when(world.getBlockAt(1, 0,93)).thenReturn(block7);
        when(world.getBlockAt(-1, 0,106)).thenReturn(block8);
        when(world.getBlockAt(1, 1,1)).thenReturn(block9);
        when(world.getBlockAt(0, 1,1)).thenReturn(block10);
        when(world.getBlockAt(4, 101,1)).thenReturn(block11);
        when(world.getBlockAt(2, 0,1)).thenReturn(block12);
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

        when(world.getBlockAt(4, 0,0)).thenReturn(blockUnique);
        when(world.getBlockAt(2, 50,0)).thenReturn(blockUnique2);
        when(world.getBlockAt(3, 100,0)).thenReturn(blockUnique3);
        when(world.getBlockAt(0, 0,100)).thenReturn(blockUnique4);
        when(world.getBlockAt(500, 0,0)).thenReturn(blockUnique5);
        when(world.getBlockAt(509, 0,0)).thenReturn(blockUnique6);
        when(world.getBlockAt(511, 0,0)).thenReturn(blockUnique7);
        when(world.getBlockAt(any())).thenAnswer(new Answer() {
            public Block answer(InvocationOnMock invocation) {
                Location location = (Location) invocation.getArguments()[0];
                return location.getWorld().getBlockAt((int) location.getX(), (int) location.getY(), (int) location.getZ());
            }
        });
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
        MaterialData data = mock(MaterialData.class);
        ItemStack itemStack = createItemStack(mat);
        when(data.toItemStack(1)).thenReturn(itemStack);
        when(state.getData()).thenReturn(data);
        when(block.getState()).thenReturn(state);
        when(block.getType()).thenReturn(mat);
        when(block.getLocation()).thenReturn(location);
        return block;
    }

    public static Block createUniqueBlock(Material mat, String name, Location location, boolean containsPickaxe) {
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(mat);
        Chest chest = mock(Chest.class);
        Inventory inventory = mock(Inventory.class);
        when(inventory.firstEmpty()).thenReturn(1);
        ArrayList<ItemStack> arrayIS = new ArrayList<>();
        if (containsPickaxe) {
            arrayIS.add(new ItemStack(Material.IRON_PICKAXE, 1));
        }
        arrayIS.add(null);
        arrayIS.add(null);
        arrayIS.add(null);
        ListIterator<ItemStack> invIterator = arrayIS.listIterator();
        when(inventory.iterator()).thenReturn(invIterator);
        ItemStack[] itemStacks = new ItemStack[3];
        if (containsPickaxe) {
            itemStacks[0] = new ItemStack(Material.IRON_PICKAXE, 1);
        } else {
            itemStacks[0] = null;
        }
        itemStacks[1] = null;
        itemStacks[2] = null;
        when(inventory.getContents()).thenReturn(itemStacks);
        when(inventory.addItem(Matchers.any(ItemStack.class))).thenThrow(new SuccessException());
        when(chest.getInventory()).thenReturn(inventory);
        MaterialData materialData = mock(MaterialData.class);
        when(block.getState()).thenReturn(chest);
        when(chest.getData()).thenReturn(materialData);
        ItemStack is = createUniqueItemStack(mat, name);
        when(materialData.toItemStack(1)).thenReturn(is);
        when(materialData.toItemStack(Matchers.anyInt())).thenReturn(is);
        when(block.getType()).thenReturn(mat);
        when(block.getLocation()).thenReturn(location);
        return block;
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
