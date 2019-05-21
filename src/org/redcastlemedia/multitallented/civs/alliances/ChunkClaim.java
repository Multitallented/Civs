package org.redcastlemedia.multitallented.civs.alliances;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChunkClaim {
    private final int x;
    private final int z;
    private final World world;
    private Alliance alliance;
    private int hp;
    private long lastEnter;

    public ChunkClaim(int x, int z, World world, Alliance alliance) {
        this.x = x;
        this.z = z;
        this.world = world;
        this.alliance = alliance;
    }

    @Override
    public String toString() {
        return world.getUID() + " " + x + " " + z;
    }
    public static ChunkClaim fromString(String claimString, Alliance alliance) {
        String[] splitString = claimString.split(" ");
        if (splitString.length != 3) {
            return null;
        }
        World world = Bukkit.getWorld(UUID.fromString(splitString[0]));
        int x = Integer.parseInt(splitString[1]);
        int y = Integer.parseInt(splitString[2]);
        return new ChunkClaim(x, y, world, alliance);
    }
    public static ChunkClaim fromChunk(Chunk chunk) {
        for (Alliance alliance : AllianceManager.getInstance().getAllAlliances()) {
            if (alliance.getNationClaims().containsKey(chunk.getWorld().getUID()) &&
                    alliance.getNationClaims().get(chunk.getWorld().getUID())
                            .containsKey(chunk.getX() + "," + chunk.getZ())) {
                return alliance.getNationClaims().get(chunk.getWorld().getUID())
                        .get(chunk.getX() + "," + chunk.getZ());
            }
        }
        return null;
    }
    public Chunk getChunk() {
        return world.getChunkAt(x, z);
    }
}
