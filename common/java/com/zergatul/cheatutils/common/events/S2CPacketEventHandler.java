package com.zergatul.cheatutils.common.events;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class S2CPacketEventHandler {

    private final List<Entry> entries = new ArrayList<>();
    private final List<S2CPacketHandler> handlers = new ArrayList<>();
    private int counter;

    public void add(S2CPacketHandler handler) {
        add(handler, 0);
    }

    public void add(S2CPacketHandler handler, int priority) {
        entries.add(new Entry(handler, priority, counter++));
        Collections.sort(entries);

        handlers.clear();
        entries.stream().map(entry -> entry.handler).forEach(handlers::add);
    }

    public boolean trigger(PacketListener listener, Packet<?> packet) {
        for (S2CPacketHandler handler : handlers) {
            if (!handler.handle(listener, packet)) {
                return false;
            }
        }
        return true;
    }

    private record Entry(S2CPacketHandler handler, int priority1, int priority2) implements Comparable<Entry> {
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