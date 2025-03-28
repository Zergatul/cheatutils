package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.render.gl.Position3dTextureProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL30.*;

public class Texture3dRenderer {

    private Position3dTextureProgram program;

    public void begin() {
        createGlObjectsIfRequired();
        program.buffer.clear();
    }

    public void quad(
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            float x4, float y4, float z4, float u4, float v4
    ) {
        program.buffer.add(x1);
        program.buffer.add(y1);
        program.buffer.add(z1);
        program.buffer.add(u1);
        program.buffer.add(v1);

        program.buffer.add(x2);
        program.buffer.add(y2);
        program.buffer.add(z2);
        program.buffer.add(u2);
        program.buffer.add(v2);

        program.buffer.add(x3);
        program.buffer.add(y3);
        program.buffer.add(z3);
        program.buffer.add(u3);
        program.buffer.add(v3);

        program.buffer.add(x1);
        program.buffer.add(y1);
        program.buffer.add(z1);
        program.buffer.add(u1);
        program.buffer.add(v1);

        program.buffer.add(x3);
        program.buffer.add(y3);
        program.buffer.add(z3);
        program.buffer.add(u3);
        program.buffer.add(v3);

        program.buffer.add(x4);
        program.buffer.add(y4);
        program.buffer.add(z4);
        program.buffer.add(u4);
        program.buffer.add(v4);


    }

    public void end(int width, int height, int textureId) {
        Matrix4f matrix = new Matrix4f().ortho(0, width, height, 0, -1, 1);
        end(matrix, textureId);
    }

    public void end(Matrix4f matrix, int textureId) {
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        program.draw(matrix, textureId);
    }

    private void createGlObjectsIfRequired() {
        if (program == null) {
            program = new Position3dTextureProgram();
        }
    }
}