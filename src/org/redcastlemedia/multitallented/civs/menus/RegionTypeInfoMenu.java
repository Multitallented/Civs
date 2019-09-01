package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.util.StructureUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.*;

public class RegionTypeInfoMenu extends Menu {
    static String MENU_NAME = "CivRegionInfo";

    public RegionTypeInfoMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        RegionType regionType = (RegionType) getData(civilian.getUuid(), "regionType");
        String regionName = regionType.getProcessedName();

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null ||
                !event.getCurrentItem().hasItemMeta() ||
                event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(event.getInventory().getItem(0).getItemMeta().getDisplayName())) {
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.IRON_PICKAXE)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            List<List<CVItem>> reqs = regionType.getReqs();
            for (int i=0; i<reqs.size(); i++) {
                for (int j=0; j<reqs.get(i).size(); j++) {
                    CVItem cvItem = reqs.get(i).get(j);
                    if (cvItem.getGroup() != null) {
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add("g:" + cvItem.getGroup());
                        lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                                "click-for-item-group"));
                        cvItem.setLore(lore);
                        reqs.get(i).set(j, cvItem);
                    }
                }
            }
            event.getWhoClicked().openInventory(RecipeMenu.createMenuCVItem(reqs, event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.CHEST)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
            if (!displayName.contains("Reagents")) {
                return;
            }
            int index = Integer.parseInt(displayName.replace("Reagents", ""));
            event.getWhoClicked().openInventory(RecipeMenu.createMenuCVItem(regionType.getUpkeeps().get(index).getReagents(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.HOPPER)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            int index = Integer.parseInt(event.getCurrentItem().getItemMeta().getDisplayName().replace("Input", ""));
            event.getWhoClicked().openInventory(RecipeMenu.createMenuCVItem(regionType.getUpkeeps().get(index).getInputs(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.DISPENSER)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            int index = Integer.parseInt(event.getCurrentItem().getItemMeta().getDisplayName().replace("Output", ""));
            event.getWhoClicked().openInventory(RecipeMenu.createMenuCVItem(regionType.getUpkeeps().get(index).getOutputs(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ConfirmationMenu.createMenu(civilian, regionType));
            return;
        }

        //TODO finish this stub
    }

    public static Inventory createMenu(Civilian civilian, RegionType regionType) {
        return createMenu(civilian, regionType, true);
    }
    public static Inventory createMenu(Civilian civilian, RegionType regionType, boolean showPrice) {
        return createInventory(civilian, regionType, showPrice, false);
    }
    public static Inventory createMenuInfinite(Civilian civilian, RegionType regionType) {
        return createInventory(civilian, regionType, true, true);
    }
    private static Inventory createInventory(Civilian civilian, RegionType regionType, boolean showPrice, boolean isInfinite) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        StructureUtil.showGuideBoundingBox(player, player.getLocation(), regionType, isInfinite);
        Inventory inventory = Bukkit.createInventory(null, 9 + 9*regionType.getUpkeeps().size(), MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        String localizedRegionName = localeManager.getTranslation(civilian.getLocale(), regionType.getProcessedName() + "-name");

        Map<String, Object> data = new HashMap<>();
        data.put("regionType", regionType);
        setNewData(civilian.getUuid(), data);

        //0 Icon
        CVItem cvItem = regionType.getShopIcon().clone();
        List<String> lore = new ArrayList<>();
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), regionType.getProcessedName() + "-name"));
        lore.add(localeManager.getTranslation(civilian.getLocale(), "size") +
                ": " + (regionType.getBuildRadiusX() * 2 + 1) + "x" + (regionType.getBuildRadiusZ() * 2 + 1) + "x" + (regionType.getBuildRadiusY() * 2 + 1));
        if (regionType.getEffectRadius() != regionType.getBuildRadius()) {
            lore.add(localeManager.getTranslation(civilian.getLocale(), "range") +
                    ": " + regionType.getEffectRadius());
        }

        lore.addAll(Util.textWrap(regionType.getDescription(civilian.getLocale())));
        cvItem.setLore(lore);
        inventory.setItem(0, cvItem.createItemStack());

        //1 Price
        boolean isCivsAdmin = Civs.perm != null && Civs.perm.has(player, "civs.admin");
        boolean hasShopPerms = Civs.perm != null && Civs.perm.has(player, "civs.shop");
        String maxLimit = civilian.isAtMax(regionType);
        boolean isInShop = regionType.getInShop();
        lore = new ArrayList<>();
        boolean hasItemUnlocked = ItemManager.getInstance().hasItemUnlocked(civilian, regionType);
        if (showPrice && (isCivsAdmin || (hasShopPerms && maxLimit == null && isInShop))) {
            CVItem priceItem;
            if (hasItemUnlocked || isCivsAdmin) {
                priceItem = CVItem.createCVItemFromString("EMERALD");
            } else {
                priceItem = CVItem.createCVItemFromString("IRON_BARS");
                lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "item-locked"));
            }
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + regionType.getPrice());
            priceItem.setLore(lore);
            inventory.setItem(1, priceItem.createItemStack());
        } else if (showPrice && hasShopPerms && isInShop) {
            CVItem priceItem = CVItem.createCVItemFromString("BARRIER");
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            int max = maxLimit.equals(regionType.getProcessedName()) ? regionType.getCivMax() :
                    ConfigManager.getInstance().getGroups().get(maxLimit);
            lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "max-item")
                    .replace("$1", maxLimit)
                    .replace("$2", "" + max));

        }

        //2 Rebuild
        if (regionType.getRebuild() != null) {
            String rebuildType = regionType.getRebuild();
            List<CivItem> rebuildGroup = ItemManager.getInstance().getItemGroup(rebuildType);
            if (!rebuildGroup.isEmpty()) {
                CivItem baseRebuildItem = rebuildGroup.get(0);
                CVItem rebuildItem = baseRebuildItem.clone();
                lore = new ArrayList<>();
//              lore.add();
                rebuildItem.setLore(lore);
                inventory.setItem(2, rebuildItem.createItemStack());
            }
        }

        //3 evolve
        //TODO evolve regions and exp

        //4 biome/location reqs
        if (!regionType.getBiomes().isEmpty()) {
            CVItem cvItem1 = CVItem.createCVItemFromString("GRASS_BLOCK");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "biomes"));
            lore = new ArrayList<>();
            for (Biome biome : regionType.getBiomes()) {
                lore.add(biome.name());
            }
            inventory.setItem(4, cvItem1.createItemStack());
        }

        //5 town reqs
        if (regionType.getTowns() != null && !regionType.getTowns().isEmpty()) {
            CVItem cvItem5 = CVItem.createCVItemFromString("OAK_DOOR");
            cvItem5.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "towns"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "town-req-desc"));
            lore.addAll(regionType.getTowns());
            cvItem5.setLore(lore);
            inventory.setItem(5, cvItem5.createItemStack());
        }

        //6 build-reqs
        CVItem cvItem1 = CVItem.createCVItemFromString("IRON_PICKAXE");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                "build-reqs-title"));
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "build-reqs")
                .replace("$1", localizedRegionName));
        cvItem1.setLore(lore);
        inventory.setItem(6, cvItem1.createItemStack());

        //7 effects
        {
            CVItem cvItem2 = CVItem.createCVItemFromString("POTION");
            cvItem2.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "effects"));
            lore = new ArrayList<>(regionType.getEffects().keySet());
            cvItem2.setLore(lore);
            inventory.setItem(7, cvItem2.createItemStack());
        }

        //8 back button
        inventory.setItem(8, getBackButton(civilian));

        for (int i = 0; i < regionType.getUpkeeps().size(); i++) {
            //9 reagents
            if (!regionType.getUpkeeps().get(i).getReagents().isEmpty()) {
                CVItem cvItem2 = CVItem.createCVItemFromString("CHEST");
                cvItem2.setDisplayName("Reagents" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "reagents")
                        .replace("$1", localizedRegionName));
                cvItem2.setLore(lore);
                inventory.setItem(9 + i * 9, cvItem2.createItemStack());
            }

            //10 upkeep
            if (!regionType.getUpkeeps().get(i).getInputs().isEmpty()) {
                CVItem cvItem3 = CVItem.createCVItemFromString("HOPPER");
                cvItem3.setDisplayName("Input" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "upkeep")
                        .replace("$1", localizedRegionName));
                cvItem3.setLore(lore);
                inventory.setItem(10 + i * 9, cvItem3.createItemStack());
            }

            //11 output
            if (!regionType.getUpkeeps().get(i).getOutputs().isEmpty()) {
                CVItem cvItem4 = CVItem.createCVItemFromString("DISPENSER");
                cvItem4.setDisplayName("Output" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "output")
                        .replace("$1", localizedRegionName));
                cvItem4.setLore(lore);
                inventory.setItem(11 + i * 9, cvItem4.createItemStack());
            }

            //12 payout
            if (regionType.getUpkeeps().get(i).getPayout() > 0) {
                CVItem cvItem4 = CVItem.createCVItemFromString("EMERALD_BLOCK");
                cvItem4.setDisplayName("Payout" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "payout")
                        .replace("$1", regionType.getUpkeeps().get(i).getPayout() + ""));
                cvItem4.setLore(lore);
                inventory.setItem(12 + i * 9, cvItem4.createItemStack());
            }
            //13 power input
            if (regionType.getUpkeeps().get(i).getPowerInput() > 0) {
                CVItem cvItem4 = CVItem.createCVItemFromString("REDSTONE_ORE");
                cvItem4.setDisplayName("PowerInput" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "power-input")
                        .replace("$1", regionType.getUpkeeps().get(i).getPowerInput() + ""));
                cvItem4.setLore(lore);
                inventory.setItem(13 + i * 9, cvItem4.createItemStack());
            }
            //14 power output
            if (regionType.getUpkeeps().get(i).getPowerOutput() > 0) {
                CVItem cvItem4 = CVItem.createCVItemFromString("REDSTONE_TORCH");
                cvItem4.setDisplayName("PowerOutput" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "power-output")
                        .replace("$1", regionType.getUpkeeps().get(i).getPowerOutput() + ""));
                cvItem4.setLore(lore);
                inventory.setItem(14 + i * 9, cvItem4.createItemStack());
            }
        }

        //TODO finish this stub

        return inventory;
    }
}
