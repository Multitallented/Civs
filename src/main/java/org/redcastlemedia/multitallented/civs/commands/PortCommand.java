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
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.effects.RaidPortEffect;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@CivsCommand(keys = { Constants.PORT, "spawn", "home" })
public class PortCommand extends CivCommand {
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

        if (Civs.perm != null && !Civs.perm.has(player, Constants.PORT_PERMISSION)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    LocaleConstants.PERMISSION_DENIED));
            return true;
        }

        final Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        if (!configManager.getPortDuringCombat() && civilian.isInCombat()) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "in-combat"));
            return true;
        }

        if (cooldowns.containsKey(player.getUniqueId())) {
            long cooldown = cooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
            if (cooldown > 0) {
                player.sendMessage(Civs.getPrefix() +
                        localeManager.getTranslation(player, "cooldown")
                                .replace("$1", ((int) cooldown / 1000) + ""));
                return true;
            }
        }

        if (player.getHealth() < ConfigManager.getInstance().getPortDamage()) {
            int healthNeeded = ConfigManager.getInstance().getPortDamage() + 1 - (int) player.getHealth();
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "need-more-health").replace("$1", healthNeeded + ""));
            return true;
        }

        if (player.getFoodLevel() < ConfigManager.getInstance().getPortStamina()) {
            int foodNeeded = ConfigManager.getInstance().getPortStamina() + 1 - player.getFoodLevel();
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "need-more-stamina").replace("$1", foodNeeded + ""));
            return true;
        }

        if (civilian.getMana() < ConfigManager.getInstance().getPortMana()) {
            int manaNeeded = ConfigManager.getInstance().getPortMana() + 1 - civilian.getMana();
            ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civilian.getCurrentClass().getType());
            String manaTitle = LocaleManager.getInstance().getTranslation(player,
                    classType.getManaTitle());
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "need-more-mana").replace("$1", manaNeeded + "")
                    .replace("$2", manaTitle));
            return true;
        }

        double moneyNeeded = ConfigManager.getInstance().getPortMoney();
        if (moneyNeeded > 0 && Civs.econ != null &&
                !Civs.econ.has(player, moneyNeeded)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "not-enough-money").replace("$1", moneyNeeded + ""));
            return true;
        }

        Region r = null;
        Location destination;
        if (args[0].equalsIgnoreCase(Constants.PORT) && args.length > 1) {
            //Check if region is a port
            r = RegionManager.getInstance().getRegionAt(Region.idToLocation(args[1]));
            if (r == null || !canPort(r, player.getUniqueId(), null)) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        LocaleConstants.PORT_NOT_FOUND));
                return true;
            }
        } else if (args.length > 1) {
            String townName = args[1];
            Town town = TownManager.getInstance().getTown(townName);
            if (town == null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        LocaleConstants.PORT_NOT_FOUND));
                return true;
            }
            for (Region region : TownManager.getInstance().getContainingRegions(town.getName())) {
                if (canPort(region, player.getUniqueId(), town)) {
                    r = region;
                    break;
                }
            }
            if (r == null) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        LocaleConstants.PORT_NOT_FOUND));
                return true;
            }
        } else {
            return true;
        }
        destination = new Location(r.getLocation().getWorld(),
                r.getLocation().getX(),
                r.getLocation().getY() + 1,
                r.getLocation().getZ());

        final Player p = player;
        final Location l = destination;

        long delay = 1L;
        long warmup = ConfigManager.getInstance().getPortWarmup() * 20L;
        if (warmup > 0) {
            delay = warmup;
        }
        if (r.getEffects().containsKey(RaidPortEffect.KEY)) {
            RaidPortEffect.portedTo.add(player);
        }
        player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                "port-warmup").replace("$1", (warmup / 20) + ""));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) warmup, 2));
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), () -> {
            if (!p.isOnline() || p.isDead()) {
                return;
            }
            if (civilian.isInCombat()) {
                p.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "in-combat"));
                return;
            }

            if (civilian.getMana() < ConfigManager.getInstance().getPortMana()) {
                int manaNeeded = ConfigManager.getInstance().getPortMana() + 1 - civilian.getMana();
                ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civilian.getCurrentClass().getType());
                String manaTitle = LocaleManager.getInstance().getTranslation(p,
                        classType.getManaTitle());
                p.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "need-more-mana").replace("$1", manaNeeded + "")
                        .replace("$2", manaTitle));
                return;
            } else {
                civilian.setMana(civilian.getMana() - ConfigManager.getInstance().getPortMana());
            }

            double moneyNeeded1 = ConfigManager.getInstance().getPortMoney();
            if (moneyNeeded1 > 0 && Civs.econ != null &&
                    !Civs.econ.has(p, moneyNeeded1)) {
                p.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "not-enough-money").replace("$1", moneyNeeded1 + ""));
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
            cooldowns.put(p.getUniqueId(), System.currentTimeMillis() + ConfigManager.getInstance().getPortCooldown() * 1000L);
            p.teleport(new Location(l.getWorld(), l.getX(), l.getY() + 1, l.getZ()));
            p.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "teleported"));
        }, delay);
        return true;
    }

    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            return getTownNames(args[1]);
        }
        return super.getWord(commandSender, args);
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        if (!(commandSender instanceof Player)) {
            return false;
        }
        Player player = (Player) commandSender;
        return Civs.perm == null || Civs.perm.has(player, Constants.PORT_PERMISSION);
    }

    public static boolean canPort(Region r, UUID uuid, Town town) {
        try {
            if (!r.getEffects().containsKey(Constants.PORT)) {
                return false;
            }
            if ("public".equals(r.getEffects().get(Constants.PORT))) {
                return true;
            }
            boolean privatePort = r.getEffects().get(Constants.PORT) != null &&
                    !r.getEffects().get(Constants.PORT).equals("");
            if (town == null) {
                town = TownManager.getInstance().getTownAt(r.getLocation());
            }
            boolean townPrivatePort = privatePort && r.getEffects().get(Constants.PORT).equals("town");
            boolean memberPrivatePort = privatePort && r.getEffects().get(Constants.PORT).equals(Constants.MEMBER);
            boolean ownerPrivatePort = privatePort && r.getEffects().get(Constants.PORT).equals(Constants.OWNER);
            if (!r.getPeople().containsKey(uuid)) {
                return false;
            }
            if (privatePort) {
                if (townPrivatePort && (town == null || !town.getPeople().containsKey(uuid) ||
                        town.getPeople().get(uuid).contains(Constants.ALLY))) {
                    return false;
                }
                if (memberPrivatePort && r.getPeople().get(uuid).contains(Constants.ALLY)) {
                    return false;
                }
                if (ownerPrivatePort && (r.getPeople().get(uuid).contains(Constants.ALLY) ||
                        r.getPeople().get(uuid).contains(Constants.MEMBER))) {
                    return false;
                }
            }
        } catch (Exception e) {
            Civs.logger.log(Level.SEVERE, "Exception when trying to execute port command", e);
            return false;
        }
        return true;
    }
}
