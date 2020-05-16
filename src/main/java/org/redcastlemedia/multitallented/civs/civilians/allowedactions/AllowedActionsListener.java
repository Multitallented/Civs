package org.redcastlemedia.multitallented.civs.civilians.allowedactions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.effects.RepairEffect;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsSingleton @SuppressWarnings("unused")
public class AllowedActionsListener implements Listener {
    private static AllowedActionsListener allowedActionsListener;

    public static AllowedActionsListener getInstance() {
        if (allowedActionsListener == null) {
            allowedActionsListener = new AllowedActionsListener();
            Bukkit.getPluginManager().registerEvents(allowedActionsListener, Civs.getInstance());
        }
        return allowedActionsListener;
    }

    boolean hasAndDeductXP(final Player player, int levels) {
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return true;
        }

        if (player.getLevel() < levels) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "insufficient-exp"));
            return false;
        } else {
            player.setLevel(player.getLevel() - levels);
            return true;
        }
    }

    // Priority = HIGH so that this gets called _after_ Unbreakable so that the clone we return has the Unbreakable tag set.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEnchantItem(EnchantItemEvent event) {
        final Player player = event.getEnchanter();
        if (player.getGameMode() != GameMode.SURVIVAL || (Civs.perm != null &&
                Civs.perm.has(player, Constants.ADMIN_PERMISSION))) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String localClassName = LocaleManager.getInstance().getRawTranslationWithPlaceholders(player,
                civilian.getCurrentClass().getType() + LocaleConstants.NAME_SUFFIX);

        int enchants = 0;

        int returningLevels = 0;
        Map<Enchantment, Integer> toAdd = new HashMap<>();

        int totalLevels = 0;
        for (Enchantment e : event.getEnchantsToAdd().keySet()) {
            totalLevels += event.getEnchantsToAdd().get(e);
        }
        final int initialCost = event.getExpLevelCost();
        float xpPerLevel = initialCost;
        if (totalLevels > 0) {
            xpPerLevel /= totalLevels;
        }

        enchants = reduceOrRemoveEnchants(event, player, civilian, localClassName, enchants, toAdd, xpPerLevel);

        final ItemStack limitedItem = cloneNewItemStackWithEnchants(event.getItem(), toAdd);

        returningLevels = getReturningLevels(event, enchants, returningLevels, initialCost);

        final int returnedLevels = returningLevels;

        Bukkit.getScheduler().runTaskLater(Civs.getInstance(),
                () -> setCorrectEnchantItemsAndExp(player, limitedItem, returnedLevels), 1);
    }

    private void setCorrectEnchantItemsAndExp(Player player, ItemStack limitedItem, int returnedLevels) {
        if (!player.isOnline() || player.getOpenInventory().getTopInventory().getType() != InventoryType.ENCHANTING) {
            return;
        }
        EnchantingInventory enchantingInventory = (EnchantingInventory) player.getOpenInventory().getTopInventory();

        if (enchantingInventory.getItem() == null || enchantingInventory.getItem().getType() != limitedItem.getType()) {
            return;
        }
        enchantingInventory.setItem(limitedItem);

        if (returnedLevels > 0) {
            if (enchantingInventory.getSecondary() != null) {
                ItemStack lapis = enchantingInventory.getSecondary().clone();
                lapis.setAmount(lapis.getAmount() + returnedLevels);
                enchantingInventory.setSecondary(lapis);
            }

            player.giveExpLevels(returnedLevels);
        }
    }

    private int getReturningLevels(EnchantItemEvent event, int enchants, int returningLevels, int initialCost) {
        if (enchants == 0) {
            returningLevels = event.whichButton() + 1;
        } else {
            switch (event.whichButton()) {
                default:
                case 0:
                    returningLevels = 0;
                    break;
                case 1:
                    if ((float) event.getExpLevelCost() / initialCost <= 1.0F / 2.0F) {
                        returningLevels = 1;
                    }
                    break;
                case 2:
                    if ((float) event.getExpLevelCost() / initialCost <= 1.0F / 3.0F) {
                        returningLevels = 2;
                    } else if ((float) event.getExpLevelCost() / initialCost <= 2.0F / 3.0F) {
                        returningLevels = 1;
                    }
                    break;
            }
        }
        return returningLevels;
    }

    private int reduceOrRemoveEnchants(EnchantItemEvent event, Player player, Civilian civilian, String localClassName, int enchants, Map<Enchantment, Integer> toAdd, float xpPerLevel) {
        for (Enchantment enchantment : event.getEnchantsToAdd().keySet()) {
            int level = event.getEnchantsToAdd().get(enchantment);
            int returnedXP = (int) (0.5F + (xpPerLevel * level));

            if (enchants == 0) {
                int maxEnchantLevel = civilian.getCurrentClass().getMaxEnchantLevel(enchantment);
                if (maxEnchantLevel < level) {
                    event.setExpLevelCost(event.getExpLevelCost() - returnedXP);
                    event.getItem().removeEnchantment(enchantment);

                    if (maxEnchantLevel > 0) {
                        toAdd.put(enchantment, maxEnchantLevel);
                        int addedXP = (int)(0.5F + (xpPerLevel * maxEnchantLevel));
                        event.setExpLevelCost(event.getExpLevelCost() + addedXP);
                        enchants++;
                    }

                    player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                            player, "action-not-allowed").replace("$1", localClassName)
                            .replace("$2", enchantment.getKey().getKey() + " " + level));
                } else {
                    enchants++;
                    toAdd.put (enchantment, level);
                }
            } else {
                event.getItem().removeEnchantment(enchantment);
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                        player, "action-not-allowed").replace("$1", localClassName)
                        .replace("$2", enchantment.getKey().getKey() + " " + level));
            }
        }
        return enchants;
    }

    private void reduceOrRemoveEnchants(ItemStack itemStack, Player player, Civilian civilian, String localClassName) {
        for (Map.Entry<Enchantment, Integer> entry : itemStack.getItemMeta().getEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            int maxEnchantLevel = civilian.getCurrentClass().getMaxEnchantLevel(enchantment);
            if (maxEnchantLevel < level) {
                if (maxEnchantLevel < 1) {
                    itemStack.removeEnchantment(enchantment);
                } else {
                    itemStack.removeEnchantment(enchantment);
                    itemStack.addEnchantment(enchantment, maxEnchantLevel);
                }

                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                        player, "action-not-allowed").replace("$1", localClassName)
                        .replace("$2", enchantment.getKey().getKey() + " " + level));
            }
        }
    }

    @NotNull
    private ItemStack cloneNewItemStackWithEnchants(ItemStack item, Map<Enchantment, Integer> toAdd) {
        final ItemStack limitedItem = item.clone();
        if (!toAdd.isEmpty()) {
            if (limitedItem.getType() == Material.BOOK || limitedItem.getType() == Material.ENCHANTED_BOOK) {
                limitedItem.setType (Material.ENCHANTED_BOOK);
                ItemStack ebook = new ItemStack(limitedItem); // to reset the meta to Storage type
                EnchantmentStorageMeta bookMeta = null;
                try {
                    bookMeta = (EnchantmentStorageMeta) ebook.getItemMeta();
                } catch (Exception e) {
                    Civs.logger.log(Level.WARNING, "Unable to cast bookMeta", e);
                }
                if (bookMeta != null) {
                    for (Map.Entry<Enchantment, Integer> entry : toAdd.entrySet()) {
                        bookMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                    }
                    limitedItem.setItemMeta(bookMeta);
                }
            } else {
                limitedItem.addEnchantments(toAdd);
            }
        }
        return limitedItem;
    }

    private boolean isRepair(final ItemStack slot0, final ItemStack slot1) {
        if (slot0 == null || slot1 == null || slot1.getItemMeta() == null) {
            return false;
        }

        // Repair with same item type, first needs repair, second item with no enchants.
        if (slot0.getType() == slot1.getType() && slot0 instanceof Damageable &&
                ((Damageable) slot0).getDamage() > 0 &&
                !slot1.getItemMeta().hasEnchants())
            return true;

        return RepairEffect.getRequiredReagent(slot0.getType()).contains(slot1.getType());
    }

    // Is set with ClickEvent, when placing in crafting slot of an anvil.
    private static Map <UUID, Integer> prevXP = new HashMap<>();

    @EventHandler (ignoreCancelled = true)
    void closeAnvilMonitor (InventoryCloseEvent event) {
        if (event.getInventory().getType()== InventoryType.ANVIL) {
            prevXP.remove(event.getPlayer().getUniqueId());
        }
    }

    //  Listen to PrepareItemCraftEvent and return one of the books
    @EventHandler (ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.NOTHING || event.getCurrentItem() == null) {
            return;
        }
        Inventory inv = event.getInventory();
        final InventoryAction action = event.getAction();
        boolean isPlace = isPlace(action);
        final Player player = (Player) event.getWhoClicked();

        if (player.getGameMode() != GameMode.SURVIVAL || (Civs.perm != null &&
                Civs.perm.has(player, Constants.ADMIN_PERMISSION))) {
            return;
        }

        // Check if equipping armor
        final int SHIELD_CLICK_SLOT = 45;
        boolean isArmorSlot = event.getSlotType() == InventoryType.SlotType.ARMOR;
        boolean isCraftingInventory = inv.getType() == InventoryType.CRAFTING;
        boolean isArmor = RepairEffect.isArmor(event.getCurrentItem().getType());
        boolean isShield = event.getCurrentItem().getType() == Material.SHIELD;
        boolean isShiftClickWithArmorOrShield = isCraftingInventory &&
                event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
                (isArmor || isShield);
        if ((isArmorSlot && isPlace) ||
                (isCraftingInventory && isPlace && event.getRawSlot() == SHIELD_CLICK_SLOT) ||
                isShiftClickWithArmorOrShield) {
            PlayerInventory playerInventory = player.getInventory();
            if (fixArmorOnEquip(event, action, player, playerInventory)) {
                return;
            }

        }

        boolean isQuickbar = event.getSlotType() == InventoryType.SlotType.QUICKBAR;
        boolean isWeapon = RepairEffect.isWeapon(event.getCurrentItem().getType());
        boolean isShiftClickWithWeapon = isCraftingInventory &&
                event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && isWeapon;
        if (((isQuickbar && isPlace && isWeapon) || isShiftClickWithWeapon) &&
                cancelEventIfItemIsDisallowed(event, player, event.getCurrentItem())) {
            return;
        }
        if (((isQuickbar && isPlace) || (isCraftingInventory &&
                event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) &&
                cancelEventIfPotionIsDisallowed(event, player, event.getCurrentItem())) {
            return;
        }

        /* Sometimes, Bukkit client places item in crafting without giving server event.
         * Hence, add double-check to see if they just crafted an illegal item. If this works well, could remove
         *  other code, but that is more disturbing to players since it appeared to work.
         * Conclusion: note in static hashmap Xp when they place item in anvil (or open it and erase when they close it)
         *   and then restore that if they've crafted something illegal.
         */
        final InventoryType inventoryType = inv.getType();
        if ((inventoryType == InventoryType.ANVIL || inventoryType == InventoryType.MERCHANT) &&
                event.getSlotType() == InventoryType.SlotType.RESULT) {
            handleAnvilCraftResult(event, action, player, inventoryType);
        } else if (inv.getType() == InventoryType.ANVIL && event.getSlotType() == InventoryType.SlotType.CRAFTING) {

            if (isPlace) {
                Integer curXP = player.getLevel();
                prevXP.put(player.getUniqueId(), curXP);
            }
        }
    }

    private void handleAnvilCraftResult(InventoryClickEvent event, InventoryAction action, Player player, InventoryType inventoryType) {
        ItemStack result = event.getCurrentItem();

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String localClassName = LocaleManager.getInstance().getRawTranslationWithPlaceholders(player,
                civilian.getCurrentClass().getType() + LocaleConstants.NAME_SUFFIX);
        String disallowedEnchant = fixOrTestItem(result, civilian, true);
        if (disallowedEnchant != null) {
            switch (action) {
                case DROP_ALL_SLOT:
                case DROP_ONE_SLOT:
                case PICKUP_HALF:
                case PICKUP_ONE:
                case PICKUP_SOME:
                case PICKUP_ALL:
                case MOVE_TO_OTHER_INVENTORY:
                case HOTBAR_MOVE_AND_READD:
                    player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                            "action-not-allowed").replace("$1", localClassName)
                            .replace("$2", disallowedEnchant));
                    event.setCancelled(true);
                    break;
                default:
                    break;
            }
            Integer previousExp = prevXP.get(player.getUniqueId());
            if (previousExp == null && inventoryType == InventoryType.ANVIL) {
                Civs.logger.log(Level.WARNING, "Cannot restore XP after anvil; didnt record on prior to event");
            }
            final int playerXP = (previousExp != null ? previousExp : player.getLevel());
            final PlayerInventory playerInventory = player.getInventory();

            Bukkit.getScheduler().runTaskLater(Civs.getInstance(),
                    () -> fixAnvilCraft(player, event, playerInventory, playerXP), 1L);
        }
    }

    private void fixAnvilCraft(Player player, InventoryClickEvent event, PlayerInventory playerInventory, int playerXP) {
        if (!player.isOnline()) {
            return;
        }
        InventoryType inventoryType = event.getInventory().getType();
        if (player.getOpenInventory().getTopInventory().getType() != inventoryType) {
            return;
        }
        Inventory aInventory = player.getOpenInventory().getTopInventory();
        InventoryView pInventory = player.getOpenInventory();
        Inventory inv = event.getInventory();
        ItemStack[] anvilContents = inv.getContents();
        ItemStack crafted = event.getCurrentItem();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        final ItemStack slot0 = anvilContents[0];
        final ItemStack slot1 = anvilContents[1];
        final int slot0Amount = slot0 != null ? slot0.getAmount() : 0;
        final int slot1Amount = slot1 != null ? slot1.getAmount() : 0;
        int newSlot0Amount = slot0Amount;
        int newSlot1Amount = slot1Amount;

        InventoryAction action = event.getAction();
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                action == InventoryAction.HOTBAR_SWAP ||
                action == InventoryAction.HOTBAR_MOVE_AND_READD) {
            // When shift-click or number, item is not on cursor but already in inventory/hotbar.
            for (int i = 0; i < playerInventory.getSize(); i++) {
                ItemStack fixer = playerInventory.getItem(i);
                if (fixer == null || !crafted.isSimilar(fixer)) {
                    continue;
                }
                int beforeLevels = getTotalEnchantLevels(fixer);
                String disallowed = fixOrTestItem(fixer, civilian, false);

                if (inventoryType == InventoryType.ANVIL) {
                    return;
                } else if (beforeLevels > 0) {
                    // Code duplicated below; modify BOTH
                    float returnFraction = 1.0F - (float) getTotalEnchantLevels (fixer) / beforeLevels;
                    newSlot0Amount = getNewAmount(aInventory, newSlot0Amount, returnFraction, slot0Amount, 0);
                    newSlot1Amount = getNewAmount(aInventory, newSlot1Amount, returnFraction, slot1Amount, 1);
                }
                break;
            }
        } else {
            ItemStack fixer = pInventory.getCursor() ;
            int beforeLevels = getTotalEnchantLevels(fixer);
            fixOrTestItem(fixer, civilian, false);
            pInventory.setCursor (fixer);

            if (inventoryType == InventoryType.ANVIL) {
                return; // don't return ingredients or restore levels
            }
            // TODO: return some ingredients on MERCHANT trades
            float returnFraction = 1.0F - (float)getTotalEnchantLevels (fixer) / beforeLevels;
            newSlot0Amount = getNewSlotAmount(aInventory, newSlot0Amount, returnFraction, slot0Amount, 0);
            newSlot1Amount = getNewSlotAmount(aInventory, newSlot1Amount, returnFraction, slot1Amount, 1);
        }

        // Return Ingredients
        if (slot0 != null) {
            slot0.setAmount(newSlot0Amount); // villager trades often can use either slot
            aInventory.setItem(0, slot0);
        }
        if (slot1 != null) {
            slot1.setAmount(newSlot1Amount); // when repairing with raw materials, sometimes the amount changed
            aInventory.setItem(1, slot1);
        }

        if (inventoryType == InventoryType.ANVIL) {
            player.setLevel(playerXP);
        }
    }

    private int getNewSlotAmount(Inventory aInventory, int newSlot0Amount, float returnFraction, int slot0Amount, int i2) {
        if (slot0Amount > 0) {
            int newAmount = aInventory.getItem(i2).getAmount();
            int used = slot0Amount - newAmount;
            newSlot0Amount = newAmount + (int) (used * returnFraction); // round down
        }
        return newSlot0Amount;
    }

    private int getNewAmount(Inventory aInventory, int newSlot0Amount, float returnFraction, int slot0Amount, int i2) {
        if (slot0Amount > 0) {
            int used = slot0Amount - aInventory.getItem(i2).getAmount();
            newSlot0Amount = aInventory.getItem(i2).getAmount() + (int) (used * returnFraction); // round down
        }
        return newSlot0Amount;
    }

    private boolean fixArmorOnEquip(InventoryClickEvent event,
                                    InventoryAction action,
                                    Player player,
                                    PlayerInventory playerInventory) {
        ItemStack item = null;
        final int SHIELD_SLOT = 40;
        switch (action) {
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
            case SWAP_WITH_CURSOR:
                item = event.getCursor();
                break;
            case MOVE_TO_OTHER_INVENTORY:
                item = event.getCurrentItem();
                if (item == null) {
                    break;
                }
                Material material = item.getType();
                if (RepairEffect.isHelmet(material) && playerInventory.getHelmet() != null)
                    return true;
                if (RepairEffect.isChestplate(material) && playerInventory.getChestplate() != null)
                    return true;
                if (RepairEffect.isLeggings(material) && playerInventory.getLeggings() != null)
                    return true;
                if (RepairEffect.isBoots(material) && playerInventory.getBoots() != null)
                    return true;
                if (material == Material.SHIELD && playerInventory.getItem(SHIELD_SLOT) != null)
                    return true;
                break;
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                item = playerInventory.getItem(event.getHotbarButton());
                break;
            default:
                Civs.logger.log(Level.WARNING, "Unable to process armor move event");
                break;
        }

        if (item == null) {
            return true;
        }
        if (cancelEventIfItemIsDisallowed(event, player, item)) {
            return true;
        }
        cancelEventIfItemHasDisallowedEnchants(event, player, item);
        return false;
    }

    private boolean cancelEventIfPotionIsDisallowed(Cancellable event, Player player, ItemStack item) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof PotionMeta)) {
            return false;
        }
        String localClassName = LocaleManager.getInstance().getRawTranslationWithPlaceholders(player,
                civilian.getCurrentClass().getType() + LocaleConstants.NAME_SUFFIX);
        PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
        PotionEffectType potionEffectType = potionMeta.getBasePotionData().getType().getEffectType();
        if (potionEffectType != null) {
            int level = civilian.getCurrentClass().isPotionEffectAllowed(potionEffectType);
            int potionLevel = potionMeta.getBasePotionData().isUpgraded() ? 2 : 1;
            if (level < potionLevel) {
                event.setCancelled(true);
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "action-not-allowed").replace("$1", localClassName)
                        .replace("$2", potionEffectType.getName()));
                return true;
            }
        }
        for (PotionEffect potionEffect : potionMeta.getCustomEffects()) {
            int level = civilian.getCurrentClass().isPotionEffectAllowed(potionEffect.getType());
            int potionLevel = potionEffect.getAmplifier();
            if (level < potionLevel) {
                event.setCancelled(true);
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "action-not-allowed").replace("$1", localClassName)
                        .replace("$2", potionEffect.getType().getName()));
                return true;
            }
        }
        return false;
    }

    private boolean cancelEventIfItemIsDisallowed(Cancellable event, Player player, ItemStack item) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian.getCurrentClass().isItemAllowed(item.getType())) {
            event.setCancelled(true);
            return true;
        }
        return false;
    }

    private boolean isPlace(InventoryAction action) {
        boolean isPlace = false;
        switch (action) {
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
            case SWAP_WITH_CURSOR:
            case MOVE_TO_OTHER_INVENTORY: // could be..
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                isPlace = true;
                break;
            default:
                break;
        }
        return isPlace;
    }

    static Map <UUID, Long> lastMsg = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onEntityItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        if (player.getGameMode() != GameMode.SURVIVAL || (Civs.perm != null &&
                Civs.perm.has(player, Constants.ADMIN_PERMISSION))) {
            return;
        }
        ItemStack item = event.getItem().getItemStack();

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String disallowedEnchant = fixOrTestItem (item, civilian, true);
        if (disallowedEnchant != null) {
            event.setCancelled (true);

            Long lastTime = lastMsg.get(player.getUniqueId());
            long currTime = System.currentTimeMillis();

            if (lastTime == null || currTime - lastTime > 2000) {
                lastMsg.put(player.getUniqueId(), currTime);
                String localClassName = LocaleManager.getInstance().getRawTranslationWithPlaceholders(player,
                        civilian.getCurrentClass().getType() + LocaleConstants.NAME_SUFFIX);
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "action-not-allowed").replace("$1", localClassName)
                        .replace("$2", disallowedEnchant));
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerHeldItem(PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL || (Civs.perm != null &&
                Civs.perm.has(player, Constants.ADMIN_PERMISSION))) {
            return;
        }
        ItemStack item = player.getInventory().getItem (event.getNewSlot());

        if (item == null) {
            return;
        }
        cancelEventIfItemHasDisallowedEnchants(event, player, item);
    }

    private void cancelEventIfItemHasDisallowedEnchants(Cancellable event, Player player, ItemStack item) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String disallowedEnchant = fixOrTestItem(item, civilian, true);
        if (disallowedEnchant != null) {
            event.setCancelled (true);

            String localClassName = LocaleManager.getInstance().getRawTranslationWithPlaceholders(player,
                    civilian.getCurrentClass().getType() + LocaleConstants.NAME_SUFFIX);
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "action-not-allowed").replace("$1", localClassName)
                    .replace("$2", disallowedEnchant));
        }
    }

    /* Written to handle automatic equipping of armor by a player 1 block away from dispenser. but could
     be extended to include fixing all such acquired items. But that is not consistent with "Fix held items"
     context which allows player to acquire them, just not use them.
     On auto-equip of armor, supplied vector is zero.
     */
    @EventHandler (ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();
        if (!RepairEffect.isArmor(item.getType()) || event.getVelocity().length() != 0D) {
            return;
        }

        Block b = event.getBlock();
        Location loc = b.getLocation();
        Player p = null;

        // Find nearby Player
        if (!(b.getType() == Material.DISPENSER || b.getType() == Material.DROPPER)) {
            return;
        }
        event.setCancelled(true);
    }

    private String fixOrTestItem(ItemStack itemStack, Civilian civilian, boolean testOnly) {
        String disallowed = null;
        for (Map.Entry<Enchantment, Integer> entry : itemStack.getItemMeta().getEnchants().entrySet()) {
            int maxLevel = civilian.getCurrentClass().getMaxEnchantLevel(entry.getKey());
            if (maxLevel < entry.getValue()) {
                disallowed = entry.getKey().getKey().getKey();
                if (!testOnly) {
                    Player player = Bukkit.getPlayer(civilian.getUuid());
                    String localClassName = LocaleManager.getInstance().getRawTranslationWithPlaceholders(player,
                            civilian.getCurrentClass().getType() + LocaleConstants.NAME_SUFFIX);
                    reduceOrRemoveEnchants(itemStack, player, civilian, localClassName);
                }
            }
        }
        return disallowed;
    }

    private int getTotalEnchantLevels (final ItemStack item) {
        int total = 0;
        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasEnchants()) {
            return 0;
        }
        for (Enchantment e : meta.getEnchants().keySet())
            total += meta.getEnchantLevel (e);
        return total;
    }

    public void onDisable() {
        prevXP.clear();
        lastMsg.clear();
    }
}
