package com.zergatul.cheatutils.render.gl;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

public class EspLinesProgram extends Program {

    private final int mvpUniform;

    public EspLinesProgram() {
        super("esp-lines", new PositionColorVertexData());

        mvpUniform = GL30.glGetUniformLocation(id, "MVP");
        if (mvpUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }
    }

    public void draw(Matrix4f mvp) {
        buffer.upload();

        GL30.glUseProgram(id);
        GL30.glUniformMatrix4fv(mvpUniform, false, mvp.get(new float[16]));

        buffer.VAO.bind();
        GL30.glDrawArrays(GL30.GL_LINES, 0, buffer.vertices());
        buffer.VAO.unbind();
    }

    @Override
    protected void bindAttributes() {
        GL30.glBindAttribLocation(id, 0, "Position");
        GL30.glBindAttribLocation(id, 1, "InColor");
    }
}