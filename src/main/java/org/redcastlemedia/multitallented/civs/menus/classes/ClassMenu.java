package org.redcastlemedia.multitallented.civs.menus.classes;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.spells.SpellType;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = Constants.CLASS) @SuppressWarnings("unused")
public class ClassMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        CivClass civClass = null;
        if (!params.containsKey(Constants.CLASS)) {
            civClass = civilian.getCurrentClass();
        } else {
            for (CivClass cClass: civilian.getCivClasses()) {
                if (cClass.getId() == Integer.parseInt(params.get(Constants.CLASS))) {
                    civClass = cClass;
                }
            }
        }
        if (civClass == null) {
            civClass = civilian.getCurrentClass();
        }
        data.put(Constants.CLASS, civClass);
        Player player = Bukkit.getPlayer(civilian.getUuid());
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civClass.getType());
        String localClassName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                civClass.getType() + LocaleConstants.NAME_SUFFIX);
        String manaTitle = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                classType.getManaTitle());
        data.put("className", localClassName + civClass.getId());
        data.put("classTypeName", localClassName);
        String allowedItemsString = getAllowedActionsString(classType.getAllowedActions());
        data.put("allowedItems", allowedItemsString);
        data.put("classManaName", manaTitle);
        data.put("maxHealth", classType.getMaxHealth());
        data.put("maxMana", classType.getMaxMana());
        data.put("manaRegen", classType.getManaPerSecond());
        data.put("level", civClass.getLevel());

        if (params.containsKey("swap")) {
            data.put("swap", Integer.parseInt(params.get("swap")));
        }

        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if ("icon".equals(menuIcon.getKey())) {
            CivClass civClass = (CivClass) MenuManager.getData(civilian.getUuid(), Constants.CLASS);
            ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civClass.getType());
            CVItem cvItem = classType.getShopIcon(player);
            cvItem.setDisplayName(cvItem.getDisplayName() + civClass.getId());
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("destroy".equals(menuIcon.getKey()) && civilian.getCivClasses().size() < 2) {
            return new ItemStack(Material.AIR);
        } else if (menuIcon.getKey().startsWith("spell^")) {
            int length = menuIcon.getKey().length();
            CivClass civClass = (CivClass) MenuManager.getData(civilian.getUuid(), Constants.CLASS);
            int index = Integer.parseInt(menuIcon.getKey().substring(length - 1, length));
            index = civClass.getSpellSlotOrder().get(index);
            if (civClass.getSelectedSpells().containsKey(index)) {
                String spellName = civClass.getSelectedSpells().get(index);
                CVItem cvItem = ItemManager.getInstance().getItemType(spellName).getShopIcon(player);
                cvItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "spell-slot-desc")));
                if (MenuManager.getAllData(civilian.getUuid()).containsKey("swap")) {
                    cvItem.getLore().add(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                            "spell-slot-desc-change-order"));
                }
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else if (MenuManager.getAllData(civilian.getUuid()).containsKey("swap")) {
                CVItem cvItem = menuIcon.createCVItem(player, count);
                cvItem.getLore().add(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "spell-slot-desc-change-order"));
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return true;
        }
        if (actionString.startsWith("change-order")) {
            CivClass civClass = (CivClass) MenuManager.getData(civilian.getUuid(), Constants.CLASS);
            if (MenuManager.getAllData(civilian.getUuid()).containsKey("swap")) {
                int swap = (int) MenuManager.getData(civilian.getUuid(), "swap");
                swap = civClass.getSpellSlotOrder().get(swap);
                int length = actionString.length();
                int index = Integer.parseInt(actionString.substring(length - 1, length));
                index = civClass.getSpellSlotOrder().get(index);
                if (index != swap) {
                    civClass.getSpellSlotOrder().put(index, swap);
                    civClass.getSpellSlotOrder().put(swap, index);
                    ClassManager.getInstance().saveClass(civClass);
                }
            } else {
                int length = actionString.length();
                int index = Integer.parseInt(actionString.substring(length - 1, length));
                index = civClass.getSpellSlotOrder().get(index);
                MenuManager.getAllData(civilian.getUuid()).put("swap", index);
            }
            MenuManager.getInstance().refreshMenu(civilian);
            return true;
        } else if ("spend-exp".equals(actionString)) {
            CivClass civClass = (CivClass) MenuManager.getData(civilian.getUuid(), Constants.CLASS);
            if (player.getLevel() < civClass.getLevel() || player.getLevel() < 1) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "not-enough-levels").replace("$1", "" + Math.max(1, civClass.getLevel() - player.getLevel())));
                return true;
            }
            player.setLevel(Math.max(0, player.getLevel() - 1));
            civClass.setLevel(civClass.getLevel() + 1);
            MenuManager.getAllData(civilian.getUuid()).put("level", civClass.getLevel());
            ClassManager.getInstance().saveClass(civClass);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }

    private String getAllowedActionsString(Map<String, Integer> allowedActions) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : allowedActions.entrySet()) {
            stringBuilder.append(entry.getKey()).append(".");
            stringBuilder.append(entry.getValue());
            stringBuilder.append(",");
        }
        return stringBuilder.substring(0, stringBuilder.length() - 1);
    }
}
