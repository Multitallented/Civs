package org.redcastlemedia.multitallented.civs.menus.common;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.events.PlayerLeaveTownEvent;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.skills.SkillManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.*;

@CivsMenu(name = "confirmation")
@SuppressWarnings("unused")
public class ConfirmationMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("item")) {
            CivItem civItem = ItemManager.getInstance().getItemType(params.get("item"));
            data.put("item", civItem);
        }
        if (params.containsKey("type")) {
            data.put("type", params.get("type"));
        }
        if (params.containsKey(Constants.REGION)) {
            data.put(Constants.REGION, RegionManager.getInstance().getRegionById(params.get(Constants.REGION)));
        }
        if (params.containsKey(Constants.TOWN)) {
            data.put(Constants.TOWN, TownManager.getInstance().getTown(params.get(Constants.TOWN)));
        }
        if (params.containsKey(Constants.CLASS)) {
            for (CivClass civClass : civilian.getCivClasses()) {
                if (civClass.getId().equals(UUID.fromString(params.get(Constants.CLASS)))) {
                    data.put(Constants.CLASS, civClass);
                    break;
                }
            }
        }
        return data;
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        String type = (String) MenuManager.getData(civilian.getUuid(), "type");
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if ("confirm".equals(actionString)) {
            CivItem civItem = (CivItem) MenuManager.getData(civilian.getUuid(), "item");
            Region region = (Region) MenuManager.getData(civilian.getUuid(), Constants.REGION);
            Town town = (Town) MenuManager.getData(civilian.getUuid(), Constants.TOWN);
            if (type == null) {
                return true;
            }
            if ("buy".equals(type) && civItem != null) {
                buyItem(civItem, player, civilian);
                player.closeInventory();
            } else if ("destroy".equals(type)) {
                if (region == null && town == null) {
                    CivClass civClass = (CivClass) MenuManager.getData(civilian.getUuid(), Constants.CLASS);
                    ClassManager.getInstance().deleteClass(civClass);
                    player.closeInventory();
                } else {
                    destroyRegionOrTown(region, town, civilian, player);
                    player.closeInventory();
                }
            } else if ("leave".equals(type)) {
                if (town != null) {
                    PlayerLeaveTownEvent event = new PlayerLeaveTownEvent(civilian.getUuid(), town);
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        town.getRawPeople().remove(civilian.getUuid());
                        player.sendMessage(LocaleManager.getInstance().getTranslation(player,
                                "you-left-town").replace("$1", town.getName()));
                        TownManager.getInstance().saveTown(town);
                    }
                }
                player.closeInventory();
            }
            return true;
        } else if ("reject".equals(actionString)) {
            // Do nothing here currently
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }

    private void destroyRegionOrTown(Region region, Town town, Civilian civilian, Player player) {
        if (region != null) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            boolean canOverride = !regionType.getEffects().containsKey("cant_override");
            Town overrideTown = null;
            if (canOverride) {
                overrideTown = TownManager.getInstance().getTownAt(region.getLocation());
            }
            boolean hasOverride = overrideTown != null && overrideTown.getRawPeople().containsKey(player.getUniqueId()) &&
                    overrideTown.getRawPeople().get(player.getUniqueId()).contains(Constants.OWNER);
            if (!hasOverride && doesntHavePermission(civilian, region.getPeople(), player)) {
                return;
            }
            if (Civs.econ != null) {
                Civs.econ.depositPlayer(player, regionType.getPrice(civilian) / 2);
            }
            RegionManager.getInstance().removeRegion(region, true, true);
            CivilianListener.getInstance().shouldCancelBlockBreak(region.getLocation().getBlock(), player);
            ItemManager.getInstance().addMinItems(civilian);
        } else if (town != null) {
            if (doesntHavePermission(civilian, town.getPeople(), player)) {
                return;
            }
            TownManager.getInstance().removeTown(town, true);
        }
    }

    private boolean doesntHavePermission(Civilian civilian, Map<UUID, String> people, Player player) {
        LocaleManager localeManager = LocaleManager.getInstance();
        if ((!people.containsKey(civilian.getUuid()) ||
                !people.get(civilian.getUuid()).contains(Constants.OWNER)) &&
                (Civs.perm == null || !Civs.perm.has(player, Constants.ADMIN_PERMISSION))) {
            player.closeInventory();
            player.sendMessage(Civs.getPrefix() +
                    localeManager.getTranslation(player, "no-permission"));
            return true;
        }
        return false;
    }

    private void buyItem(CivItem civItem, Player player, Civilian civilian) {
        LocaleManager localeManager = LocaleManager.getInstance();
        double price = SkillManager.getInstance().getSkillDiscountedPrice(civilian, civItem);

        if (price > 0 && Civs.econ == null) {
            player.sendMessage(Civs.getPrefix() + " Econ plugin not enabled or hooked through Vault.");
            player.closeInventory();
            return;
        }

        if (price > 0 && (Civs.econ != null && !Civs.econ.has(player, price))) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "not-enough-money").replace("$1", price + ""));
            player.closeInventory();
            return;
        }

        if (Civs.econ != null && price > 0) {
            Civs.econ.withdrawPlayer(player, price);
        }
        player.sendMessage(Civs.getPrefix() +
                localeManager.getTranslation(player, "item-bought")
                        .replace("$1", civItem.getDisplayName(player))
                        .replace("$2", Util.getNumberFormat(price, civilian.getLocale())));
        player.closeInventory();

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(civItem.createItemStack(player));
        } else {
            if (civilian.getStashItems().containsKey(civItem.getProcessedName())) {
                civilian.getStashItems().put(civItem.getProcessedName(),
                        civItem.getQty() + civilian.getStashItems().get(civItem.getProcessedName()));
            } else {
                civilian.getStashItems().put(civItem.getProcessedName(), civItem.getQty());
            }
        }
        TutorialManager.getInstance().completeStep(civilian, TutorialManager.TutorialType.BUY, civItem.getProcessedName());
        CivilianManager.getInstance().saveCivilian(civilian);
    }
}
