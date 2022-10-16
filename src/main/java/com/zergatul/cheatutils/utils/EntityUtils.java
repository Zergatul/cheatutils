package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.webui.EntityInfoApi;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.*;
import net.minecraft.world.Dimension;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ColumnFuzzedBiomeMagnifier;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class EntityUtils {

    private static Logger logger = LogManager.getLogger(EntityInfoApi.class);

    private static final Class[] hardcodedClasses = new Class[] {
            PlayerEntity.class
    };

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

        EntityType playerEntityType = ForgeRegistries.ENTITIES.getValue(new ResourceLocation("minecraft:player"));

        World level = new FakeLevel();
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
        }).filter(Objects::nonNull).collect(Collectors.toList());

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
        }).filter(Objects::nonNull).sorted((i1, i2) -> i1.simpleName.compareToIgnoreCase(i2.simpleName)).collect(Collectors.toList());

        classMap = new HashMap<>(classes.size());
        for (EntityInfo info: classes) {
            classMap.put(info.clazz.getName(), info);
        }
    }

    public static class EntityInfo {

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

    private static class FakeLevel extends World {

        protected FakeLevel() {
            /*SimpleRegistry<net.minecraft.world.Dimension> simpleregistry = dimensiongeneratorsettings.dimensions();
            net.minecraft.world.Dimension dimension = simpleregistry.get(Dimension.OVERWORLD);
            ChunkGenerator chunkgenerator;
            DimensionType dimensiontype;
            if (dimension == null) {
                dimensiontype = this.registryHolder.dimensionTypes().getOrThrow(DimensionType.OVERWORLD_LOCATION);
                chunkgenerator = DimensionGeneratorSettings.makeDefaultOverworld(this.registryHolder.registryOrThrow(Registry.BIOME_REGISTRY), this.registryHolder.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY), (new Random()).nextLong());
            } else {
                dimensiontype = dimension.type();
                chunkgenerator = dimension.generator();
            }*/

            super(
                    new ISpawnWorldInfo() {
                        @Override
                        public void setXSpawn(int p_76058_1_) {
                            
                        }

                        @Override
                        public void setYSpawn(int p_76056_1_) {

                        }

                        @Override
                        public void setZSpawn(int p_76087_1_) {

                        }

                        @Override
                        public void setSpawnAngle(float p_241859_1_) {

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
                        public void setRaining(boolean p_76084_1_) {

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
                    World.OVERWORLD,
                    new FakeDimensionType(),
                    () -> null,
                    true,
                    true,
                    0);
        }

        @Override
        public void sendBlockUpdated(BlockPos p_184138_1_, BlockState p_184138_2_, BlockState p_184138_3_, int p_184138_4_) {

        }

        @Override
        public void playSound(@Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_) {

        }

        @Override
        public void playSound(@Nullable PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_) {

        }

        @Nullable
        @Override
        public Entity getEntity(int p_73045_1_) {
            return null;
        }

        @Nullable
        @Override
        public MapData getMapData(String p_217406_1_) {
            return null;
        }

        @Override
        public void setMapData(MapData p_217399_1_) {

        }

        @Override
        public int getFreeMapId() {
            return 0;
        }

        @Override
        public void destroyBlockProgress(int p_175715_1_, BlockPos p_175715_2_, int p_175715_3_) {

        }

        @Override
        public Scoreboard getScoreboard() {
            return null;
        }

        @Override
        public RecipeManager getRecipeManager() {
            return null;
        }

        @Override
        public ITagCollectionSupplier getTagManager() {
            return null;
        }

        @Override
        public ITickList<Block> getBlockTicks() {
            return null;
        }

        @Override
        public ITickList<Fluid> getLiquidTicks() {
            return null;
        }

        @Override
        public AbstractChunkProvider getChunkSource() {
            return null;
        }

        @Override
        public void levelEvent(@Nullable PlayerEntity p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_) {

        }

        @Override
        public DynamicRegistries registryAccess() {
            return null;
        }

        @Override
        public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
            return 0;
        }

        @Override
        public List<? extends PlayerEntity> players() {
            return null;
        }

        @Override
        public Biome getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
            return null;
        }
    }

    private static class FakeDimensionType extends DimensionType {
        protected FakeDimensionType() {
            super(OptionalLong.empty(), true, false, false, true, 1.0D, false, false, true, false, true, 256, ColumnFuzzedBiomeMagnifier.INSTANCE, BlockTags.INFINIBURN_OVERWORLD.getName(), OVERWORLD_EFFECTS, 0.0F);
        }
    }
}