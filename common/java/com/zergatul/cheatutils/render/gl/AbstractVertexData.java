package com.zergatul.cheatutils.render.gl;

import com.zergatul.cheatutils.utils.UnsafeUtil;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import sun.misc.Unsafe;

public abstract class AbstractVertexData {

    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);
    private static final Unsafe UNSAFE = UnsafeUtil.get();

    public VertexArrayObject VAO;
    public VertexBufferObject VBO;

    private long address;
    private int position;
    private int capacity;

    public AbstractVertexData() {
        VAO = new VertexArrayObject();
        VBO = new VertexBufferObject();

        VAO.bind();
        VBO.bind();

        bindAttributes();

        VBO.unbind();
        VAO.unbind();

        capacity = 65536;
        address = ALLOCATOR.malloc(capacity);
        position = 0;
    }

    public void add(float value) {
        if (position == capacity) {
            capacity *= 2;
            address = ALLOCATOR.realloc(address, capacity);
        }

        UNSAFE.putFloat(address + position, value);
        position += 4;
    }

    public void clear() {
        position = 0;
    }

    public int vertices() {
        return position / getBytesPerVertex();
    }

    public void upload() {
        VBO.bind();
        GL30.nglBufferData(GL30.GL_ARRAY_BUFFER, position, address, GL30.GL_DYNAMIC_DRAW);
        VBO.unbind();
    }

    public void delete() {
        VAO.delete();
        VBO.delete();
        ALLOCATOR.free(address);
    }

    protected abstract void bindAttributes();

    protected abstract int getBytesPerVertex();
}