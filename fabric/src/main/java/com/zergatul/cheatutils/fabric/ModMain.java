package com.zergatul.cheatutils.fabric;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.ModLifecycle;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.core.registries.BuiltInClientRegistries;
import net.minecraft.core.Registry;

/** Runs inside the Minecraft constructor, before options and the debug profile are loaded. */
public final class ModMain implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientChunkEvents.CHUNK_LOAD.register((_, chunk) -> Events.RawChunkLoaded.trigger(chunk));
        ClientChunkEvents.CHUNK_UNLOAD.register((_, chunk) -> Events.RawChunkUnloaded.trigger(chunk));

        // Fabric has no events for these, this is early enough
        ModLifecycle.registerKeyMappings(KeyMappingHelper::registerKeyMapping);
        // Client registries freeze at the end of Minecraft construction, after the debug profile is loaded.
        DebugScreenExtensions.register((id, entry) -> Registry.register(BuiltInClientRegistries.DEBUG_SCREEN_ENTRY, id, entry));
    }
}