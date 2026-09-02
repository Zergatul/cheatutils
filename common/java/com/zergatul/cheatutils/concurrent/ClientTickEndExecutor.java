package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.common.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

public class ClientTickEndExecutor extends EventExecutor {

    public static final ClientTickEndExecutor instance = new ClientTickEndExecutor();

    private final List<PendingTask> tasks = new ArrayList<>();

    ClientTickEndExecutor() {
        super(100);
        Events.ClientTickStart.add(this::onTickStart);
        Events.ClientTickEnd.add(this::onTickEnd);
        Events.Close.add(this::onClose);
    }

    public CompletableFuture<Void> waitTicks(int ticks) {
        if (isShutdown()) {
            throw new RejectedExecutionException("Executor is shut down.");
        }
        CompletableFuture<Void> future = new CompletableFuture<>();
        tasks.add(new PendingTask(ticks, future));
        return future;
    }

    private void onTickStart() {
        for (int i = 0; i < tasks.size(); i++) {
            PendingTask task = tasks.get(i);
            if (task.future.isDone()) {
                tasks.remove(i--);
            } else {
                task.ticks--;
            }
        }
    }

    private void onTickEnd() {
        processQueue();

        for (int i = 0; i < tasks.size(); i++) {
            PendingTask task = tasks.get(i);
            if (task.future.isDone()) {
                tasks.remove(i--);
            } else if (task.ticks <= 0) {
                task.future.complete(null);
                tasks.remove(i--);
            }
        }
    }

    void onClose() {
        shutdownNow();
        for (PendingTask task : tasks) {
            task.future.cancel(false);
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