package com.zergatul.cheatutils.collections;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CounterMap implements Iterable<String> {

    private final Map<String, Integer> map = new HashMap<>();

    public void clear() {
        map.clear();
    }

    public int get(String key) {
        return map.get(key);
    }

    public void inc(String key) {
        map.merge(key, 1, Integer::sum);
    }

    public Iterable<String> keys() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Iterator<String> iterator() {
        return new CounterIterator();
    }

    private class CounterIterator implements Iterator<String> {

        private final Iterator<String> keys;

        public CounterIterator() {
            this.keys = map.keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return keys.hasNext();
        }

        @Override
        public String next() {
            String key = keys.next();
            return key + "=" + map.get(key);
        }
    }
}