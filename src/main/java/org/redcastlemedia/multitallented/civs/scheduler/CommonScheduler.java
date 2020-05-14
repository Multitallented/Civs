package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.*;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.*;
import org.redcastlemedia.multitallented.civs.util.AnnouncementUtil;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.StructureUtil;

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
    protected static final Map<UUID, ChunkClaim> lastClaims = new HashMap<>();
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
                checkPlayer((Player) players.toArray()[j]);
            }
            RegionTickUtil.runUpkeeps();
            if (i == maxTPS - 1) {
                i = 0;
                notTwoSecond = !notTwoSecond;
                if (!notTwoSecond) {
                    Bukkit.getPluginManager().callEvent(new TwoSecondEvent());
                    UnloadedInventoryHandler.getInstance().loadChunks();
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
            playerInChunk(player);
        } catch (Exception e) {
            Civs.logger.log(Level.SEVERE, "Error occurred during Civs heartbeat player check", e);
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
        double maxMana = 0;
        double maxManaPerSecond = 0;
        for (CivClass civClass : civilian.getCivClasses()) {
            maxMana = Math.max(maxMana, civClass.getMaxMana());
            maxManaPerSecond = Math.max(maxManaPerSecond, civClass.getManaPerSecond());
        }
        setConvertedMana(civilian, maxMana, maxManaPerSecond);
    }
    void setConvertedMana(Civilian civilian, double maxMana, double manaPerSecond) {
        if (civilian.getMana() < 100 && manaPerSecond > 0) {
            double currentConvertedMana = (double) civilian.getMana() / 100 * maxMana;
            int newMana = (int) ((currentConvertedMana + manaPerSecond) / maxMana * 100);
            civilian.setMana(newMana);
        }
    }

    private void playerInChunk(Player player) {
        ChunkClaim claim = ChunkClaim.fromLocation(player.getLocation());
        ChunkClaim lastClaim = lastClaims.get(player.getUniqueId());
        if (claim.getNation() == null) {
            if (lastClaim.getNation() != null) {
                exitClaim(lastClaim, claim, player);
            }
            return;
        }
        if (lastClaim.getNation() == null) {
            enterClaim(null, claim, player);
        } else if (!lastClaim.equals(claim)) {
            exitClaim(lastClaim, claim, player);
            enterClaim(lastClaim, claim, player);
        } else {
            stayInClaim(claim, player);
        }
    }

    private void stayInClaim(ChunkClaim claim, Player player) {
        if (claim.getNation() == null) {
            claimNeutralChunk(claim, player);
            return;
        }
        final long CAPTURE_TIME = ConfigManager.getInstance().getAllianceClaimCaptureTime() * 1000;
        if (claim.getLastEnter() != -1 &&
                claim.getLastEnter() + CAPTURE_TIME < System.currentTimeMillis()) {
            boolean isInNation = NationManager.getInstance().isInNation(player.getUniqueId(), claim.getNation());
            if (!isInNation && TownManager.getInstance().getTownAt(player.getLocation()) == null) {

                Nation nation = claim.getNation();
                nation.getNationClaims().get(player.getLocation().getWorld().getUID()).remove(claim.getId());
                NationManager.getInstance().saveNation(nation);

                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                        player, "neutralized-claim"
                ).replace("$1", claim.getNation().getName()));
            } else {
                claim.setLastEnter(-1);
            }
        }
    }

    private void exitClaim(ChunkClaim lastClaim, ChunkClaim claim, Player player) {
        lastClaim.setLastEnter(-1);
        if (claim.getNation() == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                    player, "exit-town"
            ).replace("$1", lastClaim.getNation().getName()));
            lastClaims.remove(player.getUniqueId());
        } else {
            lastClaims.put(player.getUniqueId(), claim);
        }
    }

    private void claimNeutralChunk(ChunkClaim claim, Player player) {
        claim.setLastEnter(-1);
        ItemStack claimItemStack = CVItem.createCVItemFromString(ConfigManager.getInstance().getClaimMaterial()).createItemStack();
        if (!player.getInventory().contains(claimItemStack)) {
            return;
        }
        boolean saveNation = false;
        Nation nation = NationManager.getInstance().getNationByPlayer(player.getUniqueId());
        if (nation == null || !nation.getNationClaims().containsKey(claim.getWorld().getUID())) {
            return;
        }
        String northKey = (claim.getX() + 1) + "," + claim.getZ();
        String westKey = claim.getX() + "," + (claim.getZ() + 1);
        String southKey = (claim.getX() - 1) + "," + claim.getZ();
        String eastKey = claim.getX() + "," + (claim.getZ() - 1);
        if (nation.getNationClaims().get(claim.getWorld().getUID()).containsKey(northKey) ||
                nation.getNationClaims().get(claim.getWorld().getUID()).containsKey(westKey) ||
                nation.getNationClaims().get(claim.getWorld().getUID()).containsKey(southKey) ||
                nation.getNationClaims().get(claim.getWorld().getUID()).containsKey(eastKey)) {

            saveNation = true;
        }
        if (saveNation) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                    player, "alliance-chunk-claimed"
            ).replace("$1", nation.getName()));

            claim.setNation(nation);
            player.getInventory().removeItem(claimItemStack);
            nation.getNationClaims().get(claim.getWorld().getUID()).put(claim.getId(), claim);
            NationManager.getInstance().saveNation(nation);
        }
    }

    private void enterClaim(ChunkClaim lastClaim, ChunkClaim claim, Player player) {
        lastClaims.put(player.getUniqueId(), claim);
        if (claim.getNation() == null) {
            claimNeutralChunk(claim, player);
            return;
        }
        boolean isInNation = NationManager.getInstance().isInNation(player.getUniqueId(), claim.getNation());
        if (claim.getLastEnter() == -1 && !isInNation) {
            claim.setLastEnter(System.currentTimeMillis());
        } else if (isInNation) {
            claim.setLastEnter(-1);
        }
        if (lastClaim.getNation() != null && !lastClaim.getNation().equals(claim.getNation())) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                    player, "enter-town"
            ).replace("$1", claim.getNation().getName()));
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
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        String govName = "Unknown";
        if (government != null) {
            govName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    government.getName().toLowerCase() + LocaleConstants.NAME_SUFFIX);
        }
        if (ConfigManager.getInstance().isEnterExitMessagesUseTitles()) {
            player.sendTitle(ChatColor.GREEN + town.getName(), ChatColor.BLUE + govName, 5, 40, 5);
        } else {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "town-enter").replace("$1", town.getName())
                    .replace("$2", govName));
        }
        if (!town.getPeople().containsKey(player.getUniqueId())) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "town-enter-warning"));
        }
    }
    private void exitTown(Player player, Civilian civilian, Town town, TownType townType) {
        PlayerExitTownEvent playerExitTownEvent = new PlayerExitTownEvent(player.getUniqueId(),
                town, townType);
        Bukkit.getPluginManager().callEvent(playerExitTownEvent);
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

    private void playerInRegion(Player player) {
        RegionManager regionManager = RegionManager.getInstance();
        ArrayList<Region> containedRegions = new ArrayList<>();
        containedRegions.addAll(regionManager.getRegionEffectsAt(player.getLocation(), 0));

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
                if (ConfigManager.getInstance().isEnterExitMessagesUseTitles()) {
                    Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                    String localRegionTypeName = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            regionType.getProcessedName() + LocaleConstants.NAME_SUFFIX);
                    player.sendTitle(" ", ChatColor.BLUE + localRegionTypeName, 5, 40, 5);
                }
                PlayerEnterRegionEvent playerEnterRegionEvent = new PlayerEnterRegionEvent(player.getUniqueId(),
                        r, regionType);
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
