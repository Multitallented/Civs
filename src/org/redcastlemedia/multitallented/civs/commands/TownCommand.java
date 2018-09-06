package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.RegionListMenu;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
            HashMap<String, Integer> checkList = new HashMap<>(townType.getReqs());
            Set<Region> regions = RegionManager.getInstance().getRegionsXYZ(player.getLocation(), townType.getBuildRadius(),
                    townType.getBuildRadiusY(), townType.getBuildRadius(), false);
            for (Region region : regions) {
                if (checkList.containsKey(region.getType())) {
                    if (checkList.get(region.getType()) < 2) {
                        checkList.remove(region.getType());
                    } else {
                        checkList.put(region.getType(), checkList.get(region.getType()) - 1);
                    }
                }
            }
            if (!checkList.isEmpty()) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "missing-region-requirements").replace("$1", townType.getDisplayName()));
                RegionListMenu.createMenu(checkList);
                return true;
            }
        }



        HashMap<UUID, String> people = new HashMap<>();
        people.put(player.getUniqueId(), "owner");
        Location newTownLocation = player.getLocation();
        String name = Util.getValidFileName(args[1]);
        if (townType.getChild() != null) {
            Town intersectTown = intersectTowns.get(0);
            people = intersectTown.getPeople();
            newTownLocation = intersectTown.getLocation();
            name = intersectTown.getName();
            TownManager.getInstance().removeTown(intersectTown, false);
        }

        int housingCount = 0;
        for (Region region : getRegionsInTown(newTownLocation, townType.getBuildRadius(), townType.getBuildRadiusY())) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (regionType.getEffects().containsKey("housing")) {
                housingCount++;
            }
        }

        Town town = new Town(name,
                townType.getProcessedName(),
                newTownLocation,
                people,
                townType.getPower(),
                townType.getMaxPower(), housingCount, people.size());
        townManager.addTown(town);
        townManager.saveTown(town);
        player.getInventory().remove(itemStack);
        player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                "town-created").replace("$1", town.getName()));

        return true;
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
