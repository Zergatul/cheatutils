package com.zergatul.cheatutils.webui;

import com.mojang.serialization.Lifecycle;
import com.zergatul.cheatutils.interfaces.RegistryEntryReferenceMixinInterface;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.apache.http.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EntityInfoApi extends ApiBase {

    private static Logger logger = LogManager.getLogger(EntityInfoApi.class);

    private static List<EntityInfo> classes;

    @Override
    public String getRoute() {
        return "entity-info";
    }

    @Override
    public String get() throws HttpException {
        getEntityClasses();
        return gson.toJson(classes);
    }

    private static synchronized void getEntityClasses() {
        if (classes != null) {
            return;
        }

        EntityType<?> playerEntityType = ModApiWrapper.ENTITY_TYPES.getValue(new Identifier("minecraft:player"));

        HashSet<EntityInfo> set = new HashSet<>();
        try {
            set.add(new EntityInfo(PlayerEntity.class, "minecraft:player"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        World level = new FakeLevel();
        List<EntityInfo> finalClasses = ModApiWrapper.ENTITY_TYPES.getValues().stream().map(et -> {
            if (et == playerEntityType) {
                return null;
            }
            try {
                Entity entity = et.create(level);
                if (entity == null) {
                    return null;
                } else {
                    EntityInfo info = new EntityInfo(entity.getClass(), ModApiWrapper.ENTITY_TYPES.getKey(et).toString());
                    set.add(info);
                    return info;
                }
            }
            catch (Throwable throwable) {
                logger.warn("Create entity by EntityType failed");
                logger.warn(et.toString());
                throwable.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).toList();

        finalClasses.forEach(ei -> {
            Class clazz = ei.clazz.getSuperclass();
            while (Entity.class.isAssignableFrom(clazz)) {
                try {
                    EntityInfo baseInfo = new EntityInfo(clazz);
                    if (!set.contains(baseInfo)) {
                        set.add(baseInfo);
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }
                clazz = clazz.getSuperclass();
            }
        });

        classes = set.stream().sorted((i1, i2) -> i1.getSimpleName().compareToIgnoreCase(i2.getSimpleName())).toList();
    }

    private static class FakeLevel extends World {

        public FakeLevel() {
            super(
                    new FakeWritableLevelData(),
                    World.OVERWORLD,
                    createHolder(),
                    () -> null,
                    true,
                    true,
                    0,
                    0);
        }

        private static RegistryEntry<DimensionType> createHolder() {
            RegistryEntry.Reference<DimensionType> holder = RegistryEntry.Reference.standAlone(
                    new RegistryEntryOwner<>() {
                    },
                    RegistryKey.of(RegistryKeys.DIMENSION_TYPE, World.OVERWORLD.getValue()));

            DimensionType overworld = new DimensionType(
                    OptionalLong.empty(),
                    true,
                    false,
                    false,
                    true,
                    1.0,
                    true,
                    false,
                    -64,
                    384,
                    384,
                    null,
                    World.OVERWORLD.getValue(),
                    0.0f,
                    new DimensionType.MonsterSettings(false, true, new IntProvider() {
                        @Override
                        public int get(Random random) {
                            return 0;
                        }

                        @Override
                        public int getMin() {
                            return 0;
                        }

                        @Override
                        public int getMax() {
                            return 7;
                        }

                        @Override
                        public IntProviderType<?> getType() {
                            return null;
                        }
                    }, 0));

            ((RegistryEntryReferenceMixinInterface<DimensionType>) holder).setValue(overworld);
            return holder;
        }

        @Override
        public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) {

        }

        @Override
        public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {

        }

        @Override
        public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) {

        }

        @Override
        public String asString() {
            return null;
        }

        @Nullable
        @Override
        public Entity getEntityById(int id) {
            return null;
        }

        @Nullable
        @Override
        public MapState getMapState(String id) {
            return null;
        }

        @Override
        public void putMapState(String id, MapState state) {

        }

        @Override
        public int getNextMapId() {
            return 0;
        }

        @Override
        public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {

        }

        @Override
        public Scoreboard getScoreboard() {
            return new Scoreboard();
        }

        @Override
        public RecipeManager getRecipeManager() {
            return null;
        }

        @Override
        protected EntityLookup<Entity> getEntityLookup() {
            return null;
        }

        @Override
        public QueryableTickScheduler<Block> getBlockTickScheduler() {
            return null;
        }

        @Override
        public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
            return null;
        }

        @Override
        public ChunkManager getChunkManager() {
            return null;
        }

        @Override
        public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {

        }

        @Override
        public void emitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) {

        }

        @Override
        public float getBrightness(Direction direction, boolean shaded) {
            return 0;
        }

        @Override
        public List<? extends PlayerEntity> getPlayers() {
            return null;
        }

        @Override
        public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
            return null;
        }

        @Override
        public DynamicRegistryManager getRegistryManager() {
            return null;
        }

        @Override
        public FeatureSet getEnabledFeatures() {
            return FeatureSet.of(FeatureFlags.VANILLA);
        }
    }

    private static class EntityInfo {

        public Class clazz;
        public List<Class> baseClasses;
        public String id;

        public EntityInfo(Class clazz) throws Exception {
            this(clazz, null);
        }

        public EntityInfo(Class clazz, String id) throws Exception {

            if (!Entity.class.isAssignableFrom(clazz)) {
                throw new Exception("Not supported");
            }

            this.clazz = clazz;

            this.baseClasses = new ArrayList<>();
            while (clazz != Entity.class) {
                clazz = clazz.getSuperclass();
                this.baseClasses.add(clazz);
            }

            this.id = id;
        }

        public String getSimpleName() {
            return clazz.getSimpleName();
        }

        @Override
        public int hashCode() {
            return clazz.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EntityInfo ei) {
                return ei.clazz == clazz;
            } else {
                return false;
            }
        }
    }

    private static class FakeWritableLevelData implements MutableWorldProperties {

        @Override
        public void setSpawnX(int p_78651_) {

        }

        @Override
        public void setSpawnY(int p_78652_) {

        }

        @Override
        public void setSpawnZ(int p_78653_) {

        }

        @Override
        public void setSpawnAngle(float p_78648_) {

        }

        @Override
        public int getSpawnX() {
            return 0;
        }

        @Override
        public int getSpawnY() {
            return 0;
        }

        @Override
        public int getSpawnZ() {
            return 0;
        }

        @Override
        public float getSpawnAngle() {
            return 0;
        }

        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public long getTimeOfDay() {
            return 0;
        }

        @Override
        public boolean isThundering() {
            return false;
        }

        @Override
        public boolean isRaining() {
            return false;
        }

        @Override
        public void setRaining(boolean p_78171_) {

        }

        @Override
        public boolean isHardcore() {
            return false;
        }

        @Override
        public GameRules getGameRules() {
            return null;
        }

        @Override
        public Difficulty getDifficulty() {
            return null;
        }

        @Override
        public boolean isDifficultyLocked() {
            return false;
        }
    }

    private static class FakeBootstrapContext implements Registerable<DimensionType> {

        public DimensionType overworld;

        @Override
        public RegistryEntry.Reference<DimensionType> register(RegistryKey<DimensionType> key, DimensionType value, Lifecycle lifecycle) {
            if (key == DimensionTypes.OVERWORLD) {
                overworld = value;
            }
            return null;
        }

        @Override
        public <S> RegistryEntryLookup<S> getRegistryLookup(RegistryKey<? extends Registry<? extends S>> registryRef) {
            return null;
        }
    }
}