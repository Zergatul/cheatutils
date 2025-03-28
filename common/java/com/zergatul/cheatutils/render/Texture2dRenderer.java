package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.render.gl.Position2dTextureProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL30.*;

public class Texture2dRenderer {

    private Position2dTextureProgram program;

    public void begin() {
        createGlObjectsIfRequired();
        program.buffer.clear();
    }

    public void rect(int x, int y, int width, int height, int texX, int texY, int texWidth, int texHeight, int texSizeX, int texSizeY) {
        quad(
                x, y, 1F * texX / texSizeX, 1F * texY / texSizeY,
                x, y + height, 1F * texX / texSizeX, 1F * (texY + texHeight) / texSizeY,
                x + width, y + height, 1F * (texX + texWidth) / texSizeX, 1F * (texY + texHeight) / texSizeY,
                x + width, y, 1F * (texX + texWidth) / texSizeX, 1F * texY / texSizeY);
    }

    public void rect(float x, float y, float width, float height) {
        quad(
                x, y, 0, 0,
                x, y + height, 0, 1,
                x + width, y + height, 1, 1,
                x + width, y, 1, 0);
    }

    public void quad(
            float x1, float y1, float u1, float v1,
            float x2, float y2, float u2, float v2,
            float x3, float y3, float u3, float v3,
            float x4, float y4, float u4, float v4
    ) {
        program.buffer.add(x1);
        program.buffer.add(y1);
        program.buffer.add(u1);
        program.buffer.add(v1);

        program.buffer.add(x2);
        program.buffer.add(y2);
        program.buffer.add(u2);
        program.buffer.add(v2);

        program.buffer.add(x3);
        program.buffer.add(y3);
        program.buffer.add(u3);
        program.buffer.add(v3);

        program.buffer.add(x1);
        program.buffer.add(y1);
        program.buffer.add(u1);
        program.buffer.add(v1);

        program.buffer.add(x4);
        program.buffer.add(y4);
        program.buffer.add(u4);
        program.buffer.add(v4);

        program.buffer.add(x3);
        program.buffer.add(y3);
        program.buffer.add(u3);
        program.buffer.add(v3);
    }

    public void end(int width, int height, int textureId) {
        Matrix4f matrix = new Matrix4f().ortho(0, width, height, 0, -1, 1);
        end(matrix, textureId);
    }

    public void end(Matrix4f matrix, int textureId) {
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        program.draw(matrix, textureId);

        glEnable(GL_DEPTH_TEST);
    }

    private void createGlObjectsIfRequired() {
        if (program == null) {
            program = new Position2dTextureProgram();
        }
    }
}