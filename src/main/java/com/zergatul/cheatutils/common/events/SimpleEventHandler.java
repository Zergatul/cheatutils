package com.zergatul.cheatutils.common.events;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SimpleEventHandler {

    private final List<Entry> entries = new ArrayList<>();

    public void add(Runnable handler) {
        add(handler, 0);
    }

    public void add(Runnable handler, int priority) {
        entries.add(new Entry(handler, priority));
        entries.sort(Comparator.comparingInt(entry -> entry.priority));
    }

    public void trigger() {
        for (Entry entry : new ArrayList<>(entries)) {
            entry.handler.run();
        }
    }

    private static class Entry {
        final Runnable handler;
        final int priority;

        Entry(Runnable handler, int priority) {
            this.handler = handler;
            this.priority = priority;
        }
    }
}