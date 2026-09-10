package com.zergatul.cheatutils.mixins.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkProviderClient.class)
public interface ChunkProviderClientAccessor {

    @Accessor("loadedChunks")
    Long2ObjectMap<Chunk> getLoadedChunks_CU();
}