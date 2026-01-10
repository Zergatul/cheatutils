package com.zergatul.cheatutils.render.gl;

import com.mojang.blaze3d.opengl.GlStateManager;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public class OutlineDrawProgram extends Program {

    private final int textureUniform;
    private final int overlayColorUniform;
    private final int pixelWidthUniform;
    private final int pixelHeightUniform;

    public OutlineDrawProgram() {
        super("outline-draw", new Position3dTextureVertexData());

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

        GlStateManager._activeTexture(GL30.GL_TEXTURE0 + unit);
        fb.bindTexture();
        GL33.glBindSampler(unit, Sampler.DEFAULT.getId());

        GL30.glUniform1i(textureUniform, unit);
        GL30.glUniform1f(pixelWidthUniform, fb.getPixelWidth());
        GL30.glUniform1f(pixelHeightUniform, fb.getPixelHeight());
        GL30.glUniform4f(overlayColorUniform, r, g, b, a);

        buffer.VAO.bind();
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, buffer.vertices());
        buffer.VAO.unbind();
    }

    @Override
    protected void bindAttributes() {
        GL30.glBindAttribLocation(id, 0, "InPosition");
        GL30.glBindAttribLocation(id, 1, "InTexCoords");
    }
}