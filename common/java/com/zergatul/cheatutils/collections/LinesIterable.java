package com.zergatul.cheatutils.collections;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class LinesIterable<T> implements Iterable<Pair<T, T>> {

    private final List<T> list;

    public LinesIterable(List<T> list) {
        this.list = list;
    }

    @NotNull
    @Override
    public Iterator<Pair<T, T>> iterator() {
        return new LinesIterator<>(list);
    }

    private static class LinesIterator<T> implements Iterator<Pair<T, T>> {

        private final List<T> list;
        private int index;

        private LinesIterator(List<T> list) {
            this.list = list;
        }

        @Override
        public boolean hasNext() {
            return index < list.size() - 1;
        }

        @Override
        public Pair<T, T> next() {
            T element1 = list.get(index++);
            T element2 = list.get(index);
            return new Pair<>(element1, element2);
        }
    }
}