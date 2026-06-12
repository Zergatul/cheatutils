package com.zergatul.cheatutils.tests.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@NullMarked
public class MockLevel extends Level {

    private final MockChunkSource chunkSource;
    private final MockLevelEntityStorage entityStorage;

    private MockLevel(
            WritableLevelData levelData,
            ResourceKey<Level> dimension,
            RegistryAccess registryAccess,
            Holder<DimensionType> dimensionTypeRegistration,
            boolean isClientSide,
            boolean isDebug,
            long biomeZoomSeed,
            int maxChainedNeighborUpdates
    ) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
        this.chunkSource = new MockChunkSource(this);
        this.entityStorage = new MockLevelEntityStorage();
    }

    public static MockLevel create() {
        WritableLevelData levelData = new MockWritableLevelData();
        RegistryAccess registryAccess = new MockRegistryAccess();
        Holder<DimensionType> dimensionType = registryAccess.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(BuiltinDimensionTypes.OVERWORLD);
        return new MockLevel(levelData, Level.OVERWORLD, registryAccess, dimensionType, true, false, 0, 0);
    }

    public void block(int x, int y, int z, Block block) {
        box(x, y, z, x, y, z, block);
    }

    public void block(int x, int y, int z, BlockState state) {
        box(x, y, z, x, y, z, state);
    }

    public void platform(int y, int x1, int z1, int x2, int z2, Block block) {
        box(x1, y, z1, x2, y, z2, block);
    }

    public void box(int x1, int y1, int z1, int x2, int y2, int z2, Block block) {
        box(x1, y1, z1, x2, y2, z2, block.defaultBlockState());
    }

    public void box(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    setBlock(new BlockPos(x, y, z), state, Block.UPDATE_NONE);
                }
            }
        }
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        entityStorage.addEntity(entity);
        return true;
    }

    @Override
    public void sendBlockUpdated(BlockPos pos, BlockState old, BlockState current, @Block.UpdateFlags int updateFlags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playSeededSound(@Nullable Entity except, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playSeededSound(@Nullable Entity except, Entity sourceEntity, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void explode(@Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float r, boolean fire, ExplosionInteraction interactionType, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles, WeightedList<ExplosionParticleInfo> blockParticles, Holder<SoundEvent> explosionSound) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String gatherChunkSourceStats() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRespawnData(LevelData.RespawnData respawnData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LevelData.RespawnData getRespawnData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable Entity getEntity(int id) {
        return entityStorage.get(id);
    }

    @Override
    public Collection<EnderDragonPart> dragonParts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TickRateManager tickRateManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable MapItemSavedData getMapData(MapId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroyBlockProgress(int id, BlockPos blockPos, int progress) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Scoreboard getScoreboard() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RecipeAccess recipeAccess() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected LevelEntityGetter<Entity> getEntities() {
        return entityStorage;
    }

    @Override
    public ClockManager clockManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int quartX, int quartY, int quartZ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSeaLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EnvironmentAttributeSystem environmentAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PotionBrewing potionBrewing() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FuelValues fuelValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChunkSource getChunkSource() {
        return chunkSource;
    }

    @Override
    public void levelEvent(@Nullable Entity source, int type, BlockPos pos, int data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 position, GameEvent.Context context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends Player> players() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WorldBorder getWorldBorder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void neighborShapeChanged(Direction direction, BlockPos pos, BlockPos neighborPos, BlockState neighborState, @Block.UpdateFlags int updateFlags, int updateLimit) {
        // skip vanilla implementation
    }
}