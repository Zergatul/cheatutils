package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Serializes writes, flushes and cancellation so deleted profiles cannot reappear. */
public class ConfigWriterQueue implements AutoCloseable {

    public static final ConfigWriterQueue instance = new ConfigWriterQueue();

    private final ScheduledExecutorService executor;
    private final Map<File, Entry> pending;
    private boolean closed;

    public ConfigWriterQueue() {
        this.executor = Executors.newSingleThreadScheduledExecutor(ConfigWriterQueue::createExecutorThread);
        this.pending = new HashMap<>();
        Events.Close.add(this::close);
    }

    public synchronized void clear() {
        pending.values().forEach(entry -> entry.future.cancel(false));
        pending.clear();
    }

    public synchronized void flush(File file) {
        Entry entry = pending.remove(file);
        if (entry != null) {
            entry.future.cancel(false);
            entry.runnable.run();
        }
    }

    public synchronized void queue(File file, long timeout, Runnable runnable) {
        if (closed) {
            return;
        }

        cancel(file);
        Entry entry = new Entry(runnable);
        pending.put(file, entry);
        entry.future = executor.schedule(() -> save(file, entry), timeout, TimeUnit.NANOSECONDS);
    }

    public synchronized void cancel(File file) {
        Entry entry = pending.remove(file);
        if (entry != null) {
            entry.future.cancel(false);
        }
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        for (File file : pending.keySet().toArray(new File[0])) {
            flush(file);
        }
        executor.shutdown();
    }

    private synchronized void save(File file, Entry entry) {
        if (pending.get(file) == entry) {
            pending.remove(file);
            entry.runnable.run();
        }
    }

    private static Thread createExecutorThread(Runnable runnable) {
        Thread thread = new Thread(runnable, Constants.MOD_ID + " config writer");
        thread.setDaemon(true);
        return thread;
    }

    private static class Entry {

        private final Runnable runnable;
        private ScheduledFuture<?> future;

        private Entry(Runnable runnable) {
            this.runnable = runnable;
        }
    }
}
