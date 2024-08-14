package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.scripting.MethodDescription;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.function.Function;

@SuppressWarnings("unused")
public class GameApi {

    private static final Minecraft mc = Minecraft.getInstance();

    public DimensionApi dimension = new DimensionApi();
    public BlocksApi blocks = new BlocksApi();
    public EntitiesApi entities = new EntitiesApi();

    @MethodDescription("""
            Returns true is you are in single player world
            """)
    public boolean isSinglePlayer() {
        return mc.getSingleplayerServer() != null;
    }

    @MethodDescription("""
            Returns Minecraft version
            """)
    public String getVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    @MethodDescription("""
            Returns name of your Minecraft profile
            """)
    public String getUserName() {
        return mc.getUser().getName();
    }

    @MethodDescription("""
            Returns current game tick number
            """)
    public int getTick() {
        if (mc.level == null) {
            return 0;
        }
        return (int) mc.level.getGameTime();
    }

    @MethodDescription("""
            In ticks. Cycles from 0 to 24000.
            """)
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

        @MethodDescription("""
                Gets entity count by class name in render distance
                """)
        public int getCount(String className) {
            EntityUtils.EntityInfo info = EntityUtils.getEntityClass(ClassRemapper.toObf(className));
            if (info == null) {
                return Integer.MIN_VALUE;
            }

            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                return 0;
            }

            int count = 0;
            for (Entity entity: level.entitiesForRendering()) {
                if (info.clazz.isAssignableFrom(entity.getClass())) {
                    count++;
                }
            }

            return count;
        }

        @MethodDescription("""
                Gets entity count by Minecraft id in render distance
                """)
        public int getCountById(String id) {
            ResourceLocation location = ResourceLocation.parse(id);
            EntityType<?> type = Registries.ENTITY_TYPES.getValue(location);
            if (type == null) {
                return Integer.MIN_VALUE;
            }

            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                return 0;
            }

            int count = 0;
            for (Entity entity: level.entitiesForRendering()) {
                if (entity.getType() == type) {
                    count++;
                }
            }

            return count;
        }

        @MethodDescription("""
                Returns integer entity id
                """)
        public int findClosestEntityById(String id) {
            if (mc.level == null || mc.player == null) {
                return Integer.MIN_VALUE;
            }

            EntityType<?> type = Registries.ENTITY_TYPES.getValue(ResourceLocation.parse(id));
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

        @MethodDescription("""
                Returns integer entity id
                """)
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

        @MethodDescription("""
                Returns Minecraft id of entity, or empty string is entity does not exist
                """)
        public String getType(int entityId) {
            return getStringValue(entityId, entity -> {
                EntityType<?> type = entity.getType();
                return Registries.ENTITY_TYPES.getKey(type).toString();
            });
        }

        public boolean hasCustomName(int entityId) {
            return getBooleanValue(entityId, Entity::hasCustomName);
        }

        @MethodDescription("""
                Gets display name of an entity
                """)
        public String getDisplayName(int entityId) {
            return getStringValue(entityId, entity -> {
                Component name = entity.getDisplayName();
                if (name == null) {
                    return "";
                } else {
                    return name.getString();
                }
            });
        }

        @MethodDescription("""
                Gets name of an entity
                """)
        public String getName(int entityId) {
            return getStringValue(entityId, entity -> {
                Component name = entity.getName();
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

        public boolean isBaby(int entityId) {
            return getBooleanValue(entityId, entity -> {
                if (entity instanceof LivingEntity living) {
                    return living.isBaby();
                } else {
                    return false;
                }
            });
        }

        public String getEquippedHeadItemId(int entityId) {
            return getStringValue(entityId, getEquippedItemId(EquipmentSlot.HEAD));
        }

        public String getEquippedChestItemId(int entityId) {
            return getStringValue(entityId, getEquippedItemId(EquipmentSlot.CHEST));
        }

        public String getEquippedLegsItemId(int entityId) {
            return getStringValue(entityId, getEquippedItemId(EquipmentSlot.LEGS));
        }

        public String getEquippedFeetItemId(int entityId) {
            return getStringValue(entityId, getEquippedItemId(EquipmentSlot.FEET));
        }

        public String getEquippedMainHandItemId(int entityId) {
            return getStringValue(entityId, getEquippedItemId(EquipmentSlot.MAINHAND));
        }

        public String getEquippedOffHandItemId(int entityId) {
            return getStringValue(entityId, getEquippedItemId(EquipmentSlot.OFFHAND));
        }

        public int getHealth(int entityId) {
            return getIntegerValue(entityId, entity -> {
                if (entity instanceof LivingEntity living) {
                    return (int) living.getHealth();
                } else {
                    return Integer.MIN_VALUE;
                }
            });
        }

        private Function<Entity, String> getEquippedItemId(EquipmentSlot slot) {
            return entity -> {
                if (entity instanceof LivingEntity living) {
                    ItemStack stack = living.getItemBySlot(slot);
                    if (stack.isEmpty()) {
                        return "";
                    }
                    return Registries.ITEMS.getKey(stack.getItem()).toString();
                } else {
                    return "";
                }
            };
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

        private int getIntegerValue(int entityId, Function<Entity, Integer> getter) {
            if (mc.level == null) {
                return Integer.MIN_VALUE;
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return Integer.MIN_VALUE;
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

        @MethodDescription("""
                Returns block id at specified coordinates. Example return value: "minecraft:stone"
                """)
        public String getId(int x, int y, int z) {
            if (mc.level == null) {
                return "";
            }

            Block block = mc.level.getBlockState(new BlockPos(x, y, z)).getBlock();
            return Registries.BLOCKS.getKey(block).toString();
        }

        @MethodDescription("""
                Returns true if you can place another block at specified coordinates
                """)
        public boolean canBeReplaced(int x, int y, int z) {
            if (mc.level == null) {
                return false;
            }

            return mc.level.getBlockState(new BlockPos(x, y, z)).canBeReplaced();
        }

        @MethodDescription("""
                Returns true if block is water or lava source
                """)
        public boolean isFluidSource(int x, int y, int z) {
            if (mc.level == null) {
                return false;
            }

            return mc.level.getBlockState(new BlockPos(x, y, z)).getFluidState().isSource();
        }

        @MethodDescription("""
                Returns integer tag of BlockState at specified coordinates.
                For example you can check tag "age" for "minecraft:wheat" to see when crops are ready to be harvested.
                """)
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