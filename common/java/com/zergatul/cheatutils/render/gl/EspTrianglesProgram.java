package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class EspTrianglesProgram extends Program {

    public EspTrianglesProgram() {
        super("esp-triangles", new PositionColorVertexData());
    }

    public void draw() {
        buffer.upload();

        GL30.glUseProgram(id);

        buffer.VAO.bind();
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, buffer.vertices());
        buffer.VAO.unbind();
    }

    @Override
    protected void bindAttributes() {
        GL30.glBindAttribLocation(id, 0, "Position");
        GL30.glBindAttribLocation(id, 1, "InColor");
    }
}