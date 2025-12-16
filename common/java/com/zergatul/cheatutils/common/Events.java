package com.zergatul.cheatutils.common;

import com.zergatul.cheatutils.common.events.*;
import com.zergatul.cheatutils.controllers.SnapshotChunk;
import net.minecraft.client.DeltaTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Vector2ic;

public class Events {
    // to better understand sequence of events they are ordered in trigger order

    // called once during loading
    public static final ParameterizedEventHandler<IKeyBindingRegistry> RegisterKeyBindings = new ParameterizedEventHandler<>();

    public static final SimpleEventHandler ClientTickStart = new SimpleEventHandler();
    // GameRenderer.pick(1)
    public static final ParameterizedEventHandler<DeltaTracker> RenderTickStart = new ParameterizedEventHandler<>();
    // GameRenderer.pick(partial)
    public static final ParameterizedEventHandler<GetFieldOfViewEvent> GetFieldOfView = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler BeforeRenderWorld = new SimpleEventHandler();
    public static final ParameterizedEventHandler<RenderWorldLastEvent> AfterRenderWorld = new ParameterizedEventHandler<>();
    // GetFieldOfView again
    public static final ParameterizedEventHandler<RenderGuiEvent> PreRenderGui = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<RenderGuiEvent> PostRenderGui = new ParameterizedEventHandler<>();

    public static final SimpleEventHandler BeforeHandleKeyBindings = new SimpleEventHandler();
    public static final ParameterizedEventHandler<BlockPos> StartDestroyBlock = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler AfterHandleKeyBindings = new SimpleEventHandler();

    public static final SimpleEventHandler BeforeEntitiesTick = new SimpleEventHandler();

    public static final SimpleEventHandler BeforePlayerAiStep = new SimpleEventHandler();
    // player position is calculated here
    public static final SimpleEventHandler AfterPlayerAiStep = new SimpleEventHandler();

    public static final SimpleEventHandler BeforeSendPlayerPos = new SimpleEventHandler();
    // send player position to the server
    public static final SimpleEventHandler AfterSendPlayerPos = new SimpleEventHandler();

    public static final SimpleEventHandler ClientTickEnd = new SimpleEventHandler();






    public static final ParameterizedEventHandler<Connection> ClientPlayerLoggingIn = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler ClientPlayerLoggingOut = new SimpleEventHandler();
    public static final ParameterizedEventHandler<LevelChunk> RawChunkLoaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<LevelChunk> RawChunkUnloaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<BlockUpdateEvent> RawBlockUpdated = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<SnapshotChunk> ChunkLoaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<ChunkPos> ChunkUnloaded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<BlockUpdateEvent> BlockUpdated = new ParameterizedEventHandler<>();



    public static final CancelableEventHandler<PreRenderGuiOverlayEvent> PreRenderGuiOverlay = new CancelableEventHandler<>();

    public static final CancelableEventHandler<MouseScrollEvent> MouseScroll = new CancelableEventHandler<>();

    public static final SimpleEventHandler LevelUnload = new SimpleEventHandler();
    public static final SimpleEventHandler DimensionChange = new SimpleEventHandler();
    public static final ParameterizedEventHandler<GatherTooltipComponentsEvent> GatherTooltipComponents = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<Entity> EntityAdded = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<Entity> EntityRemoved = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<PreRenderTooltipEvent> PreRenderTooltip = new CancelableEventHandler<>();
    public static final ParameterizedEventHandler<Vector2ic> TooltipPositioned = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler PostRenderTooltip = new SimpleEventHandler();
    public static final ParameterizedEventHandler<ScreenRenderEvent> AfterScreenRendered = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<SendChatEvent> SendChat = new CancelableEventHandler<>();
    public static final CancelableEventHandler<BeforeAttackEvent> BeforeAttack = new CancelableEventHandler<>();
    public static final ParameterizedEventHandler<Entity> EntityInteract = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<BlockPos> BeforeInstaMine = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler WindowResize = new SimpleEventHandler();
    public static final ParameterizedEventHandler<Component> ChatMessageAdded = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler Close = new SimpleEventHandler();
    public static final ParameterizedEventHandler<ContainerClickEvent> ContainerMenuClick = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<ContainerScreenCalculateHoveredSlotEvent> ContainerCalculateHoveredSlot = new CancelableEventHandler<>();
    public static final CancelableEventHandler<PlayerReleaseUsingItemEvent> PlayerReleaseUsingItem = new CancelableEventHandler<>();
    public static final CancelableEventHandler<PlayerTurnByMouseEvent> PlayerTurnByMouse = new CancelableEventHandler<>();
    public static final ParameterizedEventHandler<PlayerInfoUpdateEvent> PlayerInfoUpdated = new ParameterizedEventHandler<>();



}