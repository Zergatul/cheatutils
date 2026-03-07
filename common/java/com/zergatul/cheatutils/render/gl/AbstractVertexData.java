package com.zergatul.cheatutils.render.gl;

import it.unimi.dsi.fastutil.floats.FloatList;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public abstract class AbstractVertexData {

    public VertexArrayObject VAO;
    public VertexBufferObject VBO;

    private ByteBuffer buffer;
    private int position;

    public AbstractVertexData() {
        VAO = new VertexArrayObject();
        VBO = new VertexBufferObject();

        VAO.bind();
        VBO.bind();

        bindAttributes();

        VBO.unbind();
        VAO.unbind();

        buffer = MemoryUtil.memAlloc(65536);
        position = 0;
    }

    public void add(float value) {
        ensureCapacity(position + 4);
        buffer.putFloat(position, value);
        position += 4;
    }

    public void add(FloatList list) {
        ensureCapacity(position + list.size() * 4);

        int position = this.position;
        for (int i = 0; i < list.size(); i++) {
            buffer.putFloat(position, list.getFloat(i));
            position += 4;
        }

        this.position = position;
    }

    public void clear() {
        position = 0;
    }

    public int vertices() {
        return position / getBytesPerVertex();
    }

    public void upload() {
        buffer.limit(position);
        buffer.position(0);

        VBO.bind();
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buffer, GL30.GL_DYNAMIC_DRAW);
        VBO.unbind();

        buffer.limit(buffer.capacity());
    }

    public void delete() {
        VAO.delete();
        VBO.delete();
        MemoryUtil.memFree(buffer);
        buffer = null;
    }

    protected abstract void bindAttributes();

    protected abstract int getBytesPerVertex();

    private void ensureCapacity(int required) {
        if (required <= buffer.capacity()) {
            return;
        }

        int capacity = buffer.capacity();
        while (capacity < required) {
            capacity *= 2;
        }

        buffer = MemoryUtil.memRealloc(buffer, capacity);
    }
}