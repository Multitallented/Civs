package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public class DeathListener implements Listener {

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        long jailTime = ConfigManager.getInstance().getJailTime();
        if (civilian.getLastJail() + jailTime < System.currentTimeMillis()) {
            return;
        }
        long timeRemaining = civilian.getLastJail() + jailTime - System.currentTimeMillis();

        Region region = RegionManager.getInstance().getRegionAt(location);
        if (region == null || !region.getEffects().containsKey("jail")) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                "no-commands-in-jail").replace("$1", (int) (timeRemaining / 1000) + "s"));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        Location respawnLocation = civilian.getRespawnPoint();
        if (respawnLocation == null) {
            return;
        }

        event.setRespawnLocation(respawnLocation);
        civilian.setRespawnPoint(null);
        CivilianManager.getInstance().saveCivilian(civilian);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        final Player player = event.getEntity();
        Civilian dyingCiv = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        Location deathLocation = player.getLocation();

        /*if (defaultWorld != null && !defaultWorld.equals(player.getWorld())) {
            deathLocation = new Location(defaultWorld, deathLocation.getX(), deathLocation.getY(), deathLocation.getZ());
        }*/

        //If you die in jail, then bypass jail
        RegionManager regionManager = RegionManager.getInstance();
        Region jail = regionManager.getRegionAt(deathLocation);
        boolean bypassJail = jail != null;
        Player damager = null;
        EntityDamageByEntityEvent entityDamageByEntityEvent = null;

        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            entityDamageByEntityEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
            if (entityDamageByEntityEvent.getDamager() instanceof Player) {
                damager = (Player) entityDamageByEntityEvent.getDamager();
            }
        }

        //If you didn't die in a jail, check if you died in a town with a jail
        outer: if (jail == null) {
            Town town = TownManager.getInstance().getTownAt(deathLocation);
            if (town == null) {
                break outer;
            }
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());

            if (town.getPeople().containsKey(player.getUniqueId())) {
                break outer;
            }

            for (Region r : regionManager.getContainingRegions(town.getLocation(), townType.getBuildRadius())) {
                RegionType rt = (RegionType) ItemManager.getInstance().getItemType(r.getType());

                if (rt.getEffects().containsKey("jail")) {
                    jail = r;
                    break outer;
                }
            }
        }
        //If the killer owns a jail, then use that one
        outer: if (jail == null && damager != null) {
            for (Region r : regionManager.getAllRegions()) {
                if (!r.getPeople().containsKey(damager.getUniqueId())) {
                    continue;
                }

                RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(r.getType());
                if (regionType.getEffects().keySet().contains("jail")) {
                    jail = r;
                    break outer;
                }
            }
        }
        if (!bypassJail && jail != null) {
            //If you died in a town with a jail, then put their respawn point in the jail
            dyingCiv.setRespawnPoint(jail.getLocation().add(0,1,0));
            dyingCiv.refreshJail();
            CivilianManager.getInstance().saveCivilian(dyingCiv);
            return;
        }

        double publicDistance = -1;
        double privateDistance = -1;
        Region graveyard = null;
        Region publicGraveyard = null;
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (!region.getLocation().getWorld().equals(deathLocation.getWorld())) {
                continue;
            }
            if (region.getEffects().containsKey("graveyard_public")) {
                double distance = region.getLocation().distanceSquared(deathLocation);
                if (publicDistance == -1 || publicDistance > distance) {
                    publicDistance = distance;
                    publicGraveyard = region;
                }
            } else if (region.getEffects().containsKey("graveyard")) {
                Town town = TownManager.getInstance().getTownAt(region.getLocation());
                if (town == null) {
                    continue;
                }
                if (!town.getPeople().containsKey(player.getUniqueId()) ||
                        town.getPeople().get(player.getUniqueId()).contains("guest")) {
                    continue;
                }
                double distance = region.getLocation().distanceSquared(deathLocation);
                if (privateDistance == -1 || privateDistance > distance) {
                    privateDistance = distance;
                    graveyard = region;
                }
            }
        }

        if (graveyard == null && publicGraveyard != null) {
            jail = publicGraveyard;
        } else if (graveyard != null && publicGraveyard == null) {
            jail = graveyard;
        } else if (graveyard != null) {
            if (deathLocation.distanceSquared(graveyard.getLocation()) > deathLocation.distanceSquared(publicGraveyard.getLocation())) {
                jail = publicGraveyard;
            } else {
                jail = graveyard;
            }
        }

        if (jail != null) {
            dyingCiv.setRespawnPoint(jail.getLocation().add(0,1,0));
            CivilianManager.getInstance().saveCivilian(dyingCiv);
        }

        if (damager == null) {
            return;
        }

        final Civilian damagerCiv = CivilianManager.getInstance().getCivilian(damager.getUniqueId());
        final LocaleManager localeManager = LocaleManager.getInstance();
        if (dyingCiv.getLastDeath() + ConfigManager.getInstance().getDeathGracePeriod() > System.currentTimeMillis()) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(damagerCiv.getLocale(),
                    "repeat-kill").replace("$1", player.getDisplayName()));
            return;
        }

        dyingCiv.setDeaths(dyingCiv.getDeaths() + 1);
        damagerCiv.setKills(damagerCiv.getKills() + 1);
        damagerCiv.setKillStreak(damagerCiv.getKillStreak() + 1);

        double econBonus = 0.0;
        //TODO award money?
        /*
        if (econ != null) {
            double balance = econ.bankBalance(player.getName()).balance;
            if (balance < psm.getEconBaseStolen()) {
                if (balance > 0) {
                    econBonus += balance;
                    econPay = econBonus;
                }
                if (balance - econPay > 0) {
                    econBonus += (balance - econPay) * psm.getEconPercentStolen();
                    econPay = econBonus;
                }
                if (balance - econPay - psm.getEconBaseDrop() >0) {
                    econPay += psm.getEconBaseDrop();
                } else if (balance > 0) {
                    econPay = balance;
                }
                balance = econ.bankWithdraw(player.getName(), econPay).balance;
                if (balance >0) {
                    econPay = balance * psm.getEconPercentDrop();
                    econ.bankWithdraw(player.getName(), econPay);
                }
            }
            econBonus += psm.getEconBase();
        }
         */
//        damagerCiv.addFavoriteWeapon(player.get(PlayerInventory.class).getQuickbar().getCurrentItem().getMaterial().getDisplayName());
//        dUser.addFavoriteVictim(user.NAME);
//        user.addFavoriteKiller(dUser.NAME);

        double killStreakBonus = ConfigManager.getInstance().getPointsPerKillStreak() * damagerCiv.getKillStreak();

        econBonus += damagerCiv.getKillStreak() * ConfigManager.getInstance().getMoneyPerKillStreak();
        if (damagerCiv.getKillStreak() >= 3) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Civilian civ = CivilianManager.getInstance().getCivilian(p.getUniqueId());
                p.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civ.getLocale(), "kill-streak")
                        .replace("$1", player.getDisplayName())
                        .replace("$2", damagerCiv.getKillStreak() + ""));
            }
        }


        double killJoyBonus = ConfigManager.getInstance().getPointsPerKillJoy() * dyingCiv.getKillStreak();
        econBonus += ConfigManager.getInstance().getMoneyPerKillJoy() * dyingCiv.getKillStreak();
        if (dyingCiv.getKillStreak() > 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Civilian civ = CivilianManager.getInstance().getCivilian(p.getUniqueId());
                p.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civ.getLocale(), "kill-joy")
                        .replace("$1", player.getDisplayName())
                        .replace("$2", damager.getDisplayName())
                        .replace("$3", dyingCiv.getKillStreak() + ""));
            }
        }

        if (damagerCiv.getHighestKillStreak() < damagerCiv.getKillStreak()) {
            damagerCiv.setHighestKillStreak(damagerCiv.getKillStreak());
        }
        dyingCiv.setKillStreak(0);


        double points = ConfigManager.getInstance().getPointsPerKill();
        points += killStreakBonus + killJoyBonus;
        econBonus += ConfigManager.getInstance().getMoneyPerKill();

        double healthBonus = 0;

        double maxHealth = damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (damager.getHealth() / maxHealth < 0.25) {
            healthBonus = ConfigManager.getInstance().getPointsPerHalfHealth();
        } else if (damager.getHealth() / maxHealth < 0.5) {
            healthBonus = ConfigManager.getInstance().getPointsPerQuarterHealth();
        }

        points += healthBonus;
        dyingCiv.setPoints(dyingCiv.getPoints() + ConfigManager.getInstance().getPointsPerDeath());
        damagerCiv.setPoints(damagerCiv.getPoints() + points);

        //pay econ bonus
        if (Civs.econ != null) {
            Civs.econ.depositPlayer(damager, Math.max(econBonus,0));
            Civs.econ.withdrawPlayer(player, Math.abs(ConfigManager.getInstance().getMoneyPerDeath()));
        }

        player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(dyingCiv.getLocale(), "death"
                .replace("$1", ConfigManager.getInstance().getPointsPerDeath() + "")));

        //save
        CivilianManager.getInstance().saveCivilian(dyingCiv);
        CivilianManager.getInstance().saveCivilian(damagerCiv);

        //display points
        long interval = 10L;
        final Player dPlayer = damager;
        if (points > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    dPlayer.sendMessage(Civs.getPrefix() +
                            localeManager.getTranslation(damagerCiv.getLocale(), "kill")
                                    .replace("$1", "" + ConfigManager.getInstance().getPointsPerKill()));
                }
            }, interval);
            interval += 10L;
        }
        if (healthBonus > 0) {
            final double ptsHealth = healthBonus;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    dPlayer.sendMessage(Civs.getPrefix() +
                            localeManager.getTranslation(damagerCiv.getLocale(), "low-health")
                                    .replace("$1", "" + ptsHealth));
                }
            }, interval);
            interval += 10L;
        }
        if (killStreakBonus > 0) {
            final double killStreakPts = killStreakBonus;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    player.sendMessage(Civs.getPrefix() +
                            localeManager.getTranslation(damagerCiv.getLocale(), "killstreak-points")
                                    .replace("$1", "" + killStreakPts));
                }
            }, interval);
            interval += 10L;
        }
        if (killJoyBonus > 0) {
            final double killJoyPts = killJoyBonus;
            Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    player.sendMessage(Civs.getPrefix() +
                            localeManager.getTranslation(damagerCiv.getLocale(), "killjoy-points")
                                    .replace("%amount", "" + killJoyPts));
                }
            }, interval);
            interval += 10L;
        }
        final double pts = points;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                player.sendMessage(Civs.getPrefix() +
                        localeManager.getTranslation(damagerCiv.getLocale(), "total-points")
                                .replace("%amount", "" + pts));
            }
        }, interval);
    }
}
