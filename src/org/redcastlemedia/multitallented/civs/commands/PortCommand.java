package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.util.HashMap;
import java.util.UUID;

public class PortCommand implements CivCommand {
    private HashMap<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "Unable to invite for non-players");
            return true;
        }
        Player player = (Player) commandSender;
        LocaleManager localeManager = LocaleManager.getInstance();
        ConfigManager configManager = ConfigManager.getInstance();

        final Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        if (!configManager.getPortDuringCombat() && civilian.isInCombat()) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "in-combat"));
            return true;
        }

        if (cooldowns.containsKey(player.getUniqueId())) {
            long cooldown = System.currentTimeMillis() - cooldowns.get(player.getUniqueId());
            if (cooldown < ConfigManager.getInstance().getPortCooldown() * 1000) {
                player.sendMessage(Civs.getPrefix() +
                        localeManager.getTranslation(civilian.getLocale(), "cooldown")
                                .replace("$1", ((int) cooldown / 1000) + ""));
                return true;
            }
        }

        if (player.getHealth() < ConfigManager.getInstance().getPortDamage()) {
            int healthNeeded = ConfigManager.getInstance().getPortDamage() + 1 - (int) player.getHealth();
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "need-more-health").replace("$1", healthNeeded + ""));
            return true;
        }

        if (player.getFoodLevel() < ConfigManager.getInstance().getPortStamina()) {
            int foodNeeded = ConfigManager.getInstance().getPortStamina() + 1 - player.getFoodLevel();
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "need-more-stamina").replace("$1", foodNeeded + ""));
            return true;
        }

        if (civilian.getMana() < ConfigManager.getInstance().getPortMana()) {
            int manaNeeded = ConfigManager.getInstance().getPortMana() + 1 - civilian.getMana();
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "need-more-mana").replace("$1", manaNeeded + ""));
            return true;
        }

        double moneyNeeded = ConfigManager.getInstance().getPortMoney();
        if (moneyNeeded > 0 && Civs.econ != null &&
                !Civs.econ.has(player, moneyNeeded)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "not-enough-money").replace("$1", moneyNeeded + ""));
            return true;
        }

        int j = -1;
        Region r = null;
        Location destination;
        if (args[0].equalsIgnoreCase("port") && args.length > 1) {
            //Check if region is a port
            try {
                r = RegionManager.getInstance().getRegionAt(Region.idToLocation(args[1]));
                if (r == null) {
                    player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                            "port-not-found"));
                    return true;
                }
                if (!r.getEffects().containsKey("port")) {
                    player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                            "port-not-found"));
                    return true;
                }
                boolean privatePort = r.getEffects().get("port") != null &&
                        !r.getEffects().get("port").equals("");
                if (!r.getPeople().containsKey(player.getUniqueId()) || (privatePort &&
                        !r.getPeople().get(player.getUniqueId()).contains("member") &&
                        !r.getPeople().get(player.getUniqueId()).contains("owner"))) {
                    player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                            "not-member-port"));
                    return true;
                }
            } catch (Exception e) {
                Civs.logger.severe("Exception when trying to execute port command");
                e.printStackTrace();
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "port-not-found"));
                return true;
            }
        } else if (args.length > 1) {
            String townName = args[1];
            Town town = TownManager.getInstance().getTown(townName);
            if (town == null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "port-not-found"));
                return true;
            }
            for (Region region : TownManager.getInstance().getContainingRegions(town.getName())) {
                if (!region.getEffects().containsKey("port") ||
                        region.getEffects().get("port") == null ||
                        !region.getPeople().containsKey(player.getUniqueId())) {
                    continue;
                }
                r = region;
                break;
            }
            if (r == null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                        "port-not-found"));
                return true;
            }
        } else {
            return true;
        }
        destination = new Location(r.getLocation().getWorld(),
                r.getLocation().getX(),
                r.getLocation().getY() + 1,
                r.getLocation().getZ());

        //Check to see if the region has enough reagents
//        if (r.getEffects().get("port") != null && !r.hasUpkeepItems()) {
//            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
//                    "region-missing-upkeep-items"));
//            return true;
//        }

        //Run upkeep but don't need to know if upkeep occured
//        r.runUpkeep();

        final Player p = player;
        final Location l = destination;

        long delay = 1L;
        long warmup = ConfigManager.getInstance().getPortWarmup() * 20;
        if (warmup > 0) {
            delay = warmup;
        }
        player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                "port-warmup").replace("$1", (warmup / 20) + ""));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) warmup, 2));
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (!p.isOnline() || p.isDead()) {
                    return;
                }
                if (civilian.isInCombat()) {
                    p.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            "in-combat"));
                    return;
                }

                if (civilian.getMana() < ConfigManager.getInstance().getPortMana()) {
                    int manaNeeded = ConfigManager.getInstance().getPortMana() + 1 - civilian.getMana();
                    p.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            "need-more-mana").replace("$1", manaNeeded + ""));
                    return;
                } else {
                    civilian.setMana(civilian.getMana() - ConfigManager.getInstance().getPortMana());
                }

                double moneyNeeded = ConfigManager.getInstance().getPortMoney();
                if (moneyNeeded > 0 && Civs.econ != null &&
                        !Civs.econ.has(p, moneyNeeded)) {
                    p.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            "not-enough-money").replace("$1", moneyNeeded + ""));
                    return;
                } else if (Civs.econ != null) {
                    Civs.econ.withdrawPlayer(p, ConfigManager.getInstance().getPortMoney());
                }

                if (ConfigManager.getInstance().getPortDamage() > 0) {
                    Bukkit.getPluginManager().callEvent(new EntityDamageEvent(p, EntityDamageEvent.DamageCause.CUSTOM,
                            ConfigManager.getInstance().getPortDamage()));
                }
                if (ConfigManager.getInstance().getPortStamina() > 0) {
                    p.setFoodLevel(Math.max(p.getFoodLevel() - ConfigManager.getInstance().getPortStamina(), 0));
                }
//                for (ItemStack is : reagents) {
//                    p.getInventory().removeItem(is);
//                }
                p.teleport(new Location(l.getWorld(), l.getX(), l.getY() + 1, l.getZ()));
                p.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "teleported"));
            }
        }, delay);
        return true;
    }


}
