package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.render.gl.Position2dColorProgram;
import com.zergatul.cheatutils.render.gl.Position3dColorProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;

public class Color3dRenderer {

    private Position3dColorProgram program;

    public void begin() {
        createGlObjectsIfRequired();
        program.buffer.clear();
    }

    public void quad(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float r, float g, float b, float a
    ) {
        program.buffer.add(x1);
        program.buffer.add(y1);
        program.buffer.add(z1);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x2);
        program.buffer.add(y2);
        program.buffer.add(z2);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x3);
        program.buffer.add(y3);
        program.buffer.add(z3);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x1);
        program.buffer.add(y1);
        program.buffer.add(z1);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x3);
        program.buffer.add(y3);
        program.buffer.add(z3);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x4);
        program.buffer.add(y4);
        program.buffer.add(z4);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);
    }

    public void end(Matrix4f matrix) {
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        program.draw(matrix);
    }

    private void createGlObjectsIfRequired() {
        if (program == null) {
            program = new Position3dColorProgram();
        }
    }
}