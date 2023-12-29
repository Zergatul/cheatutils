package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class OutlineDrawProgram extends Program {

    private final int textureUniform;
    private final int overlayColorUniform;
    private final int pixelWidthUniform;
    private final int pixelHeightUniform;

    public OutlineDrawProgram() {
        super("outline-draw", new PositionTextureVertexBufferData());

        textureUniform = GL30.glGetUniformLocation(id, "BufferTexture");
        if (textureUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }

        overlayColorUniform = GL30.glGetUniformLocation(id, "OverlayColor");
        if (overlayColorUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }

        pixelWidthUniform = GL30.glGetUniformLocation(id, "PixelWidth");
        if (pixelWidthUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }

        pixelHeightUniform = GL30.glGetUniformLocation(id, "PixelHeight");
        if (pixelHeightUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }
    }

    public void draw(FrameBuffer fb, float r, float g, float b, float a) {
        buffer.upload();

        GL30.glUseProgram(id);
        GL30.glUniform1i(textureUniform, 0);
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        fb.bindTexture();
        GL30.glUniform1f(pixelWidthUniform, fb.getPixelWidth());
        GL30.glUniform1f(pixelHeightUniform, fb.getPixelHeight());
        GL30.glUniform4f(overlayColorUniform, r, g, b, a);

        buffer.VAO.bind();
        buffer.draw(GL30.GL_TRIANGLES);
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
