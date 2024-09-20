package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class PositionVertexData extends AbstractVertexData {

    @Override
    protected void bindAttributes() {
        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, getBytesPerVertex(), 0);
        GL30.glEnableVertexAttribArray(0);
    }

    @Override
    protected int getBytesPerVertex() {
        return 3 * 4;
    }
}