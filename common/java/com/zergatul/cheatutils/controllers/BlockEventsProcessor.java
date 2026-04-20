package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.concurrent.MainLoopEndExecutor;
import com.zergatul.cheatutils.concurrent.ProfilerSingleThreadExecutor;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.controllers.chunks.ChunkScanTaskGroup;
import com.zergatul.cheatutils.mixins.common.accessors.ClientChunkCacheAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.ClientChunkCacheStorageAccessor;
import com.zergatul.cheatutils.modules.esp.BlockFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class BlockEventsProcessor {

    public static final BlockEventsProcessor instance = new BlockEventsProcessor();

    private static final AtomicReferenceArray<LevelChunk> EMPTY = new AtomicReferenceArray<>(0);
    private static final long CHUNK_COPY_BUDGET_NANOS = 1_000_000L;

    private final Minecraft mc = Minecraft.getInstance();
    private final ProfilerSingleThreadExecutor executor = new ProfilerSingleThreadExecutor(10000);
    private final Object collectionsLock = new Object();
    private final List<ChunkScanTaskGroup> queue = new ArrayList<>();
    private final Map<ChunkPos, ChunkScanTaskGroup> lookup = new HashMap<>();

    // Sometimes chunk unload events don't trigger for all chunks.
    // This map tracks loaded chunks, and every tick we recheck all loaded chunks, if some chunks disappear,
    // we trigger unload event for them
    private final Map<ChunkPos, Boolean> capturedChunks = new HashMap<>();

    private BlockEventsProcessor() {
        Events.RawChunkLoaded.add(this::onChunkLoaded);
        Events.RawChunkUnloaded.add(this::onChunkUnloaded);
        Events.RawBlockUpdated.add(this::onBlockUpdated);
        Events.MainLoopFrameEnd.add(this::onFrameEnd);
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

    public void requestFullScan() {
        MainLoopEndExecutor.instance.execute(() -> {
            ChunkPos[] positions = getLoadedChunksPosition();
            executor.execute(() -> {
                for (ChunkPos pos : positions) {
                    ChunkScanTaskGroup group = getOrCreateChunkTaskGroup(pos);
                    group.markForScanAll();
                }
            });
        });
    }

    public void requestScan(BlockEspConfig config) {
        MainLoopEndExecutor.instance.execute(() -> {
            ChunkPos[] positions = getLoadedChunksPosition();
            executor.execute(() -> {
                for (ChunkPos pos : positions) {
                    ChunkScanTaskGroup group = getOrCreateChunkTaskGroup(pos);
                    group.markForScan(config);
                }
            });
        });
    }

    private void onChunkLoaded(LevelChunk chunk) {
        capturedChunks.put(chunk.getPos(), Boolean.FALSE);

        synchronized (collectionsLock) {
            ChunkScanTaskGroup group = getOrCreateChunkTaskGroup(chunk.getPos());
            group.markForScanAll();
        }
    }

    private void onChunkUnloaded(LevelChunk chunk) {
        capturedChunks.remove(chunk.getPos());

        // no need to update queue, since task group for uploaded chunk will not do anything
        ChunkPos pos = chunk.getPos();
        executor.execute(() -> Events.ChunkUnloaded.trigger(pos));
    }

    public void onBlockUpdated(final BlockUpdateEvent event) {
        synchronized (collectionsLock) {
            ChunkScanTaskGroup group = getOrCreateChunkTaskGroup(event.chunk().getPos());
            group.markBlockUpdated(event);
        }
    }

    private void onFrameEnd() {
        if (mc.level == null || mc.player == null) {
            return;
        }

        processCapturedChunks();
        processChunkCopyQueue();
    }

    private void onLevelUnload() {
        synchronized (collectionsLock) {
            this.queue.forEach(ChunkScanTaskGroup::markCancelled);
            this.queue.clear();
            this.lookup.clear();
        }

        // this will remove any stale blocks that may end up here due to race condition
        // when Snapshot chunk gets created and sent to executor right before level unload occurs
        executor.execute(BlockFinder.instance::clearPositions);

        for (ChunkPos pos : capturedChunks.keySet()) {
            executor.execute(() -> Events.ChunkUnloaded.trigger(pos));
        }
        capturedChunks.clear();
    }

    private void processCapturedChunks() {
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
                ChunkPos pos = entry.getKey();
                iterator.remove();
                emitChunkUnloaded(pos);
            }
        }
    }

    private void processChunkCopyQueue() {
        assert mc.player != null;

        long deadline = System.nanoTime() + CHUNK_COPY_BUDGET_NANOS;

        // sort chunks based on distance from the player
        // should be O(n) since in most cases chunks are already sorted from previous frame
        int playerX = mc.player.getBlockX();
        int playerZ = mc.player.getBlockZ();

        // TODO: default sort allocates new array? check if can be done without reallocation
        synchronized (collectionsLock) {
            if (this.queue.isEmpty()) {
                return;
            }

            this.queue.sort((g1, g2) -> {
                long d1 = g1.distanceSqrTo(playerX, playerZ);
                long d2 = g2.distanceSqrTo(playerX, playerZ);
                return Long.compare(d1, d2);
            });
        }

        while (true) {
            ChunkScanTaskGroup group;

            synchronized (collectionsLock) {
                if (this.queue.isEmpty()) {
                    break;
                }

                // TODO: not optimal, think for better structure?
                group = this.queue.removeFirst();
                this.lookup.remove(group.pos);
            }

            if (group.cancelled) {
                continue;
            }

            processTaskGroup(group);

            if (System.nanoTime() >= deadline) {
                break;
            }
        }
    }

    private void processTaskGroup(ChunkScanTaskGroup group) {
        assert mc.level != null;

        // at this time group is no longer in our collections
        // thus it can't be modified from another threads
        LevelChunk chunk = mc.level.getChunkSource().getChunkNow(group.pos.x(), group.pos.z());
        if (chunk == null) {
            // chunk is unloaded
            return;
        }

        SnapshotChunk snapshot = SnapshotChunk.from(chunk);
        executor.execute(() -> processSnapshot(group, snapshot));
    }

    private void processSnapshot(ChunkScanTaskGroup group, SnapshotChunk chunk) {
        if (group.scanAllConfigs) {
            Events.ChunkLoaded.trigger(chunk);
        }
        if (group.queuedConfigs != null) {
            for (BlockEspConfig config : group.queuedConfigs) {
                BlockFinder.instance.scanChunkForBlock(chunk, config);
            }
        }
        if (group.queuedBlockUpdates != null) {
            group.queuedBlockUpdates.forEach(Events.BlockUpdated::trigger);
        }
    }

    private ChunkScanTaskGroup getOrCreateChunkTaskGroup(ChunkPos pos) {
        synchronized (collectionsLock) {
            ChunkScanTaskGroup group = lookup.get(pos);
            if (group == null) {
                lookup.put(pos, group = new ChunkScanTaskGroup(pos));
                queue.add(group);
            }

            return group;
        }
    }

    private void emitChunkUnloaded(ChunkPos pos) {
        executor.execute(() -> Events.ChunkUnloaded.trigger(pos));
    }

    private ChunkPos[] getLoadedChunksPosition() {
        AtomicReferenceArray<LevelChunk> chunks = getRawChunks();
        int count = 0;
        for (int i = 0; i < chunks.length(); i++) {
            if (chunks.get(i) != null) {
                count++;
            }
        }

        ChunkPos[] result = new ChunkPos[count];
        for (int i = 0, j = 0; i < chunks.length(); i++) {
            LevelChunk chunk = chunks.get(i);
            if (chunk != null) {
                result[j++] = chunk.getPos();
            }
        }
        return result;
    }
}