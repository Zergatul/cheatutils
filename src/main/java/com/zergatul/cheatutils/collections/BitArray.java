package com.zergatul.cheatutils.collections;

public class BitArray {

    private final long[] data;
    private final int bits;
    private final int mask;

    public BitArray(long[] data, int bits) {
        this.data = data;
        this.bits = bits;
        this.mask = (1 << bits) - 1;
    }

    public int get(int index) {
        int start = index * bits;
        int dataIndex = start / Long.SIZE;
        int bitIndex = start % Long.SIZE;
        if (bitIndex + bits <= Long.SIZE) {
            return (int)((data[dataIndex] >> bitIndex) & mask);
        } else {
            int leftBits = 64 - bitIndex;
            int rightBits = bits - leftBits;
            long left = data[dataIndex] >>> bitIndex;
            long right = data[dataIndex + 1] << (64 - rightBits) >>> (64 - rightBits) << leftBits;
            return (int)(left | right) & mask;
        }
    }

    public int size() {
        return data.length * Long.SIZE / bits;
    }
}