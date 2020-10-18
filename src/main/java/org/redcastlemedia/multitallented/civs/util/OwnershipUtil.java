package org.redcastlemedia.multitallented.civs.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionUpkeep;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public final class OwnershipUtil {
    private OwnershipUtil() {

    }

    public static boolean shouldDenyOwnershipOverSomeone(Town town, Civilian civilian, Civilian invitee, Player player) {
        LocaleManager localeManager = LocaleManager.getInstance();
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());

        boolean isOwner = town.getRawPeople().containsKey(civilian.getUuid()) &&
                town.getRawPeople().get(civilian.getUuid()).contains(Constants.OWNER);

        boolean inviteeIsOwner = town.getRawPeople().containsKey(invitee.getUuid()) &&
                !town.getRawPeople().get(invitee.getUuid()).contains(Constants.OWNER);

        double price = townType.getPrice(civilian) * 2;
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        boolean oligarchyOverride = player != null && !isOwner && inviteeIsOwner &&
                government.getGovernmentType() == GovernmentType.OLIGARCHY;

        boolean hasMoney = Civs.econ != null && Civs.econ.has(player, price);

        if (oligarchyOverride && !hasMoney) {
            String moneyString = Util.getNumberFormat(price, civilian.getLocale());
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
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        boolean colonialOverride = government.getGovernmentType() == GovernmentType.COLONIALISM &&
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
        if ("withdraw".equals(args[0]) && amount > town.getBankAccount()) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "not-enough-money").replace("$1", args[2]));
            return 0;
        }
        if ("deposit".equals(args[0])) {
            double maxDeposit = ConfigManager.getInstance().getMaxBankDeposit();
            if (!Civs.econ.has(player, amount)) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "not-enough-money").replace("$1", args[2]));
                return 0;
            }

            if (maxDeposit > 0 && amount + town.getBankAccount() > maxDeposit) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "too-much-money").replace("$1", Util.getNumberFormat(maxDeposit, civilian.getLocale())));
                return 0;
            }
        }

        boolean colonialOverride = hasColonialOverride(town, civilian);
        boolean isOwner = town.getRawPeople().containsKey(civilian.getUuid()) &&
                town.getRawPeople().get(civilian.getUuid()).contains(Constants.OWNER);
        if (!isOwner && !colonialOverride) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "no-permission"));
            return 0;
        }

        return amount;
    }

    public static Map<UUID, Double> getCooperativeSplit(Town town, Region originRegion) {
        HashMap<UUID, Double> payoutSplit = new HashMap<>();
        int total = 0;
        for (Region region : TownManager.getInstance().getContainingRegions(town.getName())) {
            if (region.equals(originRegion)) {
                Set<UUID> owners = region.getOwners();
                for (UUID uuid : owners) {
                    double amount = 1 / (double) owners.size();
                    if (payoutSplit.containsKey(uuid)) {
                        payoutSplit.put(uuid, payoutSplit.get(uuid) + amount);
                    } else {
                        payoutSplit.put(uuid, amount);
                    }
                }
                total++;
                continue;
            }
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            int i=0;
            for (RegionUpkeep upkeep : regionType.getUpkeeps()) {
                if (upkeep.getPayout() <= 0) {
                    i++;
                    continue;
                }
                if (region.hasUpkeepItems(i, false)) {
                    Set<UUID> owners = region.getOwners();
                    for (UUID uuid : owners) {
                        double amount = 1 / (double) owners.size();
                        if (payoutSplit.containsKey(uuid)) {
                            payoutSplit.put(uuid, payoutSplit.get(uuid) + amount);
                        } else {
                            payoutSplit.put(uuid, amount);
                        }
                    }
                    total++;
                }
                i++;
            }
        }
        HashMap<UUID, Double> payouts = new HashMap<>();
        for (UUID uuid : payoutSplit.keySet()) {
            payouts.put(uuid, payoutSplit.get(uuid) / total * 0.9);
        }
        return payouts;
    }
}
