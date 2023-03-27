package com.zergatul.cheatutils.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import com.zergatul.cheatutils.configs.ClassRemapper;
import com.zergatul.cheatutils.interfaces.RegistryEntryReferenceMixinInterface;
import com.zergatul.cheatutils.webui.EntityInfoApi;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;
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
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EntityUtils {

    private static Logger logger = LogManager.getLogger(EntityInfoApi.class);

    private static List<EntityInfo> classes;
    private static Map<String, EntityInfo> classMap;

    public static List<EntityInfo> getEntityClasses() {
        if (classes == null) {
            loadEntityClasses();
        }
        return classes;
    }

    public static EntityInfo getEntityClass(String name) {
        if (classMap == null) {
            loadEntityClasses();
        }
        return classMap.get(name);
    }

    private static synchronized void loadEntityClasses() {
        if (classes != null) {
            return;
        }

        EntityType<?> playerEntityType = ModApiWrapper.ENTITY_TYPES.getValue(new Identifier("minecraft:player"));

        List<EntityInfo> finalClasses = new ArrayList<>();

        EntityInfo playerInfo = null;
        try {
            playerInfo = new EntityInfo(PlayerEntity.class, "minecraft:player");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        HashSet<EntityInfo> set = new HashSet<>();
        if (playerInfo != null) {
            finalClasses.add(playerInfo);
            set.add(playerInfo);
        }

        World level = new FakeLevel();
        ModApiWrapper.ENTITY_TYPES.getValues().stream().map(et -> {
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
        }).filter(Objects::nonNull).forEach(finalClasses::add);

        Set<Class<?>> interfaces = new HashSet<>();

        finalClasses.forEach(ei -> {
            forEachInterface(ei.clazz, interfaces::add);

            Class clazz = ei.clazz.getSuperclass();
            while (Entity.class.isAssignableFrom(clazz)) {
                try {
                    EntityInfo baseInfo = new EntityInfo(clazz);
                    set.add(baseInfo);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }
                clazz = clazz.getSuperclass();
            }
        });

        classes = new ArrayList<>();
        classes.addAll(set);
        for (Class<?> iface: interfaces) {
            try {
                classes.add(new EntityInfo(iface));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        classes = new ArrayList<>();
        classes.addAll(set);
        for (Class<?> iface: interfaces) {
            try {
                classes.add(new EntityInfo(iface));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        classes = classes.stream().sorted((i1, i2) -> i1.simpleName.compareToIgnoreCase(i2.simpleName)).toList();

        classMap = new HashMap<>(classes.size());
        for (EntityInfo info: classes) {
            classMap.put(info.clazz.getName(), info);
        }
    }

    private static void forEachInterface(Class<?> clazz, Consumer<Class<?>> consumer) {
        while (clazz != Entity.class) {
            Arrays.stream(clazz.getInterfaces()).forEach(consumer);
            clazz = clazz.getSuperclass();
        }
    }

    public static class EntityInfo {

        public Class clazz;
        public boolean isInterface;
        public String simpleName;
        public List<Class> baseClasses;
        public List<Class> interfaces;
        public String id;

        public EntityInfo(Class clazz) throws Exception {
            this(clazz, null);
        }

        public EntityInfo(Class clazz, String id) throws Exception {
            if (clazz.isInterface()) {
                this.clazz = clazz;
                simpleName = getSimpleName(clazz);
                isInterface = true;
            } else {
                if (!Entity.class.isAssignableFrom(clazz)) {
                    throw new Exception("Not supported");
                }

                this.clazz = clazz;
                simpleName = getSimpleName(clazz);

                this.id = id;

                baseClasses = new ArrayList<>();
                while (clazz != Entity.class) {
                    clazz = clazz.getSuperclass();
                    baseClasses.add(clazz);
                }

                interfaces = new ArrayList<>();
                forEachInterface(this.clazz, iface -> interfaces.add(iface));
            }
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

        private String getSimpleName(Class clazz) {
            String rawName = ClassRemapper.fromObf(clazz.getName());
            int index = rawName.lastIndexOf('.');
            if (index < 0) {
                return rawName;
            } else {
                return rawName.substring(index + 1);
            }
        }
    }

    private static class FakeLevel extends World {

        protected FakeLevel() {
            super(
                    new FakeWritableLevelData(),
                    World.OVERWORLD,
                    new DynamicRegistryManager() {
                        @Override
                        public <E> Optional<Registry<E>> getOptional(RegistryKey<? extends Registry<? extends E>> key) {
                            if (key.equals(RegistryKeys.DAMAGE_TYPE)) {
                                var x = (Registry<E>) createDamageTypeRegistry();
                                return Optional.of(x);
                            } else {
                                return Optional.empty();
                            }
                        }

                        @Override
                        public Stream<Entry<?>> streamAllRegistries() {
                            return null;
                        }
                    },
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

        private static Registry<DamageType> createDamageTypeRegistry() {
            return new Registry<DamageType>() {
                @Override
                public RegistryKey<? extends Registry<DamageType>> getKey() {
                    return null;
                }

                @Nullable
                @Override
                public Identifier getId(DamageType value) {
                    return null;
                }

                @Override
                public Optional<RegistryKey<DamageType>> getKey(DamageType entry) {
                    return Optional.empty();
                }

                @Override
                public int getRawId(@Nullable DamageType value) {
                    return 0;
                }

                @Nullable
                @Override
                public DamageType get(@Nullable RegistryKey<DamageType> key) {
                    return null;
                }

                @Nullable
                @Override
                public DamageType get(@Nullable Identifier id) {
                    return null;
                }

                @Override
                public Lifecycle getEntryLifecycle(DamageType entry) {
                    return null;
                }

                @Override
                public Lifecycle getLifecycle() {
                    return null;
                }

                @Override
                public Set<Identifier> getIds() {
                    return null;
                }

                @Override
                public Set<Map.Entry<RegistryKey<DamageType>, DamageType>> getEntrySet() {
                    return null;
                }

                @Override
                public Set<RegistryKey<DamageType>> getKeys() {
                    return null;
                }

                @Override
                public Optional<RegistryEntry.Reference<DamageType>> getRandom(Random random) {
                    return Optional.empty();
                }

                @Override
                public boolean containsId(Identifier id) {
                    return false;
                }

                @Override
                public boolean contains(RegistryKey<DamageType> key) {
                    return false;
                }

                @Override
                public Registry<DamageType> freeze() {
                    return null;
                }

                @Override
                public RegistryEntry.Reference<DamageType> createEntry(DamageType value) {
                    return null;
                }

                @Override
                public Optional<RegistryEntry.Reference<DamageType>> getEntry(int rawId) {
                    return Optional.empty();
                }

                @Override
                public Optional<RegistryEntry.Reference<DamageType>> getEntry(RegistryKey<DamageType> key) {
                    return Optional.of(RegistryEntry.Reference.standAlone(new RegistryEntryOwner<>() {
                        @Override
                        public boolean ownerEquals(RegistryEntryOwner<DamageType> other) {
                            return false;
                        }
                    }, key));
                }

                @Override
                public RegistryEntry<DamageType> getEntry(DamageType value) {
                    return null;
                }

                @Override
                public Stream<RegistryEntry.Reference<DamageType>> streamEntries() {
                    return null;
                }

                @Override
                public Optional<RegistryEntryList.Named<DamageType>> getEntryList(TagKey<DamageType> tag) {
                    return Optional.empty();
                }

                @Override
                public RegistryEntryList.Named<DamageType> getOrCreateEntryList(TagKey<DamageType> tag) {
                    return null;
                }

                @Override
                public Stream<Pair<TagKey<DamageType>, RegistryEntryList.Named<DamageType>>> streamTagsAndEntries() {
                    return null;
                }

                @Override
                public Stream<TagKey<DamageType>> streamTags() {
                    return null;
                }

                @Override
                public void clearTags() {

                }

                @Override
                public void populateTags(Map<TagKey<DamageType>, List<RegistryEntry<DamageType>>> tagEntries) {

                }

                @Override
                public RegistryEntryOwner<DamageType> getEntryOwner() {
                    return null;
                }

                @Override
                public RegistryWrapper.Impl<DamageType> getReadOnlyWrapper() {
                    return null;
                }

                @Nullable
                @Override
                public DamageType get(int index) {
                    return null;
                }

                @Override
                public int size() {
                    return 0;
                }

                @NotNull
                @Override
                public Iterator<DamageType> iterator() {
                    return null;
                }
            };
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
}