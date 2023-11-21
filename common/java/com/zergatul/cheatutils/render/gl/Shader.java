package com.zergatul.cheatutils.render.gl;

import com.zergatul.cheatutils.utils.ResourceHelper;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Shader {

    private int id;
    private final Type type;

    public Shader(String path, Type type) {
        InputStream stream = ResourceHelper.get("shaders/" + path);
        if (stream == null) {
            throw new IllegalStateException("Cannot find shader resource.");
        }

        String code;
        try (stream) {
            code = IOUtils.toString(stream, StandardCharsets.UTF_8);
        }
        catch (Throwable e) {
            throw new IllegalStateException("Cannot read shader code.");
        }

        id = GL20.glCreateShader(type.getValue());
        GL20.glShaderSource(id, code);
        GL20.glCompileShader(id);

        int status = GL30.glGetShaderi(id, GL30.GL_COMPILE_STATUS);
        if (status == GL30.GL_FALSE) {
            String log = GL30.glGetShaderInfoLog(id);
            GL30.glDeleteShader(id);
            throw new IllegalStateException("Cannot compile shader:\n" + log);
        }

        this.type = type;
    }

    public int getId() {
        return this.id;
    }

    public Type getType() {
        return this.type;
    }

    public void delete() {
        if (id != 0) {
            GL30.glDeleteShader(id);
            id = 0;
        }
    }

    public enum Type {
        VERTEX(GL20.GL_VERTEX_SHADER),
        FRAGMENT(GL20.GL_FRAGMENT_SHADER);

        private final int value;

        Type(int value) {
            this.value = value;
        }

        private int getValue() {
            return this.value;
        }
    }
}