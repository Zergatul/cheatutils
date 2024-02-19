package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.scripting.HelpText;
import com.zergatul.cheatutils.utils.EntityUtils;
import com.zergatul.cheatutils.wrappers.ClassRemapper;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.function.Function;

public class GameApi {

    private static final Minecraft mc = Minecraft.getInstance();

    public DimensionApi dimension = new DimensionApi();
    public BlocksApi blocks = new BlocksApi();
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

    public boolean isRaining() {
        if (mc.level == null) {
            return false;
        }
        return mc.level.isRaining();
    }

    public boolean isThundering() {
        if (mc.level == null) {
            return false;
        }
        return mc.level.isThundering();
    }

    public static class DimensionApi {

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
            return getDoubleValue(entityId, Entity::getX);
        }

        public double getY(int entityId) {
            return getDoubleValue(entityId, Entity::getY);
        }

        public double getZ(int entityId) {
            return getDoubleValue(entityId, Entity::getZ);
        }

        public double getXRot(int entityId) {
            return getDoubleValue(entityId, entity -> (double) entity.getXRot());
        }

        public double getYRot(int entityId) {
            return getDoubleValue(entityId, entity -> (double) entity.getYRot());
        }

        public boolean isAlive(int entityId) {
            return getBooleanValue(entityId, Entity::isAlive);
        }

        public String getType(int entityId) {
            return getStringValue(entityId, entity -> {
                EntityType<?> type = entity.getType();
                return Registries.ENTITY_TYPES.getKey(type).toString();
            });
        }

        public boolean hasCustomName(int entityId) {
            return getBooleanValue(entityId, Entity::hasCustomName);
        }

        public String getName(int entityId) {
            return getStringValue(entityId, entity -> {
                Component name = entity.getDisplayName();
                if (name == null) {
                    return "";
                } else {
                    return name.getString();
                }
            });
        }

        public boolean isInstanceOf(int entityId, String className) {
            EntityUtils.EntityInfo info = EntityUtils.getEntityClass(ClassRemapper.toObf(className));
            if (info == null) {
                return false;
            }

            if (mc.level == null) {
                return false;
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return false;
            }

            return info.clazz.isAssignableFrom(entity.getClass());
        }

        public double getHorseMovementSpeed(int entityId) {
            return getDoubleValue(entityId, entity -> {
                if (entity instanceof LivingEntity living) {
                    AttributeInstance attribute = living.getAttribute(Attributes.MOVEMENT_SPEED);
                    return attribute != null ? attribute.getValue() * 42.16 : Double.NaN;
                } else {
                    return Double.NaN;
                }
            });
        }

        public double getHorseJumpHeight(int entityId) {
            return getDoubleValue(entityId, entity -> {
                if (entity instanceof LivingEntity living) {
                    AttributeInstance attribute = living.getAttribute(Attributes.JUMP_STRENGTH);
                    return attribute != null ? jumpStrengthToHeight(attribute.getValue()) : Double.NaN;
                } else {
                    return Double.NaN;
                }
            });
        }

        private boolean getBooleanValue(int entityId, Function<Entity, Boolean> getter) {
            if (mc.level == null) {
                return false;
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return false;
            }

            return getter.apply(entity);
        }

        private double getDoubleValue(int entityId, Function<Entity, Double> getter) {
            if (mc.level == null) {
                return Double.NaN;
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return Double.NaN;
            }

            return getter.apply(entity);
        }

        private String getStringValue(int entityId, Function<Entity, String> getter) {
            if (mc.level == null) {
                return "";
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return "";
            }

            return getter.apply(entity);
        }

        private static double jumpStrengthToHeight(double s) {
            // based on cubic interpolation from minecraft wiki data
            // {0.4, 1.1093}, {0.5, 1.6248}, {0.6, 2.2216}, {0.7, 2.8933}, {0.8, 3.6339}, {0.9, 4.4379}, {1.0, 5.29997}
            return -0.964722 * s * s * s + 5.48621 * s * s + 0.808726 * s - 0.0303267;
        }
    }

    public static class BlocksApi {

        public String getId(int x, int y, int z) {
            if (mc.level == null) {
                return "";
            }

            Block block = mc.level.getBlockState(new BlockPos(x, y, z)).getBlock();
            return Registries.BLOCKS.getKey(block).toString();
        }

        public boolean canBeReplaced(int x, int y, int z) {
            if (mc.level == null) {
                return false;
            }

            return mc.level.getBlockState(new BlockPos(x, y, z)).canBeReplaced();
        }

        public boolean isFluidSource(int x, int y, int z) {
            if (mc.level == null) {
                return false;
            }

            return mc.level.getBlockState(new BlockPos(x, y, z)).getFluidState().isSource();
        }

        public int getIntegerTag(int x, int y, int z, String tag) {
            if (mc.level == null) {
                return Integer.MIN_VALUE;
            }

            BlockState state = mc.level.getBlockState(new BlockPos(x, y, z));
            Property<?> property = state.getValues().keySet().stream()
                    .filter(p -> p.getName().equals(tag) && p.getValueClass() == Integer.class)
                    .findFirst()
                    .orElse(null);
            if (property == null) {
                return Integer.MIN_VALUE;
            }

            return (Integer) state.getValue(property);
        }
    }
}