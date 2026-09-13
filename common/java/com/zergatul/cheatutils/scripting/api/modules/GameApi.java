package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.common.RegistryExtensions;
import com.zergatul.cheatutils.scripting.types.ItemStackWrapper;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.utils.EntityUtils;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class GameApi {

    private final Minecraft mc = Minecraft.getInstance();

    public final DimensionApi dimension = new DimensionApi();
    public final BlocksApi blocks = new BlocksApi();
    public final EntitiesApi entities = new EntitiesApi();

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

    @MethodDescription("In ticks. Cycles from 0 to 24000.")
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

    public static class BlocksApi {

        private static final Minecraft mc = Minecraft.getInstance();

        public String getId(int x, int y, int z) {
            if (mc.level == null) {
                return "";
            }
            Block block = mc.level.getBlockState(new BlockPos(x, y, z)).getBlock();
            return Registries.BLOCKS.getKey(block).toString();
        }

        public boolean canBeReplaced(int x, int y, int z) {
            return mc.level != null && mc.level.getBlockState(new BlockPos(x, y, z)).canBeReplaced();
        }

        public boolean isFluidSource(int x, int y, int z) {
            return mc.level != null && mc.level.getBlockState(new BlockPos(x, y, z)).getFluidState().isSource();
        }
    }

    public static class EntitiesApi {

        private final Minecraft mc = Minecraft.getInstance();

        @MethodDescription("""
                Gets entity count by class name in render distance
                """)
        public int getCount(String className) {
            EntityUtils.EntityInfo info = EntityUtils.getEntityClass(className);
            if (info == null) {
                return Integer.MIN_VALUE;
            }

            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                return 0;
            }

            int count = 0;
            for (Entity entity : level.entitiesForRendering()) {
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
            EntityType<?> type = RegistryExtensions.safeParse(BuiltInRegistries.ENTITY_TYPE, id);
            if (type == null) {
                return Integer.MIN_VALUE;
            }

            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) {
                return 0;
            }

            int count = 0;
            for (Entity entity : level.entitiesForRendering()) {
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

            EntityType<?> type = RegistryExtensions.safeParse(BuiltInRegistries.ENTITY_TYPE, id);
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

            EntityUtils.EntityInfo info = EntityUtils.getEntityClass(className);
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

        @MethodDescription("Entity pitch in degrees.")
        public double getXRot(int entityId) {
            return getDoubleValue(entityId, entity -> (double) entity.getXRot());
        }

        @MethodDescription("Entity yaw in degrees.")
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
                return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
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
            EntityUtils.EntityInfo info = EntityUtils.getEntityClass(className);
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

        @MethodDescription("Horse movement speed in blocks per second, or NaN when unavailable.")
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

        @MethodDescription("Horse jump height in blocks, or NaN when unavailable.")
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

        public boolean isPassenger(int entityId) {
            return getBooleanValue(entityId, Entity::isPassenger);
        }

        public int getVehicle(int entityId) {
            return getIntegerValue(entityId, entity -> {
                Entity vehicle = entity.getVehicle();
                return vehicle != null ? vehicle.getId() : Integer.MIN_VALUE;
            });
        }

        public int[] getPassengers(int entityId) {
            return getIntegerArrayValue(entityId, entity -> entity.getPassengers().stream().mapToInt(Entity::getId).toArray());
        }

        public ItemStackWrapper getEquippedHeadItem(int entityId) {
            return getValue(entityId, getEquippedItem(EquipmentSlot.HEAD), () -> new ItemStackWrapper(ItemStack.EMPTY));
        }

        public ItemStackWrapper getEquippedChestItem(int entityId) {
            return getValue(entityId, getEquippedItem(EquipmentSlot.CHEST), () -> new ItemStackWrapper(ItemStack.EMPTY));
        }

        public ItemStackWrapper getEquippedLegsItem(int entityId) {
            return getValue(entityId, getEquippedItem(EquipmentSlot.LEGS), () -> new ItemStackWrapper(ItemStack.EMPTY));
        }

        public ItemStackWrapper getEquippedFeetItem(int entityId) {
            return getValue(entityId, getEquippedItem(EquipmentSlot.FEET), () -> new ItemStackWrapper(ItemStack.EMPTY));
        }

        public ItemStackWrapper getEquippedMainHandItem(int entityId) {
            return getValue(entityId, getEquippedItem(EquipmentSlot.MAINHAND), () -> new ItemStackWrapper(ItemStack.EMPTY));
        }

        public ItemStackWrapper getEquippedOffHandItem(int entityId) {
            return getValue(entityId, getEquippedItem(EquipmentSlot.OFFHAND), () -> new ItemStackWrapper(ItemStack.EMPTY));
        }

        public boolean isUsingItemWithMainHand(int entityId) {
            return getBooleanValue(entityId, entity -> {
                if (entity instanceof LivingEntity living) {
                    return living.isUsingItem() && living.getUsedItemHand() == InteractionHand.MAIN_HAND;
                } else {
                    return false;
                }
            });
        }

        public boolean isUsingItemWithOffHand(int entityId) {
            return getBooleanValue(entityId, entity -> {
                if (entity instanceof LivingEntity living) {
                    return living.isUsingItem() && living.getUsedItemHand() == InteractionHand.OFF_HAND;
                } else {
                    return false;
                }
            });
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

        @MethodDescription("""
                Returns ItemStack if entity is ItemEntity
                """)
        public ItemStackWrapper getItemStack(int entityId) {
            return getValue(entityId, (entity, factory) -> {
                if (entity instanceof ItemEntity itemEntity) {
                    return new ItemStackWrapper(itemEntity.getItem());
                } else {
                    return factory.get();
                }
            }, () -> new ItemStackWrapper(ItemStack.EMPTY));
        }

        @MethodDescription("""
                Returns all entity ids for specified entity type id.
                Entities are sorted by the distance from the player eyes.
                """)
        public int[] enumerateById(String id) {
            if (mc.level == null || mc.player == null) {
                return new int[0];
            }

            EntityType<?> type = RegistryExtensions.safeParse(BuiltInRegistries.ENTITY_TYPE, id);
            if (type == null) {
                return new int[0];
            }

            List<Entity> entities = new ArrayList<>();
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity == mc.player) {
                    continue;
                }
                if (entity.getType() == type) {
                    entities.add(entity);
                }
            }

            Vec3 eye = mc.player.getEyePosition();
            entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(eye)));

            return entities.stream().mapToInt(Entity::getId).toArray();
        }

        @MethodDescription("""
                Returns all entity ids for specified class name.
                Entities are sorted by the distance from the player eyes.
                """)
        public int[] enumerateByClass(String className) {
            if (mc.level == null || mc.player == null) {
                return new int[0];
            }

            EntityUtils.EntityInfo info = EntityUtils.getEntityClass(className);
            if (info == null) {
                return new int[0];
            }

            List<Entity> entities = new ArrayList<>();
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity == mc.player) {
                    continue;
                }
                if (info.clazz.isAssignableFrom(entity.getClass())) {
                    entities.add(entity);
                }
            }

            Vec3 eye = mc.player.getEyePosition();
            entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(eye)));

            return entities.stream().mapToInt(e -> e.getId()).toArray();
        }

        private Function<Entity, ItemStackWrapper> getEquippedItem(EquipmentSlot slot) {
            return entity -> {
                if (entity instanceof LivingEntity living) {
                    ItemStack stack = living.getItemBySlot(slot);
                    return new ItemStackWrapper(stack);
                } else {
                    return new ItemStackWrapper(ItemStack.EMPTY);
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

        private int[] getIntegerArrayValue(int entityId, Function<Entity, int[]> getter) {
            if (mc.level == null) {
                return new int[0];
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return new int[0];
            }

            return getter.apply(entity);
        }

        private <T> T getValue(int entityId, Function<Entity, T> getter, Supplier<T> defaultSupplier) {
            if (mc.level == null) {
                return defaultSupplier.get();
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return defaultSupplier.get();
            }

            return getter.apply(entity);
        }

        private <T> T getValue(int entityId, BiFunction<Entity, Supplier<T>, T> getter, Supplier<T> defaultSupplier) {
            if (mc.level == null) {
                return defaultSupplier.get();
            }

            Entity entity = mc.level.getEntity(entityId);
            if (entity == null) {
                return defaultSupplier.get();
            }

            return getter.apply(entity, defaultSupplier);
        }

        private static double jumpStrengthToHeight(double s) {
            // based on cubic interpolation from minecraft wiki data
            // {0.4, 1.1093}, {0.5, 1.6248}, {0.6, 2.2216}, {0.7, 2.8933}, {0.8, 3.6339}, {0.9, 4.4379}, {1.0, 5.29997}
            return -0.964722 * s * s * s + 5.48621 * s * s + 0.808726 * s - 0.0303267;
        }
    }
}