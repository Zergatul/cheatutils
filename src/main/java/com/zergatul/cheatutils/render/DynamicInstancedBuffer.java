package com.zergatul.cheatutils.render;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

class DynamicInstancedBuffer implements AutoCloseable {

    private static final int INITIAL_CAPACITY = 4096;

    private final int stride;
    private ByteBuffer data;
    private int vao;
    private int vbo;
    private int gpuCapacity;
    private int instances;

    public DynamicInstancedBuffer(int stride) {
        this.stride = stride;
        this.data = BufferUtils.createByteBuffer(INITIAL_CAPACITY);
    }

    public void begin() {
        data.clear();
        instances = 0;
    }

    public ByteBuffer record() {
        ensureCpuCapacity(stride);
        instances++;
        return data;
    }

    public int getInstances() {
        return instances;
    }

    public void initialize() {
        if (vao != 0) {
            return;
        }
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
    }

    public void attribute(int index, int size, int type, boolean normalized, int offset) {
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
        GL33.glVertexAttribDivisor(index, 1);
    }

    public void finishInitialization() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    public void uploadAndBind() {
        data.flip();
        int bytes = data.remaining();
        if (gpuCapacity < bytes) {
            gpuCapacity = nextPowerOfTwo(bytes);
        }

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        // Orphan the previous storage so the driver does not wait for the preceding frame.
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, gpuCapacity, GL15.GL_STREAM_DRAW);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, data);
    }

    public void unbind() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    @Override
    public void close() {
        if (vbo != 0) {
            GL15.glDeleteBuffers(vbo);
            vbo = 0;
        }
        if (vao != 0) {
            GL30.glDeleteVertexArrays(vao);
            vao = 0;
        }
        data = null;
    }

    private void ensureCpuCapacity(int additional) {
        if (data.remaining() >= additional) {
            return;
        }
        int position = data.position();
        int newCapacity = nextPowerOfTwo(data.position() + additional);
        ByteBuffer newData = BufferUtils.createByteBuffer(newCapacity);
        data.flip();
        newData.put(data);
        data = newData;
        data.position(position);
    }

    private static int nextPowerOfTwo(int value) {
        int result = INITIAL_CAPACITY;
        while (result < value) {
            result *= 2;
        }
        return result;
    }
}