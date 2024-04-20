package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.common.Events;
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

    public void queue(File file, long timeout, Runnable runnable) {
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

                        if (queue.stream().anyMatch(e -> e.file.equals(entry.file))) {
                            // if there is another entry for the same config, skip save
                            nextDelay = calculateDelay(queue.peek());
                        } else {
                            save = entry;
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
                    // don't save twice
                    Entry entry = queue.peek();
                    Entry last = queue.stream()
                            .filter(e -> e.file.equals(entry.file))
                            .max(Comparator.naturalOrder())
                            .get();
                    last.runnable.run();
                    queue.removeIf(e -> e.file.equals(entry.file));
                }
            }
        }

    }
    private int calculateDelay(Entry entry) {
        return MathUtils.clamp((int) ((System.nanoTime() - entry.time) / 1000000), MIN_WAIT_TIMEOUT, MAX_WAIT_TIMEOUT);
    }

    private record Entry(File file, long time, Runnable runnable) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry other) {
            return Long.compare(time, other.time);
        }
    }
}