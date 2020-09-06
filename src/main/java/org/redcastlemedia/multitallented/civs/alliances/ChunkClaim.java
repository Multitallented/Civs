package org.redcastlemedia.multitallented.civs.alliances;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChunkClaim {
    private final int x;
    private final int z;
    private final World world;
    private Nation nation;
    private long lastEnter;

    public ChunkClaim(int x, int z, World world, Nation nation) {
        this.x = x;
        this.z = z;
        this.world = world;
        this.nation = nation;
    }

    @Override
    public String toString() {
        return world.getUID() + " " + x + " " + z;
    }
    public static ChunkClaim fromString(String claimString) {
        ChunkClaim chunkClaim = fromString(claimString, null);
        return chunkClaim == null ? null :
                fromXZ(chunkClaim.getX(), chunkClaim.getZ(), chunkClaim.getWorld());
    }

    public static ChunkClaim fromString(String claimString, Nation nation) {
        String[] splitString = claimString.split(" ");
        if (splitString.length != 3) {
            return null;
        }
        World world = Bukkit.getWorld(UUID.fromString(splitString[0]));
        int x = Integer.parseInt(splitString[1]);
        int y = Integer.parseInt(splitString[2]);
        return new ChunkClaim(x, y, world, nation);
    }
    public static ChunkClaim fromChunk(Chunk chunk) {
        for (Nation nation : NationManager.getInstance().getAllNations()) {
            if (nation.getNationClaims().containsKey(chunk.getWorld().getUID()) &&
                    nation.getNationClaims().get(chunk.getWorld().getUID())
                            .containsKey(chunk.getX() + "," + chunk.getZ())) {
                return nation.getNationClaims().get(chunk.getWorld().getUID())
                        .get(chunk.getX() + "," + chunk.getZ());
            }
        }
        return null;
    }
    public static ChunkClaim fromLocation(Location location) {
        int x = UnloadedInventoryHandler.getChunkX(location);
        int z = UnloadedInventoryHandler.getChunkZ(location);
        return fromXZ(x, z, location.getWorld());
    }

    public static ChunkClaim fromXZ(int x, int z, World world) {
        for (Nation nation : NationManager.getInstance().getAllNations()) {
            if (nation.getNationClaims().containsKey(world.getUID()) &&
                    nation.getNationClaims().get(world.getUID())
                            .containsKey(x + "," + z)) {
                return nation.getNationClaims().get(world.getUID())
                        .get(x + "," + z);
            }
        }
        return new ChunkClaim(x, z, world, null);
    }
    public Chunk getChunk() {
        return world.getChunkAt(x, z);
    }
    public String getId() { return x + "," + z; }
}
