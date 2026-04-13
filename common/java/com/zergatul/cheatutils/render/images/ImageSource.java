package com.zergatul.cheatutils.render.images;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public interface ImageSource {

    int getWidth();
    int getHeight();
    int[] getARGB();

    static ImageSource fromBuffered(BufferedImage image) {
        return new BufferedImageSource(image);
    }

    static ImageSource fromGrayscale(ByteBuffer buffer, int width, int height) {
        return new GrayscaleImageSource(buffer, width, height);
    }
}