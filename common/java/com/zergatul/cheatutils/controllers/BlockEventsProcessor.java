package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.concurrent.ProfilerSingleThreadExecutor;
import com.zergatul.cheatutils.mixins.common.accessors.ClientChunkCacheAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.ClientChunkCacheStorageAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class BlockEventsProcessor {

    public static final BlockEventsProcessor instance = new BlockEventsProcessor();

    private static final AtomicReferenceArray<LevelChunk> EMPTY = new AtomicReferenceArray<>(0);

    private final Minecraft mc = Minecraft.getInstance();
    private final ProfilerSingleThreadExecutor executor = new ProfilerSingleThreadExecutor(10000);
    private final Map<ChunkPos, Boolean> capturedChunks = new HashMap<>();

    private BlockEventsProcessor() {
        Events.RawChunkLoaded.add(this::onChunkLoaded);
        Events.RawChunkUnloaded.add(this::onChunkUnloaded);
        Events.RawBlockUpdated.add(this::onBlockUpdated);
        Events.ClientTickEnd.add(this::onClientTickEnd);
        Events.LevelUnload.add(this::onLevelUnload);
    }

    public ProfilerSingleThreadExecutor getExecutor() {
        return executor;
    }

    public AtomicReferenceArray<LevelChunk> getRawChunks() {
        if (mc.level == null) {
            return EMPTY;
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
        capturedChunks.put(chunk.getPos(), Boolean.FALSE);
        final SnapshotChunk snapshot = SnapshotChunk.from(chunk);
        executor.execute(() -> Events.ChunkLoaded.trigger(snapshot));
    }

    private void onChunkUnloaded(LevelChunk chunk) {
        capturedChunks.remove(chunk.getPos());
        final ChunkPos pos = chunk.getPos();
        executor.execute(() -> Events.ChunkUnloaded.trigger(pos));
    }

    public void onBlockUpdated(final BlockUpdateEvent event) {
        executor.execute(() -> Events.BlockUpdated.trigger(event));
    }

    private void onClientTickEnd() {
        for (Map.Entry<ChunkPos, Boolean> entry : capturedChunks.entrySet()) {
            entry.setValue(Boolean.FALSE);
        }

        AtomicReferenceArray<LevelChunk> chunks = getRawChunks();
        for (int i = 0; i < chunks.length(); i++) {
            LevelChunk chunk = chunks.get(i);
            if (chunk != null) {
                capturedChunks.put(chunk.getPos(), Boolean.TRUE);
            }
        }

        Iterator<Map.Entry<ChunkPos, Boolean>> iterator = capturedChunks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ChunkPos, Boolean> entry = iterator.next();
            if (entry.getValue() == Boolean.FALSE) {
                iterator.remove();
                executor.execute(() -> Events.ChunkUnloaded.trigger(entry.getKey()));
            }
        }
    }

    private void onLevelUnload() {
        for (ChunkPos pos : capturedChunks.keySet()) {
            executor.execute(() -> Events.ChunkUnloaded.trigger(pos));
        }
        capturedChunks.clear();
    }
}