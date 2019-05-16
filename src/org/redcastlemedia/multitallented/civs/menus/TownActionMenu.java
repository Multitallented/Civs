package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.text.NumberFormat;
import java.util.*;

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
        Town town = (Town) getData(civilian.getUuid(), "town");
        String townName = town.getName();
        boolean isOwner = town.getRawPeople().containsKey(civilian.getUuid()) &&
                town.getRawPeople().get(civilian.getUuid()).contains("owner");

        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(),
                        "destroy"))) {
            event.getWhoClicked().closeInventory();
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().openInventory(DestroyConfirmationMenu.createMenu(civilian, town));
            return;
        }
        // TODO improve this variable somehow
        Town townOwner = TownManager.getInstance().isOwnerOfATown(civilian);
        if (townOwner != null && event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(),
                        "town-ally").replace("$1", town.getName()))) {

            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            town.getAllyInvites().add(townOwner.getName());
            event.getWhoClicked().sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "town-ally-request-sent").replace("$1", townName));
            for (UUID uuid : town.getRawPeople().keySet()) {
                if (town.getRawPeople().get(uuid).contains("owner")) {
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
                        "town-unally").replace("$1", town.getName())) &&
                townOwner != null) {

            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            for (Town myTown : TownManager.getInstance().getTowns()) {
                if (myTown.getPeople().containsKey(civilian.getUuid()) &&
                        myTown.getPeople().get(civilian.getUuid()).contains("owner")) {
                    AllianceManager.getInstance().unAlly(myTown, town);
                    for (Player cPlayer : Bukkit.getOnlinePlayers()) {
                        cPlayer.sendMessage(Civs.getPrefix() + ChatColor.RED + localeManager.getTranslation(civilian.getLocale(),
                                "town-ally-removed").replace("$1", myTown.getName())
                                .replace("$2", townName));
                    }
                }
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

        boolean oligarchyBuy = getData(civilian.getUuid(), "oligarchy-buy") != null;
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "view-members"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ViewMembersMenu.createMenu(civilian, town, oligarchyBuy));
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

        int ownerCount = 0;
        for (String role : town.getRawPeople().values()) {
            if (role.contains("owner")) {
                ownerCount++;
            }
        }
        boolean govTypeDisable = town.getGovernmentType() == GovernmentType.LIBERTARIAN ||
                town.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM ||
                town.getGovernmentType() == GovernmentType.CYBERSYNACY;

        boolean colonialOverride = getData(civilian.getUuid(), "colonial-override") != null;

        if (ConfigManager.getInstance().isAllowChangingOfGovType() &&
                (!govTypeDisable && ((isOwner && ownerCount < 2) || colonialOverride)) &&
                event.getCurrentItem().getItemMeta().getLore() != null &&
                !event.getCurrentItem().getItemMeta().getLore().isEmpty() &&
                event.getCurrentItem().getItemMeta().getLore().get(0).startsWith("Gov Type:")) {

            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(SelectGovTypeMenu.createMenu(civilian, town));
        }

    }

    public static Inventory createMenu(Civilian civilian, Town town) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());

        boolean colonialOverride = town.getGovernmentType() == GovernmentType.COLONIALISM &&
                town.getColonialTown() != null;
        colonial: if (colonialOverride) {
            for (Town cTown : TownManager.getInstance().getOwnedTowns(civilian)) {
                cTown.getName().equalsIgnoreCase(town.getColonialTown());
                break colonial;
            }
            colonialOverride = false;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("town", town);
        if (colonialOverride) {
            data.put("colonial-override", true);
        }

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

        //2 Location
        if (town.getPeople().containsKey(civilian.getUuid())) {
            CVItem cvItem2 = CVItem.createCVItemFromString("COMPASS");
            cvItem2.setDisplayName(town.getName());
            lore = new ArrayList<>();
            lore.add(town.getLocation().getWorld().getName() + " " +
                    town.getLocation().getX() + "x " + town.getLocation().getY() + "y " +
                    town.getLocation().getZ() + "z");
            cvItem2.setLore(lore);
            inventory.setItem(2, cvItem2.createItemStack());
        }

        //3 Ally / Remove ally
        Town townOwner = TownManager.getInstance().isOwnerOfATown(civilian);
        if (townOwner != null && townOwner != town && !AllianceManager.getInstance().isAllied(townOwner, town)) {
            CVItem cvItem6 = CVItem.createCVItemFromString("IRON_SWORD");
            cvItem6.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "town-ally").replace("$1", town.getName()));
            lore = new ArrayList<>();
            lore.add(townOwner.getName());
            cvItem6.setLore(lore);
            inventory.setItem(3, cvItem6.createItemStack());
        } else if (townOwner != null && townOwner != town) {
            CVItem cvItem6 = CVItem.createCVItemFromString("CREEPER_HEAD");
            cvItem6.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "town-unally").replace("$1", town.getName()));
            lore = new ArrayList<>();
            lore.add(townOwner.getName());
            cvItem6.setLore(lore);
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
                town.getPeople().get(civilian.getUuid()).contains("owner")) ||
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

        boolean govTypeDisable = town.getGovernmentType() == GovernmentType.LIBERTARIAN ||
                town.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM ||
                town.getGovernmentType() == GovernmentType.CYBERSYNACY ||
                town.getGovernmentType() == GovernmentType.COMMUNISM;

        boolean govTypeOpenToAnyone = town.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM ||
                town.getGovernmentType() == GovernmentType.LIBERTARIAN ||
                town.getGovernmentType() == GovernmentType.ANARCHY;

        boolean isOwner = town.getPeople().get(civilian.getUuid()) != null &&
                town.getPeople().get(civilian.getUuid()).contains("owner");

        boolean govTypeOwnerOverride = town.getGovernmentType() == GovernmentType.ANARCHY ||
                town.getGovernmentType() == GovernmentType.OLIGARCHY;


        if (!isOwner && town.getGovernmentType() == GovernmentType.OLIGARCHY) {
            data.put("oligarchy-buy", true);
        }
        setNewData(civilian.getUuid(), data);

        //9 People
        if (!govTypeDisable && (isOwner || govTypeOwnerOverride || colonialOverride)) {
            CVItem skull = CVItem.createCVItemFromString("PLAYER_HEAD");
            skull.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "view-members"));
            inventory.setItem(9, skull.createItemStack());
        }

        //10 Add person
        if (govTypeOpenToAnyone || isOwner || colonialOverride) {
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
        if ((!govTypeDisable || town.getGovernmentType() == GovernmentType.COMMUNISM) &&
                (isOwner || town.getGovernmentType() == GovernmentType.ANARCHY || colonialOverride) &&
                !town.getAllyInvites().isEmpty()) {
            CVItem cvItem3 = CVItem.createCVItemFromString("IRON_SWORD");
            cvItem3.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "town-ally-invites"));
            inventory.setItem(12, cvItem3.createItemStack());
        }

        //13 Goverment Type
        {
            CVItem govType = Util.getGovermentTypeIcon(civilian, town.getGovernmentType());
            inventory.setItem(13, govType.createItemStack());
        }

        //14 Bank
        if (town.getBankAccount() > 0) {
            String bankString = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(civilian.getLocale()))
                    .format(town.getBankAccount());
            CVItem cvItem2 = CVItem.createCVItemFromString("EMERALD_BLOCK");
            cvItem2.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "town-bank-balance").replace("$1", bankString));
            lore = new ArrayList<>();
            if (town.getTaxes() > 0) {
                String taxString = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(civilian.getLocale()))
                        .format(town.getTaxes());
                lore.add(localeManager.getTranslation(civilian.getLocale(), "town-tax")
                        .replace("$1", taxString));
            }
            if (isOwner || colonialOverride) {
                lore.add(localeManager.getTranslation(civilian.getLocale(), "town-tax-desc")
                        .replace("$1", town.getName()));
                lore.add(localeManager.getTranslation(civilian.getLocale(), "town-bank-desc")
                        .replace("$1", town.getName()));
            }
            cvItem2.setLore(lore);
            inventory.setItem(14, cvItem2.createItemStack());
        }


        return inventory;
    }
}
