package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.allowedactions.AllowedActionsListener;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.*;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.skills.CivSkills;
import org.redcastlemedia.multitallented.civs.skills.Skill;
import org.redcastlemedia.multitallented.civs.spells.civstate.BuiltInCivState;
import org.redcastlemedia.multitallented.civs.towns.*;
import org.redcastlemedia.multitallented.civs.tutorials.AnnouncementUtil;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.regions.StructureUtil;
import org.redcastlemedia.multitallented.civs.util.MessageUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import lombok.Getter;
import lombok.Setter;

public class CommonScheduler implements Runnable {
    @Getter
    protected static final Map<UUID, ArrayList<Region>> lastRegion = new HashMap<>();
    @Getter
    protected static final Map<UUID, Town> lastTown = new HashMap<>();
    private static final HashMap<UUID, Long> lastAnnouncment = new HashMap<>();
    private int i = 0;
    private boolean notTwoSecond = true;
    @Getter @Setter
    protected static boolean run = true;

    @Override
    public void run() {
        try {
            if (!run) {
                return;
            }
            depreciateKarma();
            StructureUtil.cleanUpExpiredBoundingBoxes();
            if (ConfigManager.getInstance().isUseParticleBoundingBoxes()) {
                StructureUtil.refreshAllBoundingBoxes();
            }

            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            int maxTPS = 5;
            int chunk = players.size() / maxTPS;
            for (int j = chunk * i; j < (i == maxTPS - 1 ? players.size() : chunk * (i + 1)); j++) {
                Player[] playerArray = players.toArray(new Player[0]);
                if (playerArray.length > j) {
                    Player player = playerArray[j];
                    if (player != null) {
                        checkPlayer(player);
                    }
                }
            }
            RegionTickUtil.runUpkeeps();
            if (i == maxTPS - 1) {
                i = 0;
                notTwoSecond = !notTwoSecond;
                if (!notTwoSecond) {
                    Bukkit.getPluginManager().callEvent(new TwoSecondEvent());
                }
            } else {
                i++;
            }
        } catch (Exception e) {
            Civs.logger.log(Level.SEVERE, "Error occurred during Civs heartbeat thread", e);
        }
    }

    private void checkPlayer(Player player) {
        try {
            playerInRegion(player);
            playerInTown(player);
            if (ConfigManager.getInstance().getUseClassesAndSpells()) {
                incrementMana(player);
            }
            if (ConfigManager.getInstance().isUseAnnouncements()) {
                sendAnnouncement(player);
            }
            checkExploration(player);
            AllowedActionsListener.dropInvalidArmorOrWeapons(player);
        } catch (Exception e) {
            Civs.logger.log(Level.SEVERE, "Error occurred during Civs heartbeat player check", e);
        }
    }

    private void checkExploration(Player player) {
        if (!ConfigManager.getInstance().isUseSkills()) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        Skill skill = civilian.getSkills().get(CivSkills.EXPLORATION.name().toLowerCase());
        if (skill != null) {
            try {
                Biome biome = player.getLocation().getBlock().getBiome();
                double exp = skill.addAccomplishment(biome.name());
                MessageUtil.saveCivilianAndSendExpNotification(player, civilian, skill, exp);
            } catch (NullPointerException npe) {
                String message = "Invalid biome at " + player.getName() + " location";
                Civs.logger.log(Level.SEVERE, message, npe);
            }
        }

    }

    private void sendAnnouncement(Player player) {
        long announcementCooldown = ConfigManager.getInstance().getAnnouncementPeriod() * 1000;
        if (!lastAnnouncment.containsKey(player.getUniqueId())) {
            lastAnnouncment.put(player.getUniqueId(), System.currentTimeMillis() + announcementCooldown);
            return;
        } else if (lastAnnouncment.get(player.getUniqueId()) > System.currentTimeMillis()) {
            return;
        } else {
            lastAnnouncment.put(player.getUniqueId(), System.currentTimeMillis() + announcementCooldown);
        }
        AnnouncementUtil.sendAnnouncement(player);
    }

    public static void removeLastAnnouncement(UUID uuid) {
        lastAnnouncment.remove(uuid);
    }

    private void depreciateKarma() {
        long karmaPeriod = ConfigManager.getInstance().getKarmaDepreciatePeriod() * 1000;
        for (Civilian civilian : CivilianManager.getInstance().getCivilians()) {
            if ((civilian.getKarma() < 2 && civilian.getKarma() > -2) ||
                    (civilian.getLastKarmaDepreciation() + karmaPeriod > System.currentTimeMillis())) {
                continue;
            }
            civilian.setLastKarmaDepreciation(System.currentTimeMillis());
            double newKarma = (double) civilian.getKarma() / 2;
            civilian.setKarma(newKarma < 0 ? (int) Math.ceil(newKarma) : (int) Math.floor(newKarma));
            CivilianManager.getInstance().saveCivilian(civilian);
        }
    }

    void incrementMana(Player player) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian.hasBuiltInState(BuiltInCivState.MANA_FREEZE_NATURAL) ||
                civilian.hasBuiltInState(BuiltInCivState.MANA_FREEZE_GAIN)) {
            return;
        }
        int maxMana = civilian.getCurrentClass().getMaxMana();
        int maxManaPerSecond = civilian.getCurrentClass().getManaPerSecond();
        if (civilian.getMana() < maxMana) {
            int newMana = Math.min(maxMana, civilian.getMana() + maxManaPerSecond);
            ManaChangeEvent manaChangeEvent = new ManaChangeEvent(civilian.getUuid(), newMana,
                    ManaChangeEvent.ManaChangeReason.NATURAL_REGEN);
            Bukkit.getPluginManager().callEvent(manaChangeEvent);
            if (!manaChangeEvent.isCancelled()) {
                civilian.setMana(newMana);
            }
        }
    }
    void playerInTown(Player player) {
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTownAt(player.getLocation());
        Town prevTown = lastTown.get(player.getUniqueId());
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        TownType townType = null;
        if (town != null) {
            townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            PlayerInTownEvent playerInTownEvent = new PlayerInTownEvent(player.getUniqueId(),
                    town, townType);
            Bukkit.getPluginManager().callEvent(playerInTownEvent);
        }
        TownType prevTownType = null;
        if (prevTown != null) {
            prevTownType = (TownType) ItemManager.getInstance().getItemType(prevTown.getType());
        }

        if (prevTown == null && town != null) {
            enterTown(player, town, townType);
        } else if (prevTown != null && town != null &&
                !prevTown.equals(town)) {
            exitTown(player, civilian, prevTown, prevTownType);
            enterTown(player, town, townType);
        } else if (town == null && prevTown != null) {
            exitTown(player, civilian, prevTown, prevTownType);
        }

        if (town == null && prevTown != null) {
            lastTown.remove(player.getUniqueId());
        } else if (town != null) {
            lastTown.put(player.getUniqueId(), town);
        }

        if (town != null && town.getRawPeople().containsKey(player.getUniqueId()) &&
                town.getRawPeople().get(player.getUniqueId()).contains(Constants.OWNER) &&
                town.getLastActive() + 10000 < System.currentTimeMillis()) {
            town.setLastActive(System.currentTimeMillis());
            TownManager.getInstance().saveTown(town);
        }
    }

    private void enterTown(Player player, Town town, TownType townType) {
        PlayerEnterTownEvent playerEnterTownEvent = new PlayerEnterTownEvent(player.getUniqueId(),
                town, townType);
        Bukkit.getPluginManager().callEvent(playerEnterTownEvent);

        if (playerEnterTownEvent.isNotify()) {
            Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            String govName = "Unknown";
            if (government != null) {
                govName = LocaleManager.getInstance().getTranslation(player,
                        government.getName().toLowerCase() + LocaleConstants.NAME_SUFFIX);
            }
            if (ConfigManager.getInstance().isEnterExitMessagesUseTitles()) {
                player.sendTitle(ChatColor.GREEN + town.getName(), ChatColor.BLUE + govName, 5, 40, 5);
            } else {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "town-enter").replace("$1", town.getName())
                        .replace("$2", govName));
            }
            if (!town.getPeople().containsKey(player.getUniqueId())) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "town-enter-warning"));
            }
        }
    }

    private void exitTown(Player player, Civilian civilian, Town town, TownType townType) {
        PlayerExitTownEvent playerExitTownEvent = new PlayerExitTownEvent(player.getUniqueId(),
                town, townType);
        Bukkit.getPluginManager().callEvent(playerExitTownEvent);
        if (playerExitTownEvent.isNotify()) {
            Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            String govName = "Unknown";
            if (government != null) {
                govName = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        government.getName().toLowerCase() + LocaleConstants.NAME_SUFFIX);
            }
            if (ConfigManager.getInstance().isEnterExitMessagesUseTitles()) {
                String wild = LocaleManager.getInstance().getTranslation(civilian.getLocale(), "wild");
                player.sendTitle(ChatColor.GREEN + wild, "", 5, 40, 5);
            } else {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "town-exit").replace("$1", town.getName())
                        .replace("$2", govName));
            }
        }
    }

    private void playerInRegion(Player player) {
        RegionManager regionManager = RegionManager.getInstance();
        ArrayList<Region> containedRegions = new ArrayList<>(regionManager.getRegionEffectsAt(player.getLocation(), 0));

        for (Region region : containedRegions) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            PlayerInRegionEvent playerInRegionEvent = new PlayerInRegionEvent(player.getUniqueId(),
                    region, regionType);
            Bukkit.getPluginManager().callEvent(playerInRegionEvent);
        }

        ArrayList<Region> previousRegions = lastRegion.get(player.getUniqueId());
        if (previousRegions == null) {
            previousRegions = new ArrayList<>();
        }

        for (Region r : containedRegions) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(r.getType());
            if (!previousRegions.contains(r)) {

                PlayerEnterRegionEvent playerEnterRegionEvent = new PlayerEnterRegionEvent(player.getUniqueId(),
                        r, regionType);

                if (playerEnterRegionEvent.isNotify() && ConfigManager.getInstance().isEnterExitMessagesUseTitles()) {
                    String localRegionTypeName = regionType.getDisplayName(player);
                    player.sendTitle(" ", ChatColor.BLUE + localRegionTypeName, 5, 40, 5);
                }

                Bukkit.getPluginManager().callEvent(playerEnterRegionEvent);
            }
        }

        for (Region r : previousRegions) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(r.getType());
            if (!containedRegions.contains(r)) {
                PlayerExitRegionEvent playerExitRegionEvent = new PlayerExitRegionEvent(player.getUniqueId(),
                        r, regionType);
                Bukkit.getPluginManager().callEvent(playerExitRegionEvent);
            }
        }

        if (!containedRegions.isEmpty()) {
            lastRegion.put(player.getUniqueId(), containedRegions);
        } else {
            lastRegion.remove(player.getUniqueId());
        }
    }
}
