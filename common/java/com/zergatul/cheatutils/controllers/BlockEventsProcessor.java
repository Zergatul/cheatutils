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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

public class BlockEventsProcessor {

    public static final BlockEventsProcessor instance = new BlockEventsProcessor();

    private static final AtomicReferenceArray<LevelChunk> EMPTY = new AtomicReferenceArray<>(0);
    private static final long CHUNK_COPY_BUDGET_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    private final Minecraft mc = Minecraft.getInstance();
    private final ProfilerSingleThreadExecutor executor = new ProfilerSingleThreadExecutor(10000);

    // lock to use when accessing queue and lookup
    private final Object collectionsLock = new Object();

    // sorted by the distance from the player, descending order, so removing from the tail is very fast
    private final ChunkQueue queue = new ChunkQueue();

    private final Map<ChunkPos, ChunkScanTaskGroup> lookup = new HashMap<>();

    // Sometimes chunk unload events don't trigger for all chunks.
    // This map tracks loaded chunks, and every tick we recheck all loaded chunks, if some chunks disappear,
    // we trigger unload event for them.
    // Accessed only from main thread.
    private final Map<ChunkPos, Boolean> capturedChunks = new HashMap<>();

    private BlockEventsProcessor() {
        Events.RawChunkLoaded.add(this::onChunkLoaded);
        Events.RawChunkUnloaded.add(this::onChunkUnloaded);
        Events.RawBlockUpdated.add(this::onBlockUpdated);
        Events.MainLoopFrameEnd.add(this::onFrameEnd);
        Events.LevelUnload.add(this::onLevelUnload);
        Events.Close.add(this::onClose);
    }

    public ProfilerSingleThreadExecutor getExecutor() {
        return executor;
    }

    // should be called from main thread only
    public AtomicReferenceArray<LevelChunk> getRawChunks() {
        if (mc.level == null) {
            return EMPTY;
        } else {
            ClientChunkCache.Storage storage = ((ClientChunkCacheAccessor) mc.level.getChunkSource()).getStorage_CU();
            return ((ClientChunkCacheStorageAccessor) (Object) storage).getChunks_CU();
        }
    }

    // any thread
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

    // any thread
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

    // main thread
    private void onChunkLoaded(LevelChunk chunk) {
        capturedChunks.put(chunk.getPos(), Boolean.FALSE);

        synchronized (collectionsLock) {
            ChunkScanTaskGroup group = getOrCreateChunkTaskGroup(chunk.getPos());
            group.markForScanAll();
        }
    }

    // main thread
    private void onChunkUnloaded(LevelChunk chunk) {
        capturedChunks.remove(chunk.getPos());

        // no need to update queue, since task group for uploaded chunk will not do anything
        ChunkPos pos = chunk.getPos();
        executor.execute(() -> Events.ChunkUnloaded.trigger(pos));
    }

    // main thread
    public void onBlockUpdated(final BlockUpdateEvent event) {
        synchronized (collectionsLock) {
            ChunkScanTaskGroup group = getOrCreateChunkTaskGroup(event.chunk().getPos());
            group.markBlockUpdated(event);
        }
    }

    // main thread
    private void onFrameEnd() {
        if (mc.level == null || mc.player == null) {
            return;
        }

        processCapturedChunks();
        processChunkCopyQueue();
    }

    // main thread
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

    // main thread
    private void onClose() {
        this.executor.shutdownNow();
    }

    // main thread
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
                executor.execute(() -> Events.ChunkUnloaded.trigger(pos));
            }
        }
    }

    // main thread
    private void processChunkCopyQueue() {
        assert mc.player != null;

        long deadline = System.nanoTime() + CHUNK_COPY_BUDGET_NANOS;

        // sort chunks based on distance from the player
        synchronized (collectionsLock) {
            this.queue.sort(mc.player.getBlockX(), mc.player.getBlockZ());
        }

        // New groups may be appended while this loop drains the queue.
        // Strict ordering is not required here; sorting is only a best-effort
        // responsiveness hint, so we avoid resorting inside the loop
        while (true) {
            ChunkScanTaskGroup group;

            synchronized (collectionsLock) {
                if (this.queue.isEmpty()) {
                    break;
                }

                group = this.queue.deque();
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

    // main thread
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

    // worker thread: BlockEventsProcessor.executor
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

    // any thread
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

    // main thread
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

    private static class ChunkQueue {

        private ChunkScanTaskGroup[] elements;
        private int size;

        public ChunkQueue() {
            this.elements = new ChunkScanTaskGroup[128];
            this.size = 0;
        }

        public void add(ChunkScanTaskGroup element) {
            if (this.size >= this.elements.length) {
                ChunkScanTaskGroup[] oldArray = this.elements;
                ChunkScanTaskGroup[] newArray = new ChunkScanTaskGroup[this.elements.length * 2];
                System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
                this.elements = newArray;
            }

            this.elements[this.size++] = element;
        }

        public void clear() {
            Arrays.fill(this.elements, 0, this.size, null);
            this.size = 0;
        }

        public void forEach(Consumer<ChunkScanTaskGroup> consumer) {
            for (int i = 0; i < this.size; i++) {
                consumer.accept(this.elements[i]);
            }
        }

        public boolean isEmpty() {
            return this.size == 0;
        }

        public ChunkScanTaskGroup deque() {
            ChunkScanTaskGroup element = this.elements[--this.size];
            this.elements[this.size] = null;
            return element;
        }

        public void sort(int playerX, int playerZ) {
            if (this.isEmpty()) {
                return;
            }

            Comparator<ChunkScanTaskGroup> comparator = (g1, g2) -> {
                long d1 = g1.distanceSqrTo(playerX, playerZ);
                long d2 = g2.distanceSqrTo(playerX, playerZ);
                return -Long.compare(d1, d2);
            };

            // begin insertion sort, since in the most cases collection should be sorted from previous frame
            // in rare cases (player crossed boundary) collection will be almost sorted
            // in extremely rate cases (player teleported) collection will require full sort
            ChunkScanTaskGroup[] elements = this.elements;
            int size = this.size;
            int moves = 0;
            int movesLimit = size / 8 + 1;

            outerLoop:
            for (int i = 1; i < size; i++) {
                ChunkScanTaskGroup element = elements[i];
                int j = i - 1;

                while (j >= 0 && comparator.compare(elements[j], element) > 0) {
                    elements[j + 1] = elements[j];
                    j--;
                    moves++;

                    // if we detect that array is not nearly sorted we fall back to default implementation
                    // however it allocates new array for merging
                    if (moves > movesLimit) {
                        elements[j + 1] = element;
                        Arrays.sort(elements, 0, size, comparator);
                        break outerLoop;
                    }
                }

                elements[j + 1] = element;
            }
        }
    }
}