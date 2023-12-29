package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class PositionVertexBufferData extends AbstractVertexBufferData {

    @Override
    protected void bindAttributes() {
        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, valuesPerVertex() * 4, 0);
        GL30.glEnableVertexAttribArray(0);
    }

    @Override
    protected int valuesPerVertex() {
        return 3;
    }
}