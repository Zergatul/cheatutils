package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class EspTrianglesAAProgram extends Program {

    private final int featherUniform;

    public EspTrianglesAAProgram() {
        super("esp-triangles-aa", new VertexData());

        featherUniform = GL30.glGetUniformLocation(id, "Feather");
        if (featherUniform == -1) {
            throw new IllegalStateException("Cannot find uniform.");
        }
    }

    public void draw(float feather) {
        buffer.upload();

        GL30.glUseProgram(id);
        GL30.glUniform1f(featherUniform, feather);

        buffer.VAO.bind();
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, buffer.vertices());
        buffer.VAO.unbind();
    }

    @Override
    protected void bindAttributes() {
        GL30.glBindAttribLocation(id, 0, "Position");
        GL30.glBindAttribLocation(id, 1, "InColor");
        GL30.glBindAttribLocation(id, 2, "Gradient");
        GL30.glBindAttribLocation(id, 3, "LineWidth");
    }

    private static class VertexData extends AbstractVertexData {

        @Override
        protected void bindAttributes() {
            GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, getBytesPerVertex(), 0);
            GL30.glEnableVertexAttribArray(0);
            GL30.glVertexAttribPointer(1, 4, GL30.GL_FLOAT, false, getBytesPerVertex(), 3 * 4);
            GL30.glEnableVertexAttribArray(1);
            GL30.glVertexAttribPointer(2, 1, GL30.GL_FLOAT, false, getBytesPerVertex(), 7 * 4);
            GL30.glEnableVertexAttribArray(2);
            GL30.glVertexAttribPointer(3, 1, GL30.GL_FLOAT, false, getBytesPerVertex(), 8 * 4);
            GL30.glEnableVertexAttribArray(3);
        }

        @Override
        protected int getBytesPerVertex() {
            return 9 * 4;
        }
    }
}