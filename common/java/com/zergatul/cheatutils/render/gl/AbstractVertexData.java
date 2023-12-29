package com.zergatul.cheatutils.render.gl;

import com.zergatul.cheatutils.utils.UnsafeUtil;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import sun.misc.Unsafe;

public abstract class AbstractVertexData {

    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);
    private static final Unsafe UNSAFE = UnsafeUtil.get();

    public final VertexArrayObject VAO = new VertexArrayObject();

    private int capacity = 65536;
    private long address = ALLOCATOR.malloc(capacity);
    private int position = 0;

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

    public void delete() {
        VAO.delete();
        ALLOCATOR.free(address);
    }

    public abstract void upload();

    public void draw(int mode) {
        GL30.glDrawArrays(mode, 0, getVertexCount());
    }

    protected void uploadBuffer(int target) {
        GL30.nglBufferData(target, position, address, GL30.GL_DYNAMIC_DRAW);
    }

    protected int getPosition() {
        return position;
    }

    protected abstract int getVertexCount();
}