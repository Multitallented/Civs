package org.redcastlemedia.multitallented.civs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.ConveyorEffect;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public final class AnnouncementUtil {
    private static HashMap<UUID, HashSet<String>> alreadySentMessages = new HashMap<>();

    public static void clearPlayer(UUID uuid) {
        alreadySentMessages.remove(uuid);
    }

    private AnnouncementUtil() {

    }

    public static void sendAnnouncement(Player player) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian.isInCombat() || !civilian.isUseAnnouncements()) {
            return;
        }
        if (!alreadySentMessages.containsKey(player.getUniqueId())) {
            alreadySentMessages.put(player.getUniqueId(), new HashSet<>());
        }
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> messages = new ArrayList<>();
        boolean isOwnerOfATown = false;
        boolean isInAnAlliance = false;
        HashSet<Town> towns = new HashSet<>();
        for (Town town : TownManager.getInstance().getTowns()) {
            if (town.getRawPeople().containsKey(player.getUniqueId())) {
                towns.add(town);
                if (town.getRawPeople().get(player.getUniqueId()).contains("owner")) {
                    isOwnerOfATown = true;
                    if (!isInAnAlliance) {
                        for (Alliance alliance : AllianceManager.getInstance().getAllAlliances()) {
                            if (alliance.getMembers().contains(town.getName())) {
                                isInAnAlliance = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (isOwnerOfATown) {
            for (Town town : towns) {
                if (town.getBankAccount() > 0 && town.getRawPeople().get(civilian.getUuid()).contains("owner") &&
                        !alreadySentMessages.get(civilian.getUuid()).contains("ann-bank-" + town.getName())) {
                    keys.add("ann-bank-" + town.getName());
                    messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-bank")
                            .replace("$1", town.getName())
                            .replace("$2", Util.getNumberFormat(town.getBankAccount(), civilian.getLocale())));
                }
            }

            if (!isInAnAlliance && TownManager.getInstance().getTowns().size() > 1 &&
                    !alreadySentMessages.get(civilian.getUuid()).contains("ann-make-allies")) {
                keys.add("ann-make-allies");
                messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-make-allies"));
            }
        }
        if (!towns.isEmpty()) {
            for (Town town : towns) {

                // Vote
                Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
                boolean isVotingTown = government.getGovernmentType() == GovernmentType.DEMOCRACY ||
                        government.getGovernmentType() == GovernmentType.COOPERATIVE ||
                        government.getGovernmentType() == GovernmentType.DEMOCRATIC_SOCIALISM ||
                        government.getGovernmentType() == GovernmentType.CAPITALISM;
                if (isVotingTown && !town.getVotes().containsKey(civilian.getUuid()) && town.getRawPeople().size() > 1 &&
                        !alreadySentMessages.get(civilian.getUuid()).contains("ann-vote-" + town.getName())) {
                    keys.add("ann-vote-" + town.getName());
                    messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-vote")
                            .replace("$1", town.getName()));
                }

                // Power
                if (town.getPower() < town.getMaxPower() / 3 && government.getGovernmentType() != GovernmentType.FEUDALISM &&
                        !alreadySentMessages.get(civilian.getUuid()).contains("ann-town-low-power-" + town.getName())) {
                    keys.add("ann-town-low-power-" + town.getName());
                    messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-town-low-power")
                            .replace("$1", town.getName())
                            .replace("$2", "" + town.getPower())
                            .replace("$3", "" + town.getMaxPower()));
                }

                // Housing
                if (town.getPopulation() >= town.getHousing() && government.getGovernmentType() != GovernmentType.FEUDALISM &&
                        !alreadySentMessages.get(civilian.getUuid()).contains("ann-town-housing-" + town.getName())) {
                    keys.add("ann-town-housing-" + town.getName());
                    messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-town-housing")
                            .replace("$1", town.getName())
                            .replace("$2", "" + town.getPopulation())
                            .replace("$3", "" + town.getHousing()));
                }
            }

        } else {
            if (!alreadySentMessages.get(civilian.getUuid()).contains("ann-town-protection")) {
                keys.add("ann-town-protection");
                messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-town-protection"));
            }
            if (!alreadySentMessages.get(civilian.getUuid()).contains("ann-town-join")) {
                keys.add("ann-town-join");
                messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-town-join"));
            }

            // check the guide
            if (!civilian.isAskForTutorial() && civilian.getTutorialIndex() != -1 &&
                    civilian.getTutorialPath().equals("default") &&
                    !alreadySentMessages.get(civilian.getUuid()).contains("ann-achievement")) {
                keys.add("ann-achievement");
                messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-achievement"));
            }
        }

        ArrayList<Region> regions = new ArrayList<>();
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (!region.getRawPeople().containsKey(player.getUniqueId()) ||
                    region.getRawPeople().get(player.getUniqueId()).contains("ally")) {
                continue;
            }
            regions.add(region);
        }
        Collections.shuffle(regions);
        int regionCount = 0;
        for (Region region : regions) {
            if (alreadySentMessages.get(civilian.getUuid()).contains("ann-missing-input-" + region.getId())) {
                continue;
            }
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (region.getFailingUpkeeps().size() < regionType.getUpkeeps().size()) {
                continue;
            }
            keys.add("ann-missing-input-" + region.getId());
            messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-missing-input")
                    .replace("$1", regionType.getName()));
            regionCount++;
            if (regionCount > 2) {
                break;
            }
        }

        // Add max group messages
        ArrayList<String> groups = new ArrayList<>(ConfigManager.getInstance().getGroups().keySet());
        Collections.shuffle(groups);
        for (String group : groups) {
            if (!civilian.isAtGroupMax(group) &&
                    !alreadySentMessages.get(civilian.getUuid()).contains("ann-limit-" + group)) {
                keys.add("ann-limit-" + group);
                messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-limit")
                        .replace("$1", "" + civilian.getCountGroup(group))
                        .replace("$2", "" + ConfigManager.getInstance().getGroups().get(group))
                        .replace("$3", group));
                break;
            }
        }

        Player lowestPlayerKarma = null;
        int lowestKarma = 0;
        Player highestPlayerBounty = null;
        double highestBounty = 0;
        for (Player cPlayer : Bukkit.getOnlinePlayers()) {
            if (player.equals(cPlayer)) {
                continue;
            }
            Civilian cCivilian = CivilianManager.getInstance().getCivilian(cPlayer.getUniqueId());
            if (civilian.isFriend(cCivilian) ||
                    !TownManager.getInstance().findCommonTowns(civilian, cCivilian).isEmpty()) {
                continue;
            }
            if (lowestKarma > cCivilian.getKarma() && cCivilian.getKarma() < civilian.getKarma()) {
                lowestPlayerKarma = cPlayer;
                lowestKarma = cCivilian.getKarma();
            }
            Bounty bounty = cCivilian.getHighestBounty();
            if (bounty != null && highestBounty < bounty.getAmount()) {
                highestBounty = bounty.getAmount();
                highestPlayerBounty = cPlayer;
            }
        }
        if (lowestPlayerKarma != null &&
                !alreadySentMessages.get(civilian.getUuid()).contains("ann-karma-" + lowestPlayerKarma.getName())) {
            keys.add("ann-karma-" + lowestPlayerKarma.getName());
            messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-karma")
                    .replace("$1", lowestPlayerKarma.getDisplayName()));
        }
        if (highestPlayerBounty != null &&
                !alreadySentMessages.get(civilian.getUuid()).contains("ann-bounty-" + highestPlayerBounty.getName())) {
            keys.add("ann-bounty-" + highestPlayerBounty.getName());
            messages.add(LocaleManager.getInstance().getRawTranslation(civilian.getLocale(), "ann-bounty")
                    .replace("$1", highestPlayerBounty.getDisplayName())
                    .replace("$2", Util.getNumberFormat(highestBounty, civilian.getLocale())));
        }

        if (messages.isEmpty()) {
            return;
        } else if (messages.size() < 2) {
            alreadySentMessages.get(civilian.getUuid()).add(keys.get(0));
            sendToPlayer(player, Civs.getRawPrefix() + messages.get(0) + " ");
            return;
        }
        Random random = new Random();
        int randIndex = random.nextInt(messages.size());
        alreadySentMessages.get(civilian.getUuid()).add(keys.get(randIndex));
        sendToPlayer(player, Civs.getRawPrefix() + messages.get(randIndex) + " ");
    }

    private static void sendToPlayer(Player player, String input) {
        BaseComponent message = Util.parseColorsComponent(input);
        TextComponent unsub = new TextComponent("[X]");
        unsub.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cv toggleann"));
        unsub.setColor(ChatColor.RED);
        unsub.setUnderlined(true);
        message.addExtra(unsub);
        player.spigot().sendMessage(message);
    }

    public static String formatTime(long duration) {
        if (duration < 60) {
            return duration + "s";
        } else if (duration < 3600) {
            return (int) (duration / 60) + "m " + (int) (duration % 60) + "s";
        } else {
            int hours = (int) (duration / 3600);
            return hours + "h " + (int) ((duration - hours * 3600) / 60) + "m " + (int) (duration % 60) + "s";
        }
    }
}
