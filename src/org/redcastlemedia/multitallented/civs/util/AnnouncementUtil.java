package org.redcastlemedia.multitallented.civs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

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
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public final class AnnouncementUtil {
    private AnnouncementUtil() {

    }

    public static void sendAnnouncement(Player player) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian.isInCombat() || !civilian.isUseAnnouncements()) {
            return;
        }
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
                if (town.getBankAccount() > 0 && town.getRawPeople().get(civilian.getUuid()).contains("owner")) {
                    messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-bank")
                            .replace("$1", town.getName())
                            .replace("$2", Util.getNumberFormat(town.getBankAccount(), civilian.getLocale())));
                }
            }

            if (!isInAnAlliance && TownManager.getInstance().getTowns().size() > 1) {
                messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-make-allies"));
            }
        } else if (!towns.isEmpty()) {
            for (Town town : towns) {

                // Vote
                boolean isVotingTown = town.getGovernmentType() == GovernmentType.DEMOCRACY ||
                        town.getGovernmentType() == GovernmentType.COOPERATIVE ||
                        town.getGovernmentType() == GovernmentType.DEMOCRATIC_SOCIALISM ||
                        town.getGovernmentType() == GovernmentType.CAPITALISM;
                if (isVotingTown && !town.getVotes().containsKey(civilian.getUuid()) && town.getRawPeople().size() > 1) {
                    messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-vote")
                            .replace("$1", town.getName()));
                }

                // Power
                if (town.getPower() < town.getMaxPower() / 3 && town.getGovernmentType() != GovernmentType.FEUDALISM) {
                    messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-town-low-power")
                            .replace("$1", town.getName())
                            .replace("$2", "" + town.getPower())
                            .replace("$3", "" + town.getMaxPower()));
                }

                // Housing
                if (town.getPopulation() >= town.getHousing() && town.getGovernmentType() != GovernmentType.FEUDALISM) {
                    messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-town-housing")
                            .replace("$1", town.getName())
                            .replace("$2", "" + town.getPopulation())
                            .replace("$3", "" + town.getHousing()));
                }
            }

        } else {
            messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-town-protection"));
            messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-town-join"));

            // check the guide
            if (!civilian.isAskForTutorial() && civilian.getTutorialIndex() != -1 &&
                    civilian.getTutorialPath().equals("default")) {
                messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-achievement"));
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
        regionOuter: for (Region region : regions) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            for (int i=0; i<regionType.getUpkeeps().size(); i++) {
                if (!region.hasUpkeepItems(i, true)) {
                    messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-missing-input")
                            .replace("$1", regionType.getName()));
                    regionCount++;
                    if (regionCount > 2) {
                        break regionOuter;
                    } else {
                        continue;
                    }
                }
            }
        }

        // Add max group messages
        for (String group : ConfigManager.getInstance().getGroups().keySet()) {
            if (!civilian.isAtGroupMax(group)) {
                messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-limit")
                        .replace("$1", "" + civilian.getCountGroup(group))
                        .replace("$2", "" + ConfigManager.getInstance().getGroups().get(group))
                        .replace("$3", group));
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
        if (lowestPlayerKarma != null) {
            messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-karma")
                    .replace("$1", lowestPlayerKarma.getDisplayName()));
        }
        if (highestPlayerBounty != null) {
            messages.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "ann-bounty")
                    .replace("$1", highestPlayerBounty.getDisplayName())
                    .replace("$2", Util.getNumberFormat(highestBounty, civilian.getLocale())));
        }

        if (messages.isEmpty()) {
            return;
        } else if (messages.size() < 2) {
            sendToPlayer(player, Civs.getPrefix() + messages.get(0) + " ");
            return;
        }
        Random random = new Random();
        int randIndex = random.nextInt(messages.size());
        sendToPlayer(player, Civs.getPrefix() + messages.get(randIndex) + " ");
    }

    private static void sendToPlayer(Player player, String input) {
        TextComponent message = Util.parseColorsComponent(input);
        TextComponent unsub = new TextComponent("[X]");
        unsub.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cv toggleann"));
        unsub.setColor(ChatColor.RED);
        unsub.setUnderlined(true);
        message.addExtra(unsub);
        player.spigot().sendMessage(message);
    }
}
