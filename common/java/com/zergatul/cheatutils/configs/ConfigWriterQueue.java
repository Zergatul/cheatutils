package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.utils.MathUtils;

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
        thread = new Thread(this::threadFunc, ModMain.MODID + " config writer");
        thread.start();

        Events.Close.add(this::onClose);
    }

    public void clear() {
        synchronized (lock) {
            queue.clear();
        }
    }

    public void flush(File file) {
        if (Profiles.instance.isInResetState()) {
            return;
        }

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
        if (Profiles.instance.isInResetState()) {
            return;
        }
        synchronized (lock) {
            queue.add(new Entry(file, System.nanoTime() + timeout, runnable));
            lock.notify();
        }
    }

    private void onClose() {
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
                        lock.wait(MathUtils.clamp(delay / 1_000_000, MIN_WAIT_TIMEOUT, MAX_WAIT_TIMEOUT));
                        continue;
                    }

                    queue.poll();
                    if (queue.stream().anyMatch(e -> e.file.equals(entry.file))) {
                        // if there is another entry for the same config, skip save
                        continue;
                    }

                    save = entry;
                }

                save.runnable.run();
            }
        } catch (InterruptedException exception) {
            // shutdown: save latest pending write per file
            synchronized (lock) {
                while (!queue.isEmpty()) {
                    Entry entry = queue.peek();

                    // don't save twice
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