package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.utils.SharedVertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

import java.awt.Color;

public class BlockOverlayRenderer {

    private final BufferBuilder worldBuffer = new BufferBuilder(4096);
    private final BufferBuilder screenBuffer = new BufferBuilder(256);

    private RenderWorldLastEvent event;
    private Vec3 view;
    private int framebuffer;
    private int texture;
    private int width;
    private int height;
    private int vertices;

    public void begin(RenderWorldLastEvent event) {
        if (this.event != null) {
            throw new IllegalStateException("Renderer is already active");
        }

        this.event = event;
        this.view = event.getCamera().getPosition();
        this.vertices = 0;
        worldBuffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
    }

    public void cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        // bottom
        quad(x2, y1, z2, x1, y1, z2, x1, y1, z1, x2, y1, z1);
        // top
        quad(x2, y2, z2, x2, y2, z1, x1, y2, z1, x1, y2, z2);
        // west
        quad(x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1);
        // east
        quad(x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2);
        // north
        quad(x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1);
        // south
        quad(x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2);
    }

    public void end(Color color) {
        if (event == null) {
            throw new IllegalStateException("Renderer is not active");
        }

        if (vertices == 0) {
            worldBuffer.discard();
            reset();
            return;
        }

        ensureFramebuffer();
        drawMask();
        drawComposite(color);
        reset();
    }

    public void close() {
        deleteFramebuffer();
    }

    private void quad(
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3,
            double x4, double y4, double z4
    ) {
        vertex(x1, y1, z1);
        vertex(x2, y2, z2);
        vertex(x4, y4, z4);
        vertex(x2, y2, z2);
        vertex(x3, y3, z3);
        vertex(x4, y4, z4);
    }

    private void vertex(double x, double y, double z) {
        worldBuffer.vertex(x - view.x, y - view.y, z - view.z).endVertex();
        vertices++;
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

        SharedVertexBuffer.instance.bind();
        SharedVertexBuffer.instance.upload(worldBuffer.end());
        SharedVertexBuffer.instance.drawWithShader(
                event.getPoseMatrix(),
                event.getProjectionMatrix(),
                GameRenderer.getPositionShader());
        VertexBuffer.unbind();
    }

    private void drawComposite(Color color) {
        Minecraft mc = Minecraft.getInstance();
        mc.getMainRenderTarget().bindWrite(false);
        GL30.glViewport(0, 0, width, height);

        screenBuffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        screenVertex(-1, -1, 0, 0);
        screenVertex(1, -1, 1, 0);
        screenVertex(1, 1, 1, 1);
        screenVertex(-1, -1, 0, 0);
        screenVertex(1, 1, 1, 1);
        screenVertex(-1, 1, 0, 1);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f);

        SharedVertexBuffer.instance.bind();
        SharedVertexBuffer.instance.upload(screenBuffer.end());
        SharedVertexBuffer.instance.drawWithShader(
                new Matrix4f(),
                new Matrix4f(),
                GameRenderer.getPositionTexShader());
        VertexBuffer.unbind();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private void screenVertex(float x, float y, float u, float v) {
        screenBuffer.vertex(x, y, 0).uv(u, v).endVertex();
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