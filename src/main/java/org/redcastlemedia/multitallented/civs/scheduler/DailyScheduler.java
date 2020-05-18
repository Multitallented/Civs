package org.redcastlemedia.multitallented.civs.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.*;
import org.redcastlemedia.multitallented.civs.util.Constants;

public class DailyScheduler implements Runnable {


    @Override
    public void run() {
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getAllRegions()) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (regionType.isDailyPeriod()) {
                region.runUpkeep(false);
            }
        }

        for (Town town : TownManager.getInstance().getTowns()) {
            if (town.isDevolvedToday()) {
                town.setDevolvedToday(false);
                TownManager.getInstance().saveTown(town);
            }
        }

        doTaxes();
        doVotes();
        addDailyPower();
        depreciateHardship();
        TownTransitionUtil.checkTownTransitions();
    }

    private void depreciateHardship() {
        long hardshipDepreciationPeriod = ConfigManager.getInstance().getHardshipDepreciationPeriod();
        for (Civilian civilian : CivilianManager.getInstance().getCivilians()) {
            if (civilian.getHardship() < 2 && civilian.getHardship() > -2) {
                continue;
            }
            if (civilian.getDaysSinceLastHardshipDepreciation() < hardshipDepreciationPeriod) {
                civilian.setDaysSinceLastHardshipDepreciation(civilian.getDaysSinceLastHardshipDepreciation() + 1);
                CivilianManager.getInstance().saveCivilian(civilian);
                continue;
            }
            civilian.setDaysSinceLastHardshipDepreciation(0);

            double baseHardship = 0;
            String townName = TownManager.getInstance().getBiggestTown(civilian);
            if (townName != null) {
                Town town = TownManager.getInstance().getTown(townName);
                TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
                baseHardship = townType.getPrice() / (double) town.getRawPeople().size();
            }

            double newHardship = ((civilian.getHardship() - baseHardship) / 2.0) + baseHardship;
            civilian.setHardship(newHardship);
            CivilianManager.getInstance().saveCivilian(civilian);
        }
    }

    private void addDailyPower() {
        HashMap<Town, Integer> addPower = new HashMap<>();

        for (Town town : TownManager.getInstance().getTowns()) {
            town.setGovTypeChangedToday(false);
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            if (government != null) {
                for (GovTypeBuff buff : government.getBuffs()) {
                    if (buff.getBuffType() == GovTypeBuff.BuffType.POWER) {
                        addPower.put(town, (int) Math.round((double) townType.getPower() * (1 + (double) buff.getAmount() / 100)));
                        break;
                    }
                }
            }
            if (!addPower.containsKey(town)) {
                addPower.put(town, townType.getPower());
            }
        }
        for (Town town : addPower.keySet()) {
            TownManager.getInstance().setTownPower(town, town.getPower() + addPower.get(town));
        }
    }

    private void doVotes() {
        HashSet<Town> saveTheseTowns = new HashSet<>();
        for (Town town : TownManager.getInstance().getTowns()) {
            if (town.getVotes().isEmpty()) {
                continue;
            }
            Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            if (government.getGovernmentType() != GovernmentType.DEMOCRACY &&
                    government.getGovernmentType() != GovernmentType.DEMOCRATIC_SOCIALISM &&
                    government.getGovernmentType() != GovernmentType.COOPERATIVE &&
                    government.getGovernmentType() != GovernmentType.CAPITALISM) {
                continue;
            }
            long daysBetweenVotes = ConfigManager.getInstance().getDaysBetweenVotes() * 86400000;
            if (town.getLastVote() > System.currentTimeMillis() - daysBetweenVotes) {
                continue;
            }
            HashMap<UUID, Integer> voteTally = new HashMap<>();
            for (UUID uuid : town.getVotes().keySet()) {
                for (UUID cUuid : town.getVotes().get(uuid).keySet()) {
                    if (voteTally.containsKey(cUuid)) {
                        voteTally.put(cUuid, voteTally.get(cUuid) + town.getVotes().get(uuid).get(cUuid));
                    } else {
                        voteTally.put(cUuid, town.getVotes().get(uuid).get(cUuid));
                    }
                }
            }
            ArrayList<UUID> mostVotesList = new ArrayList<>();
            int mostVoteCount = 0;
            for (UUID uuid : voteTally.keySet()) {
                if (mostVoteCount < voteTally.get(uuid)) {
                    mostVotesList.clear();
                    mostVotesList.add(uuid);
                    mostVoteCount = voteTally.get(uuid);
                } else if (mostVoteCount == voteTally.get(uuid)) {
                    mostVotesList.add(uuid);
                }
            }
            if (mostVotesList.isEmpty()) {
                town.setVotes(new HashMap<>());
                saveTheseTowns.add(town);
                continue;
            }

            UUID mostVotes;
            if (mostVotesList.size() == 1) {
                mostVotes = mostVotesList.get(0);
            } else {
                Random random = new Random();
                int randIndex = random.nextInt(mostVotesList.size());
                mostVotes = mostVotesList.get(randIndex);
            }

            if (town.getRawPeople().containsKey(mostVotes) &&
                    !town.getRawPeople().get(mostVotes).contains(Constants.OWNER)) {
                HashSet<UUID> setMembers = new HashSet<>();
                for (UUID uuid : town.getRawPeople().keySet()) {
                    if (town.getRawPeople().get(uuid).contains(Constants.OWNER)) {
                        setMembers.add(uuid);
                    }
                }
                for (UUID uuid : setMembers) {
                    town.setPeople(uuid, "member");
                }
                town.setPeople(mostVotes, Constants.OWNER);
            }
            town.setVotes(new HashMap<>());
            saveTheseTowns.add(town);
        }
        for (Town town : saveTheseTowns) {
            TownManager.getInstance().saveTown(town);
        }
    }

    private void doTaxes() {
        if (Civs.econ == null) {
            return;
        }
        HashSet<Town> saveThese = new HashSet<>();
        for (Town town : TownManager.getInstance().getTowns()) {
            if (town.getTaxes() < 1) {
                continue;
            }
            for (UUID uuid : town.getRawPeople().keySet()) {
                if (town.getRawPeople().get(uuid).contains("ally")) {
                    continue;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer == null || !Civs.econ.has(offlinePlayer, town.getTaxes())) {
                    continue;
                }
                Civs.econ.withdrawPlayer(offlinePlayer, town.getTaxes());
                town.setBankAccount(town.getBankAccount() + town.getTaxes());
                saveThese.add(town);
            }
        }
        for (Town town : saveThese) {
            TownManager.getInstance().saveTown(town);
        }
    }
}
