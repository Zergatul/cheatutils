package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class GlStateTracker {

    private static int VAO;
    private static int VBO;
    private static boolean blend;
    private static boolean depth;
    private static boolean cull;
    private static int texture;
    private static int binding;
    private static int program;

    public static void save() {
        VAO = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        VBO = GL30.glGetInteger(GL30.GL_ARRAY_BUFFER_BINDING);
        blend = GL30.glGetBoolean(GL30.GL_BLEND);
        depth = GL30.glGetBoolean(GL30.GL_DEPTH_TEST);
        cull = GL30.glGetBoolean(GL30.GL_CULL_FACE);
        texture = GL30.glGetInteger(GL30.GL_ACTIVE_TEXTURE);
        binding = GL30.glGetInteger(GL30.GL_TEXTURE_BINDING_2D);
        program = GL30.glGetInteger(GL30.GL_CURRENT_PROGRAM);
    }

    public static void restore() {
        GL30.glBindVertexArray(VAO);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);

        if (blend) {
            GL30.glEnable(GL30.GL_BLEND);
        } else {
            GL30.glDisable(GL30.GL_BLEND);
        }

        if (depth) {
            GL30.glEnable(GL30.GL_DEPTH_TEST);
        } else {
            GL30.glDisable(GL30.GL_DEPTH_TEST);
        }

        if (cull) {
            GL30.glEnable(GL30.GL_CULL_FACE);
        } else {
            GL30.glDisable(GL30.GL_CULL_FACE);
        }

        GL30.glActiveTexture(texture);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, binding);
        GL30.glUseProgram(program);
    }
}