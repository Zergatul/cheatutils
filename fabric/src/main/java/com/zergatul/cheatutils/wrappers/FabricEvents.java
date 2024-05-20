package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class FabricEvents {

    public static void setup() {
        WorldRenderEvents.LAST.register(context -> {
            Events.RenderWorldLast.trigger(new RenderWorldLastEvent(context.matrixStack(), context.tickDelta(), context.projectionMatrix()));
        });
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            Events.ChunkLoaded.trigger();
        });
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            Events.ChunkUnloaded.trigger();
        });
    }
}