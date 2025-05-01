package com.zergatul.cheatutils.utils;

public class IntUtils {

    public static int nextPowerOfTwo(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Input must be positive");
        }

        n--;

        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;

        return n + 1;
    }
}