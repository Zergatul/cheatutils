package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.render.gl.EntityOverlayBufferProgram;
import com.zergatul.cheatutils.render.gl.FrameBuffer;
import com.zergatul.cheatutils.render.gl.OverlayDrawProgram;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

public class EntityOverlayRenderer {

    private EntityOverlayBufferProgram bufferProgram;
    private OverlayDrawProgram drawProgram;

    public void begin() {
        createGlObjectsIfRequired();

        bufferProgram.buffer.clear();
        drawProgram.buffer.clear();

        FrameBuffer.push();

        // set draw settings
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        GL30.glDisable(GL30.GL_DEPTH_TEST);
        GL30.glDisable(GL30.GL_CULL_FACE);

        // clear framebuffer
        FrameBuffers.get1().bind();
        GL30.glClearColor(0, 0, 0, 0);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);
    }

    public void quad(
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            float x4, float y4, float z4, float u4, float v4
    ) {
        triangle(
                x1, y1, z1, u1, v1,
                x2, y2, z2, u2, v2,
                x4, y4, z4, u4, v4);
        triangle(
                x2, y2, z2, u2, v2,
                x3, y3, z3, u3, v3,
                x4, y4, z4, u4, v4);
    }

    public void triangle(
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3
    ) {
        bufferProgram.buffer.add(x1);
        bufferProgram.buffer.add(y1);
        bufferProgram.buffer.add(z1);
        bufferProgram.buffer.add(u1);
        bufferProgram.buffer.add(v1);

        bufferProgram.buffer.add(x2);
        bufferProgram.buffer.add(y2);
        bufferProgram.buffer.add(z2);
        bufferProgram.buffer.add(u2);
        bufferProgram.buffer.add(v2);

        bufferProgram.buffer.add(x3);
        bufferProgram.buffer.add(y3);
        bufferProgram.buffer.add(z3);
        bufferProgram.buffer.add(u3);
        bufferProgram.buffer.add(v3);
    }

    public void renderBuffer(Matrix4f projection, int textureId) {
        bufferProgram.draw(projection, textureId);
        bufferProgram.buffer.clear();
    }

    public void end(float red, float green, float blue, float alpha) {
        // restore settings
        GL30.glEnable(GL30.GL_CULL_FACE);

        FrameBuffer.pop();

        // set draw settings
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        GL30.glDisable(GL30.GL_DEPTH_TEST);

        // draw with shader program
        drawProgram.buffer.add(-1);
        drawProgram.buffer.add(-1);
        drawProgram.buffer.add(0);
        drawProgram.buffer.add(0);
        drawProgram.buffer.add(0);

        drawProgram.buffer.add(1);
        drawProgram.buffer.add(-1);
        drawProgram.buffer.add(0);
        drawProgram.buffer.add(1);
        drawProgram.buffer.add(0);

        drawProgram.buffer.add(-1);
        drawProgram.buffer.add(1);
        drawProgram.buffer.add(0);
        drawProgram.buffer.add(0);
        drawProgram.buffer.add(1);

        drawProgram.buffer.add(1);
        drawProgram.buffer.add(1);
        drawProgram.buffer.add(0);
        drawProgram.buffer.add(1);
        drawProgram.buffer.add(1);

        drawProgram.buffer.add(-1);
        drawProgram.buffer.add(1);
        drawProgram.buffer.add(0);
        drawProgram.buffer.add(0);
        drawProgram.buffer.add(1);

        drawProgram.buffer.add(1);
        drawProgram.buffer.add(-1);
        drawProgram.buffer.add(0);
        drawProgram.buffer.add(1);
        drawProgram.buffer.add(0);

        drawProgram.draw(FrameBuffers.get1(), red, green, blue, alpha);
        //drawProgram.unbind();

        // reset settings
        GL30.glDisable(GL30.GL_BLEND);
        GL30.glEnable(GL30.GL_DEPTH_TEST);
    }

    public void close() {
        bufferProgram.delete();
        drawProgram.delete();
    }

    private void createGlObjectsIfRequired() {
        if (bufferProgram == null) {
            bufferProgram = new EntityOverlayBufferProgram();
            drawProgram = new OverlayDrawProgram();
        }
    }
}