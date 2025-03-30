package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.render.gl.AtlasTexture;
import com.zergatul.cheatutils.render.gl.Position2dTextureColorProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;

public class TextureColor2dRenderer {

    private Position2dTextureColorProgram program;

    public void begin() {
        createGlObjectsIfRequired();
        program.buffer.clear();
    }

    public void rect(float x, float y, float width, float height, float red, float green, float blue, float alpha) {
        quad(
                x, y, 0, 0,
                x, y + height, 0, 1,
                x + width, y + height, 1, 1,
                x + width, y, 1, 0,
                red, green, blue, alpha);
    }

    public void rect(
            float x, float y, float width, float height,
            AtlasTexture.Item item,
            float red, float green, float blue, float alpha) {
        quad(
                x, y, item.getU1(), item.getV1(),
                x, y + height, item.getU1(), item.getV2(),
                x + width, y + height, item.getU2(), item.getV2(),
                x + width, y, item.getU2(), item.getV1(),
                red, green, blue, alpha);
    }

    public void quad(
            float x1, float y1, float u1, float v1,
            float x2, float y2, float u2, float v2,
            float x3, float y3, float u3, float v3,
            float x4, float y4, float u4, float v4,
            float r, float g, float b, float a
    ) {
        program.buffer.add(x1);
        program.buffer.add(y1);
        program.buffer.add(u1);
        program.buffer.add(v1);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x2);
        program.buffer.add(y2);
        program.buffer.add(u2);
        program.buffer.add(v2);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x3);
        program.buffer.add(y3);
        program.buffer.add(u3);
        program.buffer.add(v3);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x1);
        program.buffer.add(y1);
        program.buffer.add(u1);
        program.buffer.add(v1);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x4);
        program.buffer.add(y4);
        program.buffer.add(u4);
        program.buffer.add(v4);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x3);
        program.buffer.add(y3);
        program.buffer.add(u3);
        program.buffer.add(v3);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);
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
            program = new Position2dTextureColorProgram();
        }
    }
}