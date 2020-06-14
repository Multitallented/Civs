package org.redcastlemedia.multitallented.civs.civilians;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.dynmap.DynmapCommonAPI;
import org.redcastlemedia.multitallented.civs.BlockLogger;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.events.RegionCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.skills.CivSkills;
import org.redcastlemedia.multitallented.civs.skills.Skill;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.tutorials.AnnouncementUtil;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.dynmaphook.DynmapHook;
import org.redcastlemedia.multitallented.civs.placeholderexpansion.PlaceHook;
import org.redcastlemedia.multitallented.civs.spells.SpellUtil;
import org.redcastlemedia.multitallented.civs.regions.StructureUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import github.scarsz.discordsrv.DiscordSRV;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.Indyuce.mmoitems.MMOItems;

@CivsSingleton
public class CivilianListener implements Listener {

    private static CivilianListener civilianListener;

    public static CivilianListener getInstance() {
        if (civilianListener == null) {
            civilianListener = new CivilianListener();
            Bukkit.getPluginManager().registerEvents(civilianListener, Civs.getInstance());
        }
        return civilianListener;
    }

    @EventHandler
    public void onCivilianJoin(PlayerJoinEvent event) {
        CivilianManager civilianManager = CivilianManager.getInstance();
        civilianManager.loadCivilian(event.getPlayer());
        ConfigManager configManager = ConfigManager.getInstance();
        Player player = event.getPlayer();
        if (configManager.getUseStarterBook()) {
            giveMenuBookIfNoneInInventory(player);
        }
    }

    public static void giveMenuBookIfNoneInInventory(Player player) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        boolean hasStarterBook = false;
        for (ItemStack is : player.getInventory()) {
            if (is != null && Util.isStarterBook(is)) {
                hasStarterBook = true;
                break;
            }
        }
        if (!hasStarterBook) {
            ItemStack stack = Util.createStarterBook(civilian.getLocale());
            player.getInventory().addItem(stack);
        }
    }

    @EventHandler @SuppressWarnings("unused")
    public void onCivilianQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);

        if (civilian.isInCombat() && ConfigManager.getInstance().getCombatLogPenalty() > 0) {
            int penalty = (int) (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() *
                    ConfigManager.getInstance().getCombatLogPenalty() / 100);
            if (civilian.getLastDamager() != null) {
                Player damager = Bukkit.getPlayer(civilian.getLastDamager());
                if (damager != null && damager.isOnline()) {
                    player.damage(penalty);
                }
            } else {
                player.damage(penalty);
            }
        }
        if (!civilian.getCombatBar().isEmpty()) {
            SpellUtil.removeCombatBar(player, civilian);
        }
        CivilianManager.getInstance().unloadCivilian(player);
        CommonScheduler.getLastRegion().remove(uuid);
        CommonScheduler.getLastTown().remove(uuid);
        CommonScheduler.removeLastAnnouncement(uuid);
        MenuManager.clearHistory(uuid);
        MenuManager.clearData(uuid);
        TownManager.getInstance().clearInvite(uuid);
        AnnouncementUtil.clearPlayer(uuid);
        StructureUtil.removeBoundingBox(uuid);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        if (!(event.getPotion().getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPotion().getShooter();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        for (Skill skill : civilian.getSkills().values()) {
            if (skill.getType().equalsIgnoreCase(CivSkills.POTION.name())) {
                double exp = 0;
                for (PotionEffect potionEffect : event.getPotion().getEffects()) {
                    exp += skill.addAccomplishment(potionEffect.getType().getName());
                }
                if (exp > 0) {
                    CivilianManager.getInstance().saveCivilian(civilian);
                    String localSkillName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                            skill.getType() + LocaleConstants.SKILL_SUFFIX);
                    player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                            "exp-gained").replace("$1", "" + exp)
                            .replace("$2", localSkillName));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (event.getItem().getType() == Material.POTION ||
                event.getItem().getType() == Material.LINGERING_POTION) {
            PotionMeta potionMeta = (PotionMeta) event.getItem().getItemMeta();
            for (Skill skill : civilian.getSkills().values()) {
                if (skill.getType().equalsIgnoreCase(CivSkills.POTION.name())) {
                    double exp = 0;
                    if (potionMeta.getBasePotionData().getType().getEffectType() != null) {
                        exp += skill.addAccomplishment(potionMeta.getBasePotionData().getType().getEffectType().getName());
                    }
                    for (PotionEffect potionEffect : potionMeta.getCustomEffects()) {
                        exp += skill.addAccomplishment(potionEffect.getType().getName());
                    }
                    if (exp > 0) {
                        CivilianManager.getInstance().saveCivilian(civilian);
                        String localSkillName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                                skill.getType() + LocaleConstants.SKILL_SUFFIX);
                        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                                "exp-gained").replace("$1", "" + exp)
                                .replace("$2", localSkillName));
                    }
                }
            }
        } else {
            for (Skill skill : civilian.getSkills().values()) {
                if (skill.getType().equalsIgnoreCase(CivSkills.FOOD.name())) {
                    double exp = skill.addAccomplishment(event.getItem().getType().name());
                    if (exp > 0) {
                        CivilianManager.getInstance().saveCivilian(civilian);
                        String localSkillName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                                skill.getType() + LocaleConstants.SKILL_SUFFIX);
                        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                                "exp-gained").replace("$1", "" + exp)
                                .replace("$2", localSkillName));
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true) @SuppressWarnings("unused")
    public void onCraftItem(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        Skill skill = civilian.getSkills().get(CivSkills.CRAFTING.name().toLowerCase());
        if (skill != null && event.getCurrentItem() != null &&
                event.getCurrentItem().getType() != Material.AIR) {
            double exp = 0;
            for (int i = 0; i < event.getCurrentItem().getAmount(); i++) {
                exp += skill.addAccomplishment(event.getCurrentItem().getType().name());
            }
            if (exp > 0) {
                CivilianManager.getInstance().saveCivilian(civilian);
                String localSkillName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        skill.getType() + LocaleConstants.SKILL_SUFFIX);
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "exp-gained").replace("$1", "" + exp)
                        .replace("$2", localSkillName));
            }
        }
    }

    @EventHandler
    public void onRegionCreated(RegionCreatedEvent event) {
        if (event.getRegionType().getReqs().isEmpty() || event.getRegionType().getPrice() < 1) {
            return;
        }
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        for (Skill skill : civilian.getSkills().values()) {
            if (skill.getType().equalsIgnoreCase(CivSkills.BUILDING.name())) {
                double exp = skill.addAccomplishment(event.getRegion().getType());
                if (exp > 0) {
                    CivilianManager.getInstance().saveCivilian(civilian);
                    String localSkillName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                            skill.getType() + LocaleConstants.SKILL_SUFFIX);
                    player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                            "exp-gained").replace("$1", "" + exp)
                            .replace("$2", localSkillName));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCivilianDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (CVItem.isCivsItem(item.getItemStack())) {
            CivItem civItem = CivItem.getFromItemStack(item.getItemStack());
            if (civItem != null && civItem.getItemType() == CivItem.ItemType.SPELL) {
                event.setCancelled(true);
            }
        }
        if (checkDroppedItem(item.getItemStack(), event.getPlayer())) {
            item.remove();
        }
    }

    public static boolean checkDroppedItem(ItemStack itemStack, Player player) {
        if (ConfigManager.getInstance().getAllowSharingCivsItems() ||
                !CVItem.isCivsItem(itemStack)) {
            return false;
        }
        CivItem civItem = CivItem.getFromItemStack(itemStack);
        if (civItem == null) {
            return false;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        boolean hasBlueprintsMenuOpen = MenuManager.getInstance().hasMenuOpen(civilian.getUuid(), "blueprints");
        if (hasBlueprintsMenuOpen) {
            if (Civs.econ != null && civItem.getPrice() > 0) {
                Civs.econ.depositPlayer(player, civItem.getPrice());
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "refund").replace("$1", Util.getNumberFormat(civItem.getPrice(), civilian.getLocale())));
            }
            return true;
        }
        String itemName = civItem.getProcessedName();
        player.closeInventory();
        if (civilian.getStashItems().containsKey(itemName)) {
            civilian.getStashItems().put(itemName, civilian.getStashItems().get(itemName) + 1);
        } else {
            civilian.getStashItems().put(itemName, 1);
        }
        CivilianManager.getInstance().saveCivilian(civilian);
        MenuManager.openMenuFromString(civilian, "blueprints");
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCivilianDispense(BlockDispenseEvent event) {
        ItemStack is = event.getItem();
        if (!CVItem.isCivsItem(is)) {
            return;
        }
        CivItem civItem = CivItem.getFromItemStack(is);
        if (!civItem.isPlaceable()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAdvertisementBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Sign)) {
            return;
        }
        Town town = TownManager.getInstance().getTownAt(event.getBlock().getLocation());
        if (town == null) {
            return;
        }
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        if (government.getGovernmentType() != GovernmentType.IDIOCRACY) {
            return;
        }
        changeAdvertising(event.getBlock(), town, false);
        Util.checkNoise(town, event.getPlayer());
    }

    private void changeAdvertising(Block block, Town town, boolean increment) {
        Sign sign = (Sign) block.getState();
        changeAdvertising(sign.getLines(), town, increment);
    }
    private void changeAdvertising(String[] lines, Town town, boolean increment) {
        HashSet<UUID> changeThese = new HashSet<>();
        for (UUID uuid : town.getIdiocracyScore().keySet()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.getName() == null) {
                continue;
            }
            for (String line : lines) {
                if (line.contains(offlinePlayer.getName())) {
                    changeThese.add(uuid);
                }
            }
        }
        for (UUID uuid : changeThese) {
            if (increment) {
                if (!town.getIdiocracyScore().containsKey(uuid)) {
                    town.getIdiocracyScore().put(uuid, 1);
                } else {
                    town.getIdiocracyScore().put(uuid,
                            town.getIdiocracyScore().get(uuid) + 1);
                }
            } else {
                if (town.getIdiocracyScore().containsKey(uuid)) {
                    if (town.getIdiocracyScore().get(uuid) < 2) {
                        town.getIdiocracyScore().remove(uuid);
                    } else {
                        town.getIdiocracyScore().put(uuid,
                                town.getIdiocracyScore().get(uuid) - 1);
                    }
                }
            }
        }
        if (!changeThese.isEmpty()) {
            TownManager.getInstance().saveTown(town);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAdvertisementPlace(SignChangeEvent event) {
        Town town = TownManager.getInstance().getTownAt(event.getBlock().getLocation());
        if (town == null) {
            return;
        }
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        if (government.getGovernmentType() != GovernmentType.IDIOCRACY) {
            return;
        }
        changeAdvertising(event.getBlock(), town, false);
        changeAdvertising(event.getLines(), town, true);
        Util.checkNoise(town, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFireworkLaunch(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.FIREWORK_ROCKET ||
                (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        Town town = TownManager.getInstance().getTownAt(player.getLocation());
        if (town == null) {
            return;
        }
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        if (government.getGovernmentType() != GovernmentType.IDIOCRACY) {
            return;
        }
        if (!town.getIdiocracyScore().containsKey(civilian.getUuid())) {
            town.getIdiocracyScore().put(civilian.getUuid(), 1);
        } else {
            town.getIdiocracyScore().put(civilian.getUuid(),
                    town.getIdiocracyScore().get(civilian.getUuid()) + 1);
        }
        Util.checkNoise(town, event.getPlayer());
        TownManager.getInstance().saveTown(town);
    }

    @EventHandler
    public void onStarterBookClick(PlayerInteractEvent event) {
        if (event.getItem() == null ||
                (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Player player = event.getPlayer();
        if (!Util.isStarterBook(event.getItem())) {
            return;
        }
        if (ConfigManager.getInstance().getBlackListWorlds()
                .contains(event.getPlayer().getWorld().getName())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            player.performCommand("cv");
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            player.performCommand("cv");
            return;
        }
        Location location = Region.idToLocation(Region.blockLocationToString(block.getLocation()));
        Region region = RegionManager.getInstance().getRegionAt(location);
        if (region == null) {
            Set<Region> regionSet = RegionManager.getInstance().getContainingRegions(block.getLocation(), 0);
            if (!regionSet.isEmpty()) {
                region = regionSet.iterator().next();
                MenuManager.clearHistory(player.getUniqueId());
                HashMap<String, String> params = new HashMap<>();
                params.put(Constants.REGION, region.getId());
                MenuManager.getInstance().openMenu(player, Constants.REGION, params);
            } else {
                player.performCommand("cv");
            }
        } else {
            MenuManager.clearHistory(player.getUniqueId());
            HashMap<String, String> params = new HashMap<>();
            params.put(Constants.REGION, region.getId());
            MenuManager.getInstance().openMenu(player, Constants.REGION, params);
        }
        if (region != null) {
            StructureUtil.showGuideBoundingBox(player, region.getLocation(), region);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCivilianBlockBreak(BlockBreakEvent event) {
        boolean shouldCancel = shouldCancelBlockBreak(event.getBlock(), event.getPlayer());
        if (shouldCancel) {
            event.setCancelled(true);
        }
    }

    public boolean shouldCancelBlockBreak(Block block, Player player) {
        Location location = Region.idToLocation(Region.blockLocationToString(block.getLocation()));
        BlockLogger blockLogger = BlockLogger.getInstance();
        CVItem cvItem = blockLogger.getBlock(location);
        if (cvItem == null) {
            return false;
        }
        UUID uuid = null;
        if (cvItem.getLore() != null && cvItem.getLore().size() > 0) {
            uuid = UUID.fromString(ChatColor.stripColor(cvItem.getLore().get(0)));
        }
        blockLogger.removeBlock(block.getLocation());
        cvItem.setQty(1);
        if (player != null && (!ConfigManager.getInstance().getAllowSharingCivsItems() ||
                uuid == null || cvItem.getMat() != block.getType() ||
                !uuid.equals(player.getUniqueId()))) {
            block.setType(Material.AIR);
        } else {
            block.setType(Material.AIR);
            ItemStack itemStack = cvItem.createItemStack();
            int firstEmptyIndex = player == null ? -1 : player.getInventory().firstEmpty();
            if (firstEmptyIndex > -1) {
                player.getInventory().setItem(firstEmptyIndex, itemStack);
            } else {
                Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
                CivItem civItem = CivItem.getFromItemStack(cvItem);
                if (civilian.getStashItems().containsKey(civItem.getProcessedName())) {
                    civilian.getStashItems().put(civItem.getProcessedName(),
                            civilian.getStashItems().get(civItem.getProcessedName()) + 1);
                } else {
                    civilian.getStashItems().put(civItem.getProcessedName(), 1);
                }
                CivilianManager.getInstance().saveCivilian(civilian);
            }
        }
        return true;
    }

    @EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack is = event.getItemInHand();
        if (event.getPlayer() == null || !CVItem.isCivsItem(is)) {
            return;
        }
        CivItem civItem = CivItem.getFromItemStack(is);
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        if (!civItem.isPlaceable()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                    .getTranslationWithPlaceholders(event.getPlayer(),
                    "not-allowed-place").replace("$1", civItem.getDisplayName()));
            return;
        }
        if (civItem instanceof TownType) {
            Town town = TownManager.getInstance().getTownAt(event.getBlockPlaced().getLocation());
            if (town != null) {
                TownManager.getInstance().placeTown(event.getPlayer(), town.getName(), town);
            }
            event.setCancelled(true);
            String localTownName = LocaleManager.getInstance().getTranslationWithPlaceholders(event.getPlayer(),
                    civItem.getProcessedName() + LocaleConstants.NAME_SUFFIX);
            event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                    .getTranslationWithPlaceholders(event.getPlayer(),
                    "town-instructions").replace("$1", localTownName));
            return;
        }
        CVItem cvItem = CVItem.createFromItemStack(is);
        if (cvItem.getLore() == null || cvItem.getLore().isEmpty()) {
            ArrayList<String> lore = new ArrayList<>();
            lore.add(civilian.getUuid().toString());
            lore.add(cvItem.getDisplayName());
            lore.addAll(Util.textWrap(civilian, Util.parseColors(civItem.getDescription(civilian.getLocale()))));
            cvItem.setLore(lore);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlaceBlockLogger(BlockPlaceEvent event) {
        ItemStack is = event.getItemInHand();
        if (event.getPlayer() == null || !CVItem.isCivsItem(is)) {
            return;
        }
        CVItem cvItem = CVItem.createFromItemStack(is);
        BlockLogger blockLogger = BlockLogger.getInstance();
        blockLogger.putBlock(Region.idToLocation(Region.blockLocationToString(event.getBlock().getLocation())), cvItem);
    }

    // for hoppers and the like
    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveEvent(InventoryMoveItemEvent event) {
        if (ConfigManager.getInstance().getAllowSharingCivsItems()) {
            return;
        }
        if (!CVItem.isCivsItem(event.getItem())) {
            return;
        }
        if (!(event.getDestination() instanceof PlayerInventory)) {
            event.setCancelled(true);
            if (!event.getSource().getViewers().isEmpty()) {
                HumanEntity humanEntity = event.getSource().getViewers().get(0);
                humanEntity.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslationWithPlaceholders((Player) humanEntity,
                                LocaleConstants.PREVENT_CIVS_ITEM_SHARE));
            }
        }
    }
    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if ("dynmap".equalsIgnoreCase(event.getPlugin().getName())) {
            DynmapHook.dynmapCommonAPI = (DynmapCommonAPI) event.getPlugin();
            DynmapHook.initMarkerSet();
            return;
        }
        if (Constants.PLACEHOLDER_API.equals(event.getPlugin().getName()) &&
                Bukkit.getPluginManager().isPluginEnabled(Constants.PLACEHOLDER_API)) {
            new PlaceHook().register();
            Civs.placeholderAPI = (PlaceholderAPIPlugin) event.getPlugin();
            return;
        }
        if ("MMOItems".equals(event.getPlugin().getName()) &&
                Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            Civs.mmoItems = MMOItems.plugin;
            return;
        }
        if ("DiscordSRV".equals(event.getPlugin().getName()) &&
                Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            Civs.discordSRV = DiscordSRV.getPlugin();
            return;
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if ("dynmap".equalsIgnoreCase(event.getPlugin().getName())) {
            DynmapHook.dynmapCommonAPI = null;
        }
        if ("MMOItems".equals(event.getPlugin().getName()) &&
                !Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            Civs.mmoItems = null;
            return;
        }
        if ("DiscordSRV".equals(event.getPlugin().getName()) &&
                !Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            Civs.discordSRV = null;
            return;
        }
        if (Constants.PLACEHOLDER_API.equals(event.getPlugin().getName())) {
            Civs.placeholderAPI = null;
            return;
        }
    }

    @EventHandler @SuppressWarnings("unused")
    public void onRegionDestroyedEvent(RegionDestroyedEvent event) {
        UnloadedInventoryHandler.getInstance().deleteUnloadedChestInventory(event.getRegion().getLocation());
    }

    @EventHandler(ignoreCancelled = true) @SuppressWarnings("unused")
    public void onItemMoveEvent(InventoryMoveItemEvent event) {
        RegionManager.getInstance().removeCheckedRegion(event.getDestination().getLocation());
        if (event.getDestination().getHolder() instanceof Chest) {
            Location inventoryLocation = ((Chest) event.getDestination().getHolder()).getLocation();
            UnloadedInventoryHandler.getInstance().updateInventoryAtLocation(inventoryLocation);
        }
        if (event.getSource().getHolder() instanceof Chest) {
            Location inventoryLocation = ((Chest) event.getSource().getHolder()).getLocation();
            UnloadedInventoryHandler.getInstance().updateInventoryAtLocation(inventoryLocation);
        }
//        if (ConfigManager.getInstance().getAllowSharingCivsItems()) {
//            return;
//        }
//        if (!CVItem.isCivsItem(event.getItem())) {
//            return;
//        }
//        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCivilianDragItem(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Chest) {
            Location inventoryLocation = ((Chest) event.getView().getTopInventory().getHolder()).getLocation();
            UnloadedInventoryHandler.getInstance().updateInventoryAtLocation(inventoryLocation);
        }
        if (ConfigManager.getInstance().getAllowSharingCivsItems()) {
            return;
        }
        ItemStack dragged = event.getOldCursor();

        if (!CVItem.isCivsItem(dragged) ||
                MenuManager.getInstance().hasMenuOpen(event.getWhoClicked().getUniqueId())) {
            return;
        }

        int inventorySize = event.getInventory().getSize();
        for (int i : event.getRawSlots()) {
            if (i < inventorySize) {
                event.setCancelled(true);
                HumanEntity humanEntity = event.getWhoClicked();
                humanEntity.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslationWithPlaceholders((Player) humanEntity, LocaleConstants.PREVENT_CIVS_ITEM_SHARE));
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        ChatChannel chatChannel = civilian.getChatChannel();
        if (chatChannel.getChatChannelType() == ChatChannel.ChatChannelType.GLOBAL) {
            return;
        }
        event.setCancelled(true);
        if (chatChannel.getChatChannelType() == ChatChannel.ChatChannelType.FRIEND) {
            for (Player recipient : new HashSet<>(event.getRecipients())) {
                if (!civilian.getFriends().contains(recipient.getUniqueId()) &&
                        recipient != player) {
                    event.getRecipients().remove(recipient);
                }
            }
        } else if (chatChannel.getChatChannelType() == ChatChannel.ChatChannelType.LOCAL) {
            for (Player recipient : new HashSet<>(event.getRecipients())) {
                if (player != recipient &&
                        (!recipient.getWorld().equals(player.getWorld()) ||
                        10000 < recipient.getLocation().distanceSquared(player.getLocation()))) {
                    event.getRecipients().remove(recipient);
                }
            }
        } else if (chatChannel.getChatChannelType() == ChatChannel.ChatChannelType.TOWN) {
            Town town = (Town) chatChannel.getTarget();
            if (!town.getRawPeople().containsKey(player.getUniqueId())) {
                civilian.setChatChannel(new ChatChannel(ChatChannel.ChatChannelType.GLOBAL, null));
                return;
            }
            for (Player recipient : new HashSet<>(event.getRecipients())) {
                if (player != recipient && !town.getRawPeople().containsKey(recipient.getUniqueId())) {
                    event.getRecipients().remove(recipient);
                }
            }
        } else if (chatChannel.getChatChannelType() == ChatChannel.ChatChannelType.ALLIANCE) {
            Alliance alliance = (Alliance) chatChannel.getTarget();
            if (!alliance.isInAlliance(civilian.getUuid())) {
                civilian.setChatChannel(new ChatChannel(ChatChannel.ChatChannelType.GLOBAL, null));
                return;
            }
            for (Player recipient : new HashSet<>(event.getRecipients())) {
                if (player != recipient && !alliance.isInAlliance(recipient.getUniqueId())) {
                    event.getRecipients().remove(recipient);
                }
            }
        }
        if (event.getRecipients().isEmpty() || (event.getRecipients().size() == 1 &&
                player.equals(event.getRecipients().iterator().next()))) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "no-recipients").replace("$1", chatChannel.getName(player)));
        } else {
            for (Player currentPlayer : event.getRecipients()) {
                currentPlayer.sendMessage(Util.parseColors(ConfigManager.getInstance().getChatChannelFormat())
                        .replace("$channel$", chatChannel.getName(currentPlayer))
                        .replace("$player$", player.getDisplayName())
                        .replace("$message$", event.getMessage()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true) @SuppressWarnings("unused")
    public void onCivilianClickItem(InventoryClickEvent event) {
        if (event.getClickedInventory() != null) {
            Location inventoryLocation = event.getClickedInventory().getLocation();
            UnloadedInventoryHandler.getInstance().updateInventoryAtLocation(inventoryLocation);
        }
        handleCustomItem(event.getCurrentItem(), event.getWhoClicked().getUniqueId());
        if (ConfigManager.getInstance().getAllowSharingCivsItems()) {
            return;
        }
        boolean shiftClick = event.getClick().isShiftClick() && event.getClickedInventory() != null &&
                event.getClickedInventory().equals(event.getWhoClicked().getInventory());
        shiftClick = shiftClick || event.getClick() == ClickType.NUMBER_KEY;
        boolean dragToChest = event.getClickedInventory() != null &&
                !event.getClickedInventory().equals(event.getWhoClicked().getInventory());

        if (event.getView().getTopInventory().getHolder() instanceof DoubleChest) {
            DoubleChest doubleChest = (DoubleChest) event.getView().getTopInventory().getHolder();
            Location leftLocation = ((Chest) doubleChest.getLeftSide()).getLocation();
            Location rightLocation = ((Chest) doubleChest.getRightSide()).getLocation();
            RegionManager.getInstance().removeCheckedRegion(leftLocation);
            RegionManager.getInstance().removeCheckedRegion(rightLocation);
        } else {
            if (event.getClickedInventory() != null &&
                    event.getClickedInventory().getType() != InventoryType.ENDER_CHEST &&
                    event.getView().getTopInventory().getType() != InventoryType.ENDER_CHEST) {
                try {
                    RegionManager.getInstance().removeCheckedRegion(event.getView().getTopInventory().getLocation());
                } catch (NullPointerException npe) {
                    // Doesn't matter if there's an error here
                }
            }
        }

        ItemStack stackInQuestion = shiftClick ? event.getCurrentItem() : event.getCursor();

        if (stackInQuestion == null || (!shiftClick && !dragToChest)) {
            return;
        }

        if (!CVItem.isCivsItem(stackInQuestion) || MenuManager.getInstance().hasMenuOpen(event.getWhoClicked().getUniqueId())) {
            return;
        }
        HumanEntity humanEntity = event.getWhoClicked();
        event.setCancelled(true);
        humanEntity.sendMessage(Civs.getPrefix() +
                LocaleManager.getInstance().getTranslationWithPlaceholders((Player) humanEntity, LocaleConstants.PREVENT_CIVS_ITEM_SHARE));
    }

    private void handleCustomItem(ItemStack itemStack, UUID uuid) {
        if (!CVItem.isCustomItem(itemStack)) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        CVItem.translateItem(civilian, itemStack);
    }
}
