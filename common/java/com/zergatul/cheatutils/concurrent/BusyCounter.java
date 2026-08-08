package com.zergatul.cheatutils.concurrent;

import java.util.LinkedList;

public class BusyCounter {

    private static final int KEEP_SECONDS = 1;

    private final LinkedList<Entry> entries = new LinkedList<>();

    public void startWait() {
        long now = System.nanoTime();
        synchronized (entries) {
            clearOldEntries(now);
            updateLast(now);
            entries.addLast(new Entry(true, now));
        }
    }

    public void startLoad() {
        long now = System.nanoTime();
        synchronized (entries) {
            clearOldEntries(now);
            updateLast(now);
            entries.addLast(new Entry(false, now));
        }
    }

    public double getLoad(int seconds) {
        long now = System.nanoTime();
        long from = now - seconds * 1_000_000_000L;
        synchronized (entries) {
            clearOldEntries(now);
            updateLast(now);
            long totalWait = 0;
            long totalLoad = 0;
            for (Entry entry : entries) {
                if (entry.waiting) {
                    totalWait += entry.getIntersection(from, now);
                } else {
                    totalLoad += entry.getIntersection(from, now);
                }
            }
            long total = totalLoad + totalWait;
            return total == 0 ? 0 : (double) totalLoad / total;
        }
    }

    private void updateLast(long now) {
        if (!entries.isEmpty()) {
            entries.getLast().end = now;
        }
    }

    private void clearOldEntries(long now) {
        while (!entries.isEmpty() && now - entries.getFirst().end > KEEP_SECONDS * 1_000_000_000L) {
            entries.removeFirst();
        }
    }

    private static class Entry {
        private final boolean waiting;
        private final long start;
        private long end;

        private Entry(boolean waiting, long start) {
            this.waiting = waiting;
            this.start = start;
            this.end = start;
        }

        private long getIntersection(long from, long to) {
            from = Math.max(start, from);
            to = Math.min(end, to);
            return Math.max(0, to - from);
        }
    }
}