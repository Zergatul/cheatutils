package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.concurrent.ProfilerSingleThreadExecutor;
import com.zergatul.cheatutils.mixins.common.accessors.ClientChunkCacheAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.ClientChunkCacheStorageAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

public class BlockEventsProcessor {

    public static final BlockEventsProcessor instance = new BlockEventsProcessor();

    private static final AtomicReferenceArray<LevelChunk> EMPTY = new AtomicReferenceArray<>(0);
    private static final long CHUNK_COPY_BUDGET_NANOS = 4_000_000L;

    private final Minecraft mc = Minecraft.getInstance();
    private final ProfilerSingleThreadExecutor executor = new ProfilerSingleThreadExecutor(10000);
    private final Object chunkCopyLock = new Object();
    private final ArrayDeque<ChunkCopyTask> chunkCopyTasks = new ArrayDeque<>();
    private final List<Runnable> bufferedEvents = new ArrayList<>();
    private ChunkCopyTask activeChunkCopyTask;

    // Sometimes chunk unload events don't trigger for all chunks.
    // This map tracks loaded chunks, and every tick we recheck all loaded chunks, if some chunks disappear,
    // we trigger unload event for them
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

    public void requestChunkSnapshots(Object key, Consumer<SnapshotChunk[]> callback) {
        synchronized (chunkCopyLock) {
            Iterator<ChunkCopyTask> iterator = chunkCopyTasks.iterator();
            while (iterator.hasNext()) {
                ChunkCopyTask task = iterator.next();
                if (Objects.equals(task.key, key)) {
                    task.cancelled = true;
                    iterator.remove();
                }
            }

            if (activeChunkCopyTask != null && Objects.equals(activeChunkCopyTask.key, key)) {
                activeChunkCopyTask.cancelled = true;
            }

            chunkCopyTasks.add(new ChunkCopyTask(key, callback));
        }
    }

    private void onChunkLoaded(LevelChunk chunk) {
        capturedChunks.put(chunk.getPos(), Boolean.FALSE);
        if (hasChunkCopyWork()) {
            queueChunkLoaded(chunk);
        } else {
            final SnapshotChunk snapshot = SnapshotChunk.from(chunk);
            executor.execute(() -> Events.ChunkLoaded.trigger(snapshot));
        }
    }

    private void onChunkUnloaded(LevelChunk chunk) {
        capturedChunks.remove(chunk.getPos());
        final ChunkPos pos = chunk.getPos();
        emitChunkUnloaded(pos);
    }

    public void onBlockUpdated(final BlockUpdateEvent event) {
        emitBlockUpdated(event);
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
                ChunkPos pos = entry.getKey();
                iterator.remove();
                emitChunkUnloaded(pos);
            }
        }

        processChunkCopyQueue();
    }

    private void onLevelUnload() {
        cancelChunkCopyWork();
        for (ChunkPos pos : capturedChunks.keySet()) {
            executor.execute(() -> Events.ChunkUnloaded.trigger(pos));
        }
        capturedChunks.clear();
    }

    private void queueChunkLoaded(LevelChunk chunk) {
        synchronized (chunkCopyLock) {
            chunkCopyTasks.add(new ChunkCopyTask(new Object(), new LevelChunk[] { chunk }, snapshots -> {
                if (snapshots.length > 0) {
                    Events.ChunkLoaded.trigger(snapshots[0]);
                }
            }));
        }
    }

    private void emitChunkUnloaded(ChunkPos pos) {
        emitOrBuffer(() -> Events.ChunkUnloaded.trigger(pos));
    }

    private void emitBlockUpdated(BlockUpdateEvent event) {
        emitOrBuffer(() -> Events.BlockUpdated.trigger(event));
    }

    private void emitOrBuffer(Runnable runnable) {
        synchronized (chunkCopyLock) {
            if (activeChunkCopyTask != null || !chunkCopyTasks.isEmpty()) {
                bufferedEvents.add(runnable);
                return;
            }
        }

        executor.execute(runnable);
    }

    private boolean hasChunkCopyWork() {
        synchronized (chunkCopyLock) {
            return activeChunkCopyTask != null || !chunkCopyTasks.isEmpty();
        }
    }

    private void processChunkCopyQueue() {
        long deadline = System.nanoTime() + CHUNK_COPY_BUDGET_NANOS;
        boolean copiedAny = false;

        while (true) {
            ChunkCopyTask task = getActiveChunkCopyTask();
            if (task == null) {
                flushBufferedEventsIfIdle();
                return;
            }

            if (task.cancelled) {
                completeActiveChunkCopyTask(task);
                continue;
            }

            boolean copied = task.copyNext();
            copiedAny |= copied;

            if (task.isComplete()) {
                completeActiveChunkCopyTask(task);
                if (!task.cancelled) {
                    executor.execute(() -> task.callback.accept(task.getSnapshots()));
                }
                continue;
            }

            if (copiedAny && System.nanoTime() >= deadline) {
                return;
            }
        }
    }

    private ChunkCopyTask getActiveChunkCopyTask() {
        synchronized (chunkCopyLock) {
            while (activeChunkCopyTask == null) {
                activeChunkCopyTask = chunkCopyTasks.poll();
                if (activeChunkCopyTask == null) {
                    return null;
                }
                if (!activeChunkCopyTask.cancelled) {
                    break;
                }
                activeChunkCopyTask = null;
            }

            return activeChunkCopyTask;
        }
    }

    private void completeActiveChunkCopyTask(ChunkCopyTask task) {
        synchronized (chunkCopyLock) {
            if (activeChunkCopyTask == task) {
                activeChunkCopyTask = null;
            }
        }
    }

    private void flushBufferedEventsIfIdle() {
        List<Runnable> events;
        synchronized (chunkCopyLock) {
            if (activeChunkCopyTask != null || !chunkCopyTasks.isEmpty() || bufferedEvents.isEmpty()) {
                return;
            }

            events = new ArrayList<>(bufferedEvents);
            bufferedEvents.clear();
        }

        for (Runnable event : events) {
            executor.execute(event);
        }
    }

    private void cancelChunkCopyWork() {
        synchronized (chunkCopyLock) {
            if (activeChunkCopyTask != null) {
                activeChunkCopyTask.cancelled = true;
                activeChunkCopyTask = null;
            }
            for (ChunkCopyTask task : chunkCopyTasks) {
                task.cancelled = true;
            }
            chunkCopyTasks.clear();
            bufferedEvents.clear();
        }
    }

    private class ChunkCopyTask {

        private final Object key;
        private final Consumer<SnapshotChunk[]> callback;
        private final List<SnapshotChunk> snapshots = new ArrayList<>();
        private LevelChunk[] chunks;
        private int index;
        private volatile boolean cancelled;

        private ChunkCopyTask(Object key, Consumer<SnapshotChunk[]> callback) {
            this.key = key;
            this.callback = callback;
        }

        private ChunkCopyTask(Object key, LevelChunk[] chunks, Consumer<SnapshotChunk[]> callback) {
            this.key = key;
            this.chunks = chunks;
            this.callback = callback;
        }

        private boolean copyNext() {
            if (cancelled) {
                return false;
            }

            if (chunks == null) {
                chunks = getLoadedChunks();
            }

            if (index >= chunks.length) {
                return false;
            }

            snapshots.add(SnapshotChunk.from(chunks[index++]));
            return true;
        }

        private boolean isComplete() {
            return cancelled || chunks != null && index >= chunks.length;
        }

        private SnapshotChunk[] getSnapshots() {
            return snapshots.toArray(SnapshotChunk[]::new);
        }
    }

    private LevelChunk[] getLoadedChunks() {
        AtomicReferenceArray<LevelChunk> chunks = getRawChunks();
        int count = 0;
        for (int i = 0; i < chunks.length(); i++) {
            if (chunks.get(i) != null) {
                count++;
            }
        }

        LevelChunk[] result = new LevelChunk[count];
        for (int i = 0, j = 0; i < chunks.length(); i++) {
            LevelChunk chunk = chunks.get(i);
            if (chunk != null) {
                result[j++] = chunk;
            }
        }
        return result;
    }
}