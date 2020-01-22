package org.redcastlemedia.multitallented.civs.nations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Nation {
    private String name;
    private ItemStack icon;
    private Set<String> members = new HashSet<>();
    private String capitol;
    private UUID lastRenamedBy;
    private HashSet<String> effects = new HashSet<>();
    private HashMap<UUID, HashMap<String, ChunkClaim>> nationClaims = new HashMap<>();

    public int getPower() {
        int power = 0;
        for (String townName : members) {
            Town town = TownManager.getInstance().getTown(townName);
            if (town != null) {
                power += town.getPower();
            }
        }
        return power;
    }
}
