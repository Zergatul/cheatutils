package com.zergatul.cheatutils.render.gl;

import com.mojang.blaze3d.opengl.GlStateManager;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public class Position3dTextureProgram extends Program {

    private final int textureUniform;
    private final int mvpUniform;

    public Position3dTextureProgram() {
        super("position-3d-tex", new Position3dTextureVertexData());

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

        GlStateManager._activeTexture(GL30.GL_TEXTURE0 + unit);
        GlStateManager._bindTexture(texture);
        GL33.glBindSampler(unit, Sampler.DEFAULT.getId());

        GL30.glUniform1i(textureUniform, unit);
        GL30.glUniformMatrix4fv(mvpUniform, false, mvp.get(new float[16]));

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