package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.awt.Color;

public class BlockOverlayRenderer {

    private final InstancedBlockRenderer instancedRenderer = new InstancedBlockRenderer();

    private RenderWorldLastEvent event;
    private Vec3 view;
    private int framebuffer;
    private int texture;
    private int width;
    private int height;
    private ShaderProgram compositeProgram;
    private int compositeVao;
    private int textureUniform;
    private int colorUniform;

    public void begin(RenderWorldLastEvent event) {
        if (this.event != null) {
            throw new IllegalStateException("Renderer is already active");
        }

        this.event = event;
        this.view = event.getCamera().getPosition();
        instancedRenderer.begin();
    }

    public void block(double x, double y, double z) {
        instancedRenderer.block(
                (float) (x - view.x),
                (float) (y - view.y),
                (float) (z - view.z));
    }

    public void end(Color color) {
        if (event == null) {
            throw new IllegalStateException("Renderer is not active");
        }

        if (instancedRenderer.isEmpty()) {
            reset();
            return;
        }

        ensureFramebuffer();
        drawMask();
        drawComposite(color);
        reset();
    }

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

    private void drawMask() {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        GL30.glViewport(0, 0, width, height);
        GL30.glClearColor(0, 0, 0, 0);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        instancedRenderer.draw(event);
    }

    private void drawComposite(Color color) {
        Minecraft mc = Minecraft.getInstance();
        mc.getMainRenderTarget().bindWrite(false);
        GL30.glViewport(0, 0, width, height);
        ensureCompositeProgram();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._bindTexture(texture);
        GL20.glUseProgram(compositeProgram.getId());
        GL20.glUniform1i(textureUniform, 0);
        GL20.glUniform4f(
                colorUniform,
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f);
        GL30.glBindVertexArray(compositeVao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
        GlStateManager._bindTexture(0);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private void ensureCompositeProgram() {
        if (compositeProgram != null) {
            return;
        }
        String root = "assets/cheatutils/shaders/";
        compositeProgram = new ShaderProgram(
                root + "overlay-composite.vsh",
                root + "overlay-composite.fsh");
        textureUniform = compositeProgram.getUniform("BufferTexture");
        colorUniform = compositeProgram.getUniform("OverlayColor");
        compositeVao = GL30.glGenVertexArrays();
    }

    private void ensureFramebuffer() {
        Minecraft mc = Minecraft.getInstance();
        int newWidth = mc.getWindow().getWidth();
        int newHeight = mc.getWindow().getHeight();
        if (framebuffer != 0 && width == newWidth && height == newHeight) {
            return;
        }

        deleteFramebuffer();
        width = newWidth;
        height = newHeight;

        framebuffer = GL30.glGenFramebuffers();
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);

        texture = GL30.glGenTextures();
        GlStateManager._bindTexture(texture);
        GL30.glTexImage2D(
                GL30.GL_TEXTURE_2D,
                0,
                GL30.GL_RGBA8,
                width,
                height,
                0,
                GL30.GL_RGBA,
                GL30.GL_UNSIGNED_BYTE,
                0);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
        GL30.glFramebufferTexture2D(
                GL30.GL_FRAMEBUFFER,
                GL30.GL_COLOR_ATTACHMENT0,
                GL30.GL_TEXTURE_2D,
                texture,
                0);
        GL30.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GlStateManager._bindTexture(0);

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
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
            GL30.glDeleteTextures(texture);
            texture = 0;
        }
        width = 0;
        height = 0;
    }

    private void reset() {
        event = null;
        view = null;
    }
}