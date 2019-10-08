package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TownInviteConfirmationMenu extends Menu {
    static String MENU_NAME = "CivConfirmInvite";
    public TownInviteConfirmationMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        LocaleManager localeManager = LocaleManager.getInstance();
        CivilianManager civilianManager = CivilianManager.getInstance();

        Civilian civilian = civilianManager.getCivilian(event.getWhoClicked().getUniqueId());
        Town town = (Town) getData(civilian.getUuid(), "town");
        Town myTown = TownManager.getInstance().isOwnerOfATown(civilian);

        if (Menu.isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            AllianceManager.getInstance().allyTheseTowns(myTown, town);
            for (Player cPlayer : Bukkit.getOnlinePlayers()) {
                Civilian civilian1 = CivilianManager.getInstance().getCivilian(cPlayer.getUniqueId());
                cPlayer.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian1.getLocale(),
                        "town-ally-request-accepted").replace("$1", town.getName())
                        .replace("$2", myTown.getName()));
            }
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.BARRIER)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            myTown.getAllyInvites().remove(town.getName());
            event.getWhoClicked().sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "town-ally-request-denied").replace("$1", town.getName()));
            for (UUID uuid : town.getRawPeople().keySet()) {
                if (town.getRawPeople().get(uuid).contains("owner")) {
                    Player pSend = Bukkit.getPlayer(uuid);
                    if (pSend.isOnline()) {
                        pSend.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                                "town-ally-request-denied").replace("$1", myTown.getName()));
                    }
                }
            }
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, String townName) {
        Town town = TownManager.getInstance().getTown(townName);
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        LocaleManager localeManager = LocaleManager.getInstance();
        CVItem icon = new CVItem(townType.getMat(), 1, 0, town.getName());
        Map<String, Object> data = new HashMap<>();
        data.put("town", town);
        setNewData(civilian.getUuid(), data);

        inventory.setItem(0, icon.createItemStack());

        CVItem cvItem = CVItem.createCVItemFromString("EMERALD");
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "confirm"));
        inventory.setItem(3, cvItem.createItemStack());

        CVItem cvItem1 = CVItem.createCVItemFromString("BARRIER");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "reject"));
        inventory.setItem(4, cvItem1.createItemStack());

        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}
