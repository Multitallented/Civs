package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.spells.effects.DamageEffect;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.*;

public class ArrowTurret implements Listener {
    public static String KEY = "arrow_turret";
    public static HashMap<Arrow, Integer> arrowDamages = new HashMap<>();

    //Shoot arrows at mobs
    @EventHandler
    public void onRegionTickEvent(RegionTickEvent event) {
        if (ConfigManager.getInstance().getDenyArrowTurretShootAtMobs()) {
            return;
        }
        Region region = event.getRegion();
        if (!region.getEffects().containsKey(ArrowTurret.KEY)) {
            return;
        }
        RegionType regionType = event.getRegionType();
        Location location = region.getLocation();

        for (Entity e : location.getWorld().getNearbyEntities(location, regionType.getEffectRadius(),
                regionType.getEffectRadius(), regionType.getEffectRadius())) {
            if (!(e instanceof Monster)) {
                continue;
            }
            Monster monster = (Monster) e;
            if (monster.getLocation().distance(location) > regionType.getEffectRadius()) {
                continue;
            }
            ArrowTurret.shootArrow(region, monster, region.getEffects().get(ArrowTurret.KEY), false);
            break;
        }
    }

    public static void shootArrow(Region r, UUID uuid, String vars, boolean runUpkeep) {
        shootArrow(r, Bukkit.getPlayer(uuid), vars, runUpkeep);
    }

    public static void shootArrow(Region r, LivingEntity livingEntity, String vars, boolean runUpkeep) {
        Location l = r.getLocation();
        //Check if the region has the shoot arrow effect and return arrow velocity
        int damage = 1;
        double speed = 0.5;
        int spread = 12;
        String[] parts = vars.split("\\.");
        try {
            damage = Integer.parseInt(parts[0]);
        } catch (Exception e) {
            return;
        }
        if (parts.length > 1) {
            try {
                speed = Double.parseDouble(parts[1]) / 10;
            } catch (Exception e) {
                return;
            }
        }
        if (parts.length > 2) {
            try {
                spread = Integer.parseInt(parts[2]);
            } catch (Exception e) {
                return;
            }
        }

        if (l.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR ||
                l.getBlock().getRelative(BlockFace.UP, 2).getType() != Material.AIR) {
            return;
        }

        HashSet<Arrow> removeMe = new HashSet<>();
        //clean up arrows
        for (Arrow arrow : arrowDamages.keySet()) {
            if (arrow.isDead() || !arrow.isValid()) {
                removeMe.add(arrow);
            }
        }
        for (Arrow arrow : removeMe) {
            arrow.remove();
            arrowDamages.remove(arrow);
        }


        //Check if the player is invincible
        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;
            if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
                return;
            }
        }

//            EntityDamageEvent damageEvent = new EntityDamageEvent(null, DamageCause.CUSTOM, 0);
//            Bukkit.getPluginManager().callEvent(damageEvent);
//            if (damageEvent.isCancelled()) {
//                System.out.println("damage cancelled");
//                return;
//            }

        //Check if the player owns or is a member of the region
        if (r.getPeople().containsKey(livingEntity.getUniqueId())) {
            return;
        }

        //Check to see if the Townships has enough reagents
        if (runUpkeep && !r.runUpkeep(false)) {
            return;
        }

        Block block = l.getBlock();
        if (block instanceof Chest) {
            Chest chest = (Chest) block;
            List<List<CVItem>> itemsToRemove = new ArrayList<>();
            List<CVItem> arrowList = new ArrayList<>();
            arrowList.add(CVItem.createCVItemFromString("ARROW"));
            itemsToRemove.add(arrowList);
            Util.removeItems(itemsToRemove, chest.getBlockInventory());
//            chest.getBlockInventory().removeItem(new ItemStack(Material.ARROW, 1));
        }

        //Damage check before firing
//            EntityDamageEvent testEvent = new EntityDamageEvent(player, DamageCause.CUSTOM, 0);
//            Bukkit.getPluginManager().callEvent(testEvent);
//            if (testEvent.isCancelled()) {
//                System.out.println("damage test failed");
//                return;
//            }

        HashSet<Arrow> arrows = new HashSet<>();
        for (Arrow arrow : arrowDamages.keySet()) {
            if (arrow.isDead() || arrow.isOnGround() || !arrow.isValid()) {
                arrows.add(arrow);
            }
        }
        for (Arrow arrow : arrows) {
            arrowDamages.remove(arrow);
        }


        //Calculate trajectory of the arrow
        Location loc = l.getBlock().getRelative(BlockFace.UP, 2).getLocation();
        Location playerLoc = livingEntity.getEyeLocation();

        Vector vel = new Vector(playerLoc.getX() - loc.getX(), playerLoc.getY() - loc.getY(), playerLoc.getZ() - loc.getZ());

        //Make sure the target is not hiding behind something
//            if (!hasCleanShot(loc, playerLoc)) {
//                System.out.println("line of sight failed");
//                return;
//            }



        //Location playerLoc = player.getLocation().getBlock().getRelative(BlockFace.UP).getLocation();
        //playerLoc.setX(Math.floor(playerLoc.getX()) + 0.5);
        //playerLoc.setY(Math.floor(playerLoc.getY()) + 0.5);
        //playerLoc.setZ(Math.floor(playerLoc.getZ()) + 0.5);


        //Spawn and set velocity of the arrow
        Arrow arrow = l.getWorld().spawnArrow(loc, vel, (float) (speed), spread);
        arrowDamages.put(arrow, damage);
    }

    @EventHandler
    public void onPlayerInRegion(PlayerInRegionEvent event) {
        if (event.getRegion().getEffects().containsKey(KEY)) {
            ArrowTurret.shootArrow(event.getRegion(), event.getUuid(), event.getRegion().getEffects().get(KEY), true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity projectile = event.getDamager();
        if (!(projectile instanceof Arrow) || !(event.getEntity() instanceof Player)) {
            return;
        }
        Arrow arrow = (Arrow) projectile;
        Player damagee = (Player) event.getEntity();
        double maxHP = damagee.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(); //TODO check to make sure this works
        if (arrowDamages.get(arrow) == null) {
            return;
        }

        //String ownerName = arrowOwners.get(arrow);
        //Player player = null;
        //if (ownerName != null) {
        //    player = Bukkit.getPlayer(ownerName);
        //}

        int damage = (int) ((double) arrowDamages.get(arrow) / 100.0 * maxHP);
        arrowDamages.remove(arrow);
        //arrowOwners.remove(arrow);

        //if (player != null) {
        //    damagee.damage(damage, player);
        //} else {
//                damagee.damage(damage);
        //damagee.damage(damage);
        //}
//            event.setCancelled(true);
        damage = DamageEffect.adjustForArmor(damage, damagee);
        event.setDamage(damage);

    }

    private boolean hasCleanShot(Location shootHere, Location targetHere) {
        double x = shootHere.getX();
        double y = shootHere.getY();
        double z = shootHere.getZ();

        double x1 = targetHere.getX();
        double y1 = targetHere.getY();
        double z1 = targetHere.getZ();

        Vector start = new Vector(x, y, z);
        Vector end = new Vector (x1, y1, z1);

        BlockIterator bi = new BlockIterator(shootHere.getWorld(), start, end, 0, (int) shootHere.distance(targetHere));
        while (bi.hasNext()) {
            Block block = bi.next();
            System.out.println(Civs.getPrefix() + ((int) block.getLocation().getX()) +
                    ":" + ((int) block.getLocation().getY()) + ":" +
                    ((int) block.getLocation().getZ()) + " " + !Util.isSolidBlock(block.getType()));
            if (!Util.isSolidBlock(block.getType())) {
                return false;
            }
        }

        return true;
    }
}
