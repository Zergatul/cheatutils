package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class VertexBufferObject {

    private int id;

    public VertexBufferObject() {
        id = GL30.glGenBuffers();
    }

    public void bind() {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, id);
    }

    public void unbind() {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
    }

    public void delete() {
        if (id != 0) {
            GL30.glDeleteBuffers(id);
            id = 0;
        }
    }
}