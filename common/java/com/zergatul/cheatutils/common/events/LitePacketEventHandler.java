package com.zergatul.cheatutils.common.events;

import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LitePacketEventHandler {

    private final List<Entry> entries = new ArrayList<>();
    private final List<CancellablePacketHandler> handlers = new ArrayList<>();
    private int counter;

    public void add(CancellablePacketHandler handler) {
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
    public void add(CancellablePacketHandler handler, int priority) {
        entries.add(new Entry(handler, priority, counter++));
        Collections.sort(entries);

        handlers.clear();
        entries.stream().map(entry -> entry.handler).forEach(handlers::add);
    }

    public boolean trigger(Packet<?> packet) {
        for (CancellablePacketHandler handler : handlers) {
            if (!handler.handle(packet)) {
                return false;
            }
        }
        return true;
    }

    private record Entry(CancellablePacketHandler handler, int priority1, int priority2) implements Comparable<Entry> {
        @Override
        public int compareTo(@NotNull Entry other) {
            int result = Integer.compare(priority1, other.priority1);
            if (result != 0) {
                return result;
            } else {
                return Integer.compare(priority2, other.priority2);
            }
        }
    }
}