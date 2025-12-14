package com.zergatul.cheatutils.render.gl;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

public class Position2dTextureColorProgram extends Program {

    private final int textureUniform;
    private final int mvpUniform;

    public Position2dTextureColorProgram() {
        super("position-2d-tex-color", new Position2dTextureColorVertexData());

        textureUniform = GL30.glGetUniformLocation(id, "Texture");
        if (textureUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }

        mvpUniform = GL30.glGetUniformLocation(id, "MVP");
        if (mvpUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }
    }

    public void draw(Matrix4f mvp, int texture) {
        buffer.upload();

        GL30.glUseProgram(id);
        GL30.glUniform1i(textureUniform, 0);
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);
        GL30.glUniformMatrix4fv(mvpUniform, false, mvp.get(new float[16]));

        buffer.VAO.bind();
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, buffer.vertices());
        buffer.VAO.unbind();
    }

    @Override
    protected void bindAttributes() {
        GL30.glBindAttribLocation(id, 0, "InPosition");
        GL30.glBindAttribLocation(id, 1, "InTexCoords");
        GL30.glBindAttribLocation(id, 2, "InColor");
    }
}