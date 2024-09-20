package com.zergatul.cheatutils.render.gl;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

public class EspGroupLinesProgram extends Program {

    private final int mvpUniform;
    private final int colorUniform;
    private final float[] floats = new float[16];

    public EspGroupLinesProgram() {
        super("esp-group-lines", new PositionVertexData());

        mvpUniform = GL30.glGetUniformLocation(id, "MVP");
        if (mvpUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }

        colorUniform = GL30.glGetUniformLocation(id, "Color");
        if (colorUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }
    }

    public void draw(Matrix4f mvp, float r, float g, float b, float a) {
        buffer.upload();

        GL30.glUseProgram(id);

        GL30.glUniformMatrix4fv(mvpUniform, false, mvp.get(floats));
        GL30.glUniform4f(colorUniform, r, g, b, a);

        buffer.VAO.bind();
        GL30.glDrawArrays(GL30.GL_LINES, 0, buffer.vertices());
        buffer.VAO.unbind();
    }

    @Override
    protected void bindAttributes() {
        GL30.glBindAttribLocation(id, 0, "Position");
    }
}