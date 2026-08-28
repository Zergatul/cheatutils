package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.common.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// Callers should not use this executor when no level is loaded.
public class InGameTickEndExecutor extends EventExecutor {

    public static final InGameTickEndExecutor instance = new InGameTickEndExecutor();

    private final List<PendingTask> tasks = new ArrayList<>();

    private InGameTickEndExecutor() {
        super(100);
        Events.InGameTickStart.add(this::onTickStart);
        Events.InGameTickEnd.add(this::onTickEnd);
        Events.WorldUnload.add(this::onWorldUnload);
    }

    public CompletableFuture<Void> waitTicks(int ticks) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        tasks.add(new PendingTask(ticks, future));
        return future;
    }

    private void onTickStart() {
        for (PendingTask task : tasks) {
            task.ticks--;
        }
    }

    private void onTickEnd() {
        processQueue();

        for (int i = 0; i < tasks.size(); i++) {
            PendingTask task = tasks.get(i);
            if (task.ticks <= 0) {
                task.future.complete(null);
                tasks.remove(i);
                i--;
            }
        }
    }

    private void onWorldUnload() {
        clearQueue();
        for (PendingTask task : tasks) {
            task.future.completeExceptionally(new LevelUnloadedException());
        }
        tasks.clear();
    }

    private static class PendingTask {
        public int ticks;
        public final CompletableFuture<Void> future;

        public PendingTask(int ticks, CompletableFuture<Void> future) {
            this.ticks = ticks;
            this.future = future;
        }
    }
}