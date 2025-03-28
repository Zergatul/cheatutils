package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class Position2dColorVertexData extends AbstractVertexData {

    @Override
    protected void bindAttributes() {
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, getBytesPerVertex(), 0);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(1, 4, GL30.GL_FLOAT, false, getBytesPerVertex(), 2 * 4);
        GL30.glEnableVertexAttribArray(1);
    }

    @Override
    protected int getBytesPerVertex() {
        return 6 * 4;
    }
}