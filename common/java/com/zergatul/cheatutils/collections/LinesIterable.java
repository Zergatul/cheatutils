package com.zergatul.cheatutils.collections;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public class LinesIterable<T> implements Iterable<T> {

    private final List<T> list;

    public LinesIterable(List<T> list) {
        this.list = list;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new LinesIterator<>(list);
    }

    private static class LinesIterator<T> implements Iterator<T> {

        private final List<T> list;
        private int index;
        private boolean duplicate;

        private LinesIterator(List<T> list) {
            this.list = list;
        }

        @Override
        public boolean hasNext() {
            if (index == 0) {
                return list.size() >= 2;
            } else {
                return index < list.size();
            }
        }

        @Override
        public T next() {
            if (index == 0 || index == list.size() - 1) {
                return list.get(index++);
            } else {
                if (duplicate) {
                    duplicate = false;
                    return list.get(index++);
                } else {
                    duplicate = true;
                    return list.get(index);
                }
            }
        }
    }
}