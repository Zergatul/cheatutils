package com.zergatul.cheatutils.utils;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.webui.EntityInfoApi;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

        HashSet<EntityInfo> set = new HashSet<>();
        try {
            set.add(new EntityInfo(Player.class, "minecraft:player"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        Level level = new FakeLevel();
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

        classes = set.stream().sorted((i1, i2) -> i1.simpleName.compareToIgnoreCase(i2.simpleName)).toList();

        classMap = new HashMap<>(classes.size());
        for (EntityInfo info: classes) {
            classMap.put(info.clazz.getName(), info);
        }
    }

    public static class EntityInfo {

        public Class clazz;
        public String simpleName;
        public List<String> baseClasses;
        public List<String> interfaces;
        public String id;

        public EntityInfo(Class clazz) throws Exception {
            this(clazz, null);
        }

        public EntityInfo(Class clazz, String id) throws Exception {

            if (!Entity.class.isAssignableFrom(clazz)) {
                throw new Exception("Not supported");
            }

            this.clazz = clazz;
            simpleName = clazz.getSimpleName();

            this.id = id;

            baseClasses = new ArrayList<>();
            while (clazz != Entity.class) {
                clazz = clazz.getSuperclass();
                baseClasses.add(clazz.getSimpleName());
            }

            clazz = this.clazz;
            interfaces = new ArrayList<>();
            while (clazz != Entity.class) {
                for (Class<?> _interface: clazz.getInterfaces()) {
                    String interfaceName = _interface.getSimpleName();
                    interfaces.add(interfaceName);
                }
                clazz = clazz.getSuperclass();
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
            super(new WritableLevelData() {
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
                  },
                    Level.OVERWORLD,
                    new Holder<DimensionType>() {
                        @Override
                        public DimensionType value() {
                            return RegistryAccess.BUILTIN.get().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(Level.OVERWORLD.location());
                        }

                        @Override
                        public boolean isBound() {
                            return false;
                        }

                        @Override
                        public boolean is(ResourceLocation p_205713_) {
                            return false;
                        }

                        @Override
                        public boolean is(ResourceKey<DimensionType> p_205712_) {
                            return false;
                        }

                        @Override
                        public boolean is(Predicate<ResourceKey<DimensionType>> p_205711_) {
                            return false;
                        }

                        @Override
                        public boolean is(TagKey<DimensionType> p_205705_) {
                            return false;
                        }

                        @Override
                        public Stream<TagKey<DimensionType>> tags() {
                            return null;
                        }

                        @Override
                        public Either<ResourceKey<DimensionType>, DimensionType> unwrap() {
                            return null;
                        }

                        @Override
                        public Optional<ResourceKey<DimensionType>> unwrapKey() {
                            ResourceKey<DimensionType> x = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, Level.OVERWORLD.location());
                            return Optional.of(x);
                        }

                        @Override
                        public Kind kind() {
                            return null;
                        }

                        @Override
                        public boolean isValidInRegistry(Registry<DimensionType> p_205708_) {
                            return false;
                        }
                    },
                    new Supplier<ProfilerFiller>() {
                        @Override
                        public ProfilerFiller get() {
                            return null;
                        }
                    },
                    true,
                    true,
                    0,
                    0);
        }

        @Override
        public void sendBlockUpdated(BlockPos p_46612_, BlockState p_46613_, BlockState p_46614_, int p_46615_) {

        }

        @Override
        public void playSeededSound(@Nullable Player p_220363_, double p_220364_, double p_220365_, double p_220366_, SoundEvent p_220367_, SoundSource p_220368_, float p_220369_, float p_220370_, long p_220371_) {

        }

        @Override
        public void playSeededSound(@Nullable Player p_220372_, Entity p_220373_, SoundEvent p_220374_, SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_) {

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
}