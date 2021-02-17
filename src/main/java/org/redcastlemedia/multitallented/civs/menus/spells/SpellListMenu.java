package org.redcastlemedia.multitallented.civs.menus.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.spells.SpellManager;
import org.redcastlemedia.multitallented.civs.spells.SpellType;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsMenu(name = "spell-list") @SuppressWarnings("unused")
public class SpellListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        List<SpellType> spellTypeList = new ArrayList<>();
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }

        Player player = Bukkit.getPlayer(civilian.getUuid());

        CivClass selectedClass = null;
        if (params.containsKey(Constants.CLASS)) {
            for (CivClass civClass : civilian.getCivClasses()) {
                if (civClass.getId().equals(UUID.fromString(params.get(Constants.CLASS)))) {
                    selectedClass = civClass;
                    CivItem civItem = ItemManager.getInstance().getItemType(selectedClass.getType());
                    data.put("classTypeName", civItem.getDisplayName(player));
                    data.put(Constants.CLASS, civClass);
                    break;
                }
            }
        }
        int selectedSlot = -1;
        if (params.containsKey("slot")) {
            selectedSlot = Integer.parseInt(params.get("slot"));
            data.put("slot", selectedSlot);
        }
        if (selectedClass != null && selectedSlot > -1) {
            Set<SpellType> spellSet = new HashSet<>(SpellManager.getInstance().getSpellsForSlot(selectedClass, selectedSlot, true));
            spellTypeList.addAll(spellSet);
        } else if (selectedClass != null) {
            Set<SpellType> spellSet = new HashSet<>(SpellManager.getInstance().getSpellsForClass(selectedClass));
            spellTypeList.addAll(spellSet);
        }
        data.put("spells", spellTypeList);
        data.put("spellMap", new HashMap<ItemStack, SpellType>());
        int maxPage = (int) Math.ceil((double) spellTypeList.size() / (double) itemsPerPage.get("spells"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("spells".equals(menuIcon.getKey())) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            if (player == null) {
                return new ItemStack(Material.AIR);
            }
            List<SpellType> items = (List<SpellType>) MenuManager.getData(civilian.getUuid(), "spells");
            if (items.isEmpty() && count == 0) {
                // TODO add something that says you haven't unlocked any skills yet?
                return new ItemStack(Material.AIR);
            }
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            SpellType[] itemArray = new SpellType[items.size()];
            itemArray = items.toArray(itemArray);
            if (itemArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            SpellType spellType = itemArray[startIndex + count];
            CVItem cvItem = spellType.getShopIcon(player);
            cvItem.getLore().add(0, LocaleManager.getInstance().getTranslation(player,
                    "level").replace("$1", "" + civilian.getLevel(spellType)));
            List<String> unmetRequirements = ItemManager.getInstance().getAllUnmetRequirements(spellType, civilian, false);
            if (!unmetRequirements.isEmpty()) {
                cvItem.getLore().addAll(unmetRequirements);
            }
            ItemStack itemStack = cvItem.createItemStack();
            ((HashMap<ItemStack, SpellType>) MenuManager.getData(civilian.getUuid(), "spellMap"))
                    .put(itemStack, spellType);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override @SuppressWarnings("unchecked")
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("set-spell-slot".equals(actionString)) {
            CivClass civClass = (CivClass) MenuManager.getData(civilian.getUuid(), Constants.CLASS);
            if (civClass != null && MenuManager.getAllData(civilian.getUuid()).containsKey("slot")) {
                SpellType spellType = ((HashMap<ItemStack, SpellType>) MenuManager.getData(civilian.getUuid(), "spellMap"))
                        .get(itemStack);
                if (!ItemManager.getInstance().hasItemUnlocked(civilian, spellType)) {
                    return true;
                }
                int slot = (int) MenuManager.getData(civilian.getUuid(), "slot");

                Player player = Bukkit.getPlayer(civilian.getUuid());
                String oldSpellName = civClass.getSelectedSpells().get(civClass.getSpellSlotOrder().getOrDefault(slot, slot));
                if (oldSpellName != null) {
                    SpellManager.removePassiveSpell(civilian, player, oldSpellName);
                }
                civClass.getSelectedSpells().put(civClass.getSpellSlotOrder().getOrDefault(slot, slot),
                        spellType.getProcessedName());
                SpellManager.initPassiveSpell(civilian, spellType, player);

                ClassManager.getInstance().saveClass(civClass);
                return true;
            }
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}
