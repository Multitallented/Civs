package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

import java.util.HashMap;
import java.util.HashSet;

public class TNTCannon implements Listener, CreateRegionListener {
//    private final HashMap<TNTPrimed, FiredTNT> firedTNT = new HashMap<>();
    private final HashMap<Location, Long> cooldowns = new HashMap<>();

    public TNTCannon() {
        RegionManager.getInstance().addCreateRegionListener("tnt_cannon", this);
    }

    @Override
    public boolean createRegionHandler(Block block, Player player) {
        Location location = block.getLocation();
        ItemStack controllerWand = new ItemStack(Material.STICK, 1);
        ItemMeta im = controllerWand.getItemMeta();
        im.setDisplayName("Cannon Controller " + Region.locationToString(location));
        controllerWand.setItemMeta(im);

        location.getWorld().dropItemNaturally(block.getRelative(BlockFace.UP, 2).getLocation(), controllerWand);
        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if ((event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                event.getPlayer().getInventory().getItemInMainHand() == null || event.getPlayer().getInventory().getItemInMainHand().getItemMeta() == null ||
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
        if (region == null || !region.getOwners().contains(player.getUniqueId())) {
            player.sendMessage(Civs.getPrefix() + "You must be an owner to use this.");
            return;
        }
        long cooldown = 8;
        if (!region.getEffects().containsKey("tnt_cannon")) {
            return;
        }
        String[] effectParts = region.getEffects().get("tnt_cannon").split("\\.");
        if (effectParts.length > 1) {
            try {
                cooldown = Long.parseLong(effectParts[1]);
            } catch (Exception e) {
                //Do nothing and just use defaults
            }
        }
        Location fireLocation = region.getLocation().getBlock().getRelative(BlockFace.UP, 2).getLocation();
        if (!region.hasReagents()) {
            return;
        }
        event.setCancelled(true);

        if (cooldowns.get(id) != null && cooldowns.get(id) > System.currentTimeMillis()) {
            //TODO show how long till reload is done
            player.sendMessage(Civs.getPrefix() + "That " + region.getType() + " is reloading.");
            return;
        }
        HashSet<Material> materialHashSet = new HashSet<>();
        Location targetLocation = player.getTargetBlock(materialHashSet, 100).getLocation();
        if (!targetLocation.getWorld().equals(fireLocation.getWorld())) {
            return;
        }
        if (targetLocation.distanceSquared(fireLocation) < 1600) {
            player.sendMessage(Civs.getPrefix() + "That target is too close to shoot at.");
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

//        region.runUpkeep();
        Chest chest = (Chest) region.getLocation().getBlock();
        chest.getBlockInventory().removeItem(new ItemStack(Material.TNT,1));

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

        player.sendMessage(Civs.getPrefix() + "Your " + region.getType() + " has fired ordinance at your new target.");
    }

    private double functionDx(double deltaX, double deltaY, double v) {
        try {
            return 0.04 * deltaY - 0.04 * deltaX * (v * 1.732050808 + 1.96 / 0.5) + (3.8416) * Math.log(v * 0.5 / (v * 0.5 - 0.04 * deltaX / 1.96));
        } catch (Exception e) {
            return 10000;
        }
    }

        /*@EventHandler
        public void onTwoSecondEvent(ToTwoSecondEvent event) {
            HashSet<TNTPrimed> removeTNT = new HashSet<TNTPrimed>();
            for (FiredTNT tnt : firedTNT.values()) {
                if (tnt.getStage() < 2) {
                    removeTNT.add(tnt.getTNT());
                }
                TNTPrimed tntPrimed = tnt.getTNT();
                Location tntLocation = tntPrimed.getLocation();
                tntPrimed.remove();
                tntPrimed = tntLocation.getWorld().spawn(tntLocation, TNTPrimed.class);
                Vector vector = new Vector();
                //TODO set velocity and decrement the stage
            }
            for (TNTPrimed tnt : removeTNT) {
                firedTNT.remove(tnt);
            }
            HashSet<Integer> removeMe = new HashSet<Integer>();
            for (Integer id : cooldowns.keySet()) {
                if (cooldowns.get(id) < System.currentTimeMillis()) {
                    removeMe.add(id);
                }
            }
            for (Integer id : removeMe) {
                cooldowns.remove(id);
            }
        }*/

    /*private class FiredTNT {
        private int stage;
        private Location startLocation;
        private Location targetLocation;
        private TNTPrimed tnt;

        public FiredTNT(TNTPrimed tnt, int stage, Location startLocation, Location targetLocation) {
            this.tnt = tnt;
            this.stage = stage;
            this.startLocation = startLocation;
            this.targetLocation = targetLocation;
        }

        public void setTNT(TNTPrimed tnt) {
            this.tnt = tnt;
        }
        public void setStage(int stage) {
            this.stage = stage;
        }
        public void setStartLocation(Location startLocation) {
            this.startLocation = startLocation;
        }
        public void setTargetLocation(Location targetLocation) {
            this.targetLocation = targetLocation;
        }
        public TNTPrimed getTNT() {
            return tnt;
        }
        public int getStage() {
            return stage;
        }
        public Location getStartLocation() {
            return startLocation;
        }
        public Location getTargetLocation() {
            return targetLocation;
        }
    }*/
}