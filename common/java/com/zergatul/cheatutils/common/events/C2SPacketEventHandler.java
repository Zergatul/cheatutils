package com.zergatul.cheatutils.common.events;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class C2SPacketEventHandler {

    private final List<Entry> entries = new ArrayList<>();
    private final List<C2SPacketHandler> handlers = new ArrayList<>();
    private int counter;

    public void add(C2SPacketHandler handler) {
        add(handler, 0);
    }

    public void add(C2SPacketHandler handler, int priority) {
        entries.add(new Entry(handler, priority, counter++));
        Collections.sort(entries);

        handlers.clear();
        entries.stream().map(entry -> entry.handler).forEach(handlers::add);
    }

    public boolean trigger(Connection connection, Packet<?> packet) {
        for (C2SPacketHandler handler : handlers) {
            if (!handler.handle(connection, packet)) {
                return false;
            }
        }
        return true;
    }

    private record Entry(C2SPacketHandler handler, int priority1, int priority2) implements Comparable<Entry> {
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