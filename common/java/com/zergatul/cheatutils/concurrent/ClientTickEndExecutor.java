package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.common.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientTickEndExecutor extends EventExecutor {

    public static final ClientTickEndExecutor instance = new ClientTickEndExecutor();

    private final List<PendingTask> tasks = new ArrayList<>();

    private ClientTickEndExecutor() {
        super(100);
        Events.ClientTickStart.add(this::onTickStart);
        Events.ClientTickEnd.add(this::onTickEnd);
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

    private static class PendingTask {
        public int ticks;
        public final CompletableFuture<Void> future;

        public PendingTask(int ticks, CompletableFuture<Void> future) {
            this.ticks = ticks;
            this.future = future;
        }
    }
}