package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class EspGroupTrianglesProgram extends Program {

    private final int colorUniform;

    public EspGroupTrianglesProgram() {
        super("esp-group-triangles", new PositionVertexData());

        colorUniform = GL30.glGetUniformLocation(id, "Color");
        if (colorUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }
    }

    public void draw(float r, float g, float b, float a) {
        buffer.upload();

        GL30.glUseProgram(id);
        GL30.glUniform4f(colorUniform, r, g, b, a);

        buffer.VAO.bind();
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, buffer.vertices());
        buffer.VAO.unbind();
    }

    @Override
    protected void bindAttributes() {
        GL30.glBindAttribLocation(id, 0, "Position");
    }
}