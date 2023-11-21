package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class OverlayDrawProgram extends Program {

    private final int textureUniform;
    private final int overlayColorUniform;

    public OverlayDrawProgram() {
        super("overlay-draw", new PositionTextureVertexData());

        textureUniform = GL30.glGetUniformLocation(id, "BufferTexture");
        if (textureUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }

        overlayColorUniform = GL30.glGetUniformLocation(id, "OverlayColor");
        if (overlayColorUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }
    }

    public void draw(FrameBuffer fb, float r, float g, float b, float a) {
        buffer.upload();

        GL30.glUseProgram(id);
        GL30.glUniform1i(textureUniform, 0);
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        fb.bindTexture();
        GL30.glUniform4f(overlayColorUniform, r, g, b, a);

        buffer.VAO.bind();
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, buffer.vertices());
        buffer.VAO.unbind();
    }

    public void unbind() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
    }

    @Override
    protected void bindAttributes() {
        GL30.glBindAttribLocation(id, 0, "InPosition");
        GL30.glBindAttribLocation(id, 1, "InTexCoords");
    }
}