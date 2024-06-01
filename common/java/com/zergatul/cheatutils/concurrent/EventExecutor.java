package com.zergatul.cheatutils.concurrent;


import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class EventExecutor extends AbstractExecutorService {

    private final ArrayBlockingQueue<Runnable> queue;
    private volatile boolean shutdown = false;

    protected EventExecutor(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @NotNull
    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return List.copyOf(queue);
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown && queue.isEmpty();
    }

    @SuppressWarnings("BusyWait")
    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
        long endTime = System.nanoTime() + unit.toNanos(timeout);
        while (!isTerminated() && System.nanoTime() < endTime) {
            Thread.sleep(10);
        }
        return isTerminated();
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (!shutdown) {
            queue.add(command);
        }
    }

    protected void processQueue() {
        Runnable task;
        while ((task = queue.poll()) != null) {
            task.run();
        }
    }
}
