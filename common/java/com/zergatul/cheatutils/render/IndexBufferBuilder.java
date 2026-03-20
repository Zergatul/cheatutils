package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class IndexBufferBuilder {

    private final ByteBufferBuilder vertexBuffer = new ByteBufferBuilder(0x10000);
    private final ByteBufferBuilder indexBuffer = new ByteBufferBuilder(0x10000);
    private int vertices;
    private int indexes;

    public IndexBufferBuilder() {}

    public ByteBufferBuilder.Result getIndexBuffer() {
        return indexBuffer.build();
    }

    public ByteBufferBuilder.Result getVertexBuffer() {
        return vertexBuffer.build();
    }

    public int getIndexCount() {
        return indexes;
    }

    public int vertex(float x, float y, float z) {
        long pointer = vertexBuffer.reserve(3 * 4);
        MemoryUtil.memPutFloat(pointer, x);
        MemoryUtil.memPutFloat(pointer + 4L, y);
        MemoryUtil.memPutFloat(pointer + 8L, z);
        return vertices++;
    }

    public void triangle(int i1, int i2, int i3) {
        long pointer = indexBuffer.reserve(3 * 4);
        MemoryUtil.memPutInt(pointer, i1);
        MemoryUtil.memPutInt(pointer + 4L, i2);
        MemoryUtil.memPutInt(pointer + 8L, i3);
        indexes += 3;
    }

    public void clear() {
        indexes = 0;
        vertices = 0;
        indexBuffer.clear();
        vertexBuffer.clear();
    }
}