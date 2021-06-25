package org.redcastlemedia.multitallented.civs;

import java.util.Collection;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockImpl implements Block {
    private final Location location;
    private Material type = Material.CHEST;
    private Biome biome = Biome.BADLANDS;
    private final BlockState blockState;

    public BlockImpl(Location location) {
        this.location = location;
        this.blockState = new ChestImpl(location);
    }

    @Override
    public byte getData() {
        return 0;
    }

    @Override
    public BlockData getBlockData() {
        return null;
    }

    @Override
    public Block getRelative(int i, int i1, int i2) {
        return null;
    }

    @Override
    public Block getRelative(BlockFace blockFace) {
        if (blockFace == BlockFace.NORTH) {
            return new BlockImpl(new Location(location.getWorld(), location.getX() + 1, location.getY(), location.getZ()));
        } else if (blockFace == BlockFace.EAST) {
            return new BlockImpl(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ() + 1));
        } else if (blockFace == BlockFace.SOUTH) {
            return new BlockImpl(new Location(location.getWorld(), location.getX() - 1, location.getY(), location.getZ()));
        } else if (blockFace == BlockFace.WEST) {
            return new BlockImpl(new Location(location.getWorld(), location.getX(), location.getY(), location.getZ() - 1));
        }
        return new BlockImpl(new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ()));
    }

    @Override
    public Block getRelative(BlockFace blockFace, int i) {
        return null;
    }

    @Override
    public Material getType() {
        return this.type;
    }

    @Override
    public byte getLightLevel() {
        return 0;
    }

    @Override
    public byte getLightFromSky() {
        return 0;
    }

    @Override
    public byte getLightFromBlocks() {
        return 0;
    }

    @Override
    public World getWorld() {
        return this.location.getWorld();
    }

    @Override
    public int getX() {
        return (int) this.location.getX();
    }

    @Override
    public int getY() {
        return (int) this.location.getY();
    }

    @Override
    public int getZ() {
        return (int) this.location.getZ();
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public Location getLocation(Location location) {
        return this.location;
    }

    @Override
    public Chunk getChunk() {
        return this.location.getChunk();
    }

    @Override
    public void setBlockData(BlockData blockData) {

    }

    @Override
    public void setBlockData(BlockData blockData, boolean b) {

    }

    @Override
    public void setType(Material material) {
        this.type = material;
    }

    @Override
    public void setType(Material material, boolean b) {

    }

    @Override
    public BlockFace getFace(Block block) {
        return null;
    }

    @Override
    public BlockState getState() {
        return this.blockState;
    }

    @Override
    public Biome getBiome() {
        return this.biome;
    }

    @Override
    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    @Override
    public boolean isBlockPowered() {
        return false;
    }

    @Override
    public boolean isBlockIndirectlyPowered() {
        return false;
    }

    @Override
    public boolean isBlockFacePowered(BlockFace blockFace) {
        return false;
    }

    @Override
    public boolean isBlockFaceIndirectlyPowered(BlockFace blockFace) {
        return false;
    }

    @Override
    public int getBlockPower(BlockFace blockFace) {
        return 0;
    }

    @Override
    public int getBlockPower() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isLiquid() {
        return false;
    }

    @Override
    public double getTemperature() {
        return 0;
    }

    @Override
    public double getHumidity() {
        return 0;
    }

    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        return null;
    }

    @Override
    public boolean breakNaturally() {
        return false;
    }

    @Override
    public boolean breakNaturally(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean applyBoneMeal(@NotNull BlockFace blockFace) {
        return false;
    }

    @Override
    public Collection<ItemStack> getDrops() {
        return null;
    }

    @Override
    public Collection<ItemStack> getDrops(ItemStack itemStack) {
        return null;
    }

    @Override
    public @NotNull Collection<ItemStack> getDrops(@NotNull ItemStack itemStack, @Nullable Entity entity) {
        return null;
    }

    @Override
    public boolean isPreferredTool(@NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    public float getBreakSpeed(@NotNull Player player) {
        return 0;
    }

    @Override
    public boolean isPassable() {
        return false;
    }

    @Override
    public RayTraceResult rayTrace(Location location, Vector vector, double v, FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return null;
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape() {
        return null;
    }

    @Override
    public void setMetadata(String s, MetadataValue metadataValue) {

    }

    @Override
    public List<MetadataValue> getMetadata(String s) {
        return null;
    }

    @Override
    public boolean hasMetadata(String s) {
        return false;
    }

    @Override
    public void removeMetadata(String s, Plugin plugin) {

    }
}
