package com.zergatul.cheatutils.render.images;

import java.nio.ByteBuffer;

public class GrayscaleImageSource implements ImageSource {

    private final ByteBuffer buffer;
    private final int width;
    private final int height;

    public GrayscaleImageSource(ByteBuffer buffer, int width, int height) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int[] getARGB() {
        int size = width * height;
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = (buffer.get(i) << 24) | 0xFFFFFF;
        }
        return data;
    }
}