package org.redcastlemedia.multitallented.civs;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootTable;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class ChestImpl implements Chest {
    private final Location location;
    private InventoryImpl inventory = new InventoryImpl();

    public ChestImpl(Location location) {
        this.location = location;
    }

    @Override
    public Inventory getBlockInventory() {
        return inventory;
    }

    @Override
    public String getCustomName() {
        return null;
    }

    @Override
    public void setCustomName(String s) {

    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Inventory getSnapshotInventory() {
        return null;
    }

    @Override
    public Block getBlock() {
        return null;
    }

    @Override
    public MaterialData getData() {
        return null;
    }

    @Override
    public BlockData getBlockData() {
        return null;
    }

    @Override
    public Material getType() {
        return Material.CHEST;
    }

    @Override
    public byte getLightLevel() {
        return 0;
    }

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getZ() {
        return 0;
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
    public void setData(MaterialData materialData) {

    }

    @Override
    public void setBlockData(BlockData blockData) {

    }

    @Override
    public void setType(Material material) {

    }

    @Override
    public boolean update() {
        return true;
    }

    @Override
    public boolean update(boolean b) {
        return true;
    }

    @Override
    public boolean update(boolean b, boolean b1) {
        return true;
    }

    @Override
    public byte getRawData() {
        return 0;
    }

    @Override
    public void setRawData(byte b) {

    }

    @Override
    public boolean isPlaced() {
        return true;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public String getLock() {
        return null;
    }

    @Override
    public void setLock(String s) {

    }

    @Override
    public void setLootTable(LootTable lootTable) {

    }

    @Override
    public LootTable getLootTable() {
        return null;
    }

    @Override
    public void setSeed(long l) {

    }

    @Override
    public long getSeed() {
        return 0;
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
