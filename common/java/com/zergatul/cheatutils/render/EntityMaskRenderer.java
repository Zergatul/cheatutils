package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.collections.FloatList;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EntityMaskRenderer implements AutoCloseable {

    private static final int RECORD_SIZE = 5 * Float.BYTES;
    private static final int INITIAL_CAPACITY = 4096;
    private static final String SHADER_ROOT = "assets/cheatutils/shaders/";

    private final float[] matrix = new float[16];
    private ByteBuffer uploadBuffer = MemoryUtil.memAlloc(INITIAL_CAPACITY).order(ByteOrder.nativeOrder());
    private RenderWorldLastEvent event;
    private int framebuffer;
    private int maskTexture;
    private int width;
    private int height;
    private int vao;
    private int vbo;
    private int gpuCapacity;
    private int compositeVao;
    private ShaderProgram maskProgram;
    private ShaderProgram overlayProgram;
    private ShaderProgram outlineProgram;
    private int maskMvpUniform;
    private int maskTextureUniform;
    private int overlayTextureUniform;
    private int overlayColorUniform;
    private int outlineTextureUniform;
    private int outlineColorUniform;
    private int outlineTexelSizeUniform;

    public void begin(RenderWorldLastEvent event) {
        if (this.event != null) {
            throw new IllegalStateException("Entity mask renderer is already active");
        }

        this.event = event;
        ensureInitialized();
        ensureFramebuffer();

        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        GL30.glViewport(0, 0, width, height);
        GL30.glClearColor(0, 0, 0, 0);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
    }

    public void draw(FloatList vertices, int textureId) {
        if (event == null) {
            throw new IllegalStateException("Entity mask renderer is not active");
        }
        if (vertices.size() == 0) {
            return;
        }
        if (vertices.size() % 5 != 0) {
            throw new IllegalArgumentException("Entity mask vertex data is incomplete");
        }

        int bytes = vertices.size() * Float.BYTES;
        ensureUploadCapacity(bytes);
        uploadBuffer.clear();
        vertices.writeTo(uploadBuffer);
        uploadBuffer.flip();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        if (gpuCapacity < bytes) {
            gpuCapacity = nextPowerOfTwo(bytes);
        }
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, gpuCapacity, GL15.GL_STREAM_DRAW);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, uploadBuffer);

        GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._bindTexture(textureId);
        TextureStateTracker.setMinFilter(textureId, GL11.GL_NEAREST);
        GL20.glUseProgram(maskProgram.getId());
        GL20.glUniformMatrix4fv(maskMvpUniform, false, event.getProjectionMatrix().get(matrix));
        GL20.glUniform1i(maskTextureUniform, 0);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertices.size() / 5);

        GL20.glUseProgram(0);
        GlStateManager._bindTexture(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void endOverlay(Color color) {
        finish(color, false);
    }

    public void endOutline(Color color) {
        finish(color, true);
    }

    @Override
    public void close() {
        if (maskProgram != null) {
            maskProgram.close();
            maskProgram = null;
        }
        if (overlayProgram != null) {
            overlayProgram.close();
            overlayProgram = null;
        }
        if (outlineProgram != null) {
            outlineProgram.close();
            outlineProgram = null;
        }
        if (vbo != 0) {
            GL15.glDeleteBuffers(vbo);
            vbo = 0;
        }
        if (vao != 0) {
            GL30.glDeleteVertexArrays(vao);
            vao = 0;
        }
        if (compositeVao != 0) {
            GL30.glDeleteVertexArrays(compositeVao);
            compositeVao = 0;
        }
        deleteFramebuffer();
        if (uploadBuffer != null) {
            MemoryUtil.memFree(uploadBuffer);
            uploadBuffer = null;
        }
        event = null;
    }

    private void finish(Color color, boolean outline) {
        if (event == null) {
            throw new IllegalStateException("Entity mask renderer is not active");
        }

        Minecraft mc = Minecraft.getInstance();
        mc.getMainRenderTarget().bindWrite(false);
        GL30.glViewport(0, 0, width, height);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        ShaderProgram program = outline ? outlineProgram : overlayProgram;
        GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._bindTexture(maskTexture);
        GL20.glUseProgram(program.getId());
        GL20.glUniform1i(outline ? outlineTextureUniform : overlayTextureUniform, 0);
        GL20.glUniform4f(
                outline ? outlineColorUniform : overlayColorUniform,
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f);
        if (outline) {
            GL20.glUniform2f(outlineTexelSizeUniform, 1f / width, 1f / height);
        }

        GL30.glBindVertexArray(compositeVao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
        GlStateManager._bindTexture(0);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        event = null;
    }

    private void ensureInitialized() {
        if (maskProgram != null) {
            return;
        }

        maskProgram = new ShaderProgram(
                SHADER_ROOT + "entity-mask.vsh",
                SHADER_ROOT + "entity-mask.fsh",
                "inPosition", "inTexCoords");
        maskMvpUniform = maskProgram.getUniform("MVP");
        maskTextureUniform = maskProgram.getUniform("EntityTexture");

        overlayProgram = new ShaderProgram(
                SHADER_ROOT + "overlay-composite.vsh",
                SHADER_ROOT + "overlay-composite.fsh");
        overlayTextureUniform = overlayProgram.getUniform("BufferTexture");
        overlayColorUniform = overlayProgram.getUniform("OverlayColor");

        outlineProgram = new ShaderProgram(
                SHADER_ROOT + "overlay-composite.vsh",
                SHADER_ROOT + "entity-outline-composite.fsh");
        outlineTextureUniform = outlineProgram.getUniform("BufferTexture");
        outlineColorUniform = outlineProgram.getUniform("OutlineColor");
        outlineTexelSizeUniform = outlineProgram.getUniform("TexelSize");

        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, RECORD_SIZE, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, RECORD_SIZE, 3 * Float.BYTES);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

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

        maskTexture = GL30.glGenTextures();
        GlStateManager._bindTexture(maskTexture);
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
                maskTexture,
                0);
        GL30.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GlStateManager._bindTexture(0);

        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            deleteFramebuffer();
            throw new IllegalStateException("Entity ESP mask framebuffer is not complete");
        }
    }

    private void deleteFramebuffer() {
        if (framebuffer != 0) {
            GL30.glDeleteFramebuffers(framebuffer);
            framebuffer = 0;
        }
        if (maskTexture != 0) {
            GL30.glDeleteTextures(maskTexture);
            maskTexture = 0;
        }
        width = 0;
        height = 0;
    }

    private void ensureUploadCapacity(int bytes) {
        if (uploadBuffer.capacity() >= bytes) {
            return;
        }
        uploadBuffer = MemoryUtil.memRealloc(uploadBuffer, nextPowerOfTwo(bytes)).order(ByteOrder.nativeOrder());
    }

    private static int nextPowerOfTwo(int value) {
        int result = INITIAL_CAPACITY;
        while (result < value) {
            result *= 2;
        }
        return result;
    }
}