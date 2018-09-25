package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TownActionMenu extends Menu {
    public static final String MENU_NAME = "CivsTown";
    public TownActionMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        LocaleManager localeManager = LocaleManager.getInstance();

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        TownManager townManager = TownManager.getInstance();
        String townName = event.getInventory().getItem(0).getItemMeta().getDisplayName().split("@")[1];
        Town town = townManager.getTown(townName);

        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(),
                        "destroy"))) {
            event.getWhoClicked().closeInventory();
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().openInventory(DestroyConfirmationMenu.createMenu(civilian, town));
            return;
        }
        Town townOwner = TownManager.getInstance().isOwnerOfATown(civilian);
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(),
                        "town-ally"))) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            town.getAllyInvites().add(townOwner.getName());
            event.getWhoClicked().sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "town-ally-request-sent").replace("$1", townName));
            for (UUID uuid : town.getRawPeople().keySet()) {
                if (town.getRawPeople().get(uuid).equals("owner")) {
                    Player pSend = Bukkit.getPlayer(uuid);
                    if (pSend.isOnline()) {
                        pSend.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                                "town-ally-request-sent").replace("$1", townName));
                    }
                }
            }
            return;
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(),
                        "town-unally")) && townOwner != null) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            townOwner.getAllies().remove(townName);
            town.getAllies().remove(townOwner.getName());
            TownManager.getInstance().saveTown(town);
            TownManager.getInstance().saveTown(townOwner);
            for (Player cPlayer : Bukkit.getOnlinePlayers()) {
                cPlayer.sendMessage(Civs.getPrefix() + ChatColor.RED + localeManager.getTranslation(civilian.getLocale(),
                        "town-ally-removed").replace("$1", townOwner.getName())
                        .replace("$1", townName));
            }
            return;
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(),
                        "town-ally-invites")) && townOwner != null) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownInviteMenu.createMenu(civilian, 0, townName));
            return;
        }

        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "view-members"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ViewMembersMenu.createMenu(civilian, town));
            return;
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "leave-town"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(LeaveConfirmationMenu.createMenu(civilian, town));
            return;
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "add-member"))) {
            event.getWhoClicked().closeInventory();
            List<Player> people = new ArrayList<>();
            for (UUID uuid : town.getPeople().keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    people.add(player);
                }
            }
            event.getWhoClicked().openInventory(ListAllPlayersMenu.createMenu(civilian, "invite", people, 0, town.getName()));
            return;
        }

    }

    public static Inventory createMenu(Civilian civilian, Town town) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        //0 Icon
        CVItem cvItem = new CVItem(townType.getMat(), 1);
        cvItem.setDisplayName(town.getType() + "@" + town.getName());
        ArrayList<String> lore = new ArrayList<>(Util.textWrap(ChatColor.WHITE + "",
                townType.getDescription(civilian.getLocale())));
        cvItem.setLore(lore);
        inventory.setItem(0, cvItem.createItemStack());


        //1 Power
        CVItem cvItem1;
        //1 Is Working
        if (town.getPower() > 0) {
            cvItem1 = CVItem.createCVItemFromString("GREEN_WOOL");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "town-power").replace("$1", "" + town.getPower())
                    .replace("$2", "" + town.getMaxPower()));
        } else {
            cvItem1 = CVItem.createCVItemFromString("RED_WOOL");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "town-power").replace("$1", "" + town.getPower())
                    .replace("$2", "" + town.getMaxPower()));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "grace-period")
                    .replace("$1", (TownManager.getInstance().getRemainingGracePeriod(town) / 1000) + ""));
        }
        //TODO power consumption / generation
        inventory.setItem(1, cvItem1.createItemStack());

        //2 Location/Nation?
        //TODO nation display here
        if (town.getPeople().containsKey(civilian.getUuid())) {
            CVItem cvItem2 = CVItem.createCVItemFromString("COMPASS");
            cvItem2.setDisplayName(town.getName());
            lore.clear();
            lore.add(Region.locationToString(town.getLocation()));
            cvItem2.setLore(lore);
            inventory.setItem(2, cvItem2.createItemStack());
        }

        //3 Ally / Remove ally
        Town townOwner = TownManager.getInstance().isOwnerOfATown(civilian);
        if (townOwner != null && townOwner != town && !townOwner.getAllies().contains(town.getName())) {
            CVItem cvItem6 = CVItem.createCVItemFromString("IRON_SWORD");
            cvItem6.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "town-ally").replace("$1", town.getName()));
            inventory.setItem(3, cvItem6.createItemStack());
        } else if (townOwner != null && townOwner != town) {
            CVItem cvItem6 = CVItem.createCVItemFromString("CREEPER_HEAD");
            cvItem6.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "town-unally").replace("$1", town.getName()));
            inventory.setItem(3, cvItem6.createItemStack());
        }

        //4 Population
        {
            CVItem cvItem3 = CVItem.createCVItemFromString("PLAYER_HEAD");
            cvItem3.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "population"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "pop-desc")
                    .replace("$1", town.getPopulation() + "")
                    .replace("$2", town.getHousing() + "")
                    .replace("$3", town.getVillagers() + ""));
            cvItem3.setLore(lore);
            inventory.setItem(4, cvItem3.createItemStack());
        }

        //5 Bounty
        {
            CVItem cvItem6 = CVItem.createCVItemFromString("SKELETON_SKULL");
            cvItem6.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "bounty").replace("$1", town.getName()));
            lore = new ArrayList<>();
            int i=0;
            for (Bounty bounty : town.getBounties()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(bounty.getIssuer());
                lore.add(op.getName() + ": $" + bounty.getAmount());
                if (i>5) {
                    break;
                }
                i++;
            }
            cvItem6.setLore(lore);
            inventory.setItem(5, cvItem6.createItemStack());
        }

        //6 Destroy
        if ((town.getPeople().containsKey(civilian.getUuid()) &&
                town.getPeople().get(civilian.getUuid()).equals("owner")) ||
                (Civs.perm != null && Civs.perm.has(player, "civs.admin"))) {
            CVItem destroy = CVItem.createCVItemFromString("BARRIER");
            destroy.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "destroy"));
            inventory.setItem(6, destroy.createItemStack());

            //7 Rename
            CVItem cvItem3 = CVItem.createCVItemFromString("PAPER");
            cvItem3.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "rename-town"));
            lore.clear();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "rename-desc"));
            cvItem3.setLore(lore);
            inventory.setItem(7, cvItem3.createItemStack());
        }

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));
        //9 People
        if (town.getPeople().get(civilian.getUuid()) != null && town.getPeople().get(civilian.getUuid()).equals("owner")) {
            CVItem skull = CVItem.createCVItemFromString("PLAYER_HEAD");
            skull.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "view-members"));
            inventory.setItem(9, skull.createItemStack());

            //10 Add person
            CVItem skull2 = CVItem.createCVItemFromString("PLAYER_HEAD");
            skull2.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "add-member"));
            inventory.setItem(10, skull2.createItemStack());
        }

        //11 Leave Town
        if (town.getPeople().get(civilian.getUuid()) != null) {
            CVItem cvItem2 = CVItem.createCVItemFromString("OAK_DOOR");
            cvItem2.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "leave-town"));
            inventory.setItem(11, cvItem2.createItemStack());
        }

        //12 Alliance Invite
        if (town.getRawPeople().containsKey(civilian.getUuid()) &&
                town.getRawPeople().get(civilian.getUuid()).equals("owner") &&
                !town.getAllyInvites().isEmpty()) {
            CVItem cvItem3 = CVItem.createCVItemFromString("IRON_SWORD");
            cvItem3.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "town-ally-invites"));
            inventory.setItem(12, cvItem3.createItemStack());
        }


        return inventory;
    }
}
