package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.concurrent.MainLoopEndExecutor;
import com.zergatul.cheatutils.concurrent.ProfilerSingleThreadExecutor;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.controllers.chunks.ChunkScanTaskGroup;
import com.zergatul.cheatutils.modules.esp.BlockFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BlockEventsProcessor {

    public static final BlockEventsProcessor instance = new BlockEventsProcessor();

    private static final long CHUNK_COPY_BUDGET_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    private final Minecraft mc = Minecraft.getInstance();
    private final ProfilerSingleThreadExecutor executor = new ProfilerSingleThreadExecutor(10000);

    // lock to use when accessing queue and lookup
    private final Object collectionsLock = new Object();

    // sorted by the distance from the player, descending order, so removing from the tail is very fast
    private final ChunkQueue queue = new ChunkQueue();

    private final Map<ChunkPos, ChunkScanTaskGroup> lookup = new HashMap<>();

    // Raw unload callbacks are not guaranteed for every chunk. This main-thread map is
    // reconciled with ClientChunkCache after every rendered frame.
    private final Map<ChunkPos, Boolean> capturedChunks = new HashMap<>();

    private BlockEventsProcessor() {
        Events.RawChunkLoaded.add(this::onChunkLoaded);
        Events.RawChunkUnloaded.add(this::onChunkUnloaded);
        Events.RawBlockUpdated.add(this::onBlockUpdated);
        Events.MainLoopFrameEnd.add(this::onFrameEnd);
        Events.WorldUnload.add(this::onLevelUnload);
        Events.Close.add(this::onClose);
    }

    public ProfilerSingleThreadExecutor getExecutor() {
        return executor;
    }

    // Any thread.
    public void requestFullScan() {
        MainLoopEndExecutor.instance.execute(() -> {
            ChunkPos[] positions = getLoadedChunkPositions();
            executor.execute(() -> {
                for (ChunkPos pos : positions) {
                    getOrCreateChunkTaskGroup(pos).markForScanAll();
                }
            });
        });
    }

    // Any thread.
    public void requestScan(BlockEspConfig config) {
        MainLoopEndExecutor.instance.execute(() -> {
            ChunkPos[] positions = getLoadedChunkPositions();
            executor.execute(() -> {
                for (ChunkPos pos : positions) {
                    getOrCreateChunkTaskGroup(pos).markForScan(config);
                }
            });
        });
    }

    // Main thread.
    private void onChunkLoaded(LevelChunk chunk) {
        capturedChunks.put(chunk.getPos(), Boolean.FALSE);
        getOrCreateChunkTaskGroup(chunk.getPos()).markForScanAll();
    }

    // Main thread.
    private void onChunkUnloaded(LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        capturedChunks.remove(pos);
        executor.execute(() -> Events.SnapshotChunkUnloaded.trigger(pos));
    }

    // Main thread.
    private void onBlockUpdated(BlockUpdateEvent event) {
        getOrCreateChunkTaskGroup(event.chunk().getPos()).markBlockUpdated(event);
    }

    // Main thread.
    private void onFrameEnd() {
        if (mc.level == null || mc.player == null) {
            return;
        }
        processCapturedChunks();
        processChunkCopyQueue();
    }

    // Main thread.
    private void onLevelUnload() {
        synchronized (collectionsLock) {
            queue.forEach(ChunkScanTaskGroup::markCancelled);
            queue.clear();
            lookup.clear();
        }

        executor.execute(BlockFinder.instance::clearPositions);
        for (ChunkPos pos : capturedChunks.keySet()) {
            executor.execute(() -> Events.SnapshotChunkUnloaded.trigger(pos));
        }
        capturedChunks.clear();
    }

    private void onClose() {
        executor.shutdownNow();
    }

    // Main thread.
    private void processCapturedChunks() {
        Iterator<Map.Entry<ChunkPos, Boolean>> iterator = capturedChunks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ChunkPos, Boolean> entry = iterator.next();
            ChunkPos pos = entry.getKey();
            if (mc.level.getChunkSource().getChunkNow(pos.x, pos.z) == null) {
                iterator.remove();
                executor.execute(() -> Events.SnapshotChunkUnloaded.trigger(pos));
            }
        }
    }

    // Main thread.
    private void processChunkCopyQueue() {
        long deadline = System.nanoTime() + CHUNK_COPY_BUDGET_NANOS;

        synchronized (collectionsLock) {
            queue.sort(mc.player.getBlockX(), mc.player.getBlockZ());
        }

        while (true) {
            ChunkScanTaskGroup group;
            synchronized (collectionsLock) {
                if (queue.isEmpty()) {
                    break;
                }
                group = queue.dequeue();
                lookup.remove(group.pos);
            }

            if (!group.cancelled) {
                processTaskGroup(group);
            }
            if (System.nanoTime() >= deadline) {
                break;
            }
        }
    }

    // Main thread.
    private void processTaskGroup(ChunkScanTaskGroup group) {
        LevelChunk chunk = mc.level.getChunkSource().getChunkNow(group.pos.x, group.pos.z);
        if (chunk == null) {
            return;
        }

        SnapshotChunk snapshot = SnapshotChunk.from(chunk);
        executor.execute(() -> processSnapshot(group, snapshot));
    }

    // BlockEventsProcessor worker thread.
    private void processSnapshot(ChunkScanTaskGroup group, SnapshotChunk chunk) {
        if (group.scanAllConfigs) {
            Events.SnapshotChunkLoaded.trigger(chunk);
        }
        if (group.queuedConfigs != null) {
            for (BlockEspConfig config : group.queuedConfigs) {
                BlockFinder.instance.scanChunkForBlock(chunk, config);
            }
        }
        if (group.queuedBlockUpdates != null) {
            group.queuedBlockUpdates.forEach(Events.SnapshotBlockUpdated::trigger);
        }
    }

    // Any thread.
    private ChunkScanTaskGroup getOrCreateChunkTaskGroup(ChunkPos pos) {
        synchronized (collectionsLock) {
            ChunkScanTaskGroup group = lookup.get(pos);
            if (group == null) {
                group = new ChunkScanTaskGroup(pos);
                lookup.put(pos, group);
                queue.add(group);
            }
            return group;
        }
    }

    // Main thread.
    private ChunkPos[] getLoadedChunkPositions() {
        return ChunkController.instance.getLoadedChunks().stream()
                .map(pair -> pair.getSecond().getPos())
                .toArray(ChunkPos[]::new);
    }

    private static class ChunkQueue {

        private ChunkScanTaskGroup[] elements = new ChunkScanTaskGroup[128];
        private int size;

        public void add(ChunkScanTaskGroup element) {
            if (size >= elements.length) {
                elements = Arrays.copyOf(elements, elements.length * 2);
            }
            elements[size++] = element;
        }

        public void clear() {
            Arrays.fill(elements, 0, size, null);
            size = 0;
        }

        public void forEach(Consumer<ChunkScanTaskGroup> consumer) {
            for (int i = 0; i < size; i++) {
                consumer.accept(elements[i]);
            }
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public ChunkScanTaskGroup dequeue() {
            ChunkScanTaskGroup element = elements[--size];
            elements[size] = null;
            return element;
        }

        public void sort(int playerX, int playerZ) {
            if (isEmpty()) {
                return;
            }

            Comparator<ChunkScanTaskGroup> comparator = (g1, g2) ->
                    -Long.compare(g1.distanceSqrTo(playerX, playerZ), g2.distanceSqrTo(playerX, playerZ));

            // begin insertion sort, since in the most cases collection should be sorted from previous frame
            // in rare cases (player crossed boundary) collection will be almost sorted
            // in extremely rate cases (player teleported) collection will require full sort
            ChunkScanTaskGroup[] elements = this.elements;
            int size = this.size;
            int moves = 0;
            int movesLimit = size / 8 + 1;

            outer:
            for (int i = 1; i < size; i++) {
                ChunkScanTaskGroup element = elements[i];
                int j = i - 1;

                while (j >= 0 && comparator.compare(elements[j], element) > 0) {
                    elements[j + 1] = elements[j];
                    j--;

                    // if we detect that array is not nearly sorted we fall back to default implementation
                    // however it allocates new array for merging
                    if (++moves > movesLimit) {
                        elements[j + 1] = element;
                        Arrays.sort(elements, 0, size, comparator);
                        break outer;
                    }
                }

                elements[j + 1] = element;
            }
        }
    }
}
