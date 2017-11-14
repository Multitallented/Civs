package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.spells.Spell;

import java.util.HashMap;

public class TeleportEffect extends Effect {

    private String target = "self";
    private boolean setPos = false;
    private boolean other = false;
    private double x = 0,y = 0,z = 0;

    public TeleportEffect(Spell spell, String key, Object target, Entity origin, int level, ConfigurationSection section) {
        super(spell, key, target, origin, level, section);
        String tempTarget = section.getString("target", "not-a-string");
        this.x = section.getDouble("x",0);
        this.y = section.getDouble("y",0);
        this.z = section.getDouble("z",0);
        this.setPos = section.getBoolean("set", false);
        this.other = section.getBoolean("other", false);
        if (!tempTarget.equals("not-a-string")) {
            this.target = tempTarget;
        } else {
            this.target = "self";
        }
    }
    public TeleportEffect(Spell spell, String key, Object target, Entity origin, int level, String value) {
        super(spell, key, target, origin, level, value);
        setPos = false;
    }

    public boolean meetsRequirement() {
        return true;
    }
    public void apply() {
        Object target = getTarget();
        Entity origin = getOrigin();
        Location t;
        if(!setPos) {
            if (target.equals(origin)) {
                return;
            }
            float pitch = origin.getLocation().getPitch();
            float yaw = origin.getLocation().getYaw();
            if (target instanceof Location) {
                t = (Location) target;
            } else if (target instanceof Entity) {
                t = ((Entity) target).getLocation();
            } else if (target instanceof Block) {
                t = ((Block) target).getLocation();
            } else {
                return;
            }
            t.setPitch(pitch);
            t.setYaw(yaw);
        } else {
            t = new Location(origin.getWorld(), x, y, z, origin.getLocation().getYaw(), origin.getLocation().getPitch());
            t.setX(x);
            t.setY(y);
            t.setZ(z);
        }
        LivingEntity livingEntity;
        if(!other){
            livingEntity = (LivingEntity) origin;
        } else if (target instanceof LivingEntity) {
            livingEntity = (LivingEntity) target;
        } else {
            return;
        }
        Player player = null;

        if (other && target instanceof Player) {
            player = (Player) livingEntity;
//            NCPExemptionManager.exemptPermanently(player, CheckType.MOVING);
        } else if (!other && origin instanceof Player) {
            player = (Player) origin;
//            NCPExemptionManager.exemptPermanently(player, CheckType.MOVING);
        }
        livingEntity.teleport(t);
        if (player != null) {
//            NCPExemptionManager.unexempt(player, CheckType.MOVING);
        }
    }

    @Override
    public HashMap<String, Double> getVariables() {
        Entity origin = getOrigin();
        Object target = getTarget();
        HashMap<String, Double> returnMap = new HashMap<String, Double>();
        Location originLocation = origin.getLocation();
        returnMap.put("pitch", (double) originLocation.getPitch());
        returnMap.put("yaw", (double) originLocation.getYaw());
        Location t;
        if (target.equals(origin) || !(origin instanceof LivingEntity)) {
            returnMap.put("x", originLocation.getX());
            returnMap.put("y", originLocation.getY());
            returnMap.put("z", originLocation.getZ());
            return returnMap;
        }
        if (target instanceof Location) {
            t = (Location) target;
        } else if(target instanceof Entity) {
            t = ((Entity) target).getLocation();
        } else if (target instanceof Block) {
            t = ((Block) target).getLocation();
        } else  {
            returnMap.put("x", originLocation.getX());
            returnMap.put("y", originLocation.getY());
            returnMap.put("z", originLocation.getZ());
            return returnMap;
        }
        if (!t.getWorld().equals(originLocation.getWorld())) {
            returnMap.put("x", originLocation.getX());
            returnMap.put("y", originLocation.getY());
            returnMap.put("z", originLocation.getZ());
            return returnMap;
        }
        returnMap.put("x", t.getX());
        returnMap.put("y", t.getY());
        returnMap.put("z", t.getZ());
        returnMap.put("distance", t.distance(originLocation));
        return returnMap;
    }
}
