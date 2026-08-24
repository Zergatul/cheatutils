package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class FabricEvents {

    public static void setup() {
        WorldRenderEvents.LAST.register(context -> {
            int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            try {
                Events.RenderWorldLast.trigger(new RenderWorldLastEvent(context.matrixStack(), context.tickDelta(), context.projectionMatrix()));
            } finally {
                GL20.glUseProgram(program);
            }
        });
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            Events.RawChunkLoaded.trigger(chunk);
            Events.ChunkLoaded.trigger();
        });
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            Events.RawChunkUnloaded.trigger(chunk);
            Events.ChunkUnloaded.trigger();
        });
    }
}