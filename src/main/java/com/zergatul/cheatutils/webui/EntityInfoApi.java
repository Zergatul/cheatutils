package com.zergatul.cheatutils.webui;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
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
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.http.MethodNotSupportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class EntityInfoApi extends ApiBase {

    private static Logger logger = LogManager.getLogger(EntityInfoApi.class);

    private static final Class[] hardcodedClasses = new Class[] {
        Player.class
    };

    private static List<EntityInfo> classes;

    @Override
    public String getRoute() {
        return "entity-info";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        getEntityClasses();
        return gson.toJson(classes);
    }

    private static synchronized void getEntityClasses() {
        if (classes != null) {
            return;
        }

        EntityType playerEntityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("minecraft:player"));

        Level level = new FakeLevel();
        List<Class> entityClasses = ForgeRegistries.ENTITIES.getValues().stream().map(et -> {
            if (et == playerEntityType) {
                return (Class)null;
            }
            try {
                Entity entity = et.create(level);
                if (entity == null) {
                    return null;
                } else {
                    return entity.getClass();
                }
            }
            catch (Throwable throwable) {
                logger.warn("Create entity by EntityType failed");
                logger.warn(et.toString());
                throwable.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).toList();

        HashSet<Class> set = new HashSet<>();
        Arrays.stream(hardcodedClasses).forEach(set::add);
        for (Class clazz: entityClasses) {
            while (Entity.class.isAssignableFrom(clazz)) {
                set.add(clazz);
                clazz = clazz.getSuperclass();
            }
        }

        classes = set.stream().map(c -> {
            try {
                return new EntityInfo(c);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).sorted((i1, i2) -> i1.simpleName.compareToIgnoreCase(i2.simpleName)).toList();
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
                    0);
        }

        @Override
        public void sendBlockUpdated(BlockPos p_46612_, BlockState p_46613_, BlockState p_46614_, int p_46615_) {

        }

        @Override
        public void playSound(@Nullable Player p_46543_, double p_46544_, double p_46545_, double p_46546_, SoundEvent p_46547_, SoundSource p_46548_, float p_46549_, float p_46550_) {

        }

        @Override
        public void playSound(@Nullable Player p_46551_, Entity p_46552_, SoundEvent p_46553_, SoundSource p_46554_, float p_46555_, float p_46556_) {

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
        public void gameEvent(@Nullable Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_) {

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

    private static class EntityInfo {

        public Class clazz;
        public String simpleName;
        public List<String> baseClasses;

        public EntityInfo(Class clazz) throws Exception {

            if (!Entity.class.isAssignableFrom(clazz)) {
                throw new Exception("Not supported");
            }

            this.clazz = clazz;
            this.simpleName = clazz.getSimpleName();

            this.baseClasses = new ArrayList<>();
            while (clazz != Entity.class) {
                clazz = clazz.getSuperclass();
                this.baseClasses.add(clazz.getSimpleName());
            }
        }
    }
}
