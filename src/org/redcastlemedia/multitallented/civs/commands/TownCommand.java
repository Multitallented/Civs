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
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
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
        if (townManager.checkIntersect(player.getLocation(), townType)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "too-close-town").replace("$1", townType.getProcessedName()));
            return true;
        }

        HashMap<UUID, String> people = new HashMap<>();
        people.put(player.getUniqueId(), "owner");
        Town town = new Town(args[1], townType.getProcessedName(), player.getLocation(), people);
        townManager.addTown(town);
        townManager.saveTown(town);
        player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                "town-created").replace("$1", town.getName()));

        return true;
    }
}
