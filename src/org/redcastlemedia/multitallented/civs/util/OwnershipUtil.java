package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

public final class OwnershipUtil {
    private OwnershipUtil() {

    }

    public static boolean shouldDenyOwnershipOverSomeone(Town town, Civilian civilian, Civilian invitee, Player player) {
        LocaleManager localeManager = LocaleManager.getInstance();
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());

        boolean isOwner = town.getRawPeople().containsKey(civilian.getUuid()) &&
                town.getRawPeople().get(civilian.getUuid()).contains("owner");

        boolean inviteeIsOwner = town.getRawPeople().containsKey(invitee.getUuid()) &&
                !town.getRawPeople().get(invitee.getUuid()).contains("owner");

        double price = townType.getPrice() * 2;
        boolean oligarchyOverride = player != null && !isOwner && inviteeIsOwner &&
                town.getGovernmentType() == GovernmentType.OLIGARCHY;

        boolean hasMoney = Civs.econ != null && Civs.econ.has(player, price);

        if (oligarchyOverride && !hasMoney) {
            String moneyString = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(civilian.getLocale())).format(price);
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "not-enough-money").replace("$1", moneyString));
            return false;
        }

        if (inviteeIsOwner && !oligarchyOverride && !isOwner) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "no-permission"));
            }
            return true;
        }

        if (oligarchyOverride) {
            OfflinePlayer invitePlayer = Bukkit.getOfflinePlayer(invitee.getUuid());
            Civs.econ.withdrawPlayer(player, price);
            Civs.econ.depositPlayer(invitePlayer, price);
        }
        return false;
    }

    public static boolean hasColonialOverride(Town town, Civilian civilian) {
        boolean colonialOverride = town.getGovernmentType() == GovernmentType.COLONIALISM &&
                town.getColonialTown() != null;
        colonial: if (colonialOverride) {
            for (Town cTown : TownManager.getInstance().getOwnedTowns(civilian)) {
                cTown.getName().equalsIgnoreCase(town.getColonialTown());
                break colonial;
            }
            colonialOverride = false;
        }
        return colonialOverride;
    }

    public static double invalidAmountOrTown(Player player, String[] args, Civilian civilian) {
        double amount = 0;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (Exception e) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "invalid-target"));
            return 0;
        }
        if (amount < 1) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "invalid-target"));
            return 0;
        }

        // TODO possibly make this case insensitive?
        Town town = TownManager.getInstance().getTown(args[1]);
        if (town == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "invalid-target"));
            return 0;
        }
        if (amount > town.getBankAccount()) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "not-enough-money").replace("$1", args[2]));
            return 0;
        }

        boolean colonialOverride = hasColonialOverride(town, civilian);
        boolean isOwner = false;
        for (UUID uuid : town.getRawPeople().keySet()) {
            if (town.getRawPeople().get(uuid).contains("owner")) {
                isOwner = true;
            }
        }
        if (!isOwner && !colonialOverride) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "no-permission"));
            return 0;
        }

        return amount;
    }
}
