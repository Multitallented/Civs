package org.redcastlemedia.multitallented.civs.util;


import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class PlaceHook extends PlaceholderExpansion {

    private static final String ROOT_ID = "civs";
    private static final String TOWN_NAME = "townname";
    private static final String KARMA = "karma";
    private static final String KILLS = "kills";
    private static final String KILLSTREAK = "killstreak";
    private static final String HIGHEST_KILLSTREAK = "highestkillstreak";
    private static final String DEATHS = "deaths";
    private static final String POINTS = "points";
    private static final String HIGHEST_BOUNTY = "highestbounty";
    private static final String MANA = "mana";
    private static final String NATION = "nation";

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return ROOT_ID;
    }

    @Override
    public String getAuthor() {
        return Civs.getInstance().getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return Civs.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return "";
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        return routePlaceholder(civilian, identifier);
    }
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        return routePlaceholder(civilian, identifier);
    }

    private String routePlaceholder(Civilian civilian, String identifier) {
        if (TOWN_NAME.equals(identifier)) {
            return getReplacement(civilian);
        } else if (KARMA.equals(identifier)) {
            return "" + civilian.getKarma();
        } else if (KILLS.equals(identifier)) {
            return "" + civilian.getKills();
        } else if (KILLSTREAK.equals(identifier)) {
            return "" + civilian.getKillStreak();
        } else if (HIGHEST_KILLSTREAK.equals(identifier)) {
            return "" + civilian.getHighestKillStreak();
        } else if (DEATHS.equals(identifier)) {
            return "" + civilian.getDeaths();
        } else if (POINTS.equals(identifier)) {
            return "" + civilian.getPoints();
        } else if (MANA.equals(identifier)) {
            return "" + civilian.getMana();
        } else if (HIGHEST_BOUNTY.equals(identifier)) {
            Bounty bounty = civilian.getHighestBounty();
            if (bounty == null) {
                return "";
            } else if (bounty.getIssuer() == null) {
                return "Unknown $" + bounty.getAmount();
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(bounty.getIssuer());
            return offlinePlayer.getName() + " $" + bounty.getAmount();
        } else if (NATION.equals(identifier)) {
            String nation = getNation(civilian);
            if (nation == null) {
                return getReplacement(civilian);
            }
            return nation;
        } else {
            return "";
        }
    }

    private String getNation(Civilian civilian) {
        for (Alliance alliance : AllianceManager.getInstance().getAllSortedAlliances()) {
            for (String townName : alliance.getMembers()) {
                Town town = TownManager.getInstance().getTown(townName);
                if (town.getRawPeople().containsKey(civilian.getUuid()) &&
                        !town.getRawPeople().get(civilian.getUuid()).equals("ally")) {
                    return alliance.getName();
                }
            }
        }
        return null;
    }

    private String getReplacement(Civilian civilian) {
        Town town = TownManager.getInstance().isOwnerOfATown(civilian);
        if (town != null) {
            return town.getName();
        } else {
            int highestPopulation = 0;
            Town highestTown = null;
            for (Town to : TownManager.getInstance().getTowns()) {
                if (!to.getPeople().containsKey(civilian.getUuid())) {
                    continue;
                }
                int pop = to.getPopulation();
                if (pop > highestPopulation) {
                    highestTown = to;
                    highestPopulation = pop;
                }
            }
            return highestTown == null ? "" : highestTown.getName();
        }
    }
}
