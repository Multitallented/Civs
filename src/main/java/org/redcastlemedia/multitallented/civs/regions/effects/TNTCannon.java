package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.TwoSecondEvent;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

import lombok.Getter;

@CivsSingleton @SuppressWarnings("unused")
public class TNTCannon implements Listener, RegionCreatedListener {
    private final String KEY = "tnt_cannon";
    public static final String PERSISTENT_KEY = "civs_catapult";
    private final HashMap<Location, Integer> cooldowns = new HashMap<>();

    @Getter
    private final HashMap<String, Location> automaticFire = new HashMap<>();

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new TNTCannon(), Civs.getInstance());
    }

    public TNTCannon() {
        RegionManager.getInstance().addRegionCreatedListener(KEY, this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(), () -> {
            for (Iterator<Map.Entry<Location, Integer>> it = cooldowns.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<Location, Integer> entry = it.next();
                if (entry.getValue() < 2) {
                    it.remove();
                } else {
                    cooldowns.put(entry.getKey(), entry.getValue() - 1);
                }
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void listenAutomaticFire(TwoSecondEvent event) {
        for (Map.Entry<String, Location> entry : automaticFire.entrySet()) {
            Region region = RegionManager.getInstance().getRegionById(entry.getKey());
            if (region == null) {
                continue;
            }
            Location fireLocation = region.getLocation().getBlock().getRelative(BlockFace.UP, 2).getLocation();

            if (cooldowns.containsKey(region.getLocation())) {
                continue;
            }
            fireTheCannon(null, entry.getKey(), getCooldown(region), fireLocation, entry.getValue());
        }
    }

    @EventHandler
    public void onRegionDestroyed(RegionDestroyedEvent event) {
        automaticFire.remove(event.getRegion().getId());
    }

    @Override
    public void regionCreatedHandler(Region region) {
        ItemStack controllerWand = new ItemStack(Material.STICK, 1);
        ItemMeta im = controllerWand.getItemMeta();
        UUID uuid  = region.getOwners().isEmpty() ? null : region.getOwners().iterator().next();
        Player player = null;
        if (uuid != null) {
            player = Bukkit.getPlayer(uuid);
            Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
            im.setDisplayName(LocaleManager.getInstance().getTranslation(player, "cannon-controller"));
            im.setLore(new ArrayList<>(Util.textWrap(civilian, LocaleManager.getInstance()
                    .getTranslation(player, "cannon-controller-desc"))));
        } else {
            String defaultLocale = ConfigManager.getInstance().getDefaultLanguage();
            im.setDisplayName(LocaleManager.getInstance().getTranslation(defaultLocale,
                    "cannon-controller"));
            im.setLore(new ArrayList<>(Util.textWrap(LocaleManager.getInstance()
                    .getTranslation(defaultLocale, "cannon-controller-desc"))));
        }
        NamespacedKey controllerKey = new NamespacedKey(Civs.getInstance(), KEY);
        im.getPersistentDataContainer().set(controllerKey, PersistentDataType.STRING, region.getId());
        controllerWand.setItemMeta(im);


        try {
            Block block = region.getLocation().getBlock();
            if (block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();
                chest.getInventory().addItem(controllerWand);
            } else {
                region.getLocation().getWorld().dropItemNaturally(new Location(region.getLocation().getWorld(),
                        region.getLocation().getX(), region.getLocation().getY() + 2,
                        region.getLocation().getZ()), controllerWand);
            }
        } catch (Exception e) {
            Civs.logger.log(Level.WARNING, "Unable to put cannon controller in chest", e);
            region.getLocation().getWorld().dropItemNaturally(new Location(region.getLocation().getWorld(),
                    region.getLocation().getX(), region.getLocation().getY() + 2,
                    region.getLocation().getZ()), controllerWand);
        }

        if (player != null) {
            CivItem civItem = ItemManager.getInstance().getItemType(region.getType());
            String localRegionName = civItem.getDisplayName(player);
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "raid-remote").replace("$1", localRegionName));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND ||
                (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                event.getPlayer().getInventory().getItemInMainHand().getItemMeta() == null) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack controllerWand = player.getInventory().getItemInMainHand();
        NamespacedKey controllerKey = new NamespacedKey(Civs.getInstance(), KEY);
        if (!controllerWand.getItemMeta().getPersistentDataContainer().has(controllerKey, PersistentDataType.STRING)) {
            return;
        }

        String id = controllerWand.getItemMeta().getPersistentDataContainer().get(controllerKey, PersistentDataType.STRING);
        if (id == null) {
            return;
        }
        Location regionLocation = Region.idToLocation(id);
        Region region = RegionManager.getInstance().getRegionAt(regionLocation);
        if (region == null || !region.getPeople().containsKey(player.getUniqueId())
                || !region.getPeople().get(player.getUniqueId()).contains(Constants.OWNER)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    LocaleConstants.PERMISSION_DENIED));
            return;
        }
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        int cooldown = getCooldown(region);
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        if (!Util.isChunkLoadedAt(region.getLocation())) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "too-far-from-cannon").replace("$1", regionType.getDisplayName(player)));
            return;
        }
        Location fireLocation = region.getLocation().getBlock().getRelative(BlockFace.UP, 2).getLocation();
        fireLocation = new Location(fireLocation.getWorld(), fireLocation.getX() + 0.5, fireLocation.getY() + 0.5,
                fireLocation.getZ() + 0.5);
        if (!region.hasUpkeepItems()) {
            return;
        }
        event.setCancelled(true);

        if (!player.isSneaking()) {
            automaticFire.remove(id);
        }
        if (cooldowns.containsKey(regionLocation)) {
            long timeLeft = cooldowns.get(regionLocation);
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    LocaleConstants.COOLDOWN).replace("$1", Util.formatTime(player, timeLeft)));
            return;
        }

        ItemStack chestItem = player.getInventory().getChestplate();
        if (chestItem != null && chestItem.getType() == Material.ELYTRA) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "catapult-elytra-equipped"));
            return;
        }

        Block block = player.getTargetBlockExact(100, FluidCollisionMode.ALWAYS);
        if (block == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "target-too-far"));
            return;
        }
        Location targetLocation = Region.idToLocation(Region.blockLocationToString(block.getLocation()));
        if (targetLocation == null || targetLocation.getWorld() == null ||
                !targetLocation.getWorld().equals(fireLocation.getWorld())) {
            return;
        }
        if (targetLocation.distanceSquared(fireLocation) < 1600) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "target-too-close"));
            return;
        }

        fireTheCannon(player, id, cooldown, fireLocation, targetLocation);
    }

    private void fireTheCannon(@Nullable Player player, String id, int cooldown, Location fireLocation, Location targetLocation) {
        Region region = RegionManager.getInstance().getRegionById(id);
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        TNTPrimed tnt = fireLocation.getWorld().spawn(fireLocation, TNTPrimed.class);
        tnt.getPersistentDataContainer().set(NamespacedKey.minecraft(PERSISTENT_KEY), PersistentDataType.STRING, id);

        boolean upkeepRan = region.runUpkeep(false);
        if (!upkeepRan) {
            return;
        }
            /*Vector vector = new Vector((targetLocation.getX() - fireLocation.getX()) / periods,
                             (targetLocation.getY() - fireLocation.getY()) / periods + (100 / periods * 2),
                             (targetLocation.getZ() - fireLocation.getZ()) / periods);
            tnt.setVelocity(vector);*/

        //vt terminal velocity == 1.96
        //vo muzzle velocity
        //g acceleration (gravity) == 0.04
        //theta = angle of elevation == 60
        //
        double g = 0.04;
        double vt = 1.96;

//        double deltaX = Math.sqrt(Math.pow(targetLocation.getX() - fireLocation.getX(), 2) + Math.pow(targetLocation.getZ() + fireLocation.getZ(), 2));
        double deltaX = targetLocation.distance(fireLocation);
//            double deltaY = targetLocation.getY() - fireLocation.getY();

        double theta = 64.2556-0.0651852*deltaX;
        theta = Math.PI * theta / 180;

        double current = 0.977778*g*(11.4205+deltaX)/(vt*Math.cos(theta));
//            double prevPrev = 0.041*deltaX;
//            double prev = prevPrev + 0.01;
//            accuracy = accuracy / 10000;

            /*int i = 0;
            while (Math.abs(functionDx(deltaX, deltaY, prev)) > accuracy && i <= 9001) {
                current = (prevPrev*functionDx(deltaX, deltaY, prev)-prev*functionDx(deltaX,deltaY,prevPrev))/(functionDx(deltaX,deltaY,prev)-functionDx(deltaX, deltaY, prevPrev));
                prevPrev = prev;
                prev = current;
                i++;
            }*/
//            "[Townships] Val: " + Math.abs(functionDx(deltaX, deltaY, prev))
//            ChatColor.GREEN + "[Townships] Iterations: " + i
        double newX = current*Math.cos(theta)*Math.cos(Math.atan2(targetLocation.getZ() - fireLocation.getZ(),
                targetLocation.getX() - fireLocation.getX()));
        double newZ = current*Math.cos(theta)*Math.sin(Math.atan2(targetLocation.getZ() - fireLocation.getZ(),
                targetLocation.getX() - fireLocation.getX()));
        double newY = current*Math.sin(theta);

//            "[Townships] Current Velocity: " + current

        Vector vector1 = new Vector(newX, newY, newZ);
//        tnt.setVelocity(vector1);
        tnt.setGravity(false);
        tnt.setFuseTicks(240);
        Bukkit.getScheduler().runTaskLater(Civs.getInstance(), () -> {
            tnt.setGravity(true);
            tnt.setVelocity(vector1);
        }, 1L);

        if (player != null && player.isSneaking()) {
            automaticFire.put(id, targetLocation);
        }
        cooldowns.put(region.getLocation(), cooldown);

        fireLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation.getBlock().getRelative(BlockFace.NORTH,1).getLocation(), 1);
        fireLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation.getBlock().getRelative(BlockFace.EAST,1).getLocation(), 1);
        fireLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation.getBlock().getRelative(BlockFace.WEST,1).getLocation(), 1);
        fireLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation.getBlock().getRelative(BlockFace.SOUTH,1).getLocation(), 1);
        fireLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation.getBlock().getRelative(BlockFace.UP,1).getLocation(), 1);
        fireLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation, 3);
        fireLocation.getWorld().playSound(fireLocation, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);

        if (player != null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "cannon-fired").replace("$1", regionType.getDisplayName(player)));
        }
    }

    private int getCooldown(Region region) {
        try {
            return Integer.parseInt(region.getEffects().get(KEY));
        } catch (Exception e) {
            Civs.logger.log(Level.WARNING, "{0} has an improperly configured tnt_cannon effect",
                    region.getType());
        }
        return 8;
    }

    private double functionDx(double deltaX, double deltaY, double v) {
        try {
            return 0.04 * deltaY - 0.04 * deltaX * (v * 1.732050808 + 1.96 / 0.5) + (3.8416) * Math.log(v * 0.5 / (v * 0.5 - 0.04 * deltaX / 1.96));
        } catch (Exception e) {
            return 10000;
        }
    }
}
