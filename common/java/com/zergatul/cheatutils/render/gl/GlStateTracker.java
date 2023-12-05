package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class GlStateTracker {

    private static int VAO;
    private static int VBO;

    public static void save() {
        VAO = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        VBO = GL30.glGetInteger(GL30.GL_ARRAY_BUFFER_BINDING);
    }

    public static void restore() {
        GL30.glBindVertexArray(VAO);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
    }
}