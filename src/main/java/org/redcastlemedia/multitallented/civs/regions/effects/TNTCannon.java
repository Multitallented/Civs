package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsSingleton @SuppressWarnings("unused")
public class TNTCannon implements Listener, RegionCreatedListener {
    private final String KEY = "tnt_cannon";
    private final HashMap<Location, Long> cooldowns = new HashMap<>();

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new TNTCannon(), Civs.getInstance());
    }

    public TNTCannon() {
        RegionManager.getInstance().addRegionCreatedListener(KEY, this);
    }

    @Override
    public void regionCreatedHandler(Region region) {
        ItemStack controllerWand = new ItemStack(Material.STICK, 1);
        ItemMeta im = controllerWand.getItemMeta();
        im.setDisplayName("Cannon Controller " + region.getId());
        controllerWand.setItemMeta(im);

        region.getLocation().getWorld().dropItemNaturally(new Location(region.getLocation().getWorld(),
                region.getLocation().getX(), region.getLocation().getY() + 2,
                region.getLocation().getZ()), controllerWand);
        return;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND ||
                (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                event.getPlayer().getInventory().getItemInMainHand().getItemMeta() == null ||
                event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName() == null ||
                !event.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().contains("Cannon Controller")) {
            return;
        }
        Player player = event.getPlayer();
        Location id;
        try {
            String name = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
            id = Region.idToLocation(name.replace("Cannon Controller ", ""));
        } catch (Exception e) {
            return;
        }
        Region region = RegionManager.getInstance().getRegionAt(id);
        if (region == null || !region.getPeople().containsKey(player.getUniqueId())
                || !region.getPeople().get(player.getUniqueId()).contains(Constants.OWNER)) {
            player.sendMessage(Civs.getPrefix() + "You must be an owner to use this."); //TODO localize
            return;
        }
        long cooldown = 8;
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        try {
            cooldown = Long.parseLong(region.getEffects().get(KEY));
        } catch (Exception e) {
            //Do nothing and just use defaults
        }
        Location fireLocation = region.getLocation().getBlock().getRelative(BlockFace.UP, 2).getLocation();
        if (!region.hasUpkeepItems()) {
            return;
        }
        event.setCancelled(true);

        if (cooldowns.get(id) != null && cooldowns.get(id) > System.currentTimeMillis()) {
            //TODO show how long till reload is done
            player.sendMessage(Civs.getPrefix() + "That " + region.getType() + " is reloading."); //TODO localize
            return;
        }
        Block block = player.getTargetBlockExact(100, FluidCollisionMode.ALWAYS);
        if (block == null) {
            player.sendMessage(Civs.getPrefix() + "That target is too far away"); //TODO localize
            return;
        }
        Location targetLocation = Region.idToLocation(Region.blockLocationToString(block.getLocation()));
        if (!targetLocation.getWorld().equals(fireLocation.getWorld())) {
            return;
        }
        if (targetLocation.distanceSquared(fireLocation) < 1600) {
            player.sendMessage(Civs.getPrefix() + "That target is too close to shoot at."); //TODO localize
            return;
        }
        TNTPrimed tnt = fireLocation.getWorld().spawn(fireLocation, TNTPrimed.class);

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
//            player.sendMessage(ChatColor.GREEN + "[Townships] Val: " + Math.abs(functionDx(deltaX, deltaY, prev)));
//            player.sendMessage(ChatColor.GREEN + "[Townships] Iterations: " + i);
        double newX = current*Math.cos(theta)*Math.cos(Math.atan2(targetLocation.getZ() - fireLocation.getZ(), targetLocation.getX() - fireLocation.getX()));
        double newZ = current*Math.cos(theta)*Math.sin(Math.atan2(targetLocation.getZ() - fireLocation.getZ(), targetLocation.getX() - fireLocation.getX()));
        double newY = current*Math.sin(theta);

//            player.sendMessage(ChatColor.GREEN + "[Townships] Current Velocity: " + current);

        Vector vector1 = new Vector(newX, newY, newZ);
        tnt.setVelocity(vector1);
        tnt.setFuseTicks(240);

//            FiredTNT ftnt = new FiredTNT(tnt, periods, fireLocation, targetLocation);
//            firedTNT.put(tnt, ftnt);
        cooldowns.put(id, System.currentTimeMillis() + cooldown * 1000);

        region.runUpkeep();

            /*player.sendMessage(ChatColor.GREEN + "[Townships] Dx: " + deltaX);
            player.sendMessage(ChatColor.GREEN + "[Townships] Velocity: " + newX + ", " + newY + ", " + newZ);
            player.sendMessage(ChatColor.GREEN + "[Townships] Theta: " + theta);
            player.sendMessage(ChatColor.GREEN + "[Townships] Current: " + current);*/
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation.getBlock().getRelative(BlockFace.NORTH,1).getLocation(), 1);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation.getBlock().getRelative(BlockFace.EAST,1).getLocation(), 1);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation.getBlock().getRelative(BlockFace.WEST,1).getLocation(), 1);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation.getBlock().getRelative(BlockFace.SOUTH,1).getLocation(), 1);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation.getBlock().getRelative(BlockFace.UP,1).getLocation(), 1);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireLocation, 3);
        for (Player currPlayer : Bukkit.getOnlinePlayers()) {
            if (currPlayer.getLocation().distanceSquared(fireLocation) > 2500) {
                continue;
            }
            currPlayer.playSound(fireLocation, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
        }

        player.sendMessage(Civs.getPrefix() + "Your " + region.getType() + " has fired TNT at your new target."); //TODO localize
    }

    private double functionDx(double deltaX, double deltaY, double v) {
        try {
            return 0.04 * deltaY - 0.04 * deltaX * (v * 1.732050808 + 1.96 / 0.5) + (3.8416) * Math.log(v * 0.5 / (v * 0.5 - 0.04 * deltaX / 1.96));
        } catch (Exception e) {
            return 10000;
        }
    }
}
