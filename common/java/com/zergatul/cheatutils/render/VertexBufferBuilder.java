package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import org.lwjgl.system.MemoryUtil;

public class VertexBufferBuilder {

    private final ByteBufferBuilder vertexBuffer = new ByteBufferBuilder(0x10000);
    private int vertices;

    public ByteBufferBuilder.Result getVertexBuffer() {
        return vertexBuffer.build();
    }

    public int getVertexCount() {
        return vertices;
    }

    public void clear() {
        vertices = 0;
        vertexBuffer.clear();
    }

    public void vertexFloat4(float f1, float f2, float f3, float f4) {
        long pointer = vertexBuffer.reserve(4 * 4);
        MemoryUtil.memPutFloat(pointer + 0x0L, f1);
        MemoryUtil.memPutFloat(pointer + 0x4L, f2);
        MemoryUtil.memPutFloat(pointer + 0x8L, f3);
        MemoryUtil.memPutFloat(pointer + 0xCL, f4);
        vertices++;
    }

    public void vertexFloat9(float f1, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9) {
        long pointer = vertexBuffer.reserve(4 * 9);
        MemoryUtil.memPutFloat(pointer + 0x00L, f1);
        MemoryUtil.memPutFloat(pointer + 0x04L, f2);
        MemoryUtil.memPutFloat(pointer + 0x08L, f3);
        MemoryUtil.memPutFloat(pointer + 0x0CL, f4);
        MemoryUtil.memPutFloat(pointer + 0x10L, f5);
        MemoryUtil.memPutFloat(pointer + 0x14L, f6);
        MemoryUtil.memPutFloat(pointer + 0x18L, f7);
        MemoryUtil.memPutFloat(pointer + 0x1CL, f8);
        MemoryUtil.memPutFloat(pointer + 0x20L, f9);
        vertices++;
    }
}