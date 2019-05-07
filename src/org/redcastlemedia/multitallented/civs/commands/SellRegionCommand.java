package org.redcastlemedia.multitallented.civs.commands;

import java.text.NumberFormat;
import java.text.ParseException;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

public class SellRegionCommand implements CivCommand {
    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        Player player = (Player) commandSender;
        Location location = player.getLocation();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        Region region = RegionManager.getInstance().getRegionAt(location);
        if (region == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "stand-in-region").replace("$1", player.getDisplayName()));
            return true;
        }
        if (!permissionToSellRegion(player, region)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "no-permission"));
            return true;
        }

        if (args.length < 2) {
            setRegionNotForSale(player, region, -1);
            return true;
        }

        double salePrice = -1;
        try {
            salePrice = Double.parseDouble(args[1]);
        } catch (NullPointerException | NumberFormatException exception) {
            // Don't care
        }
        if (salePrice < 0) {
            return true;
        }

        setRegionNotForSale(player, region, salePrice);
        commandSender.sendMessage(Civs.getPrefix() +
                LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-sale-set")
                .replace("$1", region.getType())
                .replace("$2", NumberFormat.getCurrencyInstance().format(salePrice)));
        return true;
    }

    private void setRegionNotForSale(Player player, Region region, double price) {
        region.setForSale(price);
        RegionManager.getInstance().saveRegion(region);
    }

    private boolean permissionToSellRegion(Player player, Region region) {
        // Dont sell allow sale of a region that has multiple members
        if (region == null || !region.getPeople().containsKey(player.getUniqueId())
                || region.getPeople().keySet().size() != 1) {
            return false;
        }
        return true;
    }
}
