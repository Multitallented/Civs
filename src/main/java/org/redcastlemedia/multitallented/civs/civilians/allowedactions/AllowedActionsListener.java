package org.redcastlemedia.multitallented.civs.civilians.allowedactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.scheduler.BukkitRunnable;
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

        Bukkit.getScheduler().runTaskLater(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                setCorrectEnchantItemsAndExp(player, limitedItem, returnedLevels);
            }
        }, 1);
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
    private static Map <UUID, Byte> prevAnvilData = new HashMap<>();

    @EventHandler (ignoreCancelled = true)
    void closeAnvilMonitor (InventoryCloseEvent event) {
        if (event.getInventory().getType()== InventoryType.ANVIL) {
            prevXP.remove (event.getPlayer().getUniqueId());
            prevAnvilData.remove (event.getPlayer().getUniqueId());
        }
    }

    //  Listen to PrepareItemCraftEvent and return one of the books
    @EventHandler (ignoreCancelled = true)
    public void craftMonitor(InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.NOTHING) {
            return;
        }
        ItemStack book = null;
        ItemStack tool = null;
        Inventory inv = event.getInventory();

        final InventoryAction action = event.getAction();
        boolean isPlace = isPlace(action);
        final Player player = (Player) event.getWhoClicked();

        // Check if equipping armor
        final int SHIELD_CLICK_SLOT = 45;
        final int SHIELD_SLOT = 40;
        if ((event.getSlotType() == InventoryType.SlotType.ARMOR && isPlace) ||
                (inv.getType() == InventoryType.CRAFTING && isPlace && event.getRawSlot() == SHIELD_CLICK_SLOT ) ||
                (inv.getType() == InventoryType.CRAFTING && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY &&
                        (MaterialCategory.isArmor (event.getCurrentItem().getType()) ||
                                (event.getCurrentItem().getType() == Material.SHIELD) )
                ))
        {
            // log.info ("Armor change " + action + " in slot " + event.getSlot() + " curr item " + event.getCurrentItem() + " with " + event.getCursor() + " on cursor");
            // log.info ("Found in raw slot " + event.getRawSlot() + ": " + event.getView ().getItem(event.getRawSlot()));
            PlayerInventory pinv = player.getInventory();
            ItemStack item = null;

            // Get new armor item
            switch (action) {
                case PLACE_ALL:
                case PLACE_SOME:
                case PLACE_ONE:
                case SWAP_WITH_CURSOR:
                    item = event.getCursor();
                    break;
                case MOVE_TO_OTHER_INVENTORY:
                    item = event.getCurrentItem();
                    Material m = item.getType();
                    if (MaterialCategory.isHelmet(m) && pinv.getHelmet() != null)
                        return;
                    if (MaterialCategory.isChestplate(m) && pinv.getChestplate() != null)
                        return;
                    if (MaterialCategory.isLeggings(m) && pinv.getLeggings() != null)
                        return;
                    if (MaterialCategory.isBoots(m) && pinv.getBoots() != null)
                        return;
                    if (m == Material.SHIELD && pinv.getItem (SHIELD_SLOT) != null)
                        return;
                    break;
                case HOTBAR_SWAP:
                case HOTBAR_MOVE_AND_READD:
                    item = pinv.getItem (event.getHotbarButton());
                    break;
                default:
                    Civs.logger.log(Level.WARNING, "Unable to process armor move event");
                    break;
            }
            //log.info ("Found new armor: " + item);

            if (item != null)	{
                // BUG on PLACE, HOT: duplicates fixed item in armor slot and on cursor, so try RunLater
                final Player p = player;
                final ItemStack fixMe = item;

                class armorFixer extends BukkitRunnable {
                    @Override
                    public void run() {
                        if ( !player.isOnline()) {
                            log.info (language.get (player, "loggedoff2", "{0} logged off before we could fix armor", player.getName()));
                            return;
                        }
                        PlayerInventory pInventory = p.getInventory();
                        boolean found = false;

                        // Find item
                        ItemStack armor[] = pInventory.getArmorContents();
                        for (int j = 0; j < armor.length; j++) {
                            ItemStack i = armor[j];
                            if (i != null && i.isSimilar (fixMe)) { // apparently cursor is quantity 0 in this case
                                if (getConfig().getBoolean ("Fix held items")) {
                                    fixItem (i, p);
                                    armor [j] = i;
                                    found = true;
                                }
                            }
                        }
                        if (found) {
                            pInventory.setArmorContents(armor);
                        } else {  // might be shield
                            ItemStack shield = pInventory.getItem (SHIELD_SLOT);
                            if (shield != null && shield.isSimilar (fixMe)) {
                                fixItem (shield, p);
                                pInventory.setItem (SHIELD_SLOT, shield);
                            } else
                                log.warning ("Cannot find armor to fix: " + fixMe);
                        }
                    }
                } // end armorFixer class

                if (getConfig().getBoolean ("Fix held items") && !player.hasPermission ("enchlimiter.useillegal"))
                    (new armorFixer()).runTaskLater(this,1);
                else if (fixOrTestItem (item, player, /*test only=*/ true)) {
                    event.setCancelled (true);
                    if (getConfig().getBoolean ("Message on cancel"))
                        player.sendMessage (language.get (player, "cancelled", chatName + ": You don't have permission to do that"));
                }
            }
            return; // armor equip
        }

        /* Sometimes, Bukkit client places item in crafting without giving server event.
         * Hence, add double-check to see if they just crafted an illegal item. If this works well, could remove
         *  other code, but that is more disturbing to players since it appeared to work.
         * Conclusion: note in static hashmap Xp when they place item in anvil (or open it and erase when they close it)
         *   and then restore that if they've crafted something illegal.
         */
        final InventoryType iType = inv.getType();
        if ((iType == InventoryType.ANVIL || iType==InventoryType.MERCHANT) && event.getSlotType() == SlotType.RESULT) {
            // log.info ("Looks like you " + action + " " + event.getSlotType() + " " + event.getCurrentItem() + " with " + event.getCursor() + " on cursor");
            ItemStack[] anvilContents = inv.getContents();
            final ItemStack slot0 = anvilContents[0];
            final ItemStack slot1 = anvilContents[1];
            final int slot0Amount = slot0 != null? slot0.getAmount() : 0;
            final int slot1Amount = slot1 != null? slot1.getAmount() : 0;
            ItemStack result = event.getCurrentItem();

            //log.info ("Crafted from 0: " + slot0);
            //log.info ("Crafted from 1: " + slot1);

            // New feature: infinite anvil
            if (getConfig().getBoolean ("Infinite anvils") && iType == InventoryType.ANVIL) {
                Block anvilBlock = player.getTargetBlock((Set<Material>)null, 6);
                if (anvilBlock != null && anvilBlock.getType() == Material.ANVIL) {
                    //log.info ("Current anvil data: " + anvilBlock.getData());
                    anvilBlock.setData ((byte)(anvilBlock.getData () & 0x03));  // 0=undamaged; bits 0-1 are compass orientation on Block
                } else
                    log.warning ("Cannot find anvil to repair");
            }

            final boolean isRepair = iType == InventoryType.ANVIL && isRepair (slot0, slot1);
            boolean stopThisRepair = false;
            if (isRepair && getConfig().getBoolean ("Stop all repairs")) {
                if ( !player.hasPermission ("enchlimiter.allrepairs")) {
                    stopThisRepair = true;
                } else {
                    log.info ("Allowing repair by " + player.getName());
                    return; // also allows if repair has illegal enchant.
                }
            }

            if (stopThisRepair || hasIllegalAnvilEnchant (result, player))
            {
                switch (action) {
                    case DROP_ALL_SLOT:
                    case DROP_ONE_SLOT:
                    case PICKUP_HALF:
                    case PICKUP_ONE:
                    case PICKUP_SOME:
                    case PICKUP_ALL:
                    case MOVE_TO_OTHER_INVENTORY:
                    case HOTBAR_MOVE_AND_READD:
                        if (getConfig().getBoolean ("Message on cancel"))
                            player.sendMessage (language.get (player, "cancelled", chatName + ": You don't have permission to do that"));

                        log.info (language.get (Bukkit.getConsoleSender(), "attempted3", "{0} almost took result of a disallowed anvil enchant", player.getName()));
                        event.setCancelled (true); // don't allow dropping the result item!
                        return;
                }
                Integer ti = prevXP.get (player.getUniqueId());
                if (ti == null && iType == InventoryType.ANVIL)
                    log.warning ("Cannot restore XP; didn't record on place");
                final int playerXP = (ti != null? ti : player.getLevel());
                final PlayerInventory pinv = player.getInventory();
                final ItemStack crafted = result;
                final boolean doDowngrade = stopThisRepair ? false : getConfig ().getBoolean ("Downgrade in anvil");
                // Add null check for item naming

                if (!doDowngrade) { // going to stop the result taking and put ingredients back

                    if (isRepair && !stopThisRepair && hasIllegalAnvilEnchant (result, player)) { // check to see if we should allow it.
                        if ( !getConfig().getBoolean ("Stop repairs") || player.hasPermission ("enchlimiter.repairs")) {
                            log.info ("Allowing repair by " + player.getName() + " of " + slot0.getType());
                            return;
                        }
                    }

                    // Repair anvil before it is destroyed and can't return item in runLater task
                    if (iType == InventoryType.ANVIL) {
                        Block anvilBlock = player.getTargetBlock((Set<Material>)null, 6);
                        if (anvilBlock != null && anvilBlock.getType() == Material.ANVIL) {
                            Byte pData = prevAnvilData.get (player.getUniqueId());
                            if (pData != null) {
                                anvilBlock.setData (pData);
                                // log.info ("restored anvil data to " + pData);
                            } else
                                log.warning ("Don't have stored anvil repair state to restore");
                        } else
                            log.warning ("Cannot find anvil to repair");
                    }
                }

                class anvilUndoer extends BukkitRunnable {
                    @Override
                    public void run() {
                        if ( !player.isOnline()) {
                            log.info (language.get (player, "loggedoff", "{0} logged off before we could cancel anvil enchant", player.getName()));
                            return;
                        }
                        if (player.getOpenInventory().getTopInventory().getType() != iType) {
                            log.warning (language.get (player, "theft", "{0} closed inventory or anvil died and got an illegal item: {1}", player.getName(), crafted));
                            return;
                        }
                        Inventory aInventory = player.getOpenInventory().getTopInventory();
                        InventoryView pInventory = player.getOpenInventory();
                        int newSlot0Amount = slot0Amount;
                        int newSlot1Amount = slot1Amount;

                        // Execute cancel
                        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
                                action == InventoryAction.HOTBAR_SWAP ||
                                action == InventoryAction.HOTBAR_MOVE_AND_READD)
                        {
                            // When shift-click or number, item is not on cursor but already in inventory/hotbar.
                            boolean found = false;
                            // log.info ("Looking in pinv for: " + crafted + " in inv[" + pinv.getSize() + "]");
                            for (int i = 0; i < pinv.getSize(); i++) {
                                // Use isSimilar because result is quantity zero (0)!
                                if (crafted.isSimilar (pinv.getItem (i))) {
                                    if (!doDowngrade)
                                        pinv.clear (i);
                                    else { // downgrade it
                                        ItemStack fixer = pinv.getItem(i);
                                        int beforeLevels = getTotalEnchantLevels (fixer);
                                        fixAnvilItem (fixer, player);
                                        pinv.setItem (i, fixer);

                                        if (iType == InventoryType.ANVIL)
                                            return; // don't return ingredients or restore levels
                                        else { 	// return some ingredients on MERCHANT trades
                                            // Code duplicated below; modify BOTH
                                            float returnFraction = 1.0F - (float)getTotalEnchantLevels (fixer) / beforeLevels;
                                            //*DEBUG*/log.info ("returning " + returnFraction + " of ingredients");
                                            if (slot0Amount > 0) {
                                                int used = slot0Amount - aInventory.getItem (0).getAmount();
                                                newSlot0Amount = aInventory.getItem (0).getAmount() + (int)(used * returnFraction); // round down
                                            }
                                            if (slot1Amount > 0) {
                                                int used = slot1Amount - aInventory.getItem (1).getAmount();
                                                newSlot1Amount = aInventory.getItem (1).getAmount() + (int)(used * returnFraction); // round down
                                            }
                                            // fall through to Return Ingredients
                                        }
                                    }
                                    //log.info ("Found and removed illegal item in raw slot " + i);
                                    found = true;
                                    break;
                                } else if (pinv.getItem(i) != null) {
                                    //	log.info (i + ": inv " + pinv.getItem(i) + " is not result ");
                                }
                            }
                            if (!found)
                                log.warning ("could not find illegal result in inventory on shift-click by " + player.getName());
                        } else if (doDowngrade) {
                            ItemStack fixer = pInventory.getCursor() ;
                            int beforeLevels = getTotalEnchantLevels (fixer);
                            fixAnvilItem (fixer, player);
                            pInventory.setCursor (fixer);

                            if (iType == InventoryType.ANVIL)
                                return; // don't return ingredients or restore levels
                            else { 	// TODO: return some ingredients on MERCHANT trades
                                // Code duplicated above; modify BOTH
                                float returnFraction = 1.0F - (float)getTotalEnchantLevels (fixer) / beforeLevels;
                                //*DEBUG*/log.info ("returning " + returnFraction + " of ingredients");
                                if (slot0Amount > 0) {
                                    int newAmount = aInventory.getItem (0).getAmount();
                                    int used = slot0Amount - newAmount;
                                    newSlot0Amount = newAmount + (int)(used * returnFraction); // round down
                                }
                                if (slot1Amount > 0) {
                                    int newAmount = aInventory.getItem (1).getAmount();
                                    int used = slot1Amount - newAmount;
                                    newSlot1Amount = newAmount + (int)(used * returnFraction); // round down
                                }
                                // fall through to Return Ingredients
                            }
                        } else
                            pInventory.setCursor (null); // take away illegal item in hand

                        // Return Ingredients
                        if (slot0 != null) {
                            slot0.setAmount (newSlot0Amount); // villager trades often can use either slot
                            aInventory.setItem(0, slot0); // return craft ingredient 1
                        }
                        if (slot1 != null) {
                            slot1.setAmount (newSlot1Amount); // when repairing with raw materials, sometimes the amount changed
                            aInventory.setItem(1, slot1); // return craft ingredient 2
                        }
                        //log.fine ("returned slot1: " + slot1);

                        if (iType == InventoryType.ANVIL && getConfig().getBoolean ("Restore levels"))
                            player.setLevel (playerXP);

                        if (doDowngrade) return;
                        if (getConfig().getBoolean ("Message on cancel"))
                            player.sendMessage (language.get (player, "cancelled", chatName + ": You don't have permission to do that"));

                        log.info (language.get (Bukkit.getConsoleSender(), "attempted3", "{0} almost took result of a disallowed anvil enchant", player.getName()));
                    }
                }
                if (player != null)
                    (new anvilUndoer()).runTask(this);
            }
        }
        else if (inv.getType()== InventoryType.ANVIL && event.getSlotType() == InventoryType.SlotType.CRAFTING) {
            ItemStack[] anvilContents = inv.getContents();
            ItemStack slot0 = anvilContents[0];
            ItemStack slot1 = anvilContents[1];

            /*** DEBUG
             Block anvilBlock = player.getTargetBlock(null, 6);
             if (anvilBlock != null && anvilBlock.getType() == Material.ANVIL) {
             log.info ("Current anvil data: " + anvilBlock.getData());
             }
             ***/

            if (isPlace) {
                // Remember for later, in case we need it.
                Integer curXP = player.getLevel();	// need to get/set levels, not XP (progress to next)
                prevXP.put (player.getUniqueId(), curXP);
                Block anvil = player.getTargetBlock ((Set<Material>)null,6);
                if (anvil != null && anvil.getType() == Material.ANVIL)
                    prevAnvilData.put (player.getUniqueId(), anvil.getData());
                //*DEBUG*/ log.info ("saved XP at " + curXP);

                // log.info ("Placed a " + event.getCursor() + " in slot " + event.getRawSlot());
                //log.info ("currentItem: " +  event.getCurrentItem());
                if (event.getRawSlot() == 1) {
                    //log.info ("reset slot1 from " + slot1 + " to " + event.getCursor());
                    slot1 = event.getCursor();
                } else if (event.getRawSlot() == 0) {
                    //log.info ("reset slot0 from " + slot0 + " to " + event.getCursor());
                    slot0 = event.getCursor();
                }
            }
            // 1 is right slot of anvil
            if (slot1 != null /*&& slot1.getType() == Material.ENCHANTED_BOOK */)
                book = slot1;
            // 0 is left slot of Anvil
            if (slot0 != null /* && slot0.getType() == Material.ENCHANTED_BOOK */)
                tool = slot0;
            // log.info ("Found book: " + book + "; tool: " + tool);
        }
        else if (inv.getType()== InventoryType.ANVIL && action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            ItemStack[] anvilContents = inv.getContents();
            ItemStack slot0 = anvilContents[0]; tool = slot0;
            ItemStack slot1 = anvilContents[1]; book = slot1;

            if (event.getSlotType() == InventoryType.SlotType.CONTAINER ||
                    event.getSlotType() == InventoryType.SlotType.QUICKBAR) {
                /** DEBUG
                 log.info ("Potential swap to Anvil. Currently in slot0: " + slot0);
                 log.info ("slot1: " + slot1);
                 log.info ("clicked: " + event.getCurrentItem());
                 **/

                /** Find if one slot is open, then assume current item will become it. Start from slot 0
                 *   Know that getCurrentItem() is not null bcs it is MOVE_TO_OTHER_INV action
                 */
                if (slot0 == null || slot0.getType() == Material.AIR) {
                    tool = event.getCurrentItem();
                } else if (slot1 == null || slot1.getType() == Material.AIR) {
                    book = event.getCurrentItem();
                }
            }
        }
        if (book != null && tool != null && isPlace)
        {
            Map<Enchantment, Integer> disallowedEnchants = getDisallowedAnvilEnchants (tool.getType(), player);
            boolean disallowed = false;
            boolean bookPlusBook = (book.getType() == Material.ENCHANTED_BOOK && tool.getType() == Material.ENCHANTED_BOOK);
            ArrayList<Enchantment> violators = new ArrayList<Enchantment>();

            ItemMeta meta = book.getItemMeta();
            if ( !(meta instanceof EnchantmentStorageMeta)) {
                if (tool.getType() == book.getType() && tool.getDurability() == 0 && book.getDurability() == 0) {
                    // log.info ("Enchant combo attempt of " + tool.getType());
                    for (Enchantment e: book.getEnchantments().keySet()) {
                        if (disallowedEnchants.containsKey (e)) {
                            if (disallowedEnchants.get(e) <= book.getEnchantmentLevel(e) ) {
                                disallowed = true; // second item alone too high
                                violators.add (e);
                            } else if (tool.getEnchantmentLevel (e) >= disallowedEnchants.get(e) - 1) {
                                disallowed = true;	// trying to boost enchant of existing item at limit
                                violators.add (e);
                            }
                        }
                    }
                }
            } else {
                EnchantmentStorageMeta bookStore = (EnchantmentStorageMeta)meta;
                for (Enchantment e: bookStore.getStoredEnchants().keySet()) {
                    //*DEBUG*/log.info ("testing for " + e + "-" + bookStore.getStoredEnchantLevel(e));
                    if (disallowedEnchants.containsKey (e)) {
                        if (disallowedEnchants.get(e) <= bookStore.getStoredEnchantLevel(e) ) {
                            disallowed = true; // book too high
                            violators.add (e);
                        }
                        else if (bookPlusBook) {
                            EnchantmentStorageMeta book1 = (EnchantmentStorageMeta)tool.getItemMeta();
                            if (book1.getStoredEnchantLevel (e) >= disallowedEnchants.get(e) - 1) {
                                disallowed = true;	// trying to boost one book with another
                                violators.add (e);
                            }
                        } else if (tool.getEnchantmentLevel (e) >= disallowedEnchants.get(e) - 1) {
                            disallowed = true;	// trying to boost enchant of existing item at limit
                            violators.add (e);
                        }
                    }
                }
            }
            //*DEBUG*/log.info ("Found violations of " + violators);
            if (event.getWhoClicked().hasPermission ("enchlimiter.disallowed"))
                disallowed = false;

            if (disallowed)
            {
                final ItemStack slot0 = tool.clone(), slot1 = book;
                final boolean disallowedEntry = disallowed;

                if (!disallowed && player.hasPermission ("enchlimiter.books")) {
                    log.info (language.get (Bukkit.getConsoleSender(), "bookEnch", "Permitting {0} to enchant book+book", player.getName()));
                    return;
                }

                class BookReturner extends BukkitRunnable {
                    @Override
                    public void run() {
                        if ( !player.isOnline()) {
                            log.info (language.get (player, "loggedoff", "{0} logged off before we could cancel anvil enchant", player.getName()));
                            return;
                        }
                        if (player.getOpenInventory().getTopInventory().getType() != InventoryType.ANVIL) {
                            //*DEBUG*/log.info (player.getName() + " closed inventory before enchant occurred");
                            return;
                        }
                        AnvilInventory aInventory = (AnvilInventory)player.getOpenInventory().getTopInventory();
                        InventoryView pInventory = player.getOpenInventory();

                        // Make sure we should still do this, that anvil still ready
                        boolean stop = false;
                        if (aInventory.getItem (0) == null || !(aInventory.getItem (0).isSimilar (slot0))) {
                            //*DEBUG*/log.info ("removed " + slot0.getType() + " before enchant occurred; instead found "+ aInventory.getItem (0));
                            //*DEBUG*/log.info ("which is " + (!(aInventory.getItem (0).isSimilar (slot0)) ? "NOT ":"") + "similar to "+ slot0);
                            stop = true;
                        }
                        if (aInventory.getItem (1) == null || !aInventory.getItem (1).isSimilar (slot1)) {
                            //*DEBUG*/log.info ("removed book before enchant occurred");
                            stop = true;
                        }
                        if (pInventory.getCursor() != null && pInventory.getCursor().getType() != Material.AIR) {
                            //*DEBUG*/log.info ("Non empty cursor slot " + pInventory.getCursor().getType()  + " for enchant result");
                            stop = true;
                        }
                        if (stop) {
                            // log.info (player.getName() + " already stopped book+book enchant");
                            return;
                        }
                        // Execute cancel
                        pInventory.setCursor (slot0); // return to user
                        aInventory.clear(0); // remove  book from tool slot

                        if (getConfig().getBoolean ("Message on cancel"))
                            player.sendMessage (language.get (player, "cancelled", chatName + ": You don't have permission to do that"));

                        if ( !disallowedEntry)
                            log.info (language.get (Bukkit.getConsoleSender(), "attempted", "{0} just tried to do a book+book enchant", player.getName()));
                        else
                            log.info (language.get (Bukkit.getConsoleSender(), "attempted2", "{0} just tried to do a disallowed anvil enchant", player.getName()));
                    }
                }
                if (player != null) {
                    if (getConfig ().getBoolean ("Downgrade in anvil")) {
                        for (Enchantment e : violators) {
                            int level = disallowedEnchants.get (e);
                            if (level == 1)
                                player.sendMessage (language.get (player, "removeWarn", chatName + ": {0} will be removed", e.getName()));
                            else
                                player.sendMessage (language.get (player, "reduceWarn", chatName + ": {0} will be reduced to {1}", e.getName(), level));
                        }
                        player.sendMessage (language.get (player, "anvilWarn", chatName + ": Some enchants will be limited/removed; see above"));
                    } else
                        (new BookReturner()).runTask(this);
                }
            }
        }
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


    @EventHandler (ignoreCancelled = true)
    void itemMonitor (ItemSpawnEvent event) {
        if (getConfig().getBoolean ("Fix spawned items") && fixItem (event.getEntity().getItemStack(), null)) {
            // log.info ("Modified enchants on spawned " + event.getEntity().getItemStack().getType());
        }
    }

    static Map <UUID, Long> lastMsg = new HashMap<UUID, Long>();

    @EventHandler (ignoreCancelled = true)
    void pickupMonitor (PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem().getItemStack();

        if ( !getConfig().getBoolean ("Stop pickup") && player.hasPermission ("enchlimiter.useillegal"))
            return;

        if (fixOrTestItem (item, player, /* testOnly=*/ getConfig().getBoolean ("Stop pickup"))) {
            if (getConfig().getBoolean ("Stop pickup")) {
                event.setCancelled (true);

                Long lastTime = lastMsg.get (player.getUniqueId());
                long currTime = System.currentTimeMillis();

                // not if in last 2 seconds
                if (lastTime == null || currTime - lastTime > 2000) {
                    lastMsg.put (player.getUniqueId(), currTime);
                    player.sendMessage (language.get (player, "disallowedPickup1",
                            chatName + ": can't pickup {0} with disallowed enchant(s)",
                            item.getType() ));
                }
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    void heldMonitor (PlayerItemHeldEvent event) {
        final Player p = event.getPlayer();
        ItemStack item = p.getInventory().getItem (event.getNewSlot());

        if (item == null)
            return;
        else if (getConfig().getBoolean ("Fix held items"))
            fixItem (item, p);
        else if (! p.hasPermission ("enchlimiter.useillegal"))
        {
            // stop hold
            /* Bukkit BUG: sometimes don't get the hold event (ex. when player spams the hotbutton key).
             *  Effect is that player can hold/use/shift-click an illegal item if they try hard enough.
             *  Recommend "Fix held items"= true, since item is fixed and multiple attempts don't matter.
             */
            if (fixOrTestItem (item, p, /*test only=*/ true)) {
                event.setCancelled (true);
                if (getConfig().getBoolean ("Message on cancel hold"))
                    p.sendMessage (language.get (p, "cancelledHold", chatName + ": You don't have permission to use that"));
            }
        }
    }

    /** Written to handle automatic equipping of armor by a player 1 block away from dispenser. but could
     be extended to include fixing all such acquired items. But that is not consistent with "Fix held items"
     context which allows player to acquire them, just not use them.
     On auto-equip of armor, supplied vector is zero.
     **/
    @EventHandler (ignoreCancelled = true)
    void transferMonitor (BlockDispenseEvent event) {
        ItemStack item = event.getItem();
        if (!MaterialCategory.isArmor (item.getType()) || event.getVelocity().length() != 0D)
            return; // nothing to do.

        Block b = event.getBlock();
        Location loc = b.getLocation();
        Player p = null;

        // Find nearby Player
        if ( !(b.getType() == Material.DISPENSER || b.getType() == Material.DROPPER)) {
            log.warning ("BlockDispenseEvent from a non-dispenser " + b);
            return;
        }
        // Bukkit bug: DirectionalContainer does not support UP/DOWN
        BlockFace facing = new DirectionalContainer(b.getType(), (byte)(b.getData() & 0x7)).getFacing();
        if ((b.getData() & 0x7) == 0)
            facing = BlockFace.DOWN;
        else if ((b.getData() & 0x7) == 1)
            facing = BlockFace.UP;
        loc.add (facing.getModX(), facing.getModY(), facing.getModZ());
        // log.info ("Checking " + facing + " (data " + b.getData() + ") location " + loc);
        for (Entity e: loc.getChunk().getEntities()) {
            if (loc.equals (e.getLocation().getBlock().getLocation())) { // within range
                // log.info ("Found adjacent entity " + e.getType());
                if (e instanceof Player) {
                    p = (Player)e;
                    break;
                }
            }
        }
        if (p != null) {
            final Player player = p;
            final ItemStack fixMe = item;

            class armorFixer extends BukkitRunnable {
                @Override
                public void run() {
                    if ( !player.isOnline()) {
                        log.info (language.get (player, "loggedoff2", "{0} logged off before we could fix armor", player.getName()));
                        return;
                    }
                    PlayerInventory pInventory = player.getInventory();

                    // Find item
                    ItemStack armor[] = pInventory.getArmorContents();
                    for (int j = 0; j < armor.length; j++) {
                        ItemStack i = armor[j];
                        if (i != null && i.equals (fixMe)) {
                            if (getConfig().getBoolean ("Fix held items"))
                                fixItem (i, player);
                            else if (! player.hasPermission ("enchlimiter.useillegal")) { //stop equipage
                                if ( !pInventory.addItem (i).isEmpty()) {
                                    // unable to store item
                                    player.getWorld().dropItem (player.getLocation(), i);
                                    log.info (player.getName() + " inventory full; dropping illegal dispensed " + i.getType());
                                }
                                armor [j] = null;
                                pInventory.setArmorContents (armor);
                                // no need to message since user may not have been expecting it.
                            }
                        }
                    }
                }
            }
            (new armorFixer()).runTask(this);
        }
    }

    // returns true if has enchants on global or anvil disallowed list or multiple when not allowed
    boolean hasIllegalAnvilEnchant (final ItemStack item, final Player p) {
        boolean limitMultiples = getConfig().getBoolean ("Limit Multiples", true) &&
                (p == null || ! p.hasPermission ("enchlimiter.multiple"));
        if (item == null || !item.hasItemMeta())
            return false;

        // Get List of disallowed enchants for that item
        Map<Enchantment, Integer> disallowedEnchants = getDisallowedAnvilEnchants (item.getType(), p);

        ItemMeta meta = item.getItemMeta();
        if ( meta instanceof EnchantmentStorageMeta) {
            if ( !((EnchantmentStorageMeta)meta).hasStoredEnchants())
                return false; // nothing to do
            else
                return fixOrTestBook (disallowedEnchants, item, p, true); // testonly
        }

        if ( !item.getItemMeta().hasEnchants() || (!limitMultiples && disallowedEnchants.isEmpty()))
            return false;  // nothing to do; leave event alive for another plugin

        // Check for multiples
        if (limitMultiples && item.getEnchantments().size() > 1)
            return true;
        else if (p != null && p.hasPermission ("enchlimiter.disallowed"))
            return false;	// no need to check

        // Check disallowed enchants
        for (Enchantment ench : item.getEnchantments().keySet()) {
            int level = item.getEnchantments().get(ench);

            if (disallowedEnchants.containsKey (ench) && level >= disallowedEnchants.get (ench) &&
                    (p == null || ! p.hasPermission ("enchlimiter.disallowed")) )
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private void updateInventory (Player p) {
        try {
            if (Player.class.getMethod ("updateInventory") != null)
                p.updateInventory();
        } catch (NoSuchMethodException ex)  {}
    }

    // returns true if item has enchants on disallowed list or multiple when not allowed
    boolean fixItem (ItemStack item, Player p) {
        return fixOrTestItem (item, p, false);
    }
    boolean fixAnvilItem (ItemStack item, Player p) {
        return fixOrTestAnvilItem (item, p, false);
    }
    boolean fixOrTestItem (ItemStack item, Player p, boolean testOnly) {
        if (item == null) return false;

        Map<Enchantment, Integer> disallowedEnchants = getDisallowedEnchants (item.getType(), p);
        if (getConfig().getBoolean ("Apply_on_global_check.anvil"))
        {
            Map<Enchantment, Integer> disallowedNewEnchants = getDisallowedAnvilEnchants (item.getType(), p);

            for (Enchantment e : disallowedNewEnchants.keySet())
                if ( !disallowedEnchants.containsKey (e))
                    disallowedEnchants.put (e, disallowedNewEnchants.get(e));
                else {
                    if (getConfig().getBoolean ("Apply_on_global_check.restrictive")) {
                        // apply highest level
                        if (disallowedNewEnchants.get (e) > disallowedEnchants.get (e))
                            disallowedEnchants.put (e, disallowedNewEnchants.get(e));
                    } else { // set lowest level
                        if (disallowedNewEnchants.get (e) < disallowedEnchants.get (e))
                            disallowedEnchants.put (e, disallowedNewEnchants.get(e));
                    }
                }
        }
        if (getConfig().getBoolean ("Apply_on_global_check.table"))
        {
            Map<Enchantment, Integer> disallowedNewEnchants = getDisallowedTableEnchants (item.getType(), p);

            for (Enchantment e : disallowedNewEnchants.keySet())
                if ( !disallowedEnchants.containsKey (e))
                    disallowedEnchants.put (e, disallowedNewEnchants.get(e));
                else {
                    if (getConfig().getBoolean ("Apply_on_global_check.restrictive")) {
                        // apply highest level
                        if (disallowedNewEnchants.get (e) > disallowedEnchants.get (e))
                            disallowedEnchants.put (e, disallowedNewEnchants.get(e));
                    } else { // set lowest level
                        if (disallowedNewEnchants.get (e) < disallowedEnchants.get (e))
                            disallowedEnchants.put (e, disallowedNewEnchants.get(e));
                    }
                }
        }

        return fixOrTestItem (disallowedEnchants, item, p, testOnly);
    }
    boolean fixOrTestAnvilItem (ItemStack item, Player p, boolean testOnly) {
        if (item == null) return false;
        Map<Enchantment, Integer> disallowedEnchants = getDisallowedAnvilEnchants (item.getType(), p);
        return fixOrTestItem (disallowedEnchants, item, p, testOnly);
    }
    // NOTE: PlayerPickup calls this: global only
    //  if testOnly is false, will remove disallowed enchants
    boolean fixOrTestItem (Map<Enchantment, Integer> disallowedEnchants, ItemStack item, Player p, boolean testOnly) {
        boolean limitMultiples = getConfig().getBoolean ("Limit Multiples", true) &&
                (p == null || ! p.hasPermission ("enchlimiter.multiple"));

        int enchants = 0;

        ItemMeta meta = item.getItemMeta();
        if ( meta instanceof EnchantmentStorageMeta) {
            if ( !((EnchantmentStorageMeta)meta).hasStoredEnchants())
                return false; // nothing to do
            else
                return fixOrTestBook (disallowedEnchants, item, p, testOnly);
        }

        if (meta == null || !meta.hasEnchants() || (!limitMultiples && disallowedEnchants.isEmpty()))
            return false;  // nothing to do; leave event alive for another plugin
        boolean modified = false;

        Map<Enchantment,Integer> onItem = new HashMap<Enchantment, Integer>();
        onItem.putAll (item.getEnchantments()); // to avoid modifying while iterating

        for (Enchantment ench : onItem.keySet()) {
            int level = onItem.get(ench);

            if ( !limitMultiples || enchants == 0) {
                if (disallowedEnchants.containsKey (ench) && level >= disallowedEnchants.get (ench) &&
                        (p == null || ! p.hasPermission ("enchlimiter.disallowed")) )
                {
                    if (testOnly) {
                        return true;
                    }
                    else
                        modified = true;

                    item.removeEnchantment (ench);
                    // Add back at lower level, if possible
                    if (disallowedEnchants.get (ench) > 1)
                        item.addEnchantment (ench, disallowedEnchants.get (ench) - 1);

                    if (getConfig().getBoolean ("Message on disallowed", true) && p != null)
                        p.sendMessage (language.get (p, "disallowed3",
                                chatName + " removed disallowed {0}-{1} from {2}", ench.getName(), level, item.getType() ));
                }
                else {
                    enchants++;
                    //*DEBUG*/log.info ("Added " + ench + "s-" + level + " to " +item.getType());
                }
            } else {
                if (testOnly) {
                    return true;
                }
                else
                    modified = true;

                item.removeEnchantment (ench);

                if (getConfig().getBoolean ("Message on limit", true) && p != null)
                    p.sendMessage (language.get (p, "limited3",
                            chatName + " removed multiple {0}-{1} from {2}", ench.getName(), level, item.getType() ));
            }
        }
        return modified;
    }
    // returns true if book has stored enchants on disallowed list or multiple when not allowed
    //  if "Stop pickup" is NOT set, will remove disallowed enchants
    //  expects only to be called with an ENCHANTED_BOOK
    boolean fixOrTestBook (Map<Enchantment, Integer> disallowedEnchants, ItemStack item, Player p, boolean testOnly) {
        if (item.getType() != Material.ENCHANTED_BOOK) {
            log.warning ("fixOrTestBook called with " + item.getType());
            return false;
        }
        boolean limitMultiples = getConfig().getBoolean ("Limit Multiples", true) &&
                (p == null || ! p.hasPermission ("enchlimiter.multiple"));

        int enchants = 0;
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta)item.getItemMeta();

        if ( !meta.hasStoredEnchants() || (!limitMultiples && disallowedEnchants.isEmpty()))
            return false;  // nothing to do; leave event alive for another plugin
        boolean modified = false;

        Map<Enchantment,Integer> onItem = new HashMap<Enchantment, Integer>();
        onItem.putAll (meta.getStoredEnchants()); // to avoid modifying while iterating

        for (Enchantment ench : onItem.keySet()) {
            int level = onItem.get(ench);

            if ( !limitMultiples || enchants == 0) {
                if (disallowedEnchants.containsKey (ench) && level >= disallowedEnchants.get (ench) &&
                        (p == null || ! p.hasPermission ("enchlimiter.disallowed")) )
                {
                    if (testOnly) {
                        return true;
                    }
                    else
                        modified = true;

                    meta.removeStoredEnchant (ench);
                    // Add back at lower level, if possible
                    if (disallowedEnchants.get (ench) > 1)
                        meta.addStoredEnchant (ench, disallowedEnchants.get (ench) - 1, true);

                    if (getConfig().getBoolean ("Message on disallowed", true) && p != null)
                        p.sendMessage (language.get (p, "disallowed3",
                                chatName + " removed disallowed {0}-{1} from {2}", ench.getName(), level, item.getType() ));
                }
                else {
                    enchants++;
                    //*DEBUG*/log.info ("Added " + ench + "s-" + level + " to " +item.getType());
                }
            } else {
                if (testOnly) {
                    return true;
                }
                else
                    modified = true;

                meta.removeStoredEnchant (ench);

                if (getConfig().getBoolean ("Message on limit", true) && p != null)
                    p.sendMessage (language.get (p, "limited3",
                            chatName + " removed multiple {0}-{1} from {2}", ench.getName(), level, item.getType() ));
            }
        }
        if (modified)
            item.setItemMeta (meta);

        return modified;
    }


    /*
     * Following getDisallowedEnchants (1) returns only for the global "Disallowed enchants"
     *   to find disallowed in an anvil, use getDisallowedAnvilEnchants()
     *   to find disallowed in an enchantment table, use getDisallowedTableEnchants()
     *     which each return the global *plus* their specific items.
     */
    // Assumes correcting formed string, either a material or a permitted "ALL_"
    private Map<Enchantment, Integer> getDisallowedEnchants (String matString)
    {
        return getDisallowedEnchants (getConfig().getConfigurationSection ("Disallowed enchants"), matString);
    }
    private Map<Enchantment, Integer> getDisallowedAnvilEnchants (String matString)
    {
        HashMap<Enchantment, Integer> results = new HashMap<Enchantment, Integer>();
        results.putAll (getDisallowedEnchants (getConfig().getConfigurationSection ("Disallowed enchants"), matString));
        results.putAll (getDisallowedEnchants (getConfig().getConfigurationSection ("Disallowed anvil enchants"), matString));
        return results;
    }
    private Map<Enchantment, Integer> getDisallowedTableEnchants (String matString)
    {
        HashMap<Enchantment, Integer> results = new HashMap<Enchantment, Integer>();
        results.putAll (getDisallowedEnchants (getConfig().getConfigurationSection ("Disallowed enchants"), matString));
        results.putAll (getDisallowedEnchants (getConfig().getConfigurationSection ("Disallowed table enchants"), matString));
        return results;
    }
    private Map<Enchantment, Integer> getDisallowedEnchants (ConfigurationSection cs, String matString)
    {
        HashMap<Enchantment, Integer> results = new HashMap<>();

        if (cs == null || !cs.isConfigurationSection (matString))
            return results; // an empty list rather than null

        for (String enchantString : cs.getConfigurationSection (matString).getKeys (false)) {
            int level = cs.getInt (matString + "." + enchantString);
            if (level < 1) {
                log.warning (cs.getCurrentPath()+"."+matString + "." + enchantString + ": " + level + "<- Unsupported enchant level");
                continue;
            }
            Enchantment enchant = null;
            if (enchantString.equals ("ALL")) {
                for (Enchantment e : Enchantment.values()) {
                    results.put (e, level);
                }
            }
            else if (enchantString.startsWith ("UNKNOWN_ENCHANT_")) {
                try {
                    final int offset = new String ("UNKNOWN_ENCHANT_").length();
                    int enchID = Integer.parseInt (enchantString.substring (offset));
                    enchant = Enchantment.getById (enchID);
                } catch (Exception ex) {
                    enchant = null;
                }
            }
            else {
                enchant = Enchantment.getByName (enchantString);
            }
            if (enchant == null && !enchantString.equals ("ALL"))
                log.warning (cs.getCurrentPath()+"." +matString + ": Unknown enchantment '" + enchantString+ "'. Refer to http://bit.ly/EnchLimit");
            else
                results.put (enchant, level);
        }
        return results;
    }
    private Map<Enchantment, Integer> getDisallowedEnchants (Material m, Player p)
    {
        return getDisallowedEnchants (getConfig().getConfigurationSection ("Disallowed enchants"), m, p);
    }
    private Map<Enchantment, Integer> getDisallowedAnvilEnchants (Material m, Player p)
    {
        HashMap<Enchantment, Integer> results = new HashMap<Enchantment, Integer>();
        results.putAll (getDisallowedEnchants (getConfig().getConfigurationSection ("Disallowed enchants"), m, p));
        results.putAll (getDisallowedEnchants (getConfig().getConfigurationSection ("Disallowed anvil enchants"), m, p));
        return results;
    }
    private Map<Enchantment, Integer> getDisallowedTableEnchants (Material m, Player p)
    {
        HashMap<Enchantment, Integer> results = new HashMap<Enchantment, Integer>();
        results.putAll (getDisallowedEnchants (getConfig().getConfigurationSection ("Disallowed enchants"), m, p));
        results.putAll (getDisallowedEnchants (getConfig().getConfigurationSection ("Disallowed table enchants"), m, p));
        return results;
    }
    // Calls previous by converting Material to string, but also adds special "ALL" cases
    private Map<Enchantment, Integer> getDisallowedEnchants (ConfigurationSection cs, Material m, Player p)
    {
        HashMap<Enchantment, Integer> results = new HashMap<Enchantment, Integer>();
        String matString = m.toString();
        if (cs == null)
            return results; // empty, non-null

        results.putAll (getDisallowedEnchants (cs, "ALL"));

        // BOOK should apply to Enchanted_BOOK, and v.v.
        if (m == Material.BOOK)
            results.putAll (getDisallowedEnchants (cs, "ENCHANTED_BOOK"));
        else if (m == Material.ENCHANTED_BOOK)
            results.putAll (getDisallowedEnchants (cs, "BOOK"));

        else if (MaterialCategory.isSword (m))
            results.putAll (getDisallowedEnchants (cs, "ALL_SWORDS"));
        else if (MaterialCategory.isSpade (m)) {
            results.putAll (getDisallowedEnchants (cs, "ALL_SPADES"));
            results.putAll (getDisallowedEnchants (cs, "ALL_SHOVELS"));
        } else if (MaterialCategory.isHoe (m))
            results.putAll (getDisallowedEnchants (cs, "ALL_HOES"));
        else if (MaterialCategory.isPick (m)) {
            results.putAll (getDisallowedEnchants (cs, "ALL_PICKS"));
            results.putAll (getDisallowedEnchants (cs, "ALL_PICKAXES"));
        } else if (MaterialCategory.isAxe (m))
            results.putAll (getDisallowedEnchants (cs, "ALL_AXES"));
        else if (MaterialCategory.isArmor (m)) {
            results.putAll (getDisallowedEnchants (cs, "ALL_ARMOR"));

            if (MaterialCategory.isHelmet (m))
                results.putAll (getDisallowedEnchants (cs, "ALL_HELMETS"));
            else if (MaterialCategory.isBoots (m))
                results.putAll (getDisallowedEnchants (cs, "ALL_BOOTS"));
            else if (MaterialCategory.isChestplate (m))
                results.putAll (getDisallowedEnchants (cs, "ALL_CHESTPLATES"));
            else if (MaterialCategory.isLeggings (m)) {
                results.putAll (getDisallowedEnchants (cs, "ALL_LEGGINGS"));
                results.putAll (getDisallowedEnchants (cs, "ALL_PANTS"));
            } else if (MaterialCategory.isBarding (m))
                results.putAll (getDisallowedEnchants (cs, "ALL_BARDING"));
            switch (MaterialCategory.getRawMaterial (m)) {
                case LEATHER:
                    results.putAll (getDisallowedEnchants (cs, "ALL_ARMOR_LEATHER"));
                    break;
                case GOLD_INGOT:
                    results.putAll (getDisallowedEnchants (cs, "ALL_ARMOR_GOLD"));
                    break;
                case DIAMOND:
                    results.putAll (getDisallowedEnchants (cs, "ALL_ARMOR_DIAMOND"));
                    break;
                case IRON_INGOT:
                    switch (m) {
                        case CHAINMAIL_BOOTS:
                        case CHAINMAIL_CHESTPLATE:
                        case CHAINMAIL_HELMET:
                        case CHAINMAIL_LEGGINGS:
                            results.putAll (getDisallowedEnchants (cs, "ALL_ARMOR_CHAIN"));
                            break;
                        default:
                            results.putAll (getDisallowedEnchants (cs, "ALL_ARMOR_IRON"));
                            break;
                    }
                    break;
            }
        }

        results.putAll (getDisallowedEnchants (cs, matString));

        // Determine what groups to read. Overrides non-grouped disallows including specifics
        Set<String> sectionGroups = Groups.get (cs.getCurrentPath());
        if (sectionGroups != null) {
            for (String groupName : sectionGroups.toArray(new String[0])) {
                if (p == null || !p.hasPermission ("enchlimiter." + groupName)) {
                    log.fine ("No permission " + groupName + " loading section");
                    results.putAll (getDisallowedEnchants (cs.getConfigurationSection (groupName), m, p));
                }
            }
        }

        return results;
    }

    private int getTotalEnchantLevels (final ItemStack item) {
        int total = 0;
        ItemMeta meta = item.getItemMeta();

        if ( !meta.hasEnchants())
            return 0;
        for (Enchantment e : meta.getEnchants().keySet())
            total += meta.getEnchantLevel (e);
        return total;
    }

    static private Set<String> Global_Groups = null;
    static private Set<String> Table_Groups = null;
    static private Set<String> Anvil_Groups = null;
    static private Map <String, Set<String> > Groups = new HashMap<>();
    void checkConfig() {
        if (getConfig().isConfigurationSection ("Disallowed enchants")) {
            Global_Groups = new HashSet<String>();	// old data lost and rely on Java garbage collector
            Groups.put ("Disallowed enchants", Global_Groups);
            checkConfig (getConfig().getConfigurationSection ("Disallowed enchants"));
        }
        if (getConfig().isConfigurationSection ("Disallowed anvil enchants")) {
            Anvil_Groups = new HashSet<String>();
            Groups.put ("Disallowed anvil enchants", Anvil_Groups);
            checkConfig (getConfig().getConfigurationSection ("Disallowed anvil enchants"));
        }
        if (getConfig().isConfigurationSection ("Disallowed table enchants")) {
            Table_Groups = new HashSet<String>();
            Groups.put ("Disallowed table enchants", Table_Groups);
            checkConfig (getConfig().getConfigurationSection ("Disallowed table enchants"));
        }
    }
    void checkConfig (ConfigurationSection cs) {
        //log.info ("Checking section " + cs.getCurrentPath());
        for (String itemString : cs.getKeys (false /*depth*/)) {
            Material m = Material.matchMaterial (itemString);
            if (itemString.equals("ALL") || itemString.startsWith ("ALL_ARMOR") || itemString.equals ("ALL_BARDING") || (itemString.startsWith ("ALL_") && itemString.endsWith ("S"))) {
                log.config (cs.getCurrentPath() +"."+ itemString + ":" + getDisallowedEnchants (cs, itemString));
                continue;
            } else if (m == null && itemString.startsWith ("Group_")) {
                // remember for later searching
                Set<String> sectionGroups = Groups.get (cs.getCurrentPath());
                if (sectionGroups != null) {
                    if (sectionGroups.add (itemString) == false) {	// remember
                        log.warning (cs.getCurrentPath() + " contains duplicate " + itemString);
                        continue;  // doesn't trigger bcs getSection takes the last one
                    }
                    else
                        log.config ("Found " + cs.getCurrentPath() + "." + itemString);
                } else {
                    log.warning ("Group_ names not allowed within " + cs.getCurrentPath());
                    continue;
                }
                // recursive check of subgroup
                checkConfig (cs.getConfigurationSection (itemString));
                continue;
            }

            if (m == null)
                log.warning (cs.getCurrentPath() + ":Unknown item: " + itemString + ". Refer to http://bit.ly/EnchMat");
            else if (m.isBlock())
                log.warning (cs.getCurrentPath() + ":Do not support blocks in disallowed enchants: " + m);
            else {
                log.config (cs.getCurrentPath() +"." + itemString + ":" + getDisallowedEnchants (cs, m, null));
            }
        }
    }


    public void onDisable()
    {
        prevXP.clear();
        prevAnvilData.clear();
        lastMsg.clear();
        if (Global_Groups != null) Global_Groups.clear();
        if (Table_Groups != null) Table_Groups.clear();
        if (Anvil_Groups != null) Anvil_Groups.clear();
    }
}
