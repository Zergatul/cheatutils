package com.zergatul.cheatutils.render.gl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

public class FrameBuffer {

    private int FBO;
    private int FBT;
    private final int width;
    private final int height;
    private final float pixelWidth;
    private final float pixelHeight;

    public FrameBuffer() {
        FBO = GL30.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, FBO);

        Window window = Minecraft.getInstance().getWindow();
        width = window.getWidth();
        height = window.getHeight();

        FBT = GL30.glGenTextures();
        GlStateManager._bindTexture(FBT);

        GL30.glTexImage2D(
                GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA,
                width, height,
                0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, 0);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, FBT, 0);

        GlStateManager._bindTexture(0);

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Framebuffer is not complete");
        }

        pixelHeight = 1f / width;
        pixelWidth = 1f / height;
    }

    public void bind() {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, FBO);
    }

    public void bindTexture() {
        GlStateManager._bindTexture(FBT);
    }

    public float getPixelWidth() {
        return pixelWidth;
    }

    public float getPixelHeight() {
        return pixelHeight;
    }

    public int getTextureId() {
        return FBT;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void delete() {
        if (FBO != 0) {
            GL30.glDeleteFramebuffers(FBO);
            FBO = 0;
        }
        if (FBT != 0) {
            GL30.glDeleteTextures(FBT);
            FBT = 0;
        }
    }
}