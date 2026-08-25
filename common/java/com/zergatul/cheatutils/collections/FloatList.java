package com.zergatul.cheatutils.collections;

import java.nio.ByteBuffer;

public class FloatList {

    private float[] array;
    private int size;

    public FloatList() {
        this(256);
    }

    public FloatList(int capacity) {
        array = new float[capacity];
    }

    public void add(float value) {
        if (size == array.length) {
            increaseCapacity();
        }
        array[size++] = value;
    }

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
    }

    public void writeTo(ByteBuffer buffer) {
        for (int i = 0; i < size; i++) {
            buffer.putFloat(array[i]);
        }
    }

    private void increaseCapacity() {
        float[] newArray = new float[array.length * 2];
        System.arraycopy(array, 0, newArray, 0, size);
        array = newArray;
    }
}