package com.zergatul.cheatutils.render.gl;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

public class FrameBuffer {

    private static int prevFBO;
    private int FBO;
    private int FBT;
    private float pixelWidth;
    private float pixelHeight;

    public FrameBuffer() {
        push();

        FBO = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, FBO);

        Window window = Minecraft.getInstance().getWindow();
        int width = window.getWidth();
        int height = window.getHeight();

        FBT = GL30.glGenTextures();
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, FBT);

        GL30.glTexImage2D(
                GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA,
                width, height,
                0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, 0);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, FBT, 0);

        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Framebuffer is not complete");
        }

        pop();

        pixelHeight = 1f / width;
        pixelWidth = 1f / height;
    }

    public void bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, FBO);
    }

    public void bindTexture() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, FBT);
    }

    public float getPixelWidth() {
        return pixelWidth;
    }

    public float getPixelHeight() {
        return pixelHeight;
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

    public static void push() {
        prevFBO = GL30.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
    }

    public static void pop() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFBO);
    }
}