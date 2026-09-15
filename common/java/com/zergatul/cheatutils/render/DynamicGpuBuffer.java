package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.zergatul.cheatutils.Constants;

import java.nio.ByteBuffer;

public class DynamicGpuBuffer {

    private final int usage;
    private GpuBuffer vertexBuffer;

    private DynamicGpuBuffer(int usage) {
        this.usage = usage;
    }

    public static DynamicGpuBuffer index() {
        return new DynamicGpuBuffer(GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST);
    }

    public static DynamicGpuBuffer vertex() {
        return new DynamicGpuBuffer(GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST);
    }

    public GpuBuffer uploadImmediate(ByteBuffer data) {
        GpuDevice device = RenderSystem.getDevice();
        boolean writeToBufferIsSlow = device.getDeviceInfo().hintsAndWorkarounds().writeToBufferIsSlow();

        if (vertexBuffer == null || vertexBuffer.size() < data.remaining() || writeToBufferIsSlow) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }

            vertexBuffer = device.createBuffer(
                    () -> Constants.MOD_ID + ": Dynamic vertex buffer",
                    this.usage,
                    data);
        } else {
            device.createCommandEncoder().writeToBuffer(vertexBuffer.slice(), data);
        }

        return vertexBuffer;
    }
}