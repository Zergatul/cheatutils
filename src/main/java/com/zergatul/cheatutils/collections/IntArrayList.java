package com.zergatul.cheatutils.collections;

public class IntArrayList {

    private int[] data;
    private int size;

    public IntArrayList() {
        this(8);
    }

    public IntArrayList(int capacity) {
        data = new int[capacity];
        size = 0;
    }

    public void add(int value) {
        ensureCapacity(size + 1);
        data[size++] = value;
    }

    public void addRange(int[] array) {
        ensureCapacity(size + array.length);
        System.arraycopy(array, 0, data, size, array.length);
        size += array.length;
    }

    public void clear() {
        size = 0;
    }

    public int[] getElements() {
        return data;
    }

    public void remove(int value) {
        int shift = 0;
        int i = 0;
        mainLoop:
        while (i < size) {
            while (data[i] == value) {
                shift++;
                i++;
                if (i == size) {
                    break mainLoop;
                }
            }
            if (shift > 0) {
                data[i - shift] = data[i];
            }
            i++;
        }
        size -= shift;
    }

    public int size() {
        return size;
    }

    private void ensureCapacity(int size) {
        if (size > data.length) {
            int newLength = data.length << 1;
            while (newLength < size) {
                newLength <<= 1;
            }
            int[] newData = new int[newLength];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
    }
}