package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class PositionTextureVertexData extends AbstractVertexData {

    @Override
    protected void bindAttributes() {
        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, getBytesPerVertex(), 0);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, getBytesPerVertex(), 3 * 4);
        GL30.glEnableVertexAttribArray(1);
    }

    @Override
    protected int getBytesPerVertex() {
        return 5 * 4;
    }
}