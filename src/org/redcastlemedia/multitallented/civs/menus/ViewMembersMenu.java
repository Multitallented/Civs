package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.towns.Town;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ViewMembersMenu extends Menu {
    public static final String MENU_NAME = "CivsMembers";
    public ViewMembersMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() ||
                event.getWhoClicked() == null) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        String locationString;
        Town town = null;
        Region region = null;
        if (getData(civilian.getUuid(), "town") != null) {
            town = (Town) getData(civilian.getUuid(), "town");
            locationString = town.getName();
        } else {
            region = (Region) getData(civilian.getUuid(), "region");
            locationString = region.getId();
        }

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        boolean oligarchyBuy = getData(civilian.getUuid(), "oligarchy-buy") != null;

        ArrayList<UUID> uuidList = (ArrayList<UUID>) getData(civilian.getUuid(), "uuidList");

        if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {

            int index = Integer.parseInt(event.getCurrentItem().getItemMeta().getLore().get(0));
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuidList.get(index));
            boolean viewSelf = player.getUniqueId().equals(civilian.getUuid());

            appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);
            event.getWhoClicked().closeInventory();
            if (town != null) {
                if (viewSelf && town.getRawPeople().keySet().size() < 2) {
                    return;
                }
                event.getWhoClicked().openInventory(MemberActionMenu.createMenu(civilian, town,
                        player.getUniqueId(), viewSelf, !oligarchyBuy));
            } else {
                if (viewSelf && region.getPeople().keySet().size() < 2) {
                    return;
                }
                event.getWhoClicked().openInventory(MemberActionMenu.createMenu(civilian, region, player.getUniqueId(), viewSelf));
            }
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, Town town, boolean oligarchyBuy) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(town.getPeople().size()) + 9, MENU_NAME);

        Map<String, Object> data = new HashMap<>();
        data.put("town", town);
        if (oligarchyBuy) {
            data.put("oligarchy-buy", true);
        }
        ArrayList<UUID> uuidList = new ArrayList<>();
        for (UUID uuid : town.getPeople().keySet()) {
            if (!town.getPeople().get(uuid).contains("ally")) {
                uuidList.add(uuid);
            }
        }
        data.put("uuidList", uuidList);
        setNewData(civilian.getUuid(), data);

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));

        setInventoryItems(inventory, town.getPeople(), civilian, false);

        return inventory;
    }

    public static Inventory createMenu(Civilian civilian, Region region) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(region.getPeople().size()) + 9, MENU_NAME);

        Map<String, Object> data = new HashMap<>();
        data.put("region", region);
        ArrayList<UUID> uuidList = new ArrayList<>();
        for (UUID uuid : region.getPeople().keySet()) {
            if (!region.getPeople().get(uuid).contains("ally")) {
                uuidList.add(uuid);
            }
        }
        data.put("uuidList", uuidList);
        setNewData(civilian.getUuid(), data);

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));

        setInventoryItems(inventory, region.getPeople(), civilian, false);

        return inventory;
    }

    private static void setInventoryItems(Inventory inventory,
                                          HashMap<UUID, String> people,
                                          Civilian civilian,
                                          boolean allowAllies) {
        ArrayList<String> lore;
        int i=9;
        for (UUID uuid : people.keySet()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player == null || (!allowAllies && people.get(uuid).contains("ally"))) {
                continue;
            }
            ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta im = (SkullMeta) playerItem.getItemMeta();
            im.setDisplayName(player.getName());
            lore = new ArrayList<>();
            lore.add("" + (i-9));
            lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), people.get(uuid)));
            im.setLore(lore);
            if (player.isOnline()) {
                im.setOwningPlayer(player);
            }
            playerItem.setItemMeta(im);
            inventory.setItem(i, playerItem);
            i++;
        }
    }
}
