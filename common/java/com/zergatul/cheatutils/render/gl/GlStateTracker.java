package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class GlStateTracker {

    private static int VAO;
    private static int VBO;
    private static boolean blend;
    private static boolean depth;
    private static boolean cull;
    private static int texture;
    private static int program;

    private static int binding0;
    private static int binding1;

    public static void save() {
        VAO = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        VBO = GL30.glGetInteger(GL30.GL_ARRAY_BUFFER_BINDING);
        blend = GL30.glIsEnabled(GL30.GL_BLEND);
        depth = GL30.glIsEnabled(GL30.GL_DEPTH_TEST);
        cull = GL30.glIsEnabled(GL30.GL_CULL_FACE);
        texture = GL30.glGetInteger(GL30.GL_ACTIVE_TEXTURE);

        program = GL30.glGetInteger(GL30.GL_CURRENT_PROGRAM);

        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        binding0 = GL30.glGetInteger(GL30.GL_TEXTURE_BINDING_2D);
        GL30.glActiveTexture(GL30.GL_TEXTURE1);
        binding1 = GL30.glGetInteger(GL30.GL_TEXTURE_BINDING_2D);
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

        GL30.glUseProgram(program);

        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, binding0);
        GL30.glActiveTexture(GL30.GL_TEXTURE1);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, binding1);

        GL30.glActiveTexture(texture);
    }
}