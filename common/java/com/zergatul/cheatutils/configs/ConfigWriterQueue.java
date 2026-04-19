package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.utils.MathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Comparator;
import java.util.PriorityQueue;

public class ConfigWriterQueue {

    public static final ConfigWriterQueue instance = new ConfigWriterQueue();

    private static final int MIN_WAIT_TIMEOUT = 15;
    private static final int MAX_WAIT_TIMEOUT = 1000;

    private final Logger logger = LogManager.getLogger(ConfigWriterQueue.class);
    private final PriorityQueue<Entry> queue = new PriorityQueue<>();
    private final Thread thread;
    private final Object event = new Object();


    private ConfigWriterQueue() {
        thread = new Thread(this::threadFunc);
        thread.start();

        Events.Close.add(this::onClose);
    }

    public void clear() {
        synchronized (queue) {
            queue.clear();
        }
    }

    public void immediate(File file, Runnable runnable) {
        if (Profiles.instance.isInResetState()) {
            return;
        }
        synchronized (queue) {
            queue.add(new Entry(file, System.nanoTime(), runnable, true));
        }
        synchronized (event) {
            event.notify();
        }
    }

    public void queue(File file, long timeout, Runnable runnable) {
        if (Profiles.instance.isInResetState()) {
            return;
        }
        synchronized (queue) {
            queue.add(new Entry(file, System.nanoTime() + timeout, runnable));
        }
    }

    private void onClose() {
        thread.interrupt();
    }

    private void threadFunc() {
        long nextDelay = MAX_WAIT_TIMEOUT;
        try {
            while (true) {
                synchronized (event) {
                    event.wait(nextDelay);
                }

                Entry save = null;
                synchronized (queue) {
                    if (queue.isEmpty()) {
                        continue;
                    }

                    Entry entry = queue.peek();
                    if (entry.time <= System.nanoTime()) {
                        queue.poll();

                        if (entry.immediate) {
                            processImmediateEntry(entry);
                            nextDelay = 0;
                            continue;
                        }

                        if (queue.stream().anyMatch(e -> e.file.equals(entry.file))) {
                            // if there is another entry for the same config, skip save
                            nextDelay = calculateDelay(queue.peek());
                        } else {
                            save = entry;
                            nextDelay = 0;
                        }
                    } else {
                        nextDelay = calculateDelay(entry);
                    }
                }

                if (save != null) {
                    save.runnable.run();
                }
            }
        } catch (InterruptedException exception) {
            // save all
            synchronized (queue) {
                while (!queue.isEmpty()) {
                    Entry entry = queue.peek();

                    // very unlikely to happen
                    if (entry.immediate) {
                        processImmediateEntry(entry);
                        continue;
                    }

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

    private void processImmediateEntry(Entry entry) {
        // we run actual save for immediate only if there was another entry for the same file
        // if not other entry was present, this means there is no changes to save
        if (queue.removeIf(e -> e.file.equals(entry.file))) {
            entry.runnable.run();
        }
    }

    private int calculateDelay(Entry entry) {
        return MathUtils.clamp((int) ((System.nanoTime() - entry.time) / 1000000), MIN_WAIT_TIMEOUT, MAX_WAIT_TIMEOUT);
    }

    private record Entry(File file, long time, Runnable runnable, boolean immediate) implements Comparable<Entry> {

        public Entry(File file, long time, Runnable runnable) {
            this(file, time, runnable, false);
        }

        @Override
        public int compareTo(Entry other) {
            return Long.compare(time, other.time);
        }

    }
}