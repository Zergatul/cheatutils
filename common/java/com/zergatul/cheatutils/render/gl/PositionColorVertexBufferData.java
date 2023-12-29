package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class PositionColorVertexBufferData extends AbstractVertexBufferData {

    @Override
    protected void bindAttributes() {
        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, valuesPerVertex() * 4, 0);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(1, 4, GL30.GL_FLOAT, false, valuesPerVertex() * 4, 3 * 4);
        GL30.glEnableVertexAttribArray(1);
    }

    @Override
    protected int valuesPerVertex() {
        return 7;
    }
}