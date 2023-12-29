package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL43;

public class ShaderStorageVertexBufferData extends AbstractVertexData {

    private final ShaderStorageBufferObject SSBO;

    public ShaderStorageVertexBufferData() {
        SSBO = new ShaderStorageBufferObject();

        VAO.bind();
        SSBO.bind();
        SSBO.unbind();
        VAO.unbind();
    }

    @Override
    public void upload() {
        SSBO.bind();
        uploadBuffer(GL43.GL_SHADER_STORAGE_BUFFER);
        SSBO.unbind();
    }

    public void delete() {
        super.delete();
        SSBO.delete();
    }

    @Override
    protected int getVertexCount() {
        return getPosition() / 4 / 3;
    }
}