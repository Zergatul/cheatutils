package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class Position2dTextureColorVertexData extends AbstractVertexData {

    @Override
    protected void bindAttributes() {
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, getBytesPerVertex(), 0);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, getBytesPerVertex(), 2 * 4);
        GL30.glEnableVertexAttribArray(1);
        GL30.glVertexAttribPointer(2, 4, GL30.GL_FLOAT, false, getBytesPerVertex(), 4 * 4);
        GL30.glEnableVertexAttribArray(2);
    }

    @Override
    protected int getBytesPerVertex() {
        return 8 * 4;
    }
}