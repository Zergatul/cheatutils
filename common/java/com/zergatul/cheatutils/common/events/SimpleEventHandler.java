package com.zergatul.cheatutils.common.events;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleEventHandler {

    private final List<Entry> entries = new ArrayList<>();
    private final List<Runnable> handlers = new ArrayList<>();
    private int counter;

    public void add(Runnable handler) {
        add(handler, 0);
    }

    public void add(Runnable handler, int priority) {
        entries.add(new Entry(handler, priority, counter++));
        Collections.sort(entries);

        handlers.clear();
        entries.stream().map(entry -> entry.handler).forEach(handlers::add);
    }

    public void trigger() {
        for (Runnable handler: handlers) {
            handler.run();
        }
    }

    private record Entry(Runnable handler, int priority1, int priority2) implements Comparable<Entry> {
        @Override
        public int compareTo(@NotNull SimpleEventHandler.Entry other) {
            int result = Integer.compare(priority1, other.priority1);
            if (result != 0) {
                return result;
            } else {
                return Integer.compare(priority2, other.priority2);
            }
        }
    }
}