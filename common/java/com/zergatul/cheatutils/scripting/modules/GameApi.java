package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.scripting.HelpText;
import com.zergatul.cheatutils.utils.EntityUtils;
import com.zergatul.cheatutils.wrappers.ClassRemapper;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class GameApi {

    private final Minecraft mc = Minecraft.getInstance();

    public DimensionApi dimension = new DimensionApi();
    public EntitiesApi entities = new EntitiesApi();

    public boolean isSinglePlayer() {
        return mc.getSingleplayerServer() != null;
    }

    public String getVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    public String getUserName() {
        return mc.getUser().getName();
    }

    public int getTick() {
        if (mc.level == null) {
            return 0;
        }
        return (int) mc.level.getGameTime();
    }

    @HelpText("In ticks. Cycles from 0 to 24000.")
    public int getDayTime() {
        if (mc.level == null) {
            return 0;
        }
        return (int) (mc.level.getDayTime() % 24000);
    }

    public static class DimensionApi {

        private static final Minecraft mc = Minecraft.getInstance();

        public boolean isOverworld() {
            if (mc.level == null) {
                return false;
            }
            return mc.level.dimension() == Level.OVERWORLD;
        }

        public boolean isNether() {
            if (mc.level == null) {
                return false;
            }
            return mc.level.dimension() == Level.NETHER;
        }

        public boolean isEnd() {
            if (mc.level == null) {
                return false;
            }
            return mc.level.dimension() == Level.END;
        }
    }

    public static class EntitiesApi {

        private final Minecraft mc = Minecraft.getInstance();

        @HelpText("Returns integer entity id")
        public int findClosestEntityById(String id) {
            if (mc.level == null || mc.player == null) {
                return Integer.MIN_VALUE;
            }

            EntityType<?> type = Registries.ENTITY_TYPES.getValue(new ResourceLocation(id));
            if (type == null) {
                return Integer.MIN_VALUE;
            }

            Entity target = null;
            double min = Double.MAX_VALUE;
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity == mc.player) {
                    continue;
                }
                if (entity.getType() == type) {
                    double dist = mc.player.distanceToSqr(entity);
                    if (dist < min) {
                        min = dist;
                        target = entity;
                    }
                }
            }

            return target == null ? Integer.MIN_VALUE : target.getId();
        }

        @HelpText("Returns integer entity id")
        public int findClosestEntityByClass(String className) {
            if (mc.level == null || mc.player == null) {
                return Integer.MIN_VALUE;
            }

            EntityUtils.EntityInfo info = EntityUtils.getEntityClass(ClassRemapper.toObf(className));
            if (info == null) {
                return Integer.MIN_VALUE;
            }

            Entity target = null;
            double min = Double.MAX_VALUE;
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity == mc.player) {
                    continue;
                }
                if (info.clazz.isAssignableFrom(entity.getClass())) {
                    double dist = mc.player.distanceToSqr(entity);
                    if (dist < min) {
                        min = dist;
                        target = entity;
                    }
                }
            }

            return target == null ? Integer.MIN_VALUE : target.getId();
        }

        public double getX(int entityId) {
            if (mc.level == null) {
                return Double.NaN;
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return Double.NaN;
            }

            return entity.getX();
        }

        public double getY(int entityId) {
            if (mc.level == null) {
                return Double.NaN;
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return Double.NaN;
            }

            return entity.getY();
        }

        public double getZ(int entityId) {
            if (mc.level == null) {
                return Double.NaN;
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return Double.NaN;
            }

            return entity.getZ();
        }
    }
}