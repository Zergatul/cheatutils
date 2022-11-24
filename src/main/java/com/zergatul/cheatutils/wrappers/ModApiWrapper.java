package com.zergatul.cheatutils.wrappers;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.network.ClientConnection;
import net.minecraft.util.registry.Registry;


public class ModApiWrapper {

    public static final WrapperRegistry<Block> BLOCKS = new WrapperRegistry<>(Registry.BLOCK);
    public static final WrapperRegistry<Item> ITEMS = new WrapperRegistry<>(Registry.ITEM);
    public static final WrapperRegistry<EntityType<?>> ENTITY_TYPES = new WrapperRegistry<>(Registry.ENTITY_TYPE);

    public static final ParameterizedEventHandler<IKeyBindingRegistry> RegisterKeyBindings = new ParameterizedEventHandler<>();
    public static final ParameterizedEventHandler<ClientConnection> ClientPlayerLoggingIn = new ParameterizedEventHandler<>();
    public static final SimpleEventHandler ClientPlayerLoggingOut = new SimpleEventHandler();
    public static final SimpleEventHandler ChunkLoaded = new SimpleEventHandler();
    public static final SimpleEventHandler ChunkUnloaded = new SimpleEventHandler();
    public static final SimpleEventHandler ClientTickStart = new SimpleEventHandler();
    public static final SimpleEventHandler ClientTickEnd = new SimpleEventHandler();
    public static final ParameterizedEventHandler<com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent> RenderWorldLast = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<com.zergatul.cheatutils.wrappers.events.PreRenderGuiOverlayEvent> PreRenderGuiOverlay = new CancelableEventHandler<>();
    public static final ParameterizedEventHandler<com.zergatul.cheatutils.wrappers.events.PostRenderGuiEvent> PostRenderGui = new ParameterizedEventHandler<>();
    public static final CancelableEventHandler<com.zergatul.cheatutils.wrappers.events.MouseScrollEvent> MouseScroll = new CancelableEventHandler<>();
    public static final SimpleEventHandler RenderTickStart = new SimpleEventHandler();
    public static final SimpleEventHandler WorldUnload = new SimpleEventHandler();

    public static void setup() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientTickStart.trigger());
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientTickEnd.trigger());
    }
}