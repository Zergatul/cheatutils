package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.wrappers.events.*;
import com.zergatul.cheatutils.wrappers.events.RenderGuiEvent;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ModApiWrapper {

    public static ForgeApi forgeEvents = new ForgeApi();

    public static final WrapperRegistry<Block> BLOCKS = new WrapperRegistry<>(ForgeRegistries.BLOCKS);
    public static final WrapperRegistry<Item> ITEMS = new WrapperRegistry<>(ForgeRegistries.ITEMS);
    public static final WrapperRegistry<EntityType<?>> ENTITY_TYPES = new WrapperRegistry<>(ForgeRegistries.ENTITY_TYPES);

    public static final ParameterizedEventHandler<IKeyBindingRegistry> RegisterKeyBindings = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler BeforeHandleKeyBindings = new SimpleEventHandler();
    public static final SimpleEventHandler AfterHandleKeyBindings = new SimpleEventHandler();
    public static final ParameterizedEventHandler<Connection> ClientPlayerLoggingIn = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler ClientPlayerLoggingOut = new SimpleEventHandler();
    public static final SimpleEventHandler ChunkLoaded = new SimpleEventHandler();
    public static final SimpleEventHandler ChunkUnloaded = new SimpleEventHandler();
    public static final ParameterizedEventHandler<LevelChunk> SmartChunkLoaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<LevelChunk> SmartChunkUnloaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<BlockUpdateEvent> BlockUpdated = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<LevelChunk> ScannerChunkLoaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<LevelChunk> ScannerChunkUnloaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<BlockUpdateEvent> ScannerBlockUpdated = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler ClientTickStart = new SimpleEventHandler();
    public static final SimpleEventHandler ClientTickEnd = new SimpleEventHandler();
    public static final ParameterizedEventHandler<RenderWorldLastEvent> RenderWorldLast = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<PreRenderGuiOverlayEvent> PreRenderGuiOverlay = new CancelableEventHandler<>();
    public static final ParameterizedEventHandler<RenderGuiEvent> PreRenderGui = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<RenderGuiEvent> PostRenderGui = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<MouseScrollEvent> MouseScroll = new CancelableEventHandler<>();
    public static final SimpleEventHandler RenderTickStart = new SimpleEventHandler();
    public static final SimpleEventHandler WorldUnload = new SimpleEventHandler();
    public static final SimpleEventHandler DimensionChange = new SimpleEventHandler();

    public static class ForgeApi {

        private static RenderWorldLastEvent renderWorldLastEvent;

        @SubscribeEvent
        public void onClientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
            ClientPlayerLoggingIn.trigger(event.getConnection());
        }

        @SubscribeEvent
        public void onClientPlayerLoggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ClientPlayerLoggingOut.trigger();
        }

        @SubscribeEvent
        public void onChunkLoaded(ChunkEvent.Load event) {
            if (!event.getLevel().isClientSide()) {
                return;
            }
            ChunkLoaded.trigger();
        }

        @SubscribeEvent
        public void onChunkUnloaded(ChunkEvent.Unload event) {
            if (!event.getLevel().isClientSide()) {
                return;
            }
            ChunkUnloaded.trigger();
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                ClientTickStart.trigger();
            }
            if (event.phase == TickEvent.Phase.END) {
                ClientTickEnd.trigger();
            }
        }

        @SubscribeEvent
        public void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
                renderWorldLastEvent = new RenderWorldLastEvent(event.getPoseStack(), event.getPartialTick(), event.getProjectionMatrix());
                RenderWorldLast.trigger(renderWorldLastEvent);
            }
        }

        @SubscribeEvent
        public void onPreRenderGui(net.minecraftforge.client.event.RenderGuiEvent.Pre event) {
            if (renderWorldLastEvent != null) {
                PreRenderGui.trigger(new RenderGuiEvent(event.getPoseStack(), renderWorldLastEvent));
            }
        }

        @SubscribeEvent
        public void onPostRenderGui(net.minecraftforge.client.event.RenderGuiEvent.Post event) {
            if (renderWorldLastEvent != null) {
                PostRenderGui.trigger(new RenderGuiEvent(event.getPoseStack(), renderWorldLastEvent));
            }
        }

        @SubscribeEvent
        public void onPreRenderGameOverlay(RenderGuiOverlayEvent.Pre event) {
            if (event.getOverlay() == GuiOverlayManager.findOverlay(VanillaGuiOverlay.PLAYER_LIST.id())) {
                if (PreRenderGuiOverlay.trigger(new PreRenderGuiOverlayEvent(PreRenderGuiOverlayEvent.GuiOverlayType.PLAYER_LIST))) {
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
            if (MouseScroll.trigger(new MouseScrollEvent(event.getScrollDelta()))) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onRenderTick(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                RenderTickStart.trigger();
            }
        }

        @SubscribeEvent
        public void onWorldUnload(LevelEvent.Unload event) {
            if (event.getLevel().isClientSide()) {
                WorldUnload.trigger();
            }
        }
    }
}