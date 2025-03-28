package com.zergatul.cheatutils.render.gl;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

public class Position2dColorProgram extends Program {

    private final int mvpUniform;

    public Position2dColorProgram() {
        super("position-2d-color", new Position2dColorVertexData());

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
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, buffer.vertices());
        buffer.VAO.unbind();
    }

    @Override
    protected void bindAttributes() {
        GL30.glBindAttribLocation(id, 0, "InPosition");
        GL30.glBindAttribLocation(id, 1, "InColor");
    }
}
