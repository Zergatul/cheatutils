package com.zergatul.cheatutils.common;

import com.zergatul.cheatutils.common.events.*;
import com.zergatul.cheatutils.controllers.SnapshotChunk;
import com.zergatul.cheatutils.render.gl.GlStateTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class Events {
    public static final SimpleEventHandler Close = new SimpleEventHandler();
    public static final ParameterizedEventHandler<IKeyBindingRegistry> RegisterKeyBindings = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler BeforeHandleKeyBindings = new SimpleEventHandler();
    public static final SimpleEventHandler AfterHandleKeyBindings = new SimpleEventHandler();
    public static final SimpleEventHandler BeforeSendPlayerPos = new SimpleEventHandler();
    public static final SimpleEventHandler AfterSendPlayerPos = new SimpleEventHandler();
    public static final ParameterizedEventHandler<Connection> ClientPlayerLoggingIn = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler ClientPlayerLoggingOut = new SimpleEventHandler();
    public static final SimpleEventHandler ChunkLoaded = new SimpleEventHandler();
    public static final SimpleEventHandler ChunkUnloaded = new SimpleEventHandler();
    public static final ParameterizedEventHandler<LevelChunk> RawChunkLoaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<LevelChunk> RawChunkUnloaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<BlockUpdateEvent> RawBlockUpdated = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<SnapshotChunk> SnapshotChunkLoaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<ChunkPos> SnapshotChunkUnloaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<BlockUpdateEvent> SnapshotBlockUpdated = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<LevelChunk> SmartChunkLoaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<LevelChunk> SmartChunkUnloaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<BlockUpdateEvent> BlockUpdated = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler ClientTickStart = new SimpleEventHandler();
    public static final SimpleEventHandler InGameTickStart = new SimpleEventHandler();
    public static final SimpleEventHandler ClientTickEnd = new SimpleEventHandler();
    public static final SimpleEventHandler InGameTickEnd = new SimpleEventHandler();
    public static final ParameterizedEventHandler<RenderWorldLastEvent> RenderWorldLastRaw = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<RenderWorldLastEvent> RenderWorldLast = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<PreRenderGuiOverlayEvent> PreRenderGuiOverlay = new CancelableEventHandler<>();
    public static final ParameterizedEventHandler<RenderGuiEvent> PreRenderGui = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<RenderGuiEvent> PostRenderGui = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<MouseScrollEvent> MouseScroll = new CancelableEventHandler<>();
    public static final SimpleEventHandler RenderTickStart = new SimpleEventHandler();
    public static final SimpleEventHandler MainLoopFrameEnd = new SimpleEventHandler();
    public static final SimpleEventHandler WorldUnload = new SimpleEventHandler();
    public static final SimpleEventHandler DimensionChange = new SimpleEventHandler();
    public static final ParameterizedEventHandler<GatherTooltipComponentsEvent> GatherTooltipComponents = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<AbstractClientPlayer> PlayerAdded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<GetFieldOfViewEvent> GetFieldOfView = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<PreRenderTooltipEvent> PreRenderTooltip = new CancelableEventHandler<>();
    public static final ParameterizedEventHandler<ContainerRenderLabelsEvent> ContainerRenderLabels = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<SetupFogEvent> SetupFog = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<SendChatEvent> SendChat = new CancelableEventHandler<>();
    public static final ParameterizedEventHandler<Entity> EntityInteract = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<BlockPos> BeforeInstaMine = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<PlayerReleaseUsingItemEvent> PlayerReleaseUsingItem = new CancelableEventHandler<>();

    static {
        ClientTickStart.add(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null && minecraft.level != null && minecraft.player != null) {
                InGameTickStart.trigger();
            }
        });
        ClientTickEnd.add(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null && minecraft.level != null && minecraft.player != null) {
                InGameTickEnd.trigger();
            }
        });
        RenderWorldLastRaw.add(event -> {
            GlStateTracker.save();
            try {
                RenderWorldLast.trigger(event);
            } finally {
                GlStateTracker.restore();
            }
        });
    }
}