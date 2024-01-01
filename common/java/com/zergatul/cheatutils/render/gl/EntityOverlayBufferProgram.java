package com.zergatul.cheatutils.render.gl;

import com.zergatul.cheatutils.render.TextureStateTracker;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

public class EntityOverlayBufferProgram extends Program {

    private final int projectionUniform;
    private final int textureUniform;

    public EntityOverlayBufferProgram() {
        super("entity-overlay-buffer", new PositionTextureVertexData());

        projectionUniform = GL30.glGetUniformLocation(id, "Projection");
        if (projectionUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }

        textureUniform = GL30.glGetUniformLocation(id, "EntityTexture");
        if (textureUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }
    }

    public void draw(Matrix4f projection, int textureId) {
        buffer.upload();

        GL30.glUseProgram(id);
        GL30.glUniformMatrix4fv(projectionUniform, false, projection.get(new float[16]));
        GL30.glUniform1i(textureUniform, 0);
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureId);

        TextureStateTracker.setTextureMinFilter(textureId, GL30.GL_NEAREST);

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