package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.*;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@CivsCommand(keys = { "setowner" })
public class SetOwnerCommand extends CivCommand {

    public boolean runCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        LocaleManager localeManager = LocaleManager.getInstance();

        boolean isAdmin;
        Civilian civilian = null;
        if (player != null) {
            civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            isAdmin = player.isOp() || (Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_PERMISSION));
        } else {
            isAdmin = true;
        }
        if (strings.length < 3) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "specify-player-region"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Please specify a player and a region");
            }
            return true;
        }

        //0 invite
        //1 player
        //2 regionname
        UUID inviteUUID = UUID.fromString(strings[1]);
        String locationString = strings[2];

        Town town = TownManager.getInstance().getTown(locationString);
        Region region = null;
        if (town == null) {
            region = RegionManager.getInstance().getRegionAt(Region.idToLocation(locationString));
        }
        if (region == null && town == null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "no-permission"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Invalid region");
            }
            return true;
        }
        if (region != null && !Util.hasOverride(region, civilian) && player != null &&
                !region.getPeople().get(player.getUniqueId()).contains(Constants.OWNER)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "no-permission"));
            return true;
        }
        OfflinePlayer invitee = Bukkit.getOfflinePlayer(inviteUUID);
        Player invitePlayer = invitee.isOnline() ? (Player) invitee : null;
        if (!invitee.isOnline() && region != null) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "player-not-online").replace("$1", "Unknown"));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Player unknown is not online");
            }
            return true;
        }
        if (region != null && invitePlayer != null &&
                region != RegionManager.getInstance().getRegionAt(invitePlayer.getLocation())) {
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "stand-in-region").replace("$1", invitePlayer.getName()));
            } else {
                commandSender.sendMessage(Civs.getPrefix() + "Please have " + invitee.getName() + " stand in the region");
            }
            return true;
        }
        if (town != null) {
            Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            if (!isAdmin && (government.getGovernmentType() == GovernmentType.DEMOCRACY ||
                    government.getGovernmentType() == GovernmentType.DEMOCRATIC_SOCIALISM)) {
                if (player != null) {
                    player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                            "no-permission"));
                }
                return true;
            }

            boolean hasPermission = civilian == null || government.getGovernmentType() == GovernmentType.ANARCHY ||
                    (town.getRawPeople().containsKey(civilian.getUuid()) &&
                            town.getRawPeople().get(civilian.getUuid()).contains(Constants.OWNER));

            boolean oligarchyOverride = !hasPermission && Civs.econ != null && government.getGovernmentType() == GovernmentType.OLIGARCHY;

            boolean colonialOverride = town.getColonialTown() != null;
            if (colonialOverride) {
                Town colonialTown = TownManager.getInstance().getTown(town.getColonialTown());
                if (civilian != null && (!colonialTown.getRawPeople().containsKey(civilian.getUuid()) ||
                        !colonialTown.getRawPeople().get(civilian.getUuid()).contains(Constants.OWNER))) {
                    colonialOverride = false;
                }
            }

            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());

            double price = townType.getPrice(civilian) * 2;

            if (oligarchyOverride && !Civs.econ.has(player, price)) {
                String priceString = Util.getNumberFormat(price, civilian.getLocale());
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "not-enough-money").replace("$1", priceString));
                return true;
            }
            if (!isAdmin && !hasPermission && !oligarchyOverride && !colonialOverride) {
                if (player != null) {
                    player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                            "no-permission"));
                }
                return true;
            }
            if (oligarchyOverride) {
                Civs.econ.withdrawPlayer(player, price);
                HashSet<UUID> uuids = new HashSet<>();
                for (UUID uuid : town.getRawPeople().keySet()) {
                    if (town.getRawPeople().get(uuid).contains(Constants.OWNER)) {
                        uuids.add(uuid);
                    }
                }
                if (!uuids.isEmpty()) {
                    for (UUID uuid : uuids) {
                        Civs.econ.depositPlayer(Bukkit.getOfflinePlayer(uuid), price / (double) uuids.size());
                    }
                }
            }
        }

        Civilian inviteCiv = CivilianManager.getInstance().getCivilian(invitee.getUniqueId());

        if (region != null) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            String maxLimit = inviteCiv.isAtMax(regionType);
            if (maxLimit != null) {
                if (player != null && invitePlayer != null) {
                    player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(
                            civilian.getLocale(), "max-qty")
                            .replace("$1", invitePlayer.getDisplayName())
                            .replace("$2", maxLimit));
                }
                return true;
            }
        }
        String name = town == null ? region.getType() : town.getName();
        if (invitePlayer != null) {
            invitePlayer.sendMessage(Civs.getPrefix() + localeManager.getTranslation(inviteCiv.getLocale(),
                    "add-owner-region").replace("$1", name));
        }
        if (player != null && civilian != null && invitePlayer != null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "owner-added-region").replace("$1", invitePlayer.getDisplayName())
                    .replace("$2", name));
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(inviteUUID);
            commandSender.sendMessage(Civs.getPrefix() + offlinePlayer.getName() + " is now an owner of your " + name);
        }
        if (region != null && region.getPeople().get(invitee.getUniqueId()) != null &&
                !region.getPeople().get(invitee.getUniqueId()).contains(Constants.OWNER)) {
            region.setPeople(invitee.getUniqueId(), Constants.OWNER);
            RegionManager.getInstance().saveRegion(region);
        } else if (town != null && town.getPeople().get(invitee.getUniqueId()) != null &&
                !town.getPeople().get(invitee.getUniqueId()).contains(Constants.OWNER)) {
            town.setPeople(invitee.getUniqueId(), Constants.OWNER);
            TownManager.getInstance().saveTown(town);
        }
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return true;
    }


    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 2) {
            addAllOnlinePlayers(suggestions, args[1]);
            return suggestions;
        }
        if (args.length == 3 && commandSender instanceof Player) {
            Player player = (Player) commandSender;
            Region region = RegionManager.getInstance().getRegionAt(player.getLocation());
            if (region != null) {
                suggestions.add(region.getId());
                suggestions.addAll(getTownNames(args[2]));
                return suggestions;
            }
        } else if (args.length == 3) {
            return getTownNames(args[2]);
        }
        return super.getWord(commandSender, args);
    }
}
