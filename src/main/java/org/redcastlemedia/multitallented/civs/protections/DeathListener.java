package org.redcastlemedia.multitallented.civs.protections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.EnterCombatEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.RepairEffect;
import org.redcastlemedia.multitallented.civs.skills.CivSkills;
import org.redcastlemedia.multitallented.civs.spells.civstate.BuiltInCivState;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsSingleton()
public class DeathListener implements Listener {

    public static void getInstance() {
        DeathListener deathListener = new DeathListener();
        Bukkit.getPluginManager().registerEvents(deathListener, Civs.getInstance());
    }

    @EventHandler(ignoreCancelled = true) @SuppressWarnings("unused")
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        boolean isAdmin = Civs.perm != null && Civs.perm.has(player, Constants.PVP_EXEMPT_PERMISSION);
        if (isAdmin) {
            return;
        }

        if (ConfigManager.getInstance().isCombatTagEnabled() &&
                !ConfigManager.getInstance().isAllowTeleportInCombat() &&
                !Util.isDisallowedByWorld(player.getWorld().getName()) &&
                getDistanceSquared(event.getFrom(), event.getTo()) > 9) {
            if (civilian.isInCombat()) {
                event.setCancelled(true);
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "in-combat"));
                return;
            }
        }

        if (PlayerTeleportEvent.TeleportCause.ENDER_PEARL.equals(event.getCause()) ||
                PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT.equals(event.getCause())) {
            Town fromTown = TownManager.getInstance().getTownAt(event.getFrom());
            Town toTown = TownManager.getInstance().getTownAt(event.getTo());
            if (isBlockedInTown(fromTown, player) || isBlockedInTown(toTown, player)) {
                event.setCancelled(true);
                return;
            }
        }

        if (!ConfigManager.getInstance().isAllowTeleportingOutOfHostileTowns()) {
            Town town = TownManager.getInstance().getTownAt(event.getFrom());
            if (town != null && !town.getPeople().containsKey(player.getUniqueId())) {
                Region region = RegionManager.getInstance().getRegionAt(event.getTo());
                if (region == null || !region.getEffects().containsKey("bypass_hostile_port")) {
                    event.setCancelled(true);
                    player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                            "no-tp-out-of-town"));
                    return;
                }
            }
        }
    }

    private boolean isBlockedInTown(Town town, Player player) {
        if (town == null) {
            return false;
        }
        if (town.getPeople().containsKey(player.getUniqueId())) {
            return false;
        }
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                "no-tp-pearl-chorus"));
        return true;
    }

    private double getDistanceSquared(Location location1, Location location2) {
        if (location1 == null || location2 == null) {
            return 0;
        }
        if (!location1.getWorld().equals(location2.getWorld())) {
            return 999999;
        }
        return location1.distanceSquared(location2);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (Util.isDisallowedByWorld(event.getEntity().getWorld().getName())) {
            return;
        }
        Player damager = null;
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;
            if (entityDamageByEntityEvent.getDamager() instanceof Player) {
                damager = (Player) entityDamageByEntityEvent.getDamager();
            } else if (entityDamageByEntityEvent.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) entityDamageByEntityEvent.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    damager = (Player) projectile.getShooter();
                }
            }

            if (damager != null) {
                Civilian damagerCiv = CivilianManager.getInstance().getCivilian(damager.getUniqueId());
                if (damagerCiv.hasBuiltInState(BuiltInCivState.NO_OUTGOING_DAMAGE) ||
                        damagerCiv.hasBuiltInState(BuiltInCivState.STUN)) {
                    event.setCancelled(true);
                    damager.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                            damager, "spell-block"));
                    return;
                }
            }
        }



        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        if (civilian.hasBuiltInState(BuiltInCivState.NO_INCOMING_DAMAGE)) {
            event.setCancelled(true);
            if (damager != null) {
                damager.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        damager, "spell-block"));
            }
            return;
        }

        if (!civilian.isInCombat()) {
            boolean setCancelled = event.isCancelled() ||
                    ProtectionHandler.shouldBlockAction(player.getLocation(), "deny_damage");
            if (setCancelled) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getDamage() > 0 && event.getCause() != EntityDamageEvent.DamageCause.SUFFOCATION &&
                event.getCause() != EntityDamageEvent.DamageCause.FALL &&
                event.getCause() != EntityDamageEvent.DamageCause.CONTACT &&
                event.getCause() != EntityDamageEvent.DamageCause.DROWNING &&
                event.getCause() != EntityDamageEvent.DamageCause.VOID) {
            checkArmorSkill(player);
        }

        long combatTagDuration = ConfigManager.getInstance().getCombatTagDuration();
        combatTagDuration *= 1000;
        if (!(event instanceof EntityDamageByEntityEvent)) {
            if (civilian.getLastDamage() > System.currentTimeMillis() - combatTagDuration) {
                civilian.setLastDamage(System.currentTimeMillis());
            } else {
                civilian.setLastDamager(null);
                civilian.setLastDamage(-1);
            }
            return;
        }
        if (damager == null && civilian.getLastDamage() < 0) {
            return;
        }
        if (damager != null) {
            Civilian damagerCiv = CivilianManager.getInstance().getCivilian(damager.getUniqueId());
            if (damagerCiv.getFriends().contains(civilian.getUuid())) {
                event.setCancelled(true);
                damager.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(damager,
                                "friendly-fire"));
                return;
            }

            if (!damagerCiv.isInCombat()) {
                boolean setCancelled = event.isCancelled() || ProtectionHandler.shouldBlockAction(player.getLocation(), "deny_pvp") ||
                        ProtectionHandler.shouldBlockAction(damager.getLocation(), "deny_pvp");
                if (setCancelled) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (cancelForWarDisabledTowns(damager, player)) {
                event.setCancelled(true);
                return;
            }
            if (cancelIfNotInPvpEnabledTown(damager, player)) {
                event.setCancelled(true);
                return;
            }

            if (damager != player) {
                if (ConfigManager.getInstance().isCombatTagEnabled() && !damagerCiv.isInCombat()) {
                    damager.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(damager,
                            "combat-tagged").replace("$1", "" + (combatTagDuration / 1000)));
                }
                damagerCiv.setLastDamage(System.currentTimeMillis());
                damagerCiv.setLastDamager(player.getUniqueId());
                EnterCombatEvent enterCombatEvent = new EnterCombatEvent(damagerCiv.getUuid());
                Bukkit.getPluginManager().callEvent(enterCombatEvent);
            }
        }
        if (!civilian.isInCombat() && player.equals(damager)) {
            return;
        }
        if (ConfigManager.getInstance().isCombatTagEnabled() && !civilian.isInCombat()) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "combat-tagged").replace("$1", "" + (combatTagDuration / 1000)));
        }
        civilian.setLastDamage(System.currentTimeMillis());
        EnterCombatEvent enterCombatEvent = new EnterCombatEvent(player.getUniqueId());
        Bukkit.getPluginManager().callEvent(enterCombatEvent);
        if (damager == null) {
            return;
        }
        civilian.setLastDamager(damager.getUniqueId());
    }

    private boolean cancelForWarDisabledTowns(Player damager, Player player) {
        boolean damagerHasWarBuilding = false;
        boolean playerHasWarBuilding = false;
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (region.getRawPeople().containsKey(player.getUniqueId()) &&
                    region.getRawPeople().get(player.getUniqueId()).contains(Constants.OWNER)) {
                RegionType rt = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                if (rt.isWarEnabled()) {
                    playerHasWarBuilding = true;
                }
            }
            if (region.getRawPeople().containsKey(damager.getUniqueId()) &&
                    region.getRawPeople().get(damager.getUniqueId()).contains(Constants.OWNER)) {
                RegionType rt = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                if (rt.isWarEnabled()) {
                    damagerHasWarBuilding = true;
                }
            }
            if (damagerHasWarBuilding && playerHasWarBuilding) {
                return false;
            }
        }
        for (Town town : TownManager.getInstance().getTownsForPlayer(player.getUniqueId())) {
            if (town.getRawPeople().containsKey(damager.getUniqueId()) &&
                    town.getRawPeople().containsKey(player.getUniqueId())) {
                return false;
            }
            if (town.isWarEnabledToday()) {
                damagerHasWarBuilding = true;
            }
        }
        for (Town town : TownManager.getInstance().getTownsForPlayer(damager.getUniqueId())) {
            if (town.isWarEnabledToday()) {
                playerHasWarBuilding = true;
            }
        }
        return !(playerHasWarBuilding && damagerHasWarBuilding);
    }

    private boolean cancelIfNotInPvpEnabledTown(Player damager, Player player) {
        if (ConfigManager.getInstance().getPvpWorlds().isEmpty()) {
            return false;
        }
        if (ConfigManager.getInstance().getPvpWorlds().contains(player.getWorld().getName()) &&
                ConfigManager.getInstance().getPvpWorlds().contains(damager.getWorld().getName())) {
            return false;
        }
        for (Town town : TownManager.getInstance().getTownsForPlayer(player.getUniqueId())) {
            if (town.isPvpEnabled()) {
                return false;
            }
        }
        for (Town town : TownManager.getInstance().getTownsForPlayer(damager.getUniqueId())) {
            if (town.isPvpEnabled()) {
                return false;
            }
        }
        return true;
    }

    private void checkArmorSkill(Player player) {
        if (!ConfigManager.getInstance().isUseSkills()) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        Set<Material> armors = new HashSet<>();
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestPlate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        if (helmet != null && RepairEffect.isHelmet(helmet.getType())) {
            armors.add(helmet.getType());
        }
        if (chestPlate != null && RepairEffect.isChestplate(chestPlate.getType())) {
            armors.add(chestPlate.getType());
        }
        if (leggings != null && RepairEffect.isLeggings(leggings.getType())) {
            armors.add(leggings.getType());
        }
        if (boots != null && RepairEffect.isBoots(boots.getType())) {
            armors.add(boots.getType());
        }
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        boolean hasShield = offHandItem != null && offHandItem.getType().equals(Material.SHIELD) &&
                player.isBlocking();
        if (hasShield) {
            civilian.awardSkill(player, "DAMAGE", CivSkills.SHIELD.name());
        }
        Collection<String> armorNames = new HashSet<>();
        for (Material mat : armors) {
            if (mat != null && mat != Material.AIR) {
                armorNames.add(mat.name());
            }
        }
        if (!armorNames.isEmpty()) {
            civilian.awardSkill(player, armorNames, CivSkills.ARMOR.name());
        }
    }

    @EventHandler
    public void onDeflectArrow(ProjectileHitEvent event) {
        if (Util.isDisallowedByWorld(event.getEntity().getWorld().getName()) ||
                !(event.getHitEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getHitEntity();
        if (!player.isBlocking()) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        civilian.awardSkill(player, "BLOCKED", CivSkills.SHIELD.name());
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        if (civilian.hasBuiltInState(BuiltInCivState.NO_COMMANDS)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                    event.getPlayer(), "spell-block"));
            return;
        }

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
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                "no-commands-in-jail").replace("$1", (int) (timeRemaining / 1000) + "s"));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        Location respawnLocation = civilian.getRespawnPoint();

        if (ConfigManager.getInstance().getUseStarterBook()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    player.getInventory().addItem(Util.createStarterBook(civilian.getLocale()));
                }
            }, 5L);
        }
        if (respawnLocation == null) {
            return;
        }

        event.setRespawnLocation(respawnLocation);
        civilian.setRespawnPoint(null);
        CivilianManager.getInstance().saveCivilian(civilian);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodHeal(EntityRegainHealthEvent event) {
        if (Util.isDisallowedByWorld(event.getEntity().getWorld().getName())) {
            return;
        }
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED ||
                ConfigManager.getInstance().getFoodHealInCombat()) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (!civilian.isInCombat()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true) @SuppressWarnings("unused")
    public void onEntityDeath(EntityDeathEvent event) {
        if (Util.isDisallowedByWorld(event.getEntity().getWorld().getName()) ||
                event.getEntity().getKiller() == null) {
            return;
        }
        Player player = event.getEntity().getKiller();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        TutorialManager.getInstance().completeStep(civilian, TutorialManager.TutorialType.KILL, event.getEntity().getType().name().toLowerCase());

        if (RepairEffect.isSword(mainHand.getType())) {
            civilian.awardSkill(player, event.getEntity().getType().name(), CivSkills.SWORD.name());
        } else if (RepairEffect.isAxe(mainHand.getType())) {
            civilian.awardSkill(player, event.getEntity().getType().name(), CivSkills.AXE.name());
        } else if (mainHand.getType() == Material.TRIDENT) {
            civilian.awardSkill(player, event.getEntity().getType().name(), CivSkills.TRIDENT.name());
        } else if (mainHand.getType() == Material.BOW) {
            civilian.awardSkill(player, event.getEntity().getType().name(), CivSkills.BOW.name());
        } else if (mainHand.getType() == Material.CROSSBOW) {
            civilian.awardSkill(player, event.getEntity().getType().name(), CivSkills.CROSSBOW.name());
        }
    }

    @EventHandler @SuppressWarnings("unused")
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (Util.isDisallowedByWorld(event.getEntity().getWorld().getName())) {
            return;
        }
        CivilianManager.getInstance().setListNeedsToBeSorted(true);
        final Player player = event.getEntity();
        Civilian dyingCiv = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        dyingCiv.setLastDamager(null);
        dyingCiv.setLastDamage(-1);

        removePlayersFromCombat(dyingCiv);

        ArrayList<ItemStack> removeMe = new ArrayList<>();
        for (ItemStack is : event.getDrops()) {
            if (CivilianListener.checkDroppedItem(is, player)) {
                removeMe.add(is);
            }
        }
        for (ItemStack is : removeMe) {
            event.getDrops().remove(is);
        }

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
        jail = findJailInTown(player, deathLocation, regionManager, jail);
        jail = findKillersJail(regionManager, jail, damager);

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

        if (damager == null || damager == player) {
            return;
        }

        final Civilian damagerCiv = CivilianManager.getInstance().getCivilian(damager.getUniqueId());
        TutorialManager.getInstance().completeStep(damagerCiv, TutorialManager.TutorialType.KILL, "player");
        final LocaleManager localeManager = LocaleManager.getInstance();
        if (dyingCiv.getLastDeath() + ConfigManager.getInstance().getDeathGracePeriod() > System.currentTimeMillis()) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(damager,
                    "repeat-kill").replace("$1", player.getDisplayName()));
            return;
        }

        int powerPerKill = ConfigManager.getInstance().getPowerPerKill();
        if (powerPerKill > 0 && !damagerCiv.isFriend(dyingCiv) &&
                TownManager.getInstance().findCommonTowns(damagerCiv, dyingCiv).isEmpty()) {
            for (Town town : new ArrayList<>(TownManager.getInstance().getTowns())) {
                if (town.isDevolvedToday() || !town.getPeople().containsKey(dyingCiv.getUuid()) ||
                        town.getPeople().get(dyingCiv.getUuid()).contains("ally")) {
                    continue;
                }
                TownManager.getInstance().setTownPower(town, town.getPower() - powerPerKill);
                TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
                double karmaChange = (double) powerPerKill / (double) town.getMaxPower() * townType.getPrice(dyingCiv);
                CivilianManager.getInstance().exchangeHardship(town, damagerCiv.getUuid(), karmaChange);
            }
            double hardshipAmount = ConfigManager.getInstance().getHardshipPerKill();
            if (hardshipAmount > 0) {
                CivilianManager.getInstance().exchangeHardship(damager.getUniqueId(), player.getUniqueId(), hardshipAmount);
            }
        }

        dyingCiv.setDeaths(dyingCiv.getDeaths() + 1);
        damagerCiv.setKills(damagerCiv.getKills() + 1);
        damagerCiv.setKillStreak(damagerCiv.getKillStreak() + 1);

        double econBonus = 0.0;

        double killStreakBonus = ConfigManager.getInstance().getPointsPerKillStreak() * damagerCiv.getKillStreak();

        econBonus += damagerCiv.getKillStreak() * ConfigManager.getInstance().getMoneyPerKillStreak();
        if (damagerCiv.getKillStreak() >= 3 && ConfigManager.getInstance().isShowKillStreakMessages()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Civs.getPrefix() + localeManager.getTranslation(p, "kill-streak")
                        .replace("$1", damager.getDisplayName())
                        .replace("$2", damagerCiv.getKillStreak() + ""));
            }
        }


        double killJoyBonus = ConfigManager.getInstance().getPointsPerKillJoy() * dyingCiv.getKillStreak();
        econBonus += ConfigManager.getInstance().getMoneyPerKillJoy() * dyingCiv.getKillStreak();
        if (dyingCiv.getKillStreak() > 2 && ConfigManager.getInstance().isShowKillStreakMessages()) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Civs.getPrefix() + localeManager.getTranslation(p, "kill-joy")
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

        //Karma
        double karmaEcon = Math.max(0, -ConfigManager.getInstance().getMoneyPerKarma() * ((double) (dyingCiv.getKarma() - damagerCiv.getKarma())));
        if (dyingCiv.getKarma() > 1 ||
                damagerCiv.isFriend(dyingCiv) ||
                !TownManager.getInstance().findCommonTowns(damagerCiv, dyingCiv).isEmpty()) {
            karmaEcon = 0;
        }
        int karma = ConfigManager.getInstance().getKarmaPerKill() + ConfigManager.getInstance().getKarmaPerKillStreak() * (damagerCiv.getKillStreak() - dyingCiv.getKillStreak());
        damagerCiv.setKarma(damagerCiv.getKarma() - karma);
        dyingCiv.setKarma(dyingCiv.getKarma() + karma);

        //pay econ bonus
        if (Civs.econ != null) {
            double totalExchange = Math.max(econBonus, 0) + karmaEcon;
            double dyingBalance = Civs.econ.getBalance(player);
            if (!ConfigManager.getInstance().isDropMoneyIfZeroBalance()) {
                totalExchange = Math.min(totalExchange, dyingBalance);
            }

            if (totalExchange > 0) {
                Civs.econ.depositPlayer(damager, totalExchange);
                Civs.econ.withdrawPlayer(player, Math.min(totalExchange, dyingBalance));
            }
        }

        double bountyBonus = 0;
        if (!dyingCiv.getBounties().isEmpty() && TownManager.getInstance().findCommonTowns(damagerCiv, dyingCiv).isEmpty()) {
            Bounty bounty = dyingCiv.getBounties().remove(dyingCiv.getBounties().size() -1);
            bountyBonus = bounty.getAmount();

            for (Town town : TownManager.getInstance().getTowns()) {
                if (!town.getPeople().containsKey(dyingCiv.getUuid())) {
                    continue;
                }
                if (town.getBounties().isEmpty()) {
                    continue;
                }
                bountyBonus += town.getBounties().remove(town.getBounties().size() -1).getAmount();
                TownManager.getInstance().saveTown(town);
            }
        } else if (!dyingCiv.getBounties().isEmpty()) {
            damager.sendMessage(Civs.getPrefix() + localeManager.getTranslation(damager,
                    "allied-bounty"));
        }
        final double BOUNTY_BONUS = bountyBonus;

        if (Civs.econ != null) {
            Civs.econ.depositPlayer(damager, bountyBonus);
        }

        player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player, "death")
                .replace("$1", ConfigManager.getInstance().getPointsPerDeath() + ""));

        //save
        CivilianManager.getInstance().saveCivilian(dyingCiv);
        CivilianManager.getInstance().saveCivilian(damagerCiv);

        for (Town town : TownManager.getInstance().getOwnedTowns(dyingCiv)) {
            Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            if (government.getGovernmentType() == GovernmentType.MERITOCRACY) {
                Util.checkMerit(town, damager);
                continue;
            }
            if (government.getGovernmentType() != GovernmentType.KRATEROCRACY) {
                continue;
            }
            if (town.getRawPeople().containsKey(damagerCiv.getUuid()) &&
                    !town.getRawPeople().get(damagerCiv.getUuid()).contains(Constants.OWNER)) {
                town.getRawPeople().put(dyingCiv.getUuid(), "member");
                town.getRawPeople().put(damagerCiv.getUuid(), Constants.OWNER);
                TownManager.getInstance().saveTown(town);
                Util.spawnRandomFirework(damager);
                for (UUID uuid : town.getRawPeople().keySet()) {
                    Player townPlayer = Bukkit.getPlayer(uuid);
                    if (townPlayer == null || !townPlayer.isOnline()) {
                        continue;
                    }
                    townPlayer.sendMessage(Civs.getPrefix() + localeManager.getTranslation(
                            townPlayer, "new-owner-town")
                            .replace("$1", damager.getDisplayName())
                            .replace("$2", player.getDisplayName())
                            .replace("$3", town.getName()));
                }
            }
        }

        //display points
        if (karma != 0) {
            if (karmaEcon == 0) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "karma").replace("$1", karma + ""));
                damager.sendMessage(Civs.getPrefix() + localeManager.getTranslation(damager,
                        "karma").replace("$1", (karma * -1) + ""));
            } else {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "karma-lost").replace("$1", karma + "").replace("$2", karmaEcon + ""));
                damager.sendMessage(Civs.getPrefix() + localeManager.getTranslation(damager,
                        "karma-gained").replace("$1", karma + "").replace("$2", karmaEcon + ""));
            }
        }
        long interval = 10L;
        final Player dPlayer = damager;
        if (bountyBonus > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    dPlayer.sendMessage(Civs.getPrefix() + ChatColor.GREEN +
                            localeManager.getTranslation(dPlayer, "bounty-bonus")
                                    .replace("$1", "" + BOUNTY_BONUS));
                }
            }, interval);
            interval += 10L;
        }
        if (points > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    dPlayer.sendMessage(Civs.getPrefix() +
                            localeManager.getTranslation(dPlayer, "kill")
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
                            localeManager.getTranslation(dPlayer, "low-health")
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
                            localeManager.getTranslation(dPlayer, "killstreak-points")
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
                            localeManager.getTranslation(dPlayer, "killjoy-points")
                                    .replace("$1", "" + killJoyPts));
                }
            }, interval);
            interval += 10L;
        }
        final double pts = points;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                player.sendMessage(Civs.getPrefix() +
                        localeManager.getTranslation(dPlayer, "total-points")
                                .replace("$1", "" + pts));
            }
        }, interval);
    }

    private void removePlayersFromCombat(Civilian dyingCiv) {
        for (Player cPlayer : Bukkit.getOnlinePlayers()) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(cPlayer.getUniqueId());
            if (civilian.getLastDamager() != null && civilian.getLastDamager().equals(dyingCiv.getUuid()) &&
                    civilian.isInCombat()) {
                civilian.setLastDamager(null);
                civilian.setLastDamage(-1);
                CivilianManager.getInstance().saveCivilian(civilian);
            }
        }
    }

    private Region findJailInTown(Player player, Location deathLocation, RegionManager regionManager, Region jail) {
        if (jail != null && jail.getEffects().containsKey("jail")) {
            return jail;
        }
        Town town = TownManager.getInstance().getTownAt(deathLocation);
        if (town == null) {
            return null;
        }
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());

        if (town.getPeople().containsKey(player.getUniqueId())) {
            return null;
        }

        for (Region r : regionManager.getContainingRegions(town.getLocation(), townType.getBuildRadius())) {
            RegionType rt = (RegionType) ItemManager.getInstance().getItemType(r.getType());

            if (rt.getEffects().containsKey("jail")) {
                return r;
            }
        }
        return jail;
    }

    private Region findKillersJail(RegionManager regionManager, Region jail, Player damager) {
        if (jail != null || damager == null) {
            return jail;
        }
        for (Region r : regionManager.getAllRegions()) {
            if (!r.getPeople().containsKey(damager.getUniqueId())) {
                continue;
            }

            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(r.getType());
            if (regionType.getEffects().containsKey("jail")) {
                return r;
            }
        }
        return jail;
    }
}
