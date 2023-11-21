package com.zergatul.cheatutils.render.gl;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

public class EspLinesProgram extends Program {

    private final int modelViewUniform;
    private final int projectionUniform;

    public EspLinesProgram() {
        super("esp-lines", new PositionColorVertexData());

        modelViewUniform = GL30.glGetUniformLocation(id, "ModelView");
        if (modelViewUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }

        projectionUniform = GL30.glGetUniformLocation(id, "Projection");
        if (projectionUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }
    }

    public void draw(Matrix4f modelView, Matrix4f projection) {
        buffer.upload();

        GL30.glUseProgram(id);
        GL30.glUniformMatrix4fv(modelViewUniform, false, modelView.get(new float[16]));
        GL30.glUniformMatrix4fv(projectionUniform, false, projection.get(new float[16]));

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