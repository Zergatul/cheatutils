package com.zergatul.cheatutils.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class EventExecutor extends AbstractExecutorService {

    private final ArrayBlockingQueue<Runnable> queue;
    private volatile boolean shutdown;

    protected EventExecutor(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public synchronized void shutdown() {
        shutdown = true;
    }

    @NotNull
    @Override
    public synchronized List<Runnable> shutdownNow() {
        shutdown = true;
        return drainQueue();
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
    public synchronized void execute(@NotNull Runnable command) {
        if (shutdown) {
            throw new RejectedExecutionException("Executor is shut down.");
        }
        if (!queue.offer(command)) {
            throw new RejectedExecutionException("Executor queue is full.");
        }
    }

    protected void clearQueue() {
        synchronized (this) {
            drainQueue();
        }
    }

    protected void processQueue() {
        Runnable task;
        while ((task = queue.poll()) != null) {
            task.run();
        }
    }

    private List<Runnable> drainQueue() {
        List<Runnable> tasks = new ArrayList<>(queue.size());
        queue.drainTo(tasks);
        for (Runnable task : tasks) {
            if (task instanceof Future<?> future) {
                future.cancel(false);
            }
        }
        return List.copyOf(tasks);
    }
}