package com.zergatul.cheatutils.common.events;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ParameterizedEventHandler<T> {

    private final List<Entry<T>> entries = new ArrayList<>();
    private final List<Consumer<T>> handlers = new ArrayList<>();
    private int counter;

    public void add(Consumer<T> handler) {
        add(handler, 0);
    }

    public void add(Consumer<T> handler, int priority) {
        entries.add(new Entry<>(handler, priority, counter++));
        Collections.sort(entries);

        handlers.clear();
        entries.stream().map(entry -> entry.handler).forEach(handlers::add);
    }

    public void trigger(T parameter) {
        for (Consumer<T> handler : handlers) {
            handler.accept(parameter);
        }
    }

    private record Entry<T>(Consumer<T> handler, int priority, int order) implements Comparable<Entry<T>> {
        @Override
        public int compareTo(@NotNull Entry<T> other) {
            int result = Integer.compare(priority, other.priority);
            return result != 0 ? result : Integer.compare(order, other.order);
        }
    }
}