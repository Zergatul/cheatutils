package com.zergatul.cheatutils.render.gl;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import static org.lwjgl.opengl.GL30.*;

public class Texture {

    private final int id;
    private final int width;
    private final int height;
    private boolean disposed;

    private Texture(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    public static Texture empty(int width, int height) {
        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        return new Texture(id, width, height);
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void update(BufferedImage image, int x, int y) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new IllegalStateException("Only TYPE_INT_ARGB is supported.");
        }

        int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);

        glBindTexture(GL_TEXTURE_2D, id);
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, image.getWidth(), image.getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, data);
    }

    public void dispose() {
        if (!disposed) {
            glDeleteTextures(id);
            disposed = true;
        }
    }
}