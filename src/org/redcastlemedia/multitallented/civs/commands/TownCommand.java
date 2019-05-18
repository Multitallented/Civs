package org.redcastlemedia.multitallented.civs.commands;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.TownCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.TownEvolveEvent;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.RegionListMenu;
import org.redcastlemedia.multitallented.civs.menus.SelectGovTypeMenu;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.HousingEffect;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Util;

public class TownCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "Unable to use town command for non-players");
            return true;
        }
        Player player = (Player) commandSender;
        LocaleManager localeManager = LocaleManager.getInstance();
        ItemManager itemManager = ItemManager.getInstance();

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        //0 town
        //1 townName
        if (args.length < 2 || !Util.validateFileName(args[1])) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "specify-town-name"));
            return true;
        }
        String name = Util.getValidFileName(args[1]);

        if (TownManager.getInstance().townNameExists(name)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "specify-town-name"));
            return true;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack == null || !CivItem.isCivsItem(itemStack)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "hold-town"));
            return true;
        }
        CivItem civItem = itemManager.getItemType(itemStack.getItemMeta().getDisplayName()
                .replace("Civs ", "").toLowerCase());

        if (civItem == null || !(civItem instanceof TownType)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "hold-town"));
            return true;
        }
        TownType townType = (TownType) civItem;

        TownManager townManager = TownManager.getInstance();
        List<Town> intersectTowns = townManager.checkIntersect(player.getLocation(), townType);
        if (intersectTowns.size() > 1 ||
                (townType.getChild() != null &&
                        !intersectTowns.isEmpty() &&
                        !townType.getChild().equals(intersectTowns.get(0).getType()))) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "too-close-town").replace("$1", townType.getProcessedName()));
            return true;
        }
        if (intersectTowns.isEmpty() && townType.getChild() != null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "must-be-built-on-top").replace("$1", townType.getProcessedName())
                    .replace("$2", townType.getChild()));
            return true;
        }

        if (!townType.getReqs().isEmpty()) {
            HashMap<String, Integer> checkList = (HashMap<String, Integer>) townType.getReqs().clone();
            Set<Region> regions = RegionManager.getInstance().getRegionsXYZ(player.getLocation(), townType.getBuildRadius(),
                    townType.getBuildRadiusY(), townType.getBuildRadius(), false);
            regionCheck: for (Region region : regions) {
                RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                String regionTypeName = regionType.getProcessedName();
                if (checkList.containsKey(regionTypeName)) {
                    if (checkList.get(regionTypeName) < 2) {
                        checkList.remove(regionTypeName);
                    } else {
                        checkList.put(regionTypeName, checkList.get(regionTypeName) - 1);
                    }
                    continue;
                }
                for (String groupType : regionType.getGroups()) {
                    String groupName = groupType.toLowerCase();
                    if (checkList.containsKey(groupName)) {
                        if (checkList.get(groupName) < 2) {
                            checkList.remove(groupName);
                        } else {
                            checkList.put(groupName, checkList.get(groupName) - 1);
                        }
                        continue regionCheck;
                    }
                }
            }
            if (!checkList.isEmpty()) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "missing-region-requirements").replace("$1", townType.getDisplayName()));
                player.openInventory(RegionListMenu.createMenu(civilian, checkList, 0));
                return true;
            }
        }



        HashMap<UUID, String> people = new HashMap<>();
        people.put(player.getUniqueId(), "owner");
        Location newTownLocation = player.getLocation();
        List<Location> childLocations = new ArrayList<>();
        TownType childTownType = null;
        int villagerCount = 0;
        if (townType.getChild() != null) {
            Town intersectTown = intersectTowns.get(0);
            if (intersectTown.getPopulation() < townType.getChildPopulation()) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(), "population-req")
                        .replace("$1", intersectTown.getType())
                        .replace("$2", "" + townType.getChildPopulation()));
                return true;
            }
            people = intersectTown.getPeople();
            newTownLocation = intersectTown.getLocation();
            childLocations.add(newTownLocation);
            name = intersectTown.getName();
            childTownType = (TownType) ItemManager.getInstance().getItemType(intersectTown.getType());
            TownManager.getInstance().removeTown(intersectTown, false, false);
            // Don't destroy the ring on upgrade
//            if (ConfigManager.getInstance().getTownRings()) {
//                intersectTown.destroyRing(false);
//            }
            villagerCount = intersectTown.getVillagers();
        }

        int housingCount = getHousingCount(newTownLocation, townType);

        Town town = new Town(name,
                townType.getProcessedName(),
                newTownLocation,
                people,
                townType.getPower(),
                townType.getMaxPower(), housingCount, villagerCount, -1);
        town.setChildLocations(childLocations);
        townManager.addTown(town);
        player.getInventory().remove(itemStack);

        if (childTownType != null) {
            TownEvolveEvent townEvolveEvent = new TownEvolveEvent(town, childTownType, townType);
            Bukkit.getPluginManager().callEvent(townEvolveEvent);

            if (town.getGovernmentType() == GovernmentType.COOPERATIVE && Civs.econ != null) {
                double price = townType.getPrice();
                price = Math.min(price, town.getBankAccount());
                Civs.econ.depositPlayer(player, price);
                town.setBankAccount(town.getBankAccount() - price);
                String priceString = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(civilian.getLocale()))
                        .format(price);
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "town-assist-price").replace("$1", priceString)
                        .replace("$2", townType.getDisplayName()));
            }

        } else {
            TownCreatedEvent townCreatedEvent = new TownCreatedEvent(town, townType);
            town.setLastVote(System.currentTimeMillis());
            Bukkit.getPluginManager().callEvent(townCreatedEvent);
        }
        townManager.saveTown(town);

        player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                "town-created").replace("$1", town.getName()));
        if (ConfigManager.getInstance().getTownRings()) {
            town.createRing();
        }
        if (ConfigManager.getInstance().isAllowChangingOfGovType() && childTownType == null) {
            player.openInventory(SelectGovTypeMenu.createMenu(civilian, town));
        }
        return true;
    }

    int getHousingCount(Location newTownLocation, TownType townType) {
        int housingCount = 0;
        for (Region region : getRegionsInTown(newTownLocation, townType.getBuildRadius(), townType.getBuildRadiusY())) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (regionType.getEffects().containsKey(HousingEffect.KEY)) {
                housingCount += Integer.parseInt(regionType.getEffects().get(HousingEffect.KEY));
            }
        }
        return housingCount;
    }

    public Set<Region> getRegionsInTown(Town town) {
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        return getRegionsInTown(town.getLocation(), townType.getBuildRadius(), townType.getBuildRadiusY());
    }

    public Set<Region> getRegionsInTown(Location location, int radius, int radiusY) {
        //TODO fix this to account for vertical radius being different
        return RegionManager.getInstance().getContainingRegions(location, radius);
    }
}
