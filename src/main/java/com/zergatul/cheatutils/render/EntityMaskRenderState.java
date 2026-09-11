package com.zergatul.cheatutils.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

// Vanilla entity renderers change fixed-function state in addition to the framebuffer and shader.
// Restore through GlStateManager so its cache stays consistent with the driver.
final class EntityMaskRenderState implements AutoCloseable {

    private int framebuffer;
    private int program;
    private int vao;
    private int arrayBuffer;
    private int matrixMode;
    private int activeTexture;
    private final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private final FloatBuffer color = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer clearColor = BufferUtils.createFloatBuffer(16);
    private final int[] textures = new int[3];
    private final int[] textureEnv = new int[3];
    private final boolean[] textureEnabled = new boolean[3];
    private boolean blend;
    private boolean depth;
    private boolean alpha;
    private boolean cull;
    private boolean lighting;
    private boolean fog;
    private boolean colorMaterial;
    private boolean rescaleNormal;
    private boolean depthMask;
    private int depthFunc;
    private int alphaFunc;
    private float alphaRef;
    private int srcRgb;
    private int dstRgb;
    private int srcAlpha;
    private int dstAlpha;
    private int equationRgb;
    private int equationAlpha;
    private float lightmapX;
    private float lightmapY;

    void capture() {
        framebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        vao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        arrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        blend = GL11.glIsEnabled(GL11.GL_BLEND);
        depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        cull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
        fog = GL11.glIsEnabled(GL11.GL_FOG);
        colorMaterial = GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL);
        rescaleNormal = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
        depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        alphaFunc = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
        alphaRef = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
        srcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        dstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        equationRgb = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB);
        equationAlpha = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
        lightmapX = OpenGlHelper.lastBrightnessX;
        lightmapY = OpenGlHelper.lastBrightnessY;

        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
        GL11.glGetFloat(GL11.GL_COLOR_CLEAR_VALUE, clearColor);
        for (int i = 0; i < textures.length; i++) {
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0 + i);
            textures[i] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            textureEnabled[i] = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            textureEnv[i] = GL11.glGetTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE);
        }
        GlStateManager.setActiveTexture(activeTexture);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
    }

    void bindDestination() {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer);
        GlStateManager.viewport(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3));
    }

    @Override
    public void close() {
        bindDestination();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(matrixMode);
        GL20.glUseProgram(program);
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, arrayBuffer);

        set(blend, GlStateManager::enableBlend, GlStateManager::disableBlend);
        set(depth, GlStateManager::enableDepth, GlStateManager::disableDepth);
        set(alpha, GlStateManager::enableAlpha, GlStateManager::disableAlpha);
        set(cull, GlStateManager::enableCull, GlStateManager::disableCull);
        set(lighting, GlStateManager::enableLighting, GlStateManager::disableLighting);
        set(fog, GlStateManager::enableFog, GlStateManager::disableFog);
        set(colorMaterial, GlStateManager::enableColorMaterial, GlStateManager::disableColorMaterial);
        set(rescaleNormal, GlStateManager::enableRescaleNormal, GlStateManager::disableRescaleNormal);
        GlStateManager.depthMask(depthMask);
        GlStateManager.depthFunc(depthFunc);
        GlStateManager.alphaFunc(alphaFunc, alphaRef);
        GlStateManager.tryBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
        GL20.glBlendEquationSeparate(equationRgb, equationAlpha);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapX, lightmapY);
        for (int i = 0; i < textures.length; i++) {
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0 + i);
            GlStateManager.bindTexture(textures[i]);
            set(textureEnabled[i], GlStateManager::enableTexture2D, GlStateManager::disableTexture2D);
            GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, textureEnv[i]);
        }
        GlStateManager.setActiveTexture(activeTexture);
        GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
        GlStateManager.clearColor(clearColor.get(0), clearColor.get(1), clearColor.get(2), clearColor.get(3));
    }

    private static void set(boolean enabled, Runnable enable, Runnable disable) {
        if (enabled) {
            enable.run();
        } else {
            disable.run();
        }
    }
}