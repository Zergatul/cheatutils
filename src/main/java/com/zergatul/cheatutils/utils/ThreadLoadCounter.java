package com.zergatul.cheatutils.utils;

import java.util.LinkedList;

public class ThreadLoadCounter {

    private static final int KEEP_SECONDS = 5;

    private volatile boolean isWaiting;
    private final LinkedList<Entry> entries;

    public ThreadLoadCounter() {
        isWaiting = true;
        entries = new LinkedList<>();
    }

    public void startWait() {
        long now = System.nanoTime();
        synchronized (entries) {
            clearOldEntries();
            updateLast(now);
            entries.addLast(new Entry(true, now));
        }
    }

    public void startLoad() {
        long now = System.nanoTime();
        synchronized (entries) {
            clearOldEntries();
            updateLast(now);
            entries.addLast(new Entry(false, now));
        }
    }

    public void dispose() {
        synchronized (entries) {
            entries.clear();
            entries.addLast(new Entry(true, System.nanoTime()));
        }
    }

    public double getLoad(int seconds) {
        long now = System.nanoTime();
        long from = now - seconds * 1000000000L;
        synchronized (entries) {
            updateLast(now);
            long totalWait = 0;
            long totalLoad = 0;
            for (Entry entry: entries) {
                if (entry.isWaiting) {
                    totalWait += entry.getIntersection(from, now);
                } else {
                    totalLoad += entry.getIntersection(from, now);
                }
            }
            if (totalLoad + totalWait == 0) {
                return 0d;
            } else {
                return 1d * totalLoad / (totalWait + totalLoad);
            }
        }
    }

    private void updateLast(long now) {
        if (entries.size() > 0) {
            entries.getLast().end = now;
        }
    }

    private void clearOldEntries() {
        long now = System.nanoTime();
        while (entries.size() > 0) {
            Entry first = entries.getFirst();
            if (now - first.end > KEEP_SECONDS * 1000000000L) {
                entries.removeFirst();
            } else {
                break;
            }
        }
    }

    private static class Entry {
        public boolean isWaiting;
        public long start;
        public long end;

        public Entry(boolean isWaiting, long start) {
            this.isWaiting = isWaiting;
            this.start = start;
            this.end = start;
        }

        public long getIntersection(long from, long to) {
            from = Math.max(start, from);
            to = Math.min(end, to);
            long result = to - from;
            return Math.max(0, result);
        }
    }
}
