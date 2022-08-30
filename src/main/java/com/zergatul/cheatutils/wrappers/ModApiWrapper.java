package com.zergatul.cheatutils.wrappers;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModApiWrapper {

    public static final WrapperRegistry<Block> BLOCKS = new WrapperRegistry<>(Registry.BLOCK);
    public static final WrapperRegistry<Item> ITEMS = new WrapperRegistry<>(Registry.ITEM);
    public static final WrapperRegistry<EntityType<?>> ENTITY_TYPES = new WrapperRegistry<>(Registry.ENTITY_TYPE);

    public static void setup() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            triggerOnClientTickStart();
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            triggerOnClientTickEnd();
        });
    }

    private static final List<Consumer<ClientConnection>> onClientPlayerLoggingIn = new ArrayList<>();
    public static void addOnClientPlayerLoggingIn(Consumer<ClientConnection> consumer) {
        onClientPlayerLoggingIn.add(consumer);
    }
    public static void triggerOnClientPlayerLoggingIn(ClientConnection connection) {
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
        private final MatrixStack matrixStack;
        private final float tickDelta;
        private final Matrix4f projectionMatrix;

        public RenderWorldLastEvent(MatrixStack matrixStack, float tickDelta, Matrix4f projectionMatrix) {
            this.matrixStack = matrixStack;
            this.tickDelta = tickDelta;
            this.projectionMatrix = projectionMatrix;
        }

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
}