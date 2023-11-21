package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL30;

public abstract class Program {

    protected int id;
    public final AbstractVertexData buffer;

    protected Program(String name, AbstractVertexData buffer) {
        Shader vertexShader = new Shader(name + ".vert", Shader.Type.VERTEX);
        Shader fragmentShader = new Shader(name + ".frag", Shader.Type.FRAGMENT);

        id = GL30.glCreateProgram();
        GL30.glAttachShader(id, vertexShader.getId());
        GL30.glAttachShader(id, fragmentShader.getId());
        bindAttributes();
        GL30.glLinkProgram(id);

        int status = GL30.glGetProgrami(id, GL30.GL_LINK_STATUS);
        if (status == GL30.GL_FALSE) {
            String log = GL30.glGetProgramInfoLog(id);
            GL30.glDeleteProgram(id);
            throw new IllegalStateException("Cannot link program:\n" + log);
        }

        vertexShader.delete();
        fragmentShader.delete();

        this.buffer = buffer;
    }

    protected abstract void bindAttributes();

    public void delete() {
        if (id != 0) {
            GL30.glDeleteProgram(id);
            id = 0;
        }
        buffer.delete();
    }
}