package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public abstract class AbstractVertexBufferData extends AbstractVertexData {

    private final VertexBufferObject VBO;

    public AbstractVertexBufferData() {
        VBO = new VertexBufferObject();

        VAO.bind();
        VBO.bind();

        bindAttributes();

        VBO.unbind();
        VAO.unbind();
    }

    @Override
    public void upload() {
        VBO.bind();
        uploadBuffer(GL30.GL_ARRAY_BUFFER);
        VBO.unbind();
    }

    @Override
    public void delete() {
        super.delete();
        VBO.delete();
    }

    @Override
    protected int getVertexCount() {
        return getPosition() / 4 / valuesPerVertex();
    }

    protected abstract void bindAttributes();

    protected abstract int valuesPerVertex();
}