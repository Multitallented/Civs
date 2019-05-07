package org.redcastlemedia.multitallented.civs;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WorldImpl implements World {
    private final String name;
    public HashSet<Entity> nearbyEntities = new HashSet<>();

    public WorldImpl(String name) {
        this.name = name;
    }

    private HashMap<String, Block> blockMap = new HashMap<>();

    public void putBlock(int x, int y, int z, Block block) {
        blockMap.put(x+":"+y+":"+z, block);
    }

    @Override
    public Block getBlockAt(int i, int i1, int i2) {
        return blockMap.get(i+":"+i1+":"+i2);
    }

    @Override
    public Block getBlockAt(Location location) {
        double x = location.getX();
        double z = location.getZ();
        if (x < 0) {
            x = Math.ceil(location.getX());
        } else {
            x = Math.floor(location.getX());
        }
        if (z < 0) {
            z = Math.ceil(location.getZ());
        } else {
            z = Math.floor(location.getZ());
        }
        String retrievalString = (int) x +":"+ (int) Math.floor(location.getY())+":"+ (int) z;
        return blockMap.get(retrievalString);
    }

    @Override
    public int getHighestBlockYAt(int i, int i1) {
        return 0;
    }

    @Override
    public int getHighestBlockYAt(Location location) {
        return 0;
    }

    @Override
    public Block getHighestBlockAt(int i, int i1) {
        return null;
    }

    @Override
    public Block getHighestBlockAt(Location location) {
        Block block = mock(Block.class);
        Location thisLocation = new Location(location.getWorld(),
                location.getX(), 255, location.getZ());
        when(block.getLocation()).thenReturn(thisLocation);
        return block;
    }

    @Override
    public Chunk getChunkAt(int i, int i1) {
        return null;
    }

    @Override
    public Chunk getChunkAt(Location location) {
        Chunk chunk = mock(Chunk.class);
        when(chunk.isLoaded()).thenReturn(true);
        return chunk;
    }

    @Override
    public Chunk getChunkAt(Block block) {
        return null;
    }

    @Override
    public boolean isChunkLoaded(Chunk chunk) {
        return true;
    }

    @Override
    public Chunk[] getLoadedChunks() {
        return new Chunk[0];
    }

    @Override
    public void loadChunk(Chunk chunk) {

    }

    @Override
    public boolean isChunkLoaded(int i, int i1) {
        return false;
    }

    @Override
    public boolean isChunkGenerated(int i, int i1) {
        return false;
    }

    @Override
    public boolean isChunkInUse(int i, int i1) {
        return false;
    }

    @Override
    public void loadChunk(int i, int i1) {

    }

    @Override
    public boolean loadChunk(int i, int i1, boolean b) {
        return false;
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        return false;
    }

    @Override
    public boolean unloadChunk(int i, int i1) {
        return false;
    }

    @Override
    public boolean unloadChunk(int i, int i1, boolean b) {
        return false;
    }

    @Override
    public boolean unloadChunk(int i, int i1, boolean b, boolean b1) {
        return false;
    }

    @Override
    public boolean unloadChunkRequest(int i, int i1) {
        return false;
    }

    @Override
    public boolean unloadChunkRequest(int i, int i1, boolean b) {
        return false;
    }

    @Override
    public boolean regenerateChunk(int i, int i1) {
        return false;
    }

    @Override
    public boolean refreshChunk(int i, int i1) {
        return false;
    }

    @Override
    public boolean isChunkForceLoaded(int i, int i1) {
        return false;
    }

    @Override
    public void setChunkForceLoaded(int i, int i1, boolean b) {

    }

    @Override
    public Collection<Chunk> getForceLoadedChunks() {
        return null;
    }

    @Override
    public Item dropItem(Location location, ItemStack itemStack) {
        return null;
    }

    @Override
    public Item dropItemNaturally(Location location, ItemStack itemStack) {
        return null;
    }

    @Override
    public Arrow spawnArrow(Location location, Vector vector, float v, float v1) {
        return null;
    }

    @Override
    public <T extends Arrow> T spawnArrow(Location location, Vector vector, float v, float v1, Class<T> aClass) {
        return null;
    }

    @Override
    public boolean generateTree(Location location, TreeType treeType) {
        return false;
    }

    @Override
    public boolean generateTree(Location location, TreeType treeType, BlockChangeDelegate blockChangeDelegate) {
        return false;
    }

    @Override
    public Entity spawnEntity(Location location, EntityType entityType) {
        return null;
    }

    @Override
    public LightningStrike strikeLightning(Location location) {
        return null;
    }

    @Override
    public LightningStrike strikeLightningEffect(Location location) {
        return null;
    }

    @Override
    public List<Entity> getEntities() {
        return null;
    }

    @Override
    public List<LivingEntity> getLivingEntities() {
        return null;
    }

    @Override
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... classes) {
        return null;
    }

    @Override
    public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> aClass) {
        return null;
    }

    @Override
    public Collection<Entity> getEntitiesByClasses(Class<?>... classes) {
        return null;
    }

    @Override
    public List<Player> getPlayers() {
        return null;
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double v, double v1, double v2) {
        return nearbyEntities;
    }

    @Override
    public Collection<Entity> getNearbyEntities(Location location, double v, double v1, double v2, Predicate<Entity> predicate) {
        return null;
    }

    @Override
    public Collection<Entity> getNearbyEntities(BoundingBox boundingBox) {
        return null;
    }

    @Override
    public Collection<Entity> getNearbyEntities(BoundingBox boundingBox, Predicate<Entity> predicate) {
        return null;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location location, Vector vector, double v) {
        return null;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location location, Vector vector, double v, double v1) {
        return null;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location location, Vector vector, double v, Predicate<Entity> predicate) {
        return null;
    }

    @Override
    public RayTraceResult rayTraceEntities(Location location, Vector vector, double v, double v1, Predicate<Entity> predicate) {
        return null;
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location location, Vector vector, double v) {
        return null;
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location location, Vector vector, double v, FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public RayTraceResult rayTraceBlocks(Location location, Vector vector, double v, FluidCollisionMode fluidCollisionMode, boolean b) {
        return null;
    }

    @Override
    public RayTraceResult rayTrace(Location location, Vector vector, double v, FluidCollisionMode fluidCollisionMode, boolean b, double v1, Predicate<Entity> predicate) {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUID() {
        return UUID.fromString("d2460330-f815-4339-9b11-cf10755ccef9");
    }

    @Override
    public Location getSpawnLocation() {
        return null;
    }

    @Override
    public boolean setSpawnLocation(Location location) {
        return false;
    }

    @Override
    public boolean setSpawnLocation(int i, int i1, int i2) {
        return false;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public void setTime(long l) {

    }

    @Override
    public long getFullTime() {
        return 0;
    }

    @Override
    public void setFullTime(long l) {

    }

    @Override
    public boolean hasStorm() {
        return false;
    }

    @Override
    public void setStorm(boolean b) {

    }

    @Override
    public int getWeatherDuration() {
        return 0;
    }

    @Override
    public void setWeatherDuration(int i) {

    }

    @Override
    public boolean isThundering() {
        return false;
    }

    @Override
    public void setThundering(boolean b) {

    }

    @Override
    public int getThunderDuration() {
        return 0;
    }

    @Override
    public void setThunderDuration(int i) {

    }

    @Override
    public boolean createExplosion(double v, double v1, double v2, float v3) {
        return false;
    }

    @Override
    public boolean createExplosion(double v, double v1, double v2, float v3, boolean b) {
        return false;
    }

    @Override
    public boolean createExplosion(double v, double v1, double v2, float v3, boolean b, boolean b1) {
        return false;
    }

    @Override
    public boolean createExplosion(Location location, float v) {
        return false;
    }

    @Override
    public boolean createExplosion(Location location, float v, boolean b) {
        return false;
    }

    @Override
    public Environment getEnvironment() {
        return null;
    }

    @Override
    public long getSeed() {
        return 0;
    }

    @Override
    public boolean getPVP() {
        return false;
    }

    @Override
    public void setPVP(boolean b) {

    }

    @Override
    public ChunkGenerator getGenerator() {
        return null;
    }

    @Override
    public void save() {

    }

    @Override
    public List<BlockPopulator> getPopulators() {
        return null;
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> aClass) throws IllegalArgumentException {
        Villager villager = mock(Villager.class);
        when(villager.getLocation()).thenReturn(location);
        nearbyEntities.add(villager);
        return (T) villager;
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> aClass, Consumer<T> consumer) throws IllegalArgumentException {
        return null;
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, MaterialData materialData) throws IllegalArgumentException {
        return null;
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, BlockData blockData) throws IllegalArgumentException {
        return null;
    }

    @Override
    public FallingBlock spawnFallingBlock(Location location, Material material, byte b) throws IllegalArgumentException {
        return null;
    }

    @Override
    public void playEffect(Location location, Effect effect, int i) {

    }

    @Override
    public void playEffect(Location location, Effect effect, int i, int i1) {

    }

    @Override
    public <T> void playEffect(Location location, Effect effect, T t) {

    }

    @Override
    public <T> void playEffect(Location location, Effect effect, T t, int i) {

    }

    @Override
    public ChunkSnapshot getEmptyChunkSnapshot(int i, int i1, boolean b, boolean b1) {
        return null;
    }

    @Override
    public void setSpawnFlags(boolean b, boolean b1) {

    }

    @Override
    public boolean getAllowAnimals() {
        return false;
    }

    @Override
    public boolean getAllowMonsters() {
        return false;
    }

    @Override
    public Biome getBiome(int i, int i1) {
        return null;
    }

    @Override
    public void setBiome(int i, int i1, Biome biome) {

    }

    @Override
    public double getTemperature(int i, int i1) {
        return 0;
    }

    @Override
    public double getHumidity(int i, int i1) {
        return 0;
    }

    @Override
    public int getMaxHeight() {
        return 255;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public boolean getKeepSpawnInMemory() {
        return false;
    }

    @Override
    public void setKeepSpawnInMemory(boolean b) {

    }

    @Override
    public boolean isAutoSave() {
        return false;
    }

    @Override
    public void setAutoSave(boolean b) {

    }

    @Override
    public void setDifficulty(Difficulty difficulty) {

    }

    @Override
    public Difficulty getDifficulty() {
        return null;
    }

    @Override
    public File getWorldFolder() {
        return null;
    }

    @Override
    public WorldType getWorldType() {
        return null;
    }

    @Override
    public boolean canGenerateStructures() {
        return false;
    }

    @Override
    public long getTicksPerAnimalSpawns() {
        return 0;
    }

    @Override
    public void setTicksPerAnimalSpawns(int i) {

    }

    @Override
    public long getTicksPerMonsterSpawns() {
        return 0;
    }

    @Override
    public void setTicksPerMonsterSpawns(int i) {

    }

    @Override
    public int getMonsterSpawnLimit() {
        return 0;
    }

    @Override
    public void setMonsterSpawnLimit(int i) {

    }

    @Override
    public int getAnimalSpawnLimit() {
        return 0;
    }

    @Override
    public void setAnimalSpawnLimit(int i) {

    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        return 0;
    }

    @Override
    public void setWaterAnimalSpawnLimit(int i) {

    }

    @Override
    public int getAmbientSpawnLimit() {
        return 0;
    }

    @Override
    public void setAmbientSpawnLimit(int i) {

    }

    @Override
    public void playSound(Location location, Sound sound, float v, float v1) {

    }

    @Override
    public void playSound(Location location, String s, float v, float v1) {

    }

    @Override
    public void playSound(Location location, Sound sound, SoundCategory soundCategory, float v, float v1) {

    }

    @Override
    public void playSound(Location location, String s, SoundCategory soundCategory, float v, float v1) {

    }

    @Override
    public String[] getGameRules() {
        return new String[0];
    }

    @Override
    public String getGameRuleValue(String s) {
        return null;
    }

    @Override
    public boolean setGameRuleValue(String s, String s1) {
        return false;
    }

    @Override
    public boolean isGameRule(String s) {
        return false;
    }

    @Override
    public <T> T getGameRuleValue(GameRule<T> gameRule) {
        return null;
    }

    @Override
    public <T> T getGameRuleDefault(GameRule<T> gameRule) {
        return null;
    }

    @Override
    public <T> boolean setGameRule(GameRule<T> gameRule, T t) {
        return false;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return null;
    }

    @Override
    public void spawnParticle(Particle particle, Location location, int i) {

    }

    @Override
    public void spawnParticle(Particle particle, double v, double v1, double v2, int i) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int i, T t) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, double v, double v1, double v2, int i, T t) {

    }

    @Override
    public void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2) {

    }

    @Override
    public void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2, T t) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, T t) {

    }

    @Override
    public void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2, double v3) {

    }

    @Override
    public void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2, double v3, T t) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6, T t) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, Location location, int i, double v, double v1, double v2, double v3, T t, boolean b) {

    }

    @Override
    public <T> void spawnParticle(Particle particle, double v, double v1, double v2, int i, double v3, double v4, double v5, double v6, T t, boolean b) {

    }

    @Override
    public Location locateNearestStructure(Location location, StructureType structureType, int i, boolean b) {
        return null;
    }

    @Override
    public Spigot spigot() {
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

    @Override
    public void sendPluginMessage(Plugin plugin, String s, byte[] bytes) {

    }

    @Override
    public Set<String> getListeningPluginChannels() {
        return null;
    }
}
