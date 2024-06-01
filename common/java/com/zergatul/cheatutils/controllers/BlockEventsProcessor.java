package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.concurrent.ProfilerSingleThreadExecutor;
import com.zergatul.cheatutils.mixins.common.accessors.ClientChunkCacheAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.ClientChunkCacheStorageAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class BlockEventsProcessor {

    public static final BlockEventsProcessor instance = new BlockEventsProcessor();

    private final ProfilerSingleThreadExecutor executor = new ProfilerSingleThreadExecutor(10000);

    private BlockEventsProcessor() {
        Events.RawChunkLoaded.add(this::onChunkLoaded);
        Events.RawChunkUnloaded.add(this::onChunkUnloaded);
        Events.RawBlockUpdated.add(this::onBlockUpdated);
    }

    public ProfilerSingleThreadExecutor getExecutor() {
        return executor;
    }

    public AtomicReferenceArray<LevelChunk> getRawChunks() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return new AtomicReferenceArray<>(0);
        } else {
            ClientChunkCache.Storage storage = ((ClientChunkCacheAccessor) mc.level.getChunkSource()).getStorage_CU();
            return ((ClientChunkCacheStorageAccessor) (Object) storage).getChunks_CU();
        }
    }

    public CompletableFuture<SnapshotChunk[]> getChunks() {
        return CompletableFuture.supplyAsync(() -> {
            AtomicReferenceArray<LevelChunk> chunks = getRawChunks();
            int count = 0;
            for (int i = 0; i < chunks.length(); i++) {
                if (chunks.get(i) != null) {
                    count++;
                }
            }
            SnapshotChunk[] snapshots = new SnapshotChunk[count];
            for (int i = 0, j = 0; i < chunks.length(); i++) {
                LevelChunk chunk = chunks.get(i);
                if (chunk != null) {
                    snapshots[j++] = SnapshotChunk.from(chunk);
                }
            }
            return snapshots;
        }, TickEndExecutor.instance);
    }

    private void onChunkLoaded(LevelChunk chunk) {
        final SnapshotChunk snapshot = SnapshotChunk.from(chunk);
        executor.execute(() -> Events.ChunkLoaded.trigger(snapshot));
    }

    private void onChunkUnloaded(LevelChunk chunk) {
        final ChunkPos pos = chunk.getPos();
        executor.execute(() -> Events.ChunkUnloaded.trigger(pos));
    }

    public void onBlockUpdated(final BlockUpdateEvent event) {
        executor.execute(() -> Events.BlockUpdated.trigger(event));
    }
}