package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.RegistryExtensions;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.extensions.LivingEntityExtension;
import com.zergatul.cheatutils.mixins.common.accessors.ColorParticleOptionAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.LocalPlayerAccessor;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.types.BlockStateWrapper;
import com.zergatul.cheatutils.scripting.types.*;
import com.zergatul.cheatutils.scripting.types.nbt.CompoundTagWrapper;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.utils.EntityUtils;
import com.zergatul.cheatutils.utils.ClientTicks;
import com.zergatul.cheatutils.utils.RayCast;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class GameApi {

    private static final Minecraft mc = Minecraft.getInstance();

    public final DimensionApi dimension = new DimensionApi();
    public final BlocksApi blocks = new BlocksApi();
    public final EntitiesApi entities = new EntitiesApi();

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
        return SharedConstants.getCurrentVersion().name();
    }

    @MethodDescription("""
            Returns name of your Minecraft profile
            """)
    public String getUserName() {
        return mc.getUser().getName();
    }

    @MethodDescription("""
            Connects to specified server. Return Future with boolean result you can await.
            When true - everything was ok.
            You can invoke it while in the menu, or while connected to another server.
            """)
    @ApiVisibility(ApiType.ACTION)
    public CompletableFuture<Boolean> connect(String server) {
        if (!ServerAddress.isValidAddress(server)) {
            return CompletableFuture.completedFuture(false);
        }
        if (mc.gui.screen() instanceof ConnectScreen) {
            return CompletableFuture.completedFuture(false);
        }

        Connection connection;
        if (mc.getConnection() != null) {
            connection = mc.getConnection().getConnection();
        } else {
            connection = null;
        }

        if (connection != null) {
            connection.disconnect(Component.literal("Connection requested from script"));
            connection.setReadOnly();
            connection.handleDisconnection();
        }

        ServerAddress address = ServerAddress.parseString(server);
        ConnectScreen.startConnecting(
                mc.gui.screen() instanceof TitleScreen ? mc.gui.screen() : new TitleScreen(),
                mc,
                address,
                new ServerData("Minecraft Server", address.getHost(), ServerData.Type.OTHER),
                false,
                null);

        CompletableFuture<Boolean> result = new CompletableFuture<>();

        class ScreenCheck implements Runnable {
            @Override
            public void run() {
                boolean shouldWait = mc.gui.screen() instanceof ConnectScreen || mc.gui.screen() instanceof LevelLoadingScreen;
                if (shouldWait) {
                    ClientTickEndExecutor.instance.waitTicks(1).thenRun(this);
                } else {
                    result.complete(!(mc.gui.screen() instanceof DisconnectedScreen));
                }
            }
        }

        ClientTickEndExecutor.instance.execute(new ScreenCheck());

        return result;
    }

    @MethodDescription("""
            Returns the current world's game-time tick as seen by the client, or 0 when no world is loaded.
            The value is initialized or resynchronized by the server and advances while the client world is ticking.
            """)
    public int getTick() {
        if (mc.level == null) {
            return 0;
        }
        return (int) mc.level.getGameTime();
    }

    @MethodDescription("""
            Returns the number of client ticks since startup.
            This counter advances in menus and continues across world and server changes.
            """)
    public long getClientTickCount() {
        return ClientTicks.get();
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

    public PlayerInfoWrapper[] getOnlinePlayers() {
        ClientPacketListener listener = mc.getConnection();
        if (listener == null) {
            return new PlayerInfoWrapper[0];
        }
        return listener.getOnlinePlayers().stream().map(PlayerInfoWrapper::new).toArray(PlayerInfoWrapper[]::new);
    }

    @MethodDescription("Ray casts from the specified entity's eyes in its look direction.")
    public HitResultWrapper rayCast(int entityId, double maxRange) {
        if (mc.level == null) {
            return new HitResultWrapper();
        }

        Entity entity = mc.level.getEntity(entityId);
        if (entity == null) {
            return new HitResultWrapper();
        }

        float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        return new HitResultWrapper(LocalPlayerAccessor.pick_CU(entity, maxRange, maxRange, partialTicks));
    }

    @MethodDescription("Returns a direct ray hit, or otherwise the pickable entity closest to the ray direction within range.")
    public RayCastEntityResult rayCastClosestEntity(Position3d origin, Position3d dir, double range) {
        return new RayCastEntityResult(RayCast.findClosestEntity(origin.asVec3(), dir.asVec3(), range));
    }

    public static class DimensionApi {

        public String get() {
            if (mc.level == null) {
                return "";
            }
            return mc.level.dimension().identifier().toString();
        }

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

        @MethodDescription("""
                Returns integer entity id by its UUID
                """)
        public int findByUuid(UUIDWrapper uuid) {
            if (mc.level == null || mc.player == null) {
                return Integer.MIN_VALUE;
            }

            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity.getUUID().equals(uuid.getRaw())) {
                    return entity.getId();
                }
            }

            return Integer.MIN_VALUE;
        }

        public Position3d getPosition(int entityId) {
            return getValue(
                    entityId,
                    entity -> new Position3d(entity.getX(), entity.getY(), entity.getZ()),
                    () -> new Position3d(0, 0, 0));
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

        @MethodDescription("Checks effect particles by color, not effect data. Color format: #RRGGBB or #RRGGBBAA.")
        public boolean hasEffectByColor(int entityId, String color) {
            return getBooleanValue(entityId, entity -> {
                if (entity instanceof LivingEntityExtension living) {
                    Integer value = ColorUtils.parseColor(color);
                    if (value == null) {
                        return false;
                    }
                    for (ParticleOptions option : living.getParticles_CU()) {
                        if (option instanceof ColorParticleOptionAccessor colorOption) {
                            if ((colorOption.getColor_CU() & 0xFFFFFF) == (value & 0xFFFFFF)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            });
        }

        @MethodDescription("""
                Checks if entity emits particle with the same color as color defined for specified effect
                """)
        public boolean hasEffectById(int entityId, String effectId) {
            return getBooleanValue(entityId, entity -> {
                assert mc.level != null;

                if (entity instanceof LivingEntityExtension living) {
                    Identifier location = Identifier.tryParse(effectId);
                    if (location == null) {
                        return false;
                    }

                    MobEffect effect = BuiltInRegistries.MOB_EFFECT.getValue(location);
                    if (effect == null) {
                        return false;
                    }
                    if (effect.isInstantaneous()) {
                        return false;
                    }

                    HolderLookup<MobEffect> lookup = mc.level.holderLookup(net.minecraft.core.registries.Registries.MOB_EFFECT);
                    Optional<Holder.Reference<MobEffect>> optional = lookup.listElements().filter(e -> e.is(location)).findFirst();
                    if (optional.isPresent()) {
                        MobEffectInstance instance = new MobEffectInstance(optional.get());
                        ParticleOptions option1 = effect.createParticleOptions(instance);
                        if (option1 instanceof ColorParticleOptionAccessor colorOption1) {
                            for (ParticleOptions option2 : living.getParticles_CU()) {
                                if (option2 instanceof ColorParticleOptionAccessor colorOption2) {
                                    if ((colorOption1.getColor_CU() & 0xFFFFFF) == (colorOption2.getColor_CU() & 0xFFFFFF)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }
                return false;
            });
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

        public CompoundTagWrapper getNbt(int entityId) {
            return getValue(entityId, (entity, factory) -> {
                TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.level().registryAccess());
                entity.saveWithoutId(valueOutput);
                return new CompoundTagWrapper(valueOutput.buildResult());
            }, () -> new CompoundTagWrapper(new CompoundTag()));
        }

        @MethodDescription("Returns a numeric NBT tag from a living entity, or Integer.MIN_VALUE when unavailable.")
        public int getIntTag(int entityId, String tag) {
            return getIntegerValue(entityId, entity -> {
                if (entity instanceof LivingEntity living) {
                    TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.level().registryAccess());
                    living.saveWithoutId(valueOutput);
                    CompoundTag compound = valueOutput.buildResult();

                    Tag item = compound.get(tag);
                    if (item instanceof NumericTag numeric) {
                        return numeric.intValue();
                    } else {
                        return Integer.MIN_VALUE;
                    }
                } else {
                    return Integer.MIN_VALUE;
                }
            });
        }

        @MethodDescription("""
                Checks few simple spawn rules for mob type at specified coordinates.
                This method doesn't check:
                 - light conditions
                 - structures bounds (we do not have them on client)
                 - more complex rules like animals can spawn on grass
                """)
        public boolean canSpawnAt(String id, int x, int y, int z) {
            if (mc.level == null) {
                return false;
            }
            EntityType<?> type = RegistryExtensions.safeParse(BuiltInRegistries.ENTITY_TYPE, id);
            if (type == null) {
                return false;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (!SpawnPlacements.isSpawnPositionOk(type, mc.level, pos)) {
                return false;
            }
            return mc.level.noBlockCollision(null, type.getSpawnAABB(x + 0.5, y, z + 0.5));
        }

        @MethodDescription("""
                Returns bounding box (or hitbox in other words) for specified entity
                """)
        public BoundingBox getBoundingBox(int entityId) {
            return getValue(
                    entityId,
                    entity -> new BoundingBox(entity.getBoundingBox()),
                    () -> new BoundingBox(new AABB(0, 0, 0, 0, 0, 0)));
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

    public static class BlocksApi {

        @MethodDescription("""
                Returns BlockState at specified position
                """)
        public BlockStateWrapper get(BlockPosWrapper pos) {
            return get(pos.getX(), pos.getY(), pos.getZ());
        }

        @MethodDescription("""
                Returns BlockState at specified position
                """)
        public BlockStateWrapper get(int x, int y, int z) {
            if (mc.level == null) {
                return new BlockStateWrapper(Blocks.AIR.defaultBlockState());
            }

            return new BlockStateWrapper(mc.level.getBlockState(new BlockPos(x, y, z)));
        }

        @MethodDescription("""
                Returns block id at specified coordinates. Example return value: "minecraft:stone"
                """)
        public String getId(BlockPosWrapper pos) {
            return getId(pos.getX(), pos.getY(), pos.getZ());
        }

        @MethodDescription("""
                Returns block id at specified coordinates. Example return value: "minecraft:stone"
                """)
        public String getId(int x, int y, int z) {
            if (mc.level == null) {
                return "";
            }

            Block block = mc.level.getBlockState(new BlockPos(x, y, z)).getBlock();
            return BuiltInRegistries.BLOCK.getKey(block).toString();
        }

        @MethodDescription("""
                Returns true if you can place another block at specified coordinates
                """)
        public boolean canBeReplaced(BlockPosWrapper pos) {
            return canBeReplaced(pos.getX(), pos.getY(), pos.getZ());
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

        @MethodDescription("Returns block-emitted light level in [0, 15].")
        public int getBlockLightLevel(int x, int y, int z) {
            if (mc.level == null) {
                return Integer.MIN_VALUE;
            }
            return mc.level.getBrightness(LightLayer.BLOCK, new BlockPos(x, y, z));
        }

        @MethodDescription("Returns sky light level in [0, 15].")
        public int getSkyLightLevel(int x, int y, int z) {
            if (mc.level == null) {
                return Integer.MIN_VALUE;
            }
            return mc.level.getBrightness(LightLayer.SKY, new BlockPos(x, y, z));
        }

        @MethodDescription("Checks whether the block state has a property with the specified name.")
        public boolean hasTag(BlockPosWrapper pos, String tag) {
            return hasTag(pos.getX(), pos.getY(), pos.getZ(), tag);
        }

        @MethodDescription("Checks whether the block state has a property with the specified name.")
        public boolean hasTag(int x, int y, int z, String tag) {
            if (mc.level == null) {
                return false;
            }

            BlockState state = mc.level.getBlockState(new BlockPos(x, y, z));
            return state.getValues().anyMatch(e -> e.property().getName().equals(tag));
        }

        @MethodDescription("""
                Returns integer property of BlockState at specified coordinates.
                For example, property "age" for "minecraft:wheat" indicates when crops are ready to harvest.
                """)
        public int getIntegerTag(BlockPosWrapper pos, String tag) {
            return getIntegerTag(pos.getX(), pos.getY(), pos.getZ(), tag);
        }

        @MethodDescription("""
                Returns integer property of BlockState at specified coordinates.
                For example, property "age" for "minecraft:wheat" indicates when crops are ready to harvest.
                """)
        public int getIntegerTag(int x, int y, int z, String tag) {
            if (mc.level == null) {
                return Integer.MIN_VALUE;
            }

            BlockState state = mc.level.getBlockState(new BlockPos(x, y, z));
            Property<?> property = state.getValues()
                    .map(Property.Value::property)
                    .filter(p -> p.getName().equals(tag) && p instanceof IntegerProperty)
                    .findFirst()
                    .orElse(null);
            if (property == null) {
                return Integer.MIN_VALUE;
            }

            return (Integer) state.getValue(property);
        }

        @MethodDescription("""
                Returns enum property of BlockState at specified coordinates.
                """)
        public String getEnumTag(BlockPosWrapper pos, String tag) {
            return getEnumTag(pos.getX(), pos.getY(), pos.getZ(), tag);
        }

        @MethodDescription("""
                Returns enum property of BlockState at specified coordinates.
                """)
        public String getEnumTag(int x, int y, int z, String tag) {
            if (mc.level == null) {
                return "";
            }

            BlockState state = mc.level.getBlockState(new BlockPos(x, y, z));
            Property<?> property = state.getValues()
                    .map(Property.Value::property)
                    .filter(p -> p.getName().equals(tag) && p instanceof EnumProperty)
                    .findFirst()
                    .orElse(null);
            if (property == null) {
                return "";
            }

            return state.getValue(property).toString();
        }

        @MethodDescription("""
                Returns boolean property of BlockState at specified coordinates.
                """)
        public boolean getBooleanTag(BlockPosWrapper pos, String tag) {
            return getBooleanTag(pos.getX(), pos.getY(), pos.getZ(), tag);
        }

        @MethodDescription("""
                Returns boolean property of BlockState at specified coordinates.
                """)
        public boolean getBooleanTag(int x, int y, int z, String tag) {
            if (mc.level == null) {
                return false;
            }

            BlockState state = mc.level.getBlockState(new BlockPos(x, y, z));
            Property<?> property = state.getValues()
                    .map(Property.Value::property)
                    .filter(p -> p.getName().equals(tag) && p instanceof BooleanProperty)
                    .findFirst()
                    .orElse(null);
            if (property == null) {
                return false;
            }

            return (Boolean) state.getValue(property);
        }
    }
}