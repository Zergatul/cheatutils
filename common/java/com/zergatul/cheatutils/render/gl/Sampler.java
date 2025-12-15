package com.zergatul.cheatutils.render.gl;

import org.lwjgl.opengl.GL33;

public class Sampler {

    public static final Sampler DEFAULT = new Sampler(GL33.GL_NEAREST, GL33.GL_NEAREST);

    private final int id;

    public Sampler(int minFilter, int magFilter) {
        this.id = GL33.glGenSamplers();
        GL33.glSamplerParameteri(id, GL33.GL_TEXTURE_MIN_FILTER, minFilter);
        GL33.glSamplerParameteri(id, GL33.GL_TEXTURE_MAG_FILTER, magFilter);
    }

    public int getId() {
        return id;
    }
}