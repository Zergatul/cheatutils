package com.zergatul.cheatutils.render.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class GlStateTracker {

    private static int vao;
    private static int vbo;
    private static boolean blend;
    private static boolean depth;
    private static boolean cull;
    private static int activeTexture;
    private static int texture0;
    private static int texture1;
    private static int program;

    public static void save() {
        vao = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        vbo = GL30.glGetInteger(GL30.GL_ARRAY_BUFFER_BINDING);
        blend = GL30.glIsEnabled(GL30.GL_BLEND);
        depth = GL30.glIsEnabled(GL30.GL_DEPTH_TEST);
        cull = GL30.glIsEnabled(GL30.GL_CULL_FACE);
        activeTexture = GL30.glGetInteger(GL30.GL_ACTIVE_TEXTURE);
        program = GL30.glGetInteger(GL20.GL_CURRENT_PROGRAM);

        GL30.glActiveTexture(GL13.GL_TEXTURE0);
        texture0 = GL30.glGetInteger(GL30.GL_TEXTURE_BINDING_2D);
        GL30.glActiveTexture(GL13.GL_TEXTURE1);
        texture1 = GL30.glGetInteger(GL30.GL_TEXTURE_BINDING_2D);
        GL30.glActiveTexture(activeTexture);
    }

    public static void restore() {
        GL30.glBindVertexArray(vao);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);

        if (blend) {
            RenderSystem.enableBlend();
        } else {
            RenderSystem.disableBlend();
        }
        if (depth) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        if (cull) {
            RenderSystem.enableCull();
        } else {
            RenderSystem.disableCull();
        }

        GL20.glUseProgram(program);

        GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._bindTexture(texture0);
        GlStateManager._activeTexture(GL13.GL_TEXTURE1);
        GlStateManager._bindTexture(texture1);
        GlStateManager._activeTexture(activeTexture);
    }
}