package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.TwoSecondEvent;

import java.util.HashSet;
import java.util.UUID;

public final class TownTransitionUtil {
    private TownTransitionUtil() {

    }

    public static void checkTownTransitions() {
        HashSet<Town> saveThese = new HashSet<>();
        int i=0;
        for (final Town town : TownManager.getInstance().getTowns()) {
            i++;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
               @Override
               public void run() {
                   if (checkTown(town)) {
                       saveThese.add(town);
                   }
               }
            },i*20);
        }
        for (Town town : saveThese) {
            TownManager.getInstance().saveTown(town);
        }
    }

    protected static boolean checkTown(Town town) {
        if (town.isGovTypeChangedToday()) {
            return false;
        }
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        if (government == null) {
            return false;
        }

        for (GovTransition transition : government.getTransitions()) {
            boolean moneyGap = transition.getMoneyGap() > 0 && Civs.econ != null && town.getRawPeople().size() > 1;
            if (moneyGap) {
                double highestMoney = 0;
                double totalMoney = 0;
                for (UUID uuid : town.getRawPeople().keySet()) {
                    double money = Civs.econ.getBalance(Bukkit.getOfflinePlayer(uuid));
                    totalMoney += money;
                    if (highestMoney < money) {
                        highestMoney = money;
                    }
                }
                if (totalMoney == 0 || highestMoney < (double) transition.getMoneyGap() / 100 * totalMoney) {
                    continue;
                }
            }

            boolean power = transition.getPower() < 0 ||
                    town.getPower() < ((double) transition.getPower() / 100 * (double) town.getMaxPower());
            if (!power) {
                continue;
            }

            boolean revolt = transition.getRevolt() > 0;
            double townRevoltPercent = ((double) town.getRevolt().size() / (double) town.getRawPeople().size());
            if (revolt && townRevoltPercent < (double) transition.getRevolt() / 100) {
                continue;
            }

            boolean inactive = transition.getInactive() > 0;
            if (inactive && (town.getLastActive() < 0 ||
                    town.getLastActive() + transition.getInactive() * 1000 > System.currentTimeMillis())) {
                continue;
            }

            GovernmentManager.getInstance().transitionGovernment(town,
                    transition.getTransitionGovernmentType(), false);
            return true;
        }
        return false;
    }
}
