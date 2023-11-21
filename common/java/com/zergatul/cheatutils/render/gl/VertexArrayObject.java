package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public class VertexArrayObject {

    private int id;

    public VertexArrayObject() {
        id = GL30.glGenVertexArrays();
    }

    public void bind() {
        GL30.glBindVertexArray(id);
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    public void delete() {
        if (id != 0) {
            GL30.glDeleteVertexArrays(id);
            id = 0;
        }
    }
}