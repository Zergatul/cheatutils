package com.zergatul.cheatutils.render;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

class ShaderProgram implements AutoCloseable {

    private int id;

    public ShaderProgram(String vertexShader, String fragmentShader, String... attributes) {
        int vertex = compile(GL20.GL_VERTEX_SHADER, vertexShader);
        int fragment = 0;
        try {
            fragment = compile(GL20.GL_FRAGMENT_SHADER, fragmentShader);
            id = GL20.glCreateProgram();
            GL20.glAttachShader(id, vertex);
            GL20.glAttachShader(id, fragment);
            for (int i = 0; i < attributes.length; i++) {
                GL20.glBindAttribLocation(id, i, attributes[i]);
            }
            GL30.glBindFragDataLocation(id, 0, "fragColor");
            GL20.glLinkProgram(id);
            if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
                throw new IllegalStateException("Cannot link shader program:\n" + GL20.glGetProgramInfoLog(id));
            }
        } catch (Throwable e) {
            if (id != 0) {
                GL20.glDeleteProgram(id);
                id = 0;
            }
            throw e;
        } finally {
            if (id != 0) {
                GL20.glDetachShader(id, vertex);
                GL20.glDetachShader(id, fragment);
            }
            GL20.glDeleteShader(vertex);
            if (fragment != 0) {
                GL20.glDeleteShader(fragment);
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getUniform(String name) {
        int location = GL20.glGetUniformLocation(id, name);
        if (location < 0) {
            throw new IllegalStateException("Cannot find shader uniform: " + name);
        }
        return location;
    }

    @Override
    public void close() {
        if (id != 0) {
            GL20.glDeleteProgram(id);
            id = 0;
        }
    }

    private static int compile(int type, String resource) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, readResource(resource));
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new IllegalStateException("Cannot compile shader " + resource + ":\n" + log);
        }
        return shader;
    }

    private static String readResource(String path) {
        ClassLoader classLoader = ShaderProgram.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Cannot find shader resource: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read shader resource: " + path, e);
        }
    }
}