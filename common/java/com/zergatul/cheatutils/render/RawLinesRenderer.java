package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.render.gl.RawLinesProgram;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

public class RawLinesRenderer {

    private static RawLinesProgram program = new RawLinesProgram();

    public void begin() {
        program.buffer.clear();
    }

    public void quad(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4
    ) {
        vertex(x1, y1, z1);
        vertex(x2, y2, z2);
        vertex(x3, y3, z3);

        vertex(x3, y3, z3);
        vertex(x4, y4, z4);
        vertex(x1, y1, z1);
    }

    public void line(
            float x1, float y1, float z1,
            float x2, float y2, float z2
    ) {
        vertex(x1, y1, z1);
        vertex(x2, y2, z2);
    }

    public void end(Matrix4f projection) {
        // set line settings
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFuncSeparate(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA, GL30.GL_ONE, GL30.GL_ZERO);
        GL30.glEnable(GL30.GL_LINE_SMOOTH);
        GL30.glDisable(GL30.GL_DEPTH_TEST);

        // draw with shader program
        program.draw(projection);

        // reset settings
        GL30.glDisable(GL30.GL_BLEND);
        GL30.glEnable(GL30.GL_DEPTH_TEST);
    }

    public void close() {
        program.delete();
    }

    public void vertex(float x, float y, float z) {
        program.buffer.add(x);
        program.buffer.add(y);
        program.buffer.add(z);
    }

    public void value(float v) {
        program.buffer.add(v);
    }
}