package com.zergatul.cheatutils;

import com.zergatul.cheatutils.common.Events;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;

public class FabricEvents {
    public static void setup() {
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
            Events.RawChunkLoaded.trigger(chunk);
        });
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            Events.RawChunkUnloaded.trigger(chunk);
        });
    }
}