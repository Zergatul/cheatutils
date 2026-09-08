package com.zergatul.cheatutils.common.events;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CancelableEventHandler<T extends CancelableEvent> {

    private final List<Entry<T>> entries = new ArrayList<>();
    private final List<Consumer<T>> handlers = new ArrayList<>();
    private int counter;

    public void add(Consumer<T> handler) {
        add(handler, 0);
    }

    /**
     *
     * @param handler  Passed function that is added to the Event list
     * @param priority Defaults to 0 when not passed.
     *                 Events are executed in ascending order of their priority.
     *                 If multiple functions have the same priority, it follows the order it was added <br>
     *                 <br>
     *                 Example execution order:
     *                 {@code Priority 1 -> Priority 2}
     */
    public void add(Consumer<T> handler, int priority) {
        entries.add(new Entry<>(handler, priority, counter++));
        Collections.sort(entries);

        handlers.clear();
        entries.stream().map(entry -> entry.handler).forEach(handlers::add);
    }

    public boolean trigger(T parameter) {
        for (Consumer<T> handler : handlers) {
            handler.accept(parameter);
            if (parameter.isCanceled()) {
                return true;
            }
        }
        return false;
    }

    private static class Entry<T> implements Comparable<Entry<T>> {
        final Consumer<T> handler;
        final int priority1, priority2;
        Entry(Consumer<T> handler, int priority1, int priority2) {
            this.handler = handler;
            this.priority1 = priority1;
            this.priority2 = priority2;
        }
        @Override
        public int compareTo(Entry<T> other) {
            int result = Integer.compare(priority1, other.priority1);
            return result != 0 ? result : Integer.compare(priority2, other.priority2);
        }
    }
}
