package org.redcastlemedia.multitallented.civs;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.civilians.CivilianTests;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class TestUtil {
    public static WorldImpl world;
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
    public static Block block13;
    public static Player player;
    public static Player player2;
    public static Block block14;
    public static Block blockUnique;
    public static Block blockUnique2;
    public static Block blockUnique3;
    public static Block blockUnique4;
    public static Block blockUnique5;
    public static Block blockUnique6;
    public static Block blockUnique7;
    public static Block blockUnique8;
    public static Block blockUnique9;
    public static Block blockUnique10;
    public static PluginManager pluginManager = mock(PluginManager.class);

    public static void serverSetup() {
        Civs.logger = mock(PluginLogger.class);

        Civs.dataLocation = new File("/src/resouces/hybrid");

        CivilianManager.getInstance();
        Server server = mock(Server.class);
        Inventory inventory = new InventoryImpl();
        Logger logger = mock(Logger.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                return args[0];
            }
        }).when(logger).severe(Matchers.anyString());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                return args[0];
            }
        }).when(logger).warning(Matchers.anyString());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                return args[0];
            }
        }).when(logger).info(Matchers.anyString());
        when(server.getLogger()).thenReturn(logger);
        when(server.createInventory(Matchers.any(InventoryHolder.class), Matchers.anyInt(), Matchers.anyString())).thenReturn(inventory);


        ItemFactory itemFactory = mock(ItemFactory.class);
        when(server.getItemFactory()).thenReturn(itemFactory);
        ItemMeta im = new ItemMetaImpl();
        when(itemFactory.getItemMeta(Matchers.any(Material.class))).thenReturn(im);
//        when(im.getDisplayName()).thenReturn("Civs Cobble");

        world = new WorldImpl("world");
        WorldImpl world2 = new WorldImpl("world2");

        UUID uuid = new UUID(1,2);
        player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getLocation()).thenReturn(new Location(world, 0.5,0.5,0.5));
        when(player.getInventory()).thenReturn(new PlayerInventoryImpl());
        when(player.getServer()).thenReturn(server);
        when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
        UUID uuid2 = new UUID(1,3);
        player2 = mock(Player.class);
        when(player2.getUniqueId()).thenReturn(uuid2);
        when(player2.getLocation()).thenReturn(new Location(world, -8197.5,69.5,3196.5));
        when(player2.getInventory()).thenReturn(new PlayerInventoryImpl());
        when(player2.getServer()).thenReturn(server);
        when(player2.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(server.getPlayer(Matchers.any(UUID.class))).thenReturn(player);
        when(server.getOnlinePlayers()).thenReturn((Collection) new ArrayList<Player>());

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
        block13 = createBlock(Material.CHEST, new Location(world, 301, 101, 1));
        block14 = createBlock(Material.WALL_SIGN, new Location(world, -8197, 68, 3196));


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
        world.putBlock(301,101,1,block13);
        world.putBlock(-8197,68,3196, block14);

        world.putBlock(1000, 1, 0,
                createBlock(Material.COBBLESTONE, new Location(world, 1000, 1, 0)));
        world.putBlock(1000, 2, 0,
                createBlock(Material.COBBLESTONE, new Location(world, 1000, 2, 0)));
        world.putBlock(994, 0, 0,
                createBlock(Material.COBBLESTONE, new Location(world, 994, 0, 0)));
        world.putBlock(994, 1, 0,
                createBlock(Material.GRASS_BLOCK, new Location(world, 994, 1, 0)));
        world.putBlock(1006, 2, 0,
                createBlock(Material.GOLD_BLOCK, new Location(world, 1006, 0, 0)));
        //Councilroom

        councilroom: {
            ArrayList<Material> matList = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                matList.add(Material.CHEST);
            }
            for (int i = 0; i < 350; i++) {
                matList.add(Material.OAK_PLANKS);
            }
            for (int i = 0; i < 60; i++) {
                matList.add(Material.OAK_LOG);
            }
            for (int i = 0; i < 8; i++) {
                matList.add(Material.BOOKSHELF);
            }
            for (int i = 0; i < 16; i++) {
                matList.add(Material.GLASS);
            }
            for (int i = 0; i < 125; i++) {
                matList.add(Material.OAK_STAIRS);
            }
            for (int i = 0; i < 8; i++) {
                matList.add(Material.SIGN);
            }
            matList.add(Material.OAK_DOOR);
            matList.add(Material.OAK_DOOR);

            int i = 0;
            for (int x = -5; x < 6; x++) {
                for (int y = 55; y < 66; y++) {
                    for (int z = 995; z < 1006; z++) {
                        if (x == 0 && y == 60 && z == 1000) {
                            continue;
                        }
                        world.putBlock(x, y, z,
                                createBlock(matList.get(i), new Location(world, x, y, z)));
                        i++;
                        if (i >= matList.size()) {
                            break councilroom;
                        }
                    }
                }
            }
        }
        {
            int i = 0;
            outer: for (int x = -8; x < -6; x++) {
                for (int y = 53; y < 66; y++) {
                    for (int z = 995; z < 1006; z++) {
                        world.putBlock(x, y, z,
                                createBlock(Material.GRASS_BLOCK, new Location(world, x, y, z)));
                        i++;
                        if (i > 100) {
                            break outer;
                        }
                    }
                }
            }
        }
        world.putBlock(0, 0, 1000,
                createBlock(Material.CHEST, new Location(world, 0, 0, 1000)));

        when(server.getWorld("world")).thenReturn(world);
        when(server.getWorld(world.getUID())).thenReturn(world);
        when(server.getWorld("world2")).thenReturn(world2);
        when(server.getPlayer(Matchers.any(UUID.class))).thenReturn(player);

        when(server.getPluginManager()).thenReturn(pluginManager);
        blockUnique = createUniqueBlock(Material.CHEST, null, new Location(world, 4,0,0), true);
        blockUnique2 = createUniqueBlock(Material.CHEST, null, new Location(world, 2,50,0), false);
        blockUnique3 = createUniqueBlock(Material.CHEST, null, new Location(world, 3,100,0), true);
        blockUnique4 = createUniqueBlock(Material.CHEST, null, new Location(world, 0, 0,100), false);
        blockUnique5 = createUniqueBlock(Material.CHEST, null, new Location(world, 500, 0,0), true);
        blockUnique6 = createUniqueBlock(Material.CHEST, null, new Location(world, 509, 0,0), false);
        blockUnique7 = createUniqueBlock(Material.CHEST, null, new Location(world, 511, 0,0), true);
        blockUnique8 = createUniqueBlock(Material.CHEST, null, new Location(world, 300, 100,0), true);
        blockUnique9 = createUniqueBlock(Material.CHEST, null, new Location(world, 1000, 0,0 ), false);
        blockUnique10 = createUniqueBlock(Material.CHEST, null, new Location(world, 0, 60,1000 ), false);

        world.putBlock(4,0,0,blockUnique);
        world.putBlock(2,50,0,blockUnique2);
        world.putBlock(3,100,0,blockUnique3);
        world.putBlock(0,0,100,blockUnique4);
        world.putBlock(500,0,0,blockUnique5);
        world.putBlock(509,0,0,blockUnique6);
        world.putBlock(511,0,0,blockUnique7);
        world.putBlock(511,0,0,blockUnique8);
        world.putBlock(1000,0,0,blockUnique9);

        Bukkit.setServer(server);

        MenuManager.getInstance();
        LocaleManager localeManager = LocaleManager.getInstance();
        HashMap<String, String> mockLanguageMap = new HashMap<>();
        mockLanguageMap.put("no-region-type-found", "No se encontró ningún tipo de región");
        localeManager.languageMap.put("es", mockLanguageMap);

        CivilianManager.getInstance().createDefaultCivilian(player);
        createDefaultClass();
        ConfigManager configManager = ConfigManager.getInstance();
//        configManager.blackListWorlds = new ArrayList<>();
//        configManager.blackListWorlds.add("Hub");
//        configManager.itemGroups = new HashMap<>();
//        configManager.itemGroups.put("sign", "SIGN,WALL_SIGN");
//        configManager.itemGroups.put("wood", "OAK_PLANKS,SPRUCE_PLANKS,BIRCH_PLANKS,JUNGLE_PLANKS,DARK_OAK_PLANKS,ACACIA_PLANKS");
//        configManager.itemGroups.put("log", "OAK_LOG,SPRUCE_LOG,BIRCH_LOG,JUNGLE_LOG,DARK_OAK_LOG,ACACIA_LOG");
//        configManager.itemGroups.put("fence", "NETHER_BRICK_FENCE,OAK_FENCE,BIRCH_FENCE,SPRUCE_FENCE,JUNGLE_FENCE,DARK_OAK_FENCE,ACACIA_FENCE,IRON_BARS");
//        configManager.itemGroups.put("fencegate", "NETHER_BRICK_FENCE_GATE,OAK_FENCE_GATE,BIRCH_FENCE_GATE,SPRUCE_FENCE_GATE,JUNGLE_FENCE_GATE,DARK_OAK_FENCE_GATE,ACACIA_FENCE_GATE");
//        configManager.itemGroups.put("door", "OAK_DOOR,ACACIA_DOOR,DARK_OAK_DOOR,SPRUCE_DOOR,BIRCH_DOOR,JUNGLE_DOOR,IRON_DOOR");
//        configManager.itemGroups.put("glass", "GLASS,GLASS_PANE,RED_STAINED_GLASS_PANE,BLACK_STAINED_GLASS_PANE,BLUE_STAINED_GLASS_PANE,BROWN_STAINED_GLASS_PANE,CYAN_STAINED_GLASS_PANE,GRAY_STAINED_GLASS_PANE,GREEN_STAINED_GLASS_PANE,LIGHT_BLUE_STAINED_GLASS_PANE,LIGHT_GRAY_STAINED_GLASS_PANE,LIME_STAINED_GLASS_PANE,MAGENTA_STAINED_GLASS_PANE,ORANGE_STAINED_GLASS_PANE,PINK_STAINED_GLASS_PANE,PURPLE_STAINED_GLASS_PANE,WHITE_STAINED_GLASS_PANE,YELLOW_STAINED_GLASS_PANE,RED_STAINED_GLASS,BLACK_STAINED_GLASS,BLUE_STAINED_GLASS,BROWN_STAINED_GLASS,CYAN_STAINED_GLASS,GRAY_STAINED_GLASS,GREEN_STAINED_GLASS,LIGHT_BLUE_STAINED_GLASS,LIGHT_GRAY_STAINED_GLASS,LIME_STAINED_GLASS,MAGENTA_STAINED_GLASS,ORANGE_STAINED_GLASS,PINK_STAINED_GLASS,PURPLE_STAINED_GLASS,WHITE_STAINED_GLASS,YELLOW_STAINED_GLASS");
//        configManager.itemGroups.put("window", "GLASS,GLASS_PANE,IRON_BARS,RED_STAINED_GLASS_PANE,BLACK_STAINED_GLASS_PANE,BLUE_STAINED_GLASS_PANE,BROWN_STAINED_GLASS_PANE,CYAN_STAINED_GLASS_PANE,GRAY_STAINED_GLASS_PANE,GREEN_STAINED_GLASS_PANE,LIGHT_BLUE_STAINED_GLASS_PANE,LIGHT_GRAY_STAINED_GLASS_PANE,LIME_STAINED_GLASS_PANE,MAGENTA_STAINED_GLASS_PANE,ORANGE_STAINED_GLASS_PANE,PINK_STAINED_GLASS_PANE,PURPLE_STAINED_GLASS_PANE,WHITE_STAINED_GLASS_PANE,YELLOW_STAINED_GLASS_PANE,RED_STAINED_GLASS,BLACK_STAINED_GLASS,BLUE_STAINED_GLASS,BROWN_STAINED_GLASS,CYAN_STAINED_GLASS,GRAY_STAINED_GLASS,GREEN_STAINED_GLASS,LIGHT_BLUE_STAINED_GLASS,LIGHT_GRAY_STAINED_GLASS,LIME_STAINED_GLASS,MAGENTA_STAINED_GLASS,ORANGE_STAINED_GLASS,PINK_STAINED_GLASS,PURPLE_STAINED_GLASS,WHITE_STAINED_GLASS,YELLOW_STAINED_GLASS,NETHER_BRICK_FENCE,OAK_FENCE,BIRCH_FENCE,SPRUCE_FENCE,JUNGLE_FENCE,DARK_OAK_FENCE,ACACIA_FENCE,IRON_BARS");
//        configManager.itemGroups.put("primary", "QUARTZ_BLOCK,OAK_PLANKS,SPRUCE_PLANKS,JUNGLE_PLANKS,BIRCH_PLANKS,DARK_OAK_PLANKS,ACACIA_PLANKS,SAND,RED_SAND,CLAY,PACKED_ICE,STONE,RED_WOOL,BLACK_WOOL,BLUE_WOOL,BROWN_WOOL,CYAN_WOOL,GRAY_WOOL,GREEN_WOOL,LIGHT_BLUE_WOOL,LIGHT_GRAY_WOOL,LIME_WOOL,MAGENTA_WOOL,ORANGE_WOOL,PINK_WOOL,PURPLE_WOOL,WHITE_WOOL,YELLOW_WOOL,TERRACOTTA,RED_TERRACOTTA,BLACK_TERRACOTTA,BLUE_TERRACOTTA,BROWN_TERRACOTTA,CYAN_TERRACOTTA,GRAY_TERRACOTTA,GREEN_TERRACOTTA,LIGHT_BLUE_TERRACOTTA,LIGHT_GRAY_TERRACOTTA,LIME_TERRACOTTA,MAGENTA_TERRACOTTA,ORANGE_TERRACOTTA,PINK_TERRACOTTA,PURPLE_TERRACOTTA,WHITE_TERRACOTTA,YELLOW_TERRACOTTA,PRISMARINE,PURPUR_BLOCK");
//        configManager.itemGroups.put("secondary", "SANDSTONE,RED_SANDSTONE,BRICKS,STONE_BRICKS,SNOW_BLOCK,COBBLESTONE,OAK_LOG,SPRUCE_LOG,BIRCH_LOG,JUNGLE_LOG,DARK_OAK_LOG,ACACIA_LOG,QUARTZ_PILLAR,CHISELED_QUARTZ_BLOCK,RED_GLAZED_TERRACOTTA,BLACK_GLAZED_TERRACOTTA,BLUE_GLAZED_TERRACOTTA,BROWN_GLAZED_TERRACOTTA,CYAN_GLAZED_TERRACOTTA,GRAY_GLAZED_TERRACOTTA,GREEN_GLAZED_TERRACOTTA,LIGHT_BLUE_GLAZED_TERRACOTTA,LIGHT_GRAY_GLAZED_TERRACOTTA,LIME_GLAZED_TERRACOTTA,MAGENTA_GLAZED_TERRACOTTA,ORANGE_GLAZED_TERRACOTTA,PINK_GLAZED_TERRACOTTA,PURPLE_GLAZED_TERRACOTTA,WHITE_GLAZED_TERRACOTTA,YELLOW_GLAZED_TERRACOTTA,DARK_PRISMARINE,PURPUR_PILLAR");
//        configManager.itemGroups.put("roof", "SMOOTH_SANDSTONE,SMOOTH_RED_SANDSTONE,SMOOTH_STONE,QUARTZ_SLAB,QUARTZ_STAIRS,SANDSTONE_STAIRS,RED_SANDSTONE_STAIRS,ACACIA_STAIRS,OAK_STAIRS,BIRCH_STAIRS,JUNGLE_STAIRS,SPRUCE_STAIRS,DARK_OAK_STAIRS,BRICK_STAIRS,COBBLESTONE_STAIRS,DARK_PRISMARINE_STAIRS,PRISMARINE_BRICK_STAIRS,PURPUR_STAIRS,STONE_BRICK_STAIRS,GRASS_BLOCK");
//        configManager.itemGroups.put("bed", "RED_BED,BLACK_BED,BLUE_BED,BROWN_BED,CYAN_BED,GRAY_BED,GREEN_BED,LIGHT_BLUE_BED,LIGHT_GRAY_BED,LIME_BED,MAGENTA_BED,ORANGE_BED,PINK_BED,PURPLE_BED,WHITE_BED,YELLOW_BED");
//        configManager.itemGroups.put("stairs", "QUARTZ_STAIRS,SANDSTONE_STAIRS,RED_SANDSTONE_STAIRS,ACACIA_STAIRS,OAK_STAIRS,BIRCH_STAIRS,JUNGLE_STAIRS,SPRUCE_STAIRS,DARK_OAK_STAIRS,BRICK_STAIRS,COBBLESTONE_STAIRS,DARK_PRISMARINE_STAIRS,PRISMARINE_BRICK_STAIRS,PURPUR_STAIRS,STONE_BRICK_STAIRS");
//        configManager.itemGroups.put("vertical", "LADDER,QUARTZ_STAIRS,SANDSTONE_STAIRS,RED_SANDSTONE_STAIRS,ACACIA_STAIRS,OAK_STAIRS,BIRCH_STAIRS,JUNGLE_STAIRS,SPRUCE_STAIRS,DARK_OAK_STAIRS,BRICK_STAIRS,COBBLESTONE_STAIRS,DARK_PRISMARINE_STAIRS,PRISMARINE_BRICK_STAIRS,PURPUR_STAIRS,STONE_BRICK_STAIRS");
        configManager.useStarterBook = false;
    }

    public static ItemStack createItemStack(Material mat) {
        return new ItemStackImpl(mat, 1);
    }

    public static ItemStack createUniqueItemStack(Material mat, String name) {
        ItemStack is = new ItemStackImpl(mat, 1);
        is.getItemMeta().setDisplayName(name);
        ArrayList<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(name);
        is.getItemMeta().setLore(lore);
        return is;
    }

    public static void createDefaultClass() {
        FileConfiguration config = new YamlConfiguration();
        ItemManager.getInstance().loadClassType(config, "default");
    }

    public static Block createBlock(Material mat, Location location) {
        Block block = new BlockImpl(location);
        block.setType(mat);
        return block;
    }

    public static Block createUniqueBlock(Material mat, String name, Location location, boolean containsPickaxe) {
        Block block = new BlockImpl(location);
        block.setType(mat);
        if (containsPickaxe) {
            ((Chest) block.getState()).getBlockInventory()
                    .addItem(mockItemStack(Material.IRON_PICKAXE, 1, null, new ArrayList<>()));
        }
        return block;
    }

    public static ItemStack mockItemStack(Material mat, int qty, String name, List<String> lore) {
        ItemStack itemStack = new ItemStackImpl(mat, qty);
        itemStack.getItemMeta().setDisplayName(name);
        itemStack.getItemMeta().setLore(lore);
        return itemStack;
    }
}
