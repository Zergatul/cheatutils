package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL43;

public class ShaderStorageBufferObject {

    private int id;

    public ShaderStorageBufferObject() {
        id = GL43.glGenBuffers();
    }

    public void bind() {
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, id);
    }

    public void unbind() {
        GL43.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void delete() {
        if (id != 0) {
            GL43.glDeleteBuffers(id);
            id = 0;
        }
    }
}