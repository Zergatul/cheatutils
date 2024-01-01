package com.zergatul.cheatutils.collections;

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

    public float get(int index) {
        return array[index];
    }

    public int size() {
        return size;
    }

    public void truncate(int size) {
        if (size < this.size) {
            this.size = size;
        }
    }

    private void increaseCapacity() {
        float[] newArray = new float[array.length * 2];
        System.arraycopy(array, 0, newArray, 0, size);
        array = newArray;
    }
}