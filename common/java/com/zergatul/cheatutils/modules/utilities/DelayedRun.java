package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.Module;

import java.util.ArrayList;
import java.util.List;

public class DelayedRun implements Module {

    public static final DelayedRun instance = new DelayedRun();

    private final List<Entry> entries = new ArrayList<>();

    private DelayedRun() {
        Events.ClientTickStart.add(this::onTickStart);
        Events.ClientTickEnd.add(this::onTickEnd);
    }

    public void add(int ticks, Runnable action) {
        entries.add(new Entry(ticks, action));
    }

    private void onTickStart() {
        for (Entry entry: entries) {
            entry.ticks--;
        }
    }

    private void onTickEnd() {
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            if (entry.ticks <= 0) {
                entry.action.run();
                entries.remove(i);
                i--;
            }
        }
    }

    private static class Entry {
        public int ticks;
        public Runnable action;

        public Entry(int ticks, Runnable action) {
            this.ticks = ticks;
            this.action = action;
        }
    }
}