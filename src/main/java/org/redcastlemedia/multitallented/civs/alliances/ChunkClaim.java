package org.redcastlemedia.multitallented.civs.alliances;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

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

    public void setNation(Nation nation) {
        setNation(nation, false);
    }

    public void setNation(Nation nation, boolean silent) {
        removePreviousNation(silent);
        putInNationClaims(nation);
        this.nation = nation;
    }

    public String getTimeUntilCapture(Player player) {
        String captureNotStarted = LocaleManager.getInstance().getTranslation(player,
                "capture-not-started");
        final long CAPTURE_TIME = ConfigManager.getInstance().getAllianceClaimCaptureTime() * 1000;
        long timeRemains = lastEnter + CAPTURE_TIME - System.currentTimeMillis();
        if (lastEnter > 0) {
            if (timeRemains > 0) {
                return Util.formatTime(player, timeRemains / 1000);
            } else {
                return Util.formatTime(player, 0);
            }
        } else {
            return captureNotStarted;
        }
    }

    private void putInNationClaims(Nation nation) {
        if (nation == null) {
            return;
        }
        if (!nation.getNationClaims().containsKey(this.world.getUID())) {
            nation.getNationClaims().put(this.world.getUID(), new HashMap<>());
        }
        nation.getNationClaims().get(this.world.getUID()).put(this.getId(), this);
        NationManager.getInstance().saveNation(nation);
    }

    private void removePreviousNation(boolean silent) {
        if (this.nation == null) {
            return;
        }
        if (!this.nation.getNationClaims().containsKey(this.world.getUID())) {
            return;
        }
        this.nation.getNationClaims().get(this.world.getUID()).remove(this.getId());
        for (String townName : nation.getMembers()) {
            Town town = TownManager.getInstance().getTown(townName);
            for (UUID uuid : town.getRawPeople().keySet()) {
                Player player1 = Bukkit.getPlayer(uuid);
                if (player1 != null && !silent) {
                    player1.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player1,
                            "neutralized-claim").replace("$1", this.nation.getName())
                            .replace("$2", "" + (this.x * 16))
                            .replace("$3", "" + (this.z * 16)));
                }
            }
        }
        NationManager.getInstance().saveNation(this.nation);
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
    @NotNull
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
