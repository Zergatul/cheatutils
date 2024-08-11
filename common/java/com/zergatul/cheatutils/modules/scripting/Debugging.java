package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.modules.Module;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Debugging implements Module {

    public static final Debugging instance = new Debugging();

    private final LinkedList<Entry> entries = new LinkedList<>();
    private int id;

    private Debugging() {}

    public synchronized void addMessage(String message) {
        id++;
        long now = System.nanoTime();
        entries.add(new Entry(id, System.nanoTime(), message));

        clearOld(now - 1000000000L);
    }

    public synchronized List<Entry> getEntries(int since) {
        List<Entry> result = new ArrayList<>();
        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry next = iterator.next();
            if (next.id > since) {
                result.add(next);
                break;
            }
        }
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    private void clearOld(long from) {
        while (entries.size() > 100 && entries.getFirst().time < from) {
            entries.removeFirst();
        }
    }

    public record Entry(int id, long time, String message) {}
}