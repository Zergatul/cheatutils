package com.zergatul.cheatutils.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import com.zergatul.cheatutils.mixins.common.accessors.HolderReferenceAccessor;
import com.zergatul.cheatutils.wrappers.ClassRemapper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.WolfVariant;
import net.minecraft.world.entity.animal.WolfVariants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class EntityUtils {

    private static final Logger logger = LogManager.getLogger(EntityUtils.class);

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

        EntityType<?> playerEntityType = com.zergatul.cheatutils.common.Registries.ENTITY_TYPES.getValue(new ResourceLocation("minecraft:player"));

        List<EntityInfo> finalClasses = new ArrayList<>();

        EntityInfo playerInfo = null;
        try {
            playerInfo = new EntityInfo(Player.class, "minecraft:player");
        }
        catch (Exception ex) {
            logger.error("Cannot create Player EntityInfo.", ex);
        }

        EntityInfo localPlayerInfo = null;
        try {
            localPlayerInfo = new EntityInfo(LocalPlayer.class);
        }
        catch (Exception ex) {
            logger.error("Cannot create LocalPlayer EntityInfo.", ex);
        }

        EntityInfo remotePlayerInfo = null;
        try {
            remotePlayerInfo = new EntityInfo(RemotePlayer.class);
        }
        catch (Exception ex) {
            logger.error("Cannot create RemotePlayer EntityInfo.", ex);
        }

        HashSet<EntityInfo> set = new HashSet<>();
        if (playerInfo != null) {
            finalClasses.add(playerInfo);
            set.add(playerInfo);
        }
        if (localPlayerInfo != null) {
            finalClasses.add(localPlayerInfo);
            set.add(localPlayerInfo);
        }
        if (remotePlayerInfo != null) {
            finalClasses.add(remotePlayerInfo);
            set.add(remotePlayerInfo);
        }

        Level level = new FakeLevel();
        com.zergatul.cheatutils.common.Registries.ENTITY_TYPES.getValues().stream().map(et -> {
            if (et == playerEntityType) {
                return null;
            }
            try {
                Entity entity = et.create(level);
                if (entity == null) {
                    return null;
                } else {
                    EntityInfo info = new EntityInfo(entity.getClass(), com.zergatul.cheatutils.common.Registries.ENTITY_TYPES.getKey(et).toString());
                    set.add(info);
                    return info;
                }
            }
            catch (Throwable throwable) {
                logger.warn("Create entity by EntityType failed: " + et.toString(), throwable);
                return null;
            }
        }).filter(Objects::nonNull).forEach(finalClasses::add);

        Set<Class<?>> interfaces = new HashSet<>();

        finalClasses.forEach(ei -> {
            forEachInterface(ei.clazz, interfaces::add);

            Class<?> clazz = ei.clazz.getSuperclass();
            while (Entity.class.isAssignableFrom(clazz)) {
                try {
                    EntityInfo baseInfo = new EntityInfo(clazz);
                    set.add(baseInfo);
                }
                catch (Exception ex) {
                    logger.warn("Cannot create EntityInfo for base class " + clazz.getName(), ex);
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
                logger.warn("Cannot create EntityInfo for interface " + iface.getName(), ex);
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

        public Class<?> clazz;
        public boolean isInterface;
        public String simpleName;
        public List<Class<?>> baseClasses;
        public List<Class<?>> interfaces;
        public String id;

        public EntityInfo(Class<?> clazz) throws Exception {
            this(clazz, null);
        }

        public EntityInfo(Class<?> clazz, String id) throws Exception {
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

        private String getSimpleName(Class<?> clazz) {
            String rawName = ClassRemapper.fromObf(clazz.getName());
            int index = rawName.lastIndexOf('.');
            if (index < 0) {
                return rawName;
            } else {
                return rawName.substring(index + 1);
            }
        }
    }

    private static class FakeLevel extends Level {

        protected FakeLevel() {
            super(
                    new FakeWritableLevelData(),
                    Level.OVERWORLD,
                    new RegistryAccess() {

                        @SuppressWarnings("unchecked")
                        @Override
                        public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> key) {
                            if (key.equals(Registries.DAMAGE_TYPE)) {
                                return Optional.of((Registry<E>) createDamageTypeRegistry());
                            } else if (key.equals(Registries.BANNER_PATTERN)) {
                                return Optional.of((Registry<E>) createBannerPatternRegistry()); // for lithium
                            } else {
                                return Optional.empty();
                            }
                        }

                        @Override
                        public Stream<RegistryEntry<?>> registries() {
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

        private static Holder<DimensionType> createHolder() {
            Holder.Reference<DimensionType> holder = Holder.Reference.createStandAlone(new HolderOwner<DimensionType>() {
                @Override
                public boolean canSerializeIn(HolderOwner<DimensionType> p_255875_) {
                    return false;
                }
            }, ResourceKey.create(Registries.DIMENSION_TYPE, Level.OVERWORLD.location()));

            var context = new FakeBootstrapContext();
            DimensionTypes.bootstrap(context);
            ((HolderReferenceAccessor) holder).bindValue_CU(context.overworld);
            return holder;
        }

        private static Registry<DamageType> createDamageTypeRegistry() {
            return new Registry<DamageType>() {
                @Override
                public ResourceKey<? extends Registry<DamageType>> key() {
                    return null;
                }

                @Nullable
                @Override
                public ResourceLocation getKey(DamageType p_123006_) {
                    return null;
                }

                @Override
                public Optional<ResourceKey<DamageType>> getResourceKey(DamageType p_123008_) {
                    return Optional.empty();
                }

                @Override
                public int getId(@Nullable DamageType p_122977_) {
                    return 0;
                }

                @Nullable
                @Override
                public DamageType get(@Nullable ResourceKey<DamageType> p_122980_) {
                    return null;
                }

                @Nullable
                @Override
                public DamageType get(@Nullable ResourceLocation p_123002_) {
                    return null;
                }

                @Override
                public Optional<RegistrationInfo> registrationInfo(ResourceKey<DamageType> p_333179_) {
                    return Optional.empty();
                }

                @Override
                public Lifecycle registryLifecycle() {
                    return null;
                }

                @Override
                public Set<ResourceLocation> keySet() {
                    return null;
                }

                @Override
                public Set<Map.Entry<ResourceKey<DamageType>, DamageType>> entrySet() {
                    return null;
                }

                @Override
                public Set<ResourceKey<DamageType>> registryKeySet() {
                    return null;
                }

                @Override
                public Optional<Holder.Reference<DamageType>> getRandom(RandomSource p_235781_) {
                    return Optional.empty();
                }

                @Override
                public boolean containsKey(ResourceLocation p_123011_) {
                    return false;
                }

                @Override
                public boolean containsKey(ResourceKey<DamageType> p_175475_) {
                    return false;
                }

                @Override
                public Registry<DamageType> freeze() {
                    return null;
                }

                @Override
                public Holder.Reference<DamageType> createIntrusiveHolder(DamageType p_206068_) {
                    return null;
                }

                @Override
                public Optional<Holder.Reference<DamageType>> getHolder(int p_206051_) {
                    return Optional.empty();
                }

                @Override
                public Optional<Holder.Reference<DamageType>> getHolder(ResourceLocation p_329586_) {
                    return Optional.empty();
                }

                @Override
                public Optional<Holder.Reference<DamageType>> getHolder(ResourceKey<DamageType> p_206050_) {
                    return Optional.of(Holder.Reference.createStandAlone(new HolderOwner<DamageType>() {
                        @Override
                        public boolean canSerializeIn(HolderOwner<DamageType> p_255875_) {
                            return false;
                        }
                    }, p_206050_));
                }

                @Override
                public Holder<DamageType> wrapAsHolder(DamageType p_263382_) {
                    return null;
                }

                @Override
                public Stream<Holder.Reference<DamageType>> holders() {
                    return null;
                }

                @Override
                public Optional<HolderSet.Named<DamageType>> getTag(TagKey<DamageType> p_206052_) {
                    return Optional.empty();
                }

                @Override
                public HolderSet.Named<DamageType> getOrCreateTag(TagKey<DamageType> p_206045_) {
                    return null;
                }

                @Override
                public Stream<Pair<TagKey<DamageType>, HolderSet.Named<DamageType>>> getTags() {
                    return null;
                }

                @Override
                public Stream<TagKey<DamageType>> getTagNames() {
                    return null;
                }

                @Override
                public void resetTags() {

                }

                @Override
                public void bindTags(Map<TagKey<DamageType>, List<Holder<DamageType>>> p_205997_) {

                }

                @Override
                public HolderOwner<DamageType> holderOwner() {
                    return null;
                }

                @Override
                public HolderLookup.RegistryLookup<DamageType> asLookup() {
                    return null;
                }

                @Nullable
                @Override
                public DamageType byId(int p_122651_) {
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

        private static Registry<BannerPattern> createBannerPatternRegistry() {
            return new Registry<BannerPattern>() {
                @Override
                public ResourceKey<? extends Registry<BannerPattern>> key() {
                    return null;
                }

                @Nullable
                @Override
                public ResourceLocation getKey(BannerPattern p_123006_) {
                    return null;
                }

                @Override
                public Optional<ResourceKey<BannerPattern>> getResourceKey(BannerPattern p_123008_) {
                    return Optional.empty();
                }

                @Override
                public int getId(@Nullable BannerPattern p_122977_) {
                    return 0;
                }

                @Nullable
                @Override
                public BannerPattern get(@Nullable ResourceKey<BannerPattern> p_122980_) {
                    return null;
                }

                @Nullable
                @Override
                public BannerPattern get(@Nullable ResourceLocation p_123002_) {
                    return null;
                }

                @Override
                public Optional<RegistrationInfo> registrationInfo(ResourceKey<BannerPattern> p_333179_) {
                    return Optional.empty();
                }

                @Override
                public Lifecycle registryLifecycle() {
                    return null;
                }

                @Override
                public Set<ResourceLocation> keySet() {
                    return Set.of();
                }

                @Override
                public Set<Map.Entry<ResourceKey<BannerPattern>, BannerPattern>> entrySet() {
                    return Set.of();
                }

                @Override
                public Set<ResourceKey<BannerPattern>> registryKeySet() {
                    return Set.of();
                }

                @Override
                public Optional<Holder.Reference<BannerPattern>> getRandom(RandomSource p_235781_) {
                    return Optional.empty();
                }

                @Override
                public boolean containsKey(ResourceLocation p_123011_) {
                    return false;
                }

                @Override
                public boolean containsKey(ResourceKey<BannerPattern> p_175475_) {
                    return false;
                }

                @Override
                public Registry<BannerPattern> freeze() {
                    return null;
                }

                @Override
                public Holder.Reference<BannerPattern> createIntrusiveHolder(BannerPattern p_206068_) {
                    return null;
                }

                @Override
                public Optional<Holder.Reference<BannerPattern>> getHolder(int p_206051_) {
                    return Optional.empty();
                }

                @Override
                public Optional<Holder.Reference<BannerPattern>> getHolder(ResourceLocation p_329586_) {
                    return Optional.empty();
                }

                @Override
                public Optional<Holder.Reference<BannerPattern>> getHolder(ResourceKey<BannerPattern> p_206050_) {
                    return Optional.empty();
                }

                @Override
                public Holder<BannerPattern> wrapAsHolder(BannerPattern p_263382_) {
                    return null;
                }

                @Override
                public Stream<Holder.Reference<BannerPattern>> holders() {
                    return Stream.empty();
                }

                @Override
                public Optional<HolderSet.Named<BannerPattern>> getTag(TagKey<BannerPattern> p_206052_) {
                    return Optional.empty();
                }

                @Override
                public HolderSet.Named<BannerPattern> getOrCreateTag(TagKey<BannerPattern> p_206045_) {
                    return null;
                }

                @Override
                public Stream<Pair<TagKey<BannerPattern>, HolderSet.Named<BannerPattern>>> getTags() {
                    return Stream.empty();
                }

                @Override
                public Stream<TagKey<BannerPattern>> getTagNames() {
                    return Stream.empty();
                }

                @Override
                public void resetTags() {

                }

                @Override
                public void bindTags(Map<TagKey<BannerPattern>, List<Holder<BannerPattern>>> p_205997_) {

                }

                @Override
                public HolderOwner<BannerPattern> holderOwner() {
                    return null;
                }

                @Override
                public HolderLookup.RegistryLookup<BannerPattern> asLookup() {
                    return null;
                }

                @Nullable
                @Override
                public BannerPattern byId(int p_122651_) {
                    return null;
                }

                @Override
                public int size() {
                    return 0;
                }

                @NotNull
                @Override
                public Iterator<BannerPattern> iterator() {
                    return null;
                }
            };
        }

        @Override
        public void sendBlockUpdated(BlockPos p_46612_, BlockState p_46613_, BlockState p_46614_, int p_46615_) {

        }

        @Override
        public void playSeededSound(@Nullable Player p_262953_, double p_263004_, double p_263398_, double p_263376_, Holder<SoundEvent> p_263359_, SoundSource p_263020_, float p_263055_, float p_262914_, long p_262991_) {

        }

        @Override
        public void playSeededSound(@Nullable Player p_220363_, double p_220364_, double p_220365_, double p_220366_, SoundEvent p_220367_, SoundSource p_220368_, float p_220369_, float p_220370_, long p_220371_) {

        }

        @Override
        public void playSeededSound(@Nullable Player p_220372_, Entity p_220373_, Holder<SoundEvent> p_263500_, SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_) {

        }

        @Override
        public String gatherChunkSourceStats() {
            return null;
        }

        @Nullable
        @Override
        public Entity getEntity(int p_46492_) {
            return null;
        }

        @Override
        public TickRateManager tickRateManager() {
            return null;
        }

        @Nullable
        @Override
        public MapItemSavedData getMapData(MapId p_335212_) {
            return null;
        }

        @Override
        public void setMapData(MapId p_332598_, MapItemSavedData p_151534_) {

        }

        @Override
        public MapId getFreeMapId() {
            return new MapId(0);
        }

        @Override
        public void destroyBlockProgress(int p_46506_, BlockPos p_46507_, int p_46508_) {

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
        protected LevelEntityGetter<Entity> getEntities() {
            return null;
        }

        @Override
        public LevelTickAccess<Block> getBlockTicks() {
            return null;
        }

        @Override
        public LevelTickAccess<Fluid> getFluidTicks() {
            return null;
        }

        @Override
        public ChunkSource getChunkSource() {
            return null;
        }

        @Override
        public void levelEvent(@Nullable Player p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_) {

        }

        @Override
        public void gameEvent(Holder<GameEvent> p_330236_, Vec3 p_220405_, GameEvent.Context p_220406_) {

        }

        @Override
        public RegistryAccess registryAccess() {
            return new RegistryAccess() {
                @Override
                public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> key) {
                    if (key.equals(Registries.WOLF_VARIANT)) {
                        return Optional.of((Registry<E>) new FakeWolfVariantRegistry());
                    }
                    return Optional.empty();
                }

                @Override
                public Stream<RegistryEntry<?>> registries() {
                    return null;
                }
            };
        }

        @Override
        public PotionBrewing potionBrewing() {
            return null;
        }

        @Override
        public FeatureFlagSet enabledFeatures() {
            return FeatureFlagSet.of(FeatureFlags.VANILLA);
        }

        @Override
        public float getShade(Direction p_45522_, boolean p_45523_) {
            return 0;
        }

        @Override
        public List<? extends Player> players() {
            return null;
        }

        @Override
        public Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
            return null;
        }
    }

    private static class FakeWritableLevelData implements WritableLevelData {
        @Override
        public BlockPos getSpawnPos() {
            return null;
        }

        @Override
        public float getSpawnAngle() {
            return 0;
        }

        @Override
        public long getGameTime() {
            return 0;
        }

        @Override
        public long getDayTime() {
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

        @Override
        public void setSpawn(BlockPos p_78649_, float p_78650_) {

        }
    }

    private static class FakeBootstrapContext implements BootstrapContext<DimensionType> {

        public DimensionType overworld;

        @Override
        public Holder.Reference<DimensionType> register(ResourceKey<DimensionType> p_256008_, DimensionType p_256454_, Lifecycle p_255725_) {
            if (p_256008_ == BuiltinDimensionTypes.OVERWORLD) {
                overworld = p_256454_;
            }
            return null;
        }

        @Override
        public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> p_256410_) {
            return null;
        }
    }

    private static class FakeWolfVariantRegistry implements Registry<WolfVariant> {

        @Override
        public ResourceKey<? extends Registry<WolfVariant>> key() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getKey(WolfVariant p_123006_) {
            return null;
        }

        @Override
        public Optional<ResourceKey<WolfVariant>> getResourceKey(WolfVariant p_123008_) {
            return Optional.empty();
        }

        @Override
        public int getId(@Nullable WolfVariant p_122977_) {
            return 0;
        }

        @Nullable
        @Override
        public WolfVariant byId(int p_122651_) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Nullable
        @Override
        public WolfVariant get(@Nullable ResourceKey<WolfVariant> p_122980_) {
            return null;
        }

        @Nullable
        @Override
        public WolfVariant get(@Nullable ResourceLocation p_123002_) {
            return null;
        }

        @Override
        public Optional<RegistrationInfo> registrationInfo(ResourceKey<WolfVariant> p_333179_) {
            return Optional.empty();
        }

        @Override
        public Lifecycle registryLifecycle() {
            return null;
        }

        @Override
        public Set<ResourceLocation> keySet() {
            return null;
        }

        @Override
        public Set<Map.Entry<ResourceKey<WolfVariant>, WolfVariant>> entrySet() {
            return null;
        }

        @Override
        public Set<ResourceKey<WolfVariant>> registryKeySet() {
            return null;
        }

        @Override
        public Optional<Holder.Reference<WolfVariant>> getRandom(RandomSource p_235781_) {
            return Optional.empty();
        }

        @Override
        public boolean containsKey(ResourceLocation p_123011_) {
            return false;
        }

        @Override
        public boolean containsKey(ResourceKey<WolfVariant> p_175475_) {
            return false;
        }

        @Override
        public Registry<WolfVariant> freeze() {
            return null;
        }

        @Override
        public Holder.Reference<WolfVariant> createIntrusiveHolder(WolfVariant p_206068_) {
            return null;
        }

        @Override
        public Optional<Holder.Reference<WolfVariant>> getHolder(int p_206051_) {
            return Optional.empty();
        }

        @Override
        public Optional<Holder.Reference<WolfVariant>> getHolder(ResourceLocation location) {
            return Optional.empty();
        }

        @Override
        public Optional<Holder.Reference<WolfVariant>> getHolder(ResourceKey<WolfVariant> key) {
            if (key.equals(WolfVariants.PALE)) {
                return Optional.of(Holder.Reference.createStandAlone(new HolderOwner<WolfVariant>() {
                    @Override
                    public boolean canSerializeIn(HolderOwner<WolfVariant> owner) {
                        return false;
                    }
                }, WolfVariants.PALE));
            }
            return Optional.empty();
        }

        @Override
        public Holder<WolfVariant> wrapAsHolder(WolfVariant p_263382_) {
            return null;
        }

        @Override
        public Stream<Holder.Reference<WolfVariant>> holders() {
            return null;
        }

        @Override
        public Optional<HolderSet.Named<WolfVariant>> getTag(TagKey<WolfVariant> p_206052_) {
            return Optional.empty();
        }

        @Override
        public HolderSet.Named<WolfVariant> getOrCreateTag(TagKey<WolfVariant> p_206045_) {
            return null;
        }

        @Override
        public Stream<Pair<TagKey<WolfVariant>, HolderSet.Named<WolfVariant>>> getTags() {
            return null;
        }

        @Override
        public Stream<TagKey<WolfVariant>> getTagNames() {
            return null;
        }

        @Override
        public void resetTags() {

        }

        @Override
        public void bindTags(Map<TagKey<WolfVariant>, List<Holder<WolfVariant>>> p_205997_) {

        }

        @Override
        public HolderOwner<WolfVariant> holderOwner() {
            return null;
        }

        @Override
        public HolderLookup.RegistryLookup<WolfVariant> asLookup() {
            return null;
        }

        @NotNull
        @Override
        public Iterator<WolfVariant> iterator() {
            return null;
        }
    }
}