package com.zergatul.cheatutils.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class IntList implements Iterable<Integer> {

    private int[] data;
    private int count;

    public IntList(int capacity) {
        data = new int[capacity];
    }

    public void add(int value) {
        if (data.length == count) {
            int[] newData = new int[data.length * 2];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }

        data[count++] = value;
    }

    public int get(int index) {
        return data[index];
    }

    public int count() {
        return count;
    }

    @NotNull
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            private int position;

            @Override
            public boolean hasNext() {
                return position < count;
            }

            @Override
            public Integer next() {
                return data[position++];
            }
        };
    }
}