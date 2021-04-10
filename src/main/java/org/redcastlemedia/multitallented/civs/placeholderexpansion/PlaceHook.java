package org.redcastlemedia.multitallented.civs.placeholderexpansion;


import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.chat.ChatManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public class PlaceHook extends PlaceholderExpansion {

    private static final String ROOT_ID = "civs";
    private static final String TOWN_NAME = "townname";
    private static final String KARMA = "karma";
    private static final String HARDSHIP = "hardship";
    private static final String KILLS = "kills";
    private static final String KILLSTREAK = "killstreak";
    private static final String HIGHEST_KILLSTREAK = "highestkillstreak";
    private static final String DEATHS = "deaths";
    private static final String POINTS = "points";
    private static final String HIGHEST_BOUNTY = "highestbounty";
    private static final String MANA = "mana";
    private static final String NATION = "nation";
    private static final String POWER = "power";
    private static final String MAX_POWER = "max_power";
    private static final String POPULATION = "population";
    private static final String HOUSING = "housing";
    private static final String CHAT_CHANNEL_NAME = "chatchannel";
    private static final String TOWN_BANK = "townbank";

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
        return routePlaceholder(civilian, identifier, player);
    }
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        return routePlaceholder(civilian, identifier, player);
    }

    private String routePlaceholder(Civilian civilian, String identifier, OfflinePlayer player) {
        if (TOWN_NAME.equals(identifier)) {
            return TownManager.getInstance().getBiggestTown(civilian);
        } else if (POWER.equals(identifier)) {
            Town town = TownManager.getInstance().getTown(TownManager.getInstance().getBiggestTown(civilian));
            if (town == null) {
                return "-";
            }
            return "" + town.getPower();
        } else if (MAX_POWER.equals(identifier)) {
            Town town = TownManager.getInstance().getTown(TownManager.getInstance().getBiggestTown(civilian));
            if (town == null) {
                return "-";
            }
            return "" + town.getMaxPower();
        } else if (POPULATION.equals(identifier)) {
            Town town = TownManager.getInstance().getTown(TownManager.getInstance().getBiggestTown(civilian));
            if (town == null) {
                return "-";
            }
            return "" + town.getPopulation();

        } else if (TOWN_BANK.equals(identifier)) {
            Town town = TownManager.getInstance().getTown(TownManager.getInstance().getBiggestTown(civilian));
            if (town == null) {
                return "-";
            }
            return "" + Util.getNumberFormat(town.getBankAccount(), civilian.getLocale());

        } else if (HOUSING.equals(identifier)) {
            Town town = TownManager.getInstance().getTown(TownManager.getInstance().getBiggestTown(civilian));
            if (town == null) {
                return "-";
            }
            return "" + town.getHousing();
        } else if (KARMA.equals(identifier)) {
            return "" + civilian.getKarma();
        } else if (HARDSHIP.equals(identifier)) {
            return "" + (int) civilian.getHardship();
        } else if (KILLS.equals(identifier)) {
            return "" + civilian.getKills();
        } else if (KILLSTREAK.equals(identifier)) {
            return "" + civilian.getKillStreak();
        } else if (HIGHEST_KILLSTREAK.equals(identifier)) {
            return "" + civilian.getHighestKillStreak();
        } else if (DEATHS.equals(identifier)) {
            return "" + civilian.getDeaths();
        } else if (POINTS.equals(identifier)) {
            return "" + (int) civilian.getPoints();
        } else if (MANA.equals(identifier)) {
            return "" + civilian.getMana();
        } else if (CHAT_CHANNEL_NAME.equals(identifier)) {
            if (ChatChannel.ChatChannelType.GLOBAL == civilian.getChatChannel().getChatChannelType()) {
                return "";
            }
            return civilian.getChatChannel().getName(player);
        } else if (HIGHEST_BOUNTY.equals(identifier)) {
            Bounty bounty = civilian.getHighestBounty();
            if (bounty == null) {
                return "-";
            }
            String bountyString = Util.getNumberFormat(bounty.getAmount(), civilian.getLocale());
            if (bounty.getIssuer() == null) {
                return "Unknown $" + bountyString;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(bounty.getIssuer());
            if (offlinePlayer.getName() == null) {
                return "Unknown $" + bountyString;
            }
            return offlinePlayer.getName() + " $" + bountyString;
        } else if (NATION.equals(identifier)) {
            String nation = ChatManager.getNation(civilian);
            if (nation == null) {
                return TownManager.getInstance().getBiggestTown(civilian);
            }
            return nation;
        } else {
            return "-";
        }
    }


}
