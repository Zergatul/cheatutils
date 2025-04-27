package com.zergatul.cheatutils.render.gl.images;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class BufferedImageSource implements ImageSource {

    private final BufferedImage image;

    public BufferedImageSource(BufferedImage image) {
        this.image = image;
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public int[] getARGB() {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new IllegalStateException("Only TYPE_INT_ARGB is supported.");
        }

        return ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }
}