package com.zergatul.cheatutils.wrappers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModApiWrapper {

    public static ForgeApi forgeEvents = new ForgeApi();

    public static final WrapperRegistry<Block> BLOCKS = new WrapperRegistry<>(ForgeRegistries.BLOCKS);
    public static final WrapperRegistry<Item> ITEMS = new WrapperRegistry<>(ForgeRegistries.ITEMS);
    public static final WrapperRegistry<EntityType<?>> ENTITY_TYPES = new WrapperRegistry<>(ForgeRegistries.ENTITY_TYPES);

    private static final List<Consumer<Connection>> onClientPlayerLoggingIn = new ArrayList<>();
    public static void addOnClientPlayerLoggingIn(Consumer<Connection> consumer) {
        onClientPlayerLoggingIn.add(consumer);
    }
    private static void triggerOnClientPlayerLoggingIn(Connection connection) {
        onClientPlayerLoggingIn.forEach(c -> c.accept(connection));
    }

    private static final List<Runnable> onClientPlayerLoggingOut = new ArrayList<>();
    public static void addOnClientPlayerLoggingOut(Runnable runnable) {
        onClientPlayerLoggingOut.add(runnable);
    }
    private static void triggerOnClientPlayerLoggingOut() {
        onClientPlayerLoggingOut.forEach(Runnable::run);
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

    private static final List<Consumer<RenderWorldLastEvent>> onRenderWorldLast = new ArrayList<>();
    public static void addOnRenderWorldLast(Consumer<RenderWorldLastEvent> consumer) {
        onRenderWorldLast.add(consumer);
    }
    public static void triggerOnRenderWorldLast(RenderWorldLastEvent event) {
        onRenderWorldLast.forEach(c -> c.accept(event));
    }

    private static final List<Consumer<PostRenderGuiEvent>> onPostRenderGui = new ArrayList<>();
    public static void addOnPostRenderGui(Consumer<PostRenderGuiEvent> consumer) {
        onPostRenderGui.add(consumer);
    }
    public static void triggerOnPostRenderGui(PostRenderGuiEvent event) {
        onPostRenderGui.forEach(c -> c.accept(event));
    }

    private static final List<Consumer<PreRenderGuiOverlayEvent>> onPreRenderGuiOverlay = new ArrayList<>();
    public static void addOnPreRenderGuiOverlay(Consumer<PreRenderGuiOverlayEvent> consumer) {
        onPreRenderGuiOverlay.add(consumer);
    }
    public static boolean triggerOnPreRenderGuiOverlay(PreRenderGuiOverlayEvent event) {
        onPreRenderGuiOverlay.forEach(c -> c.accept(event));
        return event.isCanceled();
    }

    private static final List<Consumer<MouseScrollEvent>> onMouseScroll = new ArrayList<>();
    public static void addOnMouseScroll(Consumer<MouseScrollEvent> consumer) {
        onMouseScroll.add(consumer);
    }
    public static boolean triggerOnMouseScroll(MouseScrollEvent event) {
        onMouseScroll.forEach(c -> c.accept(event));
        return event.isCanceled();
    }

    public record RenderWorldLastEvent(PoseStack matrixStack, float tickDelta, Matrix4f projectionMatrix) {

        public PoseStack getMatrixStack() {
            return matrixStack;
        }

        public float getTickDelta() {
            return tickDelta;
        }

        public Matrix4f getProjectionMatrix() {
            return projectionMatrix;
        }
    }

    public record PostRenderGuiEvent(PoseStack matrixStack, float tickDelta) {

        public PoseStack getMatrixStack() {
            return matrixStack;
        }

        public float getTickDelta() {
            return tickDelta;
        }
    }

    public static class PreRenderGuiOverlayEvent {

        private GuiOverlayType type;
        private boolean canceled;

        public PreRenderGuiOverlayEvent(GuiOverlayType type) {
            this.type = type;
        }

        public GuiOverlayType getGuiOverlayType() {
            return type;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public void cancel() {
            canceled = true;
        }
    }

    public enum GuiOverlayType {
        PLAYER_LIST
    }

    public static class MouseScrollEvent {

        private double scrollDelta;
        private boolean canceled;

        public MouseScrollEvent(double scrollDelta) {
            this.scrollDelta = scrollDelta;
        }

        public double getScrollDelta() {
            return scrollDelta;
        }

        public boolean isCanceled() {
            return canceled;
        }

        public void cancel() {
            canceled = true;
        }
    }

    public static class ForgeApi {

        @SubscribeEvent
        public void onClientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
            triggerOnClientPlayerLoggingIn(event.getConnection());
        }

        @SubscribeEvent
        public void onClientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
            triggerOnClientPlayerLoggingOut();
        }

        @SubscribeEvent
        public void onChunkLoaded(ChunkEvent.Load event) {
            if (!event.getLevel().isClientSide()) {
                return;
            }
            triggerOnChunkLoaded();
        }

        @SubscribeEvent
        public void onChunkUnloaded(ChunkEvent.Unload event) {
            if (!event.getLevel().isClientSide()) {
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
        public void onRender(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
                triggerOnRenderWorldLast(new RenderWorldLastEvent(event.getPoseStack(), event.getPartialTick(), event.getProjectionMatrix()));
            }
        }

        @SubscribeEvent
        public void render(RenderGuiEvent.Post event) {
            triggerOnPostRenderGui(new PostRenderGuiEvent(event.getPoseStack(), event.getPartialTick()));
        }

        @SubscribeEvent
        public void onPreRenderGameOverlay(RenderGuiOverlayEvent.Pre event) {
            if (event.getOverlay() == GuiOverlayManager.findOverlay(VanillaGuiOverlay.PLAYER_LIST.id())) {
                if (triggerOnPreRenderGuiOverlay(new PreRenderGuiOverlayEvent(GuiOverlayType.PLAYER_LIST))) {
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            if (triggerOnMouseScroll(new MouseScrollEvent(event.getScrollDelta()))) {
                event.setCanceled(true);
            }
        }
    }
}