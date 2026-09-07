package com.zergatul.cheatutils.configs;

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

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "cheatutils config writer");
        thread.setDaemon(true);
        return thread;
    });
    private final Map<File, Entry> pending = new HashMap<>();
    private boolean closed;

    public synchronized void queue(File file, long timeout, Runnable runnable) {
        if (closed) {
            return;
        }
        cancel(file);
        Entry entry = new Entry(runnable);
        pending.put(file, entry);
        entry.future = executor.schedule(() -> save(file, entry), timeout, TimeUnit.NANOSECONDS);
    }

    private synchronized void save(File file, Entry entry) {
        if (pending.get(file) == entry) {
            pending.remove(file);
            entry.runnable.run();
        }
    }

    public synchronized void flush(File file) {
        Entry entry = pending.remove(file);
        if (entry != null) {
            entry.future.cancel(false);
            entry.runnable.run();
        }
    }

    public synchronized void cancel(File file) {
        Entry entry = pending.remove(file);
        if (entry != null) {
            entry.future.cancel(false);
        }
    }

    public synchronized void clear() {
        pending.values().forEach(entry -> entry.future.cancel(false));
        pending.clear();
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

    private static class Entry {
        final Runnable runnable;
        ScheduledFuture<?> future;

        Entry(Runnable runnable) {
            this.runnable = runnable;
        }
    }
}
