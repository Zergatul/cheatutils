package com.zergatul.cheatutils.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import com.zergatul.cheatutils.webui.EntityInfoApi;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

        EntityType playerEntityType = ModApiWrapper.ENTITY_TYPES.getValue(new ResourceLocation("minecraft:player"));

        List<EntityInfo> finalClasses = new ArrayList<>();

        EntityInfo playerInfo = null;
        try {
            playerInfo = new EntityInfo(Player.class, "minecraft:player");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        HashSet<EntityInfo> set = new HashSet<>();
        if (playerInfo != null) {
            finalClasses.add(playerInfo);
            set.add(playerInfo);
        }

        Level level = new FakeLevel();
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

            Class<?> clazz = ei.clazz.getSuperclass();
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
                simpleName = clazz.getSimpleName();
                isInterface = true;
            } else {
                if (!Entity.class.isAssignableFrom(clazz)) {
                    throw new Exception("Not supported");
                }

                this.clazz = clazz;
                simpleName = clazz.getSimpleName();

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
    }

    private static class FakeLevel extends Level {

        protected FakeLevel() {
            super(
                    new FakeWritableLevelData(),
                    Level.OVERWORLD,
                    new RegistryAccess() {
                        @Override
                        public <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> p_123085_) {
                            if (p_123085_.equals(Registries.DAMAGE_TYPE)) {
                                var x = (Registry<E>) createDamageTypeRegistry();
                                return Optional.of(x);
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
            holder.bindValue(context.overworld);
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
                public Lifecycle lifecycle(DamageType p_123012_) {
                    return null;
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

        @Nullable
        @Override
        public MapItemSavedData getMapData(String p_46650_) {
            return null;
        }

        @Override
        public void setMapData(String p_151533_, MapItemSavedData p_151534_) {

        }

        @Override
        public int getFreeMapId() {
            return 0;
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
        public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, GameEvent.Context p_220406_) {

        }

        @Override
        public RegistryAccess registryAccess() {
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
        public void setXSpawn(int p_78651_) {

        }

        @Override
        public void setYSpawn(int p_78652_) {

        }

        @Override
        public void setZSpawn(int p_78653_) {

        }

        @Override
        public void setSpawnAngle(float p_78648_) {

        }

        @Override
        public int getXSpawn() {
            return 0;
        }

        @Override
        public int getYSpawn() {
            return 0;
        }

        @Override
        public int getZSpawn() {
            return 0;
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
    }

    private static class FakeBootstrapContext implements BootstapContext<DimensionType> {

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
}