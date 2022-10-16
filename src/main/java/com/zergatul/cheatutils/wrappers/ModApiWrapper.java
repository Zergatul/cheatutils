package com.zergatul.cheatutils.wrappers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModApiWrapper {

    public static ForgeApi forgeEvents = new ForgeApi();

    public static final WrapperRegistry<Block> BLOCKS = new WrapperRegistry<>(ForgeRegistries.BLOCKS);
    public static final WrapperRegistry<Item> ITEMS = new WrapperRegistry<>(ForgeRegistries.ITEMS);
    public static final WrapperRegistry<EntityType<?>> ENTITY_TYPES = new WrapperRegistry<>(ForgeRegistries.ENTITIES);

    private static final List<Consumer<NetworkManager>> onClientPlayerLoggingIn = new ArrayList<>();
    public static void addOnClientPlayerLoggingIn(Consumer<NetworkManager> consumer) {
        onClientPlayerLoggingIn.add(consumer);
    }
    private static void triggerOnClientPlayerLoggingIn(NetworkManager connection) {
        onClientPlayerLoggingIn.forEach(c -> c.accept(connection));
    }

    private static final List<Runnable> onChunkLoaded = new ArrayList<>();
    public static void addOnChunkLoaded(Runnable runnable) {
        onChunkLoaded.add(runnable);
    }
    public static void triggerOnChunkLoaded() {
        onChunkLoaded.forEach(Runnable::run);
    }

    private static final List<Runnable> onChunkUnloaded = new ArrayList<>();
    public static void addOnChunkUnloaded(Runnable runnable) {
        onChunkUnloaded.add(runnable);
    }
    public static void triggerOnChunkUnloaded() {
        onChunkUnloaded.forEach(Runnable::run);
    }

    private static final List<Runnable> onClientTickStart = new ArrayList<>();
    public static void addOnClientTickStart(Runnable runnable) {
        onClientTickStart.add(runnable);
    }
    public static void triggerOnClientTickStart() {
        onClientTickStart.forEach(Runnable::run);
    }

    private static final List<Runnable> onClientTickEnd = new ArrayList<>();
    public static void addOnClientTickEnd(Runnable runnable) {
        onClientTickEnd.add(runnable);
    }
    public static void triggerOnClientTickEnd() {
        onClientTickEnd.forEach(Runnable::run);
    }

    private static final List<Consumer<IKeyBindingRegistry>> onRegisterKeyBindings = new ArrayList<>();
    public static void addOnRegisterKeyBindings(Consumer<IKeyBindingRegistry> consumer) {
        onRegisterKeyBindings.add(consumer);
    }
    public static void triggerOnRegisterKeyBindings(IKeyBindingRegistry registry) {
        onRegisterKeyBindings.forEach(c -> c.accept(registry));
    }

    private static final List<Runnable> onKeyInput = new ArrayList<>();
    public static void addOnKeyInput(Runnable runnable) {
        onKeyInput.add(runnable);
    }
    public static void triggerOnKeyInput() {
        onKeyInput.forEach(Runnable::run);
    }

    private static final List<Consumer<RenderWorldLastEvent>> onRenderWorldLast = new ArrayList<>();
    public static void addOnRenderWorldLast(Consumer<RenderWorldLastEvent> consumer) {
        onRenderWorldLast.add(consumer);
    }
    public static void triggerOnRenderWorldLast(RenderWorldLastEvent event) {
        onRenderWorldLast.forEach(c -> c.accept(event));
    }

    public static class RenderWorldLastEvent {

        public RenderWorldLastEvent(MatrixStack matrixStack, float tickDelta, Matrix4f projectionMatrix) {
            this.matrixStack = matrixStack;
            this.tickDelta = tickDelta;
            this.projectionMatrix = projectionMatrix;
        }

        private MatrixStack matrixStack;
        private float tickDelta;
        private Matrix4f projectionMatrix;

        public MatrixStack getMatrixStack() {
            return matrixStack;
        }

        public float getTickDelta() {
            return tickDelta;
        }

        public Matrix4f getProjectionMatrix() {
            return projectionMatrix;
        }
    }

    public static class ForgeApi {

        @SubscribeEvent
        public void onClientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent event) {
            triggerOnClientPlayerLoggingIn(event.getNetworkManager());
        }

        @SubscribeEvent
        public void onChunkLoaded(ChunkEvent.Load event) {
            if (!event.getWorld().isClientSide()) {
                return;
            }
            triggerOnChunkLoaded();
        }

        @SubscribeEvent
        public void onChunkUnloaded(ChunkEvent.Unload event) {
            if (!event.getWorld().isClientSide()) {
                return;
            }
            triggerOnChunkUnloaded();
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                triggerOnClientTickStart();
            }
            if (event.phase == TickEvent.Phase.END) {
                triggerOnClientTickEnd();
            }
        }

        @SubscribeEvent
        public void onKeyInput(InputEvent.KeyInputEvent event) {
            triggerOnKeyInput();
        }

        @SubscribeEvent
        public void onRender(net.minecraftforge.client.event.RenderWorldLastEvent event) {
            triggerOnRenderWorldLast(new RenderWorldLastEvent(event.getMatrixStack(), event.getPartialTicks(), event.getProjectionMatrix()));
        }
    }
}