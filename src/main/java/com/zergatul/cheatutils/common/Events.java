package com.zergatul.cheatutils.common;

import com.zergatul.cheatutils.common.events.*;
import net.minecraft.entity.Entity;
import java.util.List;

public class Events {
    public static final ParameterizedEventHandler<IKeyBindingRegistry> RegisterKeyBindings = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler AfterHandleKeyBindings = new SimpleEventHandler();
    public static final SimpleEventHandler ClientTickStart = new SimpleEventHandler();
    public static final SimpleEventHandler ClientTickEnd = new SimpleEventHandler();
    public static final ParameterizedEventHandler<Float> RenderTickStart = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler LevelUnload = new SimpleEventHandler();
    public static final SimpleEventHandler OnBeforePick = new SimpleEventHandler();
    public static final SimpleEventHandler OnAfterPick = new SimpleEventHandler();
    public static final SimpleEventHandler BeforeRenderWorld = new SimpleEventHandler();
    public static final SimpleEventHandler AfterRenderWorld = new SimpleEventHandler();
    public static final SimpleEventHandler BeforeRenderEntities = new SimpleEventHandler();
    public static final SimpleEventHandler AfterRenderEntities = new SimpleEventHandler();
    public static final ParameterizedEventHandler<Entity> BeforeRenderEntity = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<Entity> AfterRenderEntity = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<List<String>> DebugInfoLeft = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<PlayerTurnByMouseEvent> PlayerTurnByMouse = new CancelableEventHandler<>();
    public static final CancelableEventHandler<SimpleCancellableEvent> RenderHand = new CancelableEventHandler<>();
    public static final SimpleEventHandler ConfigLoaded = new SimpleEventHandler();
    public static final SimpleEventHandler Close = new SimpleEventHandler();
}