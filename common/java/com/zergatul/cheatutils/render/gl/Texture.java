package com.zergatul.cheatutils.render.gl;

import com.mojang.blaze3d.opengl.GlStateManager;
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

        GlStateManager._activeTexture(GL_TEXTURE0);
        GlStateManager._bindTexture(id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);

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

        GlStateManager._activeTexture(GL_TEXTURE0);
        GlStateManager._bindTexture(id);
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, image.getWidth(), image.getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, image.getARGB());
    }

    public void dispose() {
        if (!disposed) {
            glDeleteTextures(id);
            disposed = true;
        }
    }
}