package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class EspGroupTrianglesAAProgram extends Program {

    private final int colorUniform;
    private final int lineWidthUniform;
    private final int featherUniform;

    public EspGroupTrianglesAAProgram() {
        super("esp-group-triangles-aa", new VertexData());

        colorUniform = GL30.glGetUniformLocation(id, "Color");
        if (colorUniform == -1) {
            throw new IllegalStateException("Cannot find uniform.");
        }

        lineWidthUniform = GL30.glGetUniformLocation(id, "LineWidth");
        if (lineWidthUniform == -1) {
            throw new IllegalStateException("Cannot find uniform.");
        }

        featherUniform = GL30.glGetUniformLocation(id, "Feather");
        if (featherUniform == -1) {
            throw new IllegalStateException("Cannot find uniform.");
        }
    }

    public void draw(float r, float g, float b, float a, float width, float feather) {
        buffer.upload();

        GL30.glUseProgram(id);
        GL30.glUniform4f(colorUniform, r, g, b, a);
        GL30.glUniform1f(lineWidthUniform, width);
        GL30.glUniform1f(featherUniform, feather);

        buffer.VAO.bind();
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, buffer.vertices());
        buffer.VAO.unbind();
    }

    @Override
    protected void bindAttributes() {
        GL30.glBindAttribLocation(id, 0, "Position");
        GL30.glBindAttribLocation(id, 1, "Gradient");
    }

    private static class VertexData extends AbstractVertexData {

        @Override
        protected void bindAttributes() {
            GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, getBytesPerVertex(), 0);
            GL30.glEnableVertexAttribArray(0);
            GL30.glVertexAttribPointer(1, 1, GL30.GL_FLOAT, false, getBytesPerVertex(), 3 * 4);
            GL30.glEnableVertexAttribArray(1);
        }

        @Override
        protected int getBytesPerVertex() {
            return 4 * 4;
        }
    }
}
