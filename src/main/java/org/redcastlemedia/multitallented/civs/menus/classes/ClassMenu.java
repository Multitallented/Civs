package org.redcastlemedia.multitallented.civs.menus.classes;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.redcastlemedia.multitallented.civs.spells.SpellUtil;
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
        Map<String, Integer> allowedActionMap = new HashMap<>(classType.getAllowedActions());
        for (String spellName : civClass.getSelectedSpells().values()) {
            SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(spellName);
            allowedActionMap.putAll(spellType.getAllowedActions());
        }
        String allowedItemsString = getAllowedActionsString(allowedActionMap);
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
        } else if ("level".equals(menuIcon.getKey())) {
            CivClass civClass = (CivClass) MenuManager.getData(civilian.getUuid(), Constants.CLASS);
            ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civClass.getType());
            if (civClass.getLevel() >= classType.getMaxLevel()) {
                CVItem cvItem = menuIcon.createCVItem(player, count);
                cvItem.getLore().clear();
                cvItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "level-up-max")));
                return cvItem.createItemStack();
            }
        } else if ("destroy".equals(menuIcon.getKey()) && civilian.getCivClasses().size() < 2) {
            return new ItemStack(Material.AIR);
        } else if (menuIcon.getKey().startsWith("spell^")) {
            int length = menuIcon.getKey().length();
            CivClass civClass = (CivClass) MenuManager.getData(civilian.getUuid(), Constants.CLASS);
            int index = Integer.parseInt(menuIcon.getKey().substring(length - 1, length));
            if ((index - 1) * 4 > civClass.getLevel()) {
                return new ItemStack(Material.AIR);
            }
            int mappedIndex = civClass.getSpellSlotOrder().getOrDefault(index, index);
            if (civClass.getSelectedSpells().containsKey(mappedIndex)) {
                String spellName = civClass.getSelectedSpells().get(mappedIndex);
                SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(spellName);
                CVItem cvItem = spellType.getShopIcon(player);
                cvItem.setDisplayName(cvItem.getDisplayName() + index);
                cvItem.getLore().add(0, LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "level").replace("$1", "" + civilian.getLevel(spellType)));
                cvItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        "spell-slot-desc")));
                if (MenuManager.getAllData(civilian.getUuid()).containsKey("swap")) {
                    cvItem.getLore().add(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                            "spell-slot-desc-change-order"));
                }
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                CVItem cvItem = menuIcon.createCVItem(player, count);
                cvItem.setDisplayName(cvItem.getDisplayName() + index);
                if (MenuManager.getAllData(civilian.getUuid()).containsKey("swap")) {
                    cvItem.getLore().add(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                            "spell-slot-desc-change-order"));
                }
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
                if (!civilian.getCombatBar().isEmpty()) {
                    SpellUtil.removeCombatBar(player, civilian);
                }
                int mappedSwap = (int) MenuManager.getData(civilian.getUuid(), "swap");
                MenuManager.getAllData(civilian.getUuid()).remove("swap");
                int unmappedSwap = -1;
                for (Map.Entry<Integer, Integer> entry : civClass.getSpellSlotOrder().entrySet()) {
                    if (entry.getValue() == mappedSwap) {
                        unmappedSwap = entry.getKey();
                    }
                }
                if (unmappedSwap != -1) {
                    int length = actionString.length();
                    int index = Integer.parseInt(actionString.substring(length - 1, length));
                    swapSpellSlots(civClass, mappedSwap, index);
                }
            } else {
                int length = actionString.length();
                int mappedIndex = Integer.parseInt(actionString.substring(length - 1, length));
                MenuManager.getAllData(civilian.getUuid()).put("swap", mappedIndex);
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
            ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civClass.getType());
            player.setLevel(Math.max(0, player.getLevel() - 1));
            civClass.setLevel(civClass.getLevel() + 1);
            if (Civs.perm != null) {
                for (Map.Entry<String, Integer> entry : classType.getClassPermissions().entrySet()) {
                    if (entry.getValue() == civClass.getLevel()) {
                        Civs.perm.playerAdd(player, entry.getKey());
                    }
                }
            }
            MenuManager.getAllData(civilian.getUuid()).put("level", civClass.getLevel());
            ClassManager.getInstance().saveClass(civClass);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }

    protected static void swapSpellSlots(CivClass civClass, int unmappedSwap, int index) {
        int mappedSwap = civClass.getSpellSlotOrder().get(unmappedSwap);
        int mappedIndex = civClass.getSpellSlotOrder().get(index);

        if (index != unmappedSwap) {
            civClass.getSpellSlotOrder().put(index, mappedSwap);
            civClass.getSpellSlotOrder().put(unmappedSwap, mappedIndex);
            ClassManager.getInstance().saveClass(civClass);
        }
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
