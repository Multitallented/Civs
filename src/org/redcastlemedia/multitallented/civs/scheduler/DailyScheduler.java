package org.redcastlemedia.multitallented.civs.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.GovTypeBuff;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public class DailyScheduler implements Runnable {


    @Override
    public void run() {
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getAllRegions()) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            boolean hasUpkeep = regionType.isDailyPeriod() && region.runUpkeep(false);
        }

        doTaxes();
        doVotes();
        addDailyPower();
    }

    private void addDailyPower() {
        HashMap<Town, Integer> addPower = new HashMap<>();

        for (Town town : TownManager.getInstance().getTowns()) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            if (government != null) {
                for (GovTypeBuff buff : government.getBuffs()) {
                    if (buff.getBuffType() == GovTypeBuff.BuffType.POWER) {
                        addPower.put(town, (int) (townType.getPower() * (1 + (double) buff.getAmount() / 100)));
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
            if (town.getGovernmentType() != GovernmentType.DEMOCRACY &&
                    town.getGovernmentType() != GovernmentType.DEMOCRATIC_SOCIALISM &&
                    town.getGovernmentType() != GovernmentType.COOPERATIVE &&
                    town.getGovernmentType() != GovernmentType.CAPITALISM) {
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
                int randIndex = (int) Math.floor(Math.random() * (mostVotesList.size() - 1));
                mostVotes = mostVotesList.get(randIndex);
            }

            if (town.getRawPeople().containsKey(mostVotes) &&
                    !town.getRawPeople().get(mostVotes).contains("owner")) {
                HashSet<UUID> setMembers = new HashSet<>();
                for (UUID uuid : town.getRawPeople().keySet()) {
                    if (town.getRawPeople().get(uuid).contains("owner")) {
                        setMembers.add(uuid);
                    }
                }
                for (UUID uuid : setMembers) {
                    town.setPeople(uuid, "member");
                }
                town.setPeople(mostVotes, "owner");
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
