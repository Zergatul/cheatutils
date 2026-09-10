package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class BlockEspOverlayRenderer implements AutoCloseable {

    public static final BlockEspOverlayRenderer INSTANCE = new BlockEspOverlayRenderer();

    private final InstancedBlockRenderer instancedRenderer = new InstancedBlockRenderer();
    private final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private int framebuffer;
    private int texture;
    private int width;
    private int height;
    private ShaderProgram compositeProgram;
    private int compositeVao;
    private int textureUniform;
    private int colorUniform;

    private BlockEspOverlayRenderer() {}

    public void begin() {
        instancedRenderer.begin();
    }

    public void cube(float x, float y, float z) {
        instancedRenderer.block(x, y, z);
    }

    public void end(Matrix4f mvp, Color color) {
        if (instancedRenderer.isEmpty()) {
            return;
        }

        int previousFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        viewport.clear();
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        try {
            ensureFramebuffer();
            drawMask(mvp);
        } finally {
            // Restore the actual destination, including framebuffer 0 when vanilla FBOs are disabled.
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousFramebuffer);
            GlStateManager.viewport(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3));
        }
        drawComposite(color);
    }

    @Override
    public void close() {
        instancedRenderer.close();
        if (compositeProgram != null) {
            compositeProgram.close();
            compositeProgram = null;
        }
        if (compositeVao != 0) {
            GL30.glDeleteVertexArrays(compositeVao);
            compositeVao = 0;
        }
        deleteFramebuffer();
    }

    private void drawMask(Matrix4f mvp) {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer);
        GlStateManager.viewport(0, 0, width, height);
        GlStateManager.clearColor(0, 0, 0, 0);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);

        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        instancedRenderer.draw(mvp);
    }

    private void drawComposite(Color color) {
        ensureCompositeProgram();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableDepth();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(texture);
        GL20.glUseProgram(compositeProgram.getId());
        GL20.glUniform1i(textureUniform, 0);
        GL20.glUniform4f(
                colorUniform,
                color.getRed() / 255F,
                color.getGreen() / 255F,
                color.getBlue() / 255F,
                color.getAlpha() / 255F);
        GL30.glBindVertexArray(compositeVao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
        GlStateManager.bindTexture(0);

        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
    }

    private void ensureCompositeProgram() {
        if (compositeProgram != null) {
            return;
        }
        compositeProgram = new ShaderProgram(
                Constants.SHADER_ROOT + "overlay-composite.vsh",
                Constants.SHADER_ROOT + "overlay-composite.fsh");
        textureUniform = compositeProgram.getUniform("BufferTexture");
        colorUniform = compositeProgram.getUniform("OverlayColor");
        compositeVao = GL30.glGenVertexArrays();
    }

    private void ensureFramebuffer() {
        Minecraft mc = Minecraft.getMinecraft();
        int newWidth = mc.displayWidth;
        int newHeight = mc.displayHeight;
        if (framebuffer != 0 && width == newWidth && height == newHeight) {
            return;
        }

        deleteFramebuffer();
        width = newWidth;
        height = newHeight;
        framebuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer);

        texture = GL11.glGenTextures();
        GlStateManager.bindTexture(texture);
        GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL30.glFramebufferTexture2D(
                GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GlStateManager.bindTexture(0);

        if (GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            deleteFramebuffer();
            throw new IllegalStateException("Block ESP overlay framebuffer is not complete");
        }
    }

    private void deleteFramebuffer() {
        if (framebuffer != 0) {
            GL30.glDeleteFramebuffers(framebuffer);
            framebuffer = 0;
        }
        if (texture != 0) {
            GlStateManager.deleteTexture(texture);
            texture = 0;
        }
        width = 0;
        height = 0;
    }
}