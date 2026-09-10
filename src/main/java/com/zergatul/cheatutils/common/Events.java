package com.zergatul.cheatutils.common;

import com.zergatul.cheatutils.common.events.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

public class Events {
    public static final ParameterizedEventHandler<IKeyBindingRegistry> RegisterKeyBindings = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler AfterHandleKeyBindings = new SimpleEventHandler();

    public static final SimpleEventHandler ClientTickStart = new SimpleEventHandler();
    public static final SimpleEventHandler InGameTickStart = new SimpleEventHandler();
    public static final SimpleEventHandler ClientTickEnd = new SimpleEventHandler();
    public static final SimpleEventHandler InGameTickEnd = new SimpleEventHandler();

    public static final ParameterizedEventHandler<Float> RenderTickStart = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler LevelUnload = new SimpleEventHandler();
    public static final SimpleEventHandler OnBeforePick = new SimpleEventHandler();
    public static final SimpleEventHandler OnAfterPick = new SimpleEventHandler();
    public static final SimpleEventHandler BeforeRenderWorld = new SimpleEventHandler();
    public static final ParameterizedEventHandler<RenderWorldLastEvent> AfterRenderWorld = new ParameterizedEventHandler();
    public static final SimpleEventHandler BeforeRenderEntities = new SimpleEventHandler();
    public static final SimpleEventHandler AfterRenderEntities = new SimpleEventHandler();
    public static final ParameterizedEventHandler<Entity> BeforeRenderEntity = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<Entity> AfterRenderEntity = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<PlayerTurnByMouseEvent> PlayerTurnByMouse = new CancelableEventHandler<>();
    public static final CancelableEventHandler<SimpleCancellableEvent> RenderHand = new CancelableEventHandler<>();
    public static final SimpleEventHandler ConfigLoaded = new SimpleEventHandler();
    public static final SimpleEventHandler Close = new SimpleEventHandler();

    static {
        ClientTickStart.add(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.world != null && mc.player != null) {
                InGameTickStart.trigger();
            }
        });
        ClientTickEnd.add(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.world != null && mc.player != null) {
                InGameTickEnd.trigger();
            }
        });
    }
}