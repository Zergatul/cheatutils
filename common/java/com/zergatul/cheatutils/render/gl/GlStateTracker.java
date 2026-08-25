package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class GlStateTracker {

    private static int vao;
    private static int vbo;

    public static void save() {
        vao = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        vbo = GL30.glGetInteger(GL30.GL_ARRAY_BUFFER_BINDING);
    }

    public static void restore() {
        GL30.glBindVertexArray(vao);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);
    }
}