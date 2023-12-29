package com.zergatul.cheatutils.render.gl;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL30;

public class ThickLinesProgram extends Program {

    private final int mvpUniform;
    private final int resolutionUniform;
    private final int thicknessUniform;

    public ThickLinesProgram() {
        super("thick-lines", new ShaderStorageVertexBufferData());

        mvpUniform = GL30.glGetUniformLocation(id, "u_mvp");
        if (mvpUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }

        resolutionUniform = GL30.glGetUniformLocation(id, "u_resolution");
        if (resolutionUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }

        thicknessUniform = GL30.glGetUniformLocation(id, "u_thickness");
        if (thicknessUniform == -1) {
            throw new IllegalStateException("Cannot find uniform");
        }
    }

    public void draw(Matrix4f mvp, Vector2f resolution, float thickness) {
        buffer.upload();

        GL30.glUseProgram(id);
        GL30.glUniformMatrix4fv(mvpUniform, false, mvp.get(new float[16]));
        GL30.glUniform2f(resolutionUniform, resolution.x, resolution.y);
        GL30.glUniform1f(thicknessUniform, thickness);

        buffer.VAO.bind();
        //buffer.draw(GL30.GL_TRIANGLES);
        buffer.draw(GL30.GL_LINES);
        buffer.VAO.unbind();
    }

    @Override
    protected void bindAttributes() {

    }
}