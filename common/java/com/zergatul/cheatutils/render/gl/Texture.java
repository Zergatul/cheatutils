package com.zergatul.cheatutils.render.gl;

import com.zergatul.cheatutils.render.gl.images.ImageSource;

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

        glActiveTexture(GL_TEXTURE0);
        int prevTexture = glGetInteger(GL_TEXTURE_BINDING_2D);
        glBindTexture(GL_TEXTURE_2D, id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        if (prevTexture != id) {
            glBindTexture(GL_TEXTURE_2D, prevTexture);
        }

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

    public void update(ImageSource image, int x, int y) {
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);

        glActiveTexture(GL_TEXTURE0);
        int prevTexture = glGetInteger(GL_TEXTURE_BINDING_2D);
        glBindTexture(GL_TEXTURE_2D, id);
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, image.getWidth(), image.getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, image.getARGB());
        if (prevTexture != id) {
            glBindTexture(GL_TEXTURE_2D, prevTexture);
        }
    }

    public void dispose() {
        if (!disposed) {
            glDeleteTextures(id);
            disposed = true;
        }
    }
}