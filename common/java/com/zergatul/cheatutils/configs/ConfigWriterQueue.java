package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.Constants;

import java.io.File;
import java.util.Comparator;
import java.util.PriorityQueue;

public class ConfigWriterQueue {

    public static final ConfigWriterQueue instance = new ConfigWriterQueue();

    private static final long WAIT_INDEFINITELY = 0;
    private static final long MIN_WAIT_TIMEOUT = 15;
    private static final long MAX_WAIT_TIMEOUT = 1000;

    private final PriorityQueue<Entry> queue = new PriorityQueue<>();
    private final Object lock = new Object();
    private final Thread thread;

    private ConfigWriterQueue() {
        thread = new Thread(this::threadFunc, Constants.MOD_ID + " config writer");
        thread.start();
    }

    public void clear() {
        synchronized (lock) {
            queue.clear();
        }
    }

    public void flush(File file) {
        Entry entry;
        synchronized (lock) {
            entry = queue.stream()
                    .filter(e -> e.file.equals(file))
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            if (entry == null) {
                return;
            }

            queue.removeIf(e -> e.file.equals(file));
        }

        entry.runnable.run();
    }

    public void queue(File file, long timeout, Runnable runnable) {
        synchronized (lock) {
            queue.add(new Entry(file, System.nanoTime() + timeout, runnable));
            lock.notify();
        }
    }

    public void onClose() {
        thread.interrupt();
    }

    private void threadFunc() {
        try {
            while (true) {
                Entry save;
                synchronized (lock) {
                    while (queue.isEmpty()) {
                        lock.wait(WAIT_INDEFINITELY);
                    }

                    Entry entry = queue.peek();
                    long delay = entry.time - System.nanoTime();
                    if (delay > 0) {
                        long timeout = Math.max(MIN_WAIT_TIMEOUT, Math.min(MAX_WAIT_TIMEOUT, delay / 1_000_000));
                        lock.wait(timeout);
                        continue;
                    }

                    queue.poll();
                    if (queue.stream().anyMatch(e -> e.file.equals(entry.file))) {
                        continue;
                    }

                    save = entry;
                }

                save.runnable.run();
            }
        } catch (InterruptedException exception) {
            synchronized (lock) {
                while (!queue.isEmpty()) {
                    Entry entry = queue.peek();
                    Entry last = queue.stream()
                            .filter(e -> e.file.equals(entry.file))
                            .max(Comparator.naturalOrder())
                            .orElseThrow();
                    last.runnable.run();
                    queue.removeIf(e -> e.file.equals(entry.file));
                }
            }
        }
    }

    private record Entry(File file, long time, Runnable runnable) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry other) {
            return Long.compare(time, other.time);
        }
    }
}