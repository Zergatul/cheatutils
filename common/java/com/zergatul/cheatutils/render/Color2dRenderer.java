package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.zergatul.cheatutils.render.gl.Position2dColorProgram;
import it.unimi.dsi.fastutil.floats.FloatList;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;

public class Color2dRenderer {

    private Position2dColorProgram program;

    public void begin() {
        createGlObjectsIfRequired();
        program.buffer.clear();
    }

    public void fill(FloatList list) {
        program.buffer.add(list);
    }

    public void rect(float x, float y, float width, float height, float red, float green, float blue, float alpha) {
        quad(
                x, y,
                x, y + height,
                x + width, y + height,
                x + width, y,
                red, green, blue, alpha);
    }

    public void quad(
            float x1, float y1,
            float x2, float y2,
            float x3, float y3,
            float x4, float y4,
            float r, float g, float b, float a
    ) {
        program.buffer.add(x1);
        program.buffer.add(y1);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x2);
        program.buffer.add(y2);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x3);
        program.buffer.add(y3);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x1);
        program.buffer.add(y1);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x4);
        program.buffer.add(y4);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);

        program.buffer.add(x3);
        program.buffer.add(y3);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);
    }

    public void end(Matrix4f matrix) {
        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        GlStateManager._disableDepthTest();
        GlStateManager._disableCull();

        program.draw(matrix);
    }

    private void createGlObjectsIfRequired() {
        if (program == null) {
            program = new Position2dColorProgram();
        }
    }
}