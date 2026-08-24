package com.zergatul.cheatutils.render;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
        this.data = MemoryUtil.memAlloc(INITIAL_CAPACITY).order(ByteOrder.nativeOrder());
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
        if (!GL.getCapabilities().OpenGL33 && !GL.getCapabilities().GL_ARB_instanced_arrays) {
            throw new IllegalStateException("Block ESP instancing requires OpenGL 3.3 or GL_ARB_instanced_arrays");
        }
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
    }

    public void attribute(int index, int size, int type, boolean normalized, int offset) {
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index, size, type, normalized, stride, offset);
        if (GL.getCapabilities().OpenGL33) {
            GL33.glVertexAttribDivisor(index, 1);
        } else {
            ARBInstancedArrays.glVertexAttribDivisorARB(index, 1);
        }
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
        if (data != null) {
            MemoryUtil.memFree(data);
            data = null;
        }
    }

    private void ensureCpuCapacity(int additional) {
        if (data.remaining() >= additional) {
            return;
        }
        int position = data.position();
        int newCapacity = nextPowerOfTwo(data.position() + additional);
        data = MemoryUtil.memRealloc(data, newCapacity).order(ByteOrder.nativeOrder());
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