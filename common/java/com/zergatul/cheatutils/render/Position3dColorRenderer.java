package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class Position3dColorRenderer {

    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;
    private final BufferBuilder bufferBuilder;
    private final DynamicGpuBuffer dynamicVertexBuffer;

    private Position3dColorRenderer() {
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/position-3d-color"))
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "position-3d-color"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "position-3d-color"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
                .withVertexBinding(0, VertexFormats.POSITION_3D_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                .withCull(false)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(
                () -> Constants.MOD_ID + ": Pos3d Color Renderer UBO",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                64);
        bufferBuilder = new BufferBuilder();
        dynamicVertexBuffer = DynamicGpuBuffer.vertex();
    }

    public static Position3dColorRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        bufferBuilder.clear();
    }

    public void cuboid(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            int color
    ) {
        if (x1 > x2) {
            float buf = x1;
            x1 = x2;
            x2 = buf;
        }
        if (y1 > y2) {
            float buf = y1;
            y1 = y2;
            y2 = buf;
        }
        if (z1 > z2) {
            float buf = z1;
            z1 = z2;
            z2 = buf;
        }

        quad(
                x1, y1, z1,
                x1, y1, z2,
                x2, y1, z2,
                x2, y1, z1,
                color);
        quad(
                x1, y2, z1,
                x1, y2, z2,
                x2, y2, z2,
                x2, y2, z1,
                color);
        quad(
                x1, y1, z1,
                x1, y1, z2,
                x1, y2, z2,
                x1, y2, z1,
                color);
        quad(
                x2, y1, z1,
                x2, y1, z2,
                x2, y2, z2,
                x2, y2, z1,
                color);
        quad(
                x1, y1, z1,
                x1, y2, z1,
                x2, y2, z1,
                x2, y1, z1,
                color);
        quad(
                x1, y1, z2,
                x1, y2, z2,
                x2, y2, z2,
                x2, y1, z2,
                color);
    }

    public void quad(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            int color
    ) {
        bufferBuilder.vertex(x1, y1, z1, color);
        bufferBuilder.vertex(x2, y2, z2, color);
        bufferBuilder.vertex(x3, y3, z3, color);

        bufferBuilder.vertex(x1, y1, z1, color);
        bufferBuilder.vertex(x3, y3, z3, color);
        bufferBuilder.vertex(x4, y4, z4, color);
    }

    public void end(Matrix4f mvp) {
        if (bufferBuilder.isEmpty()) {
            return;
        }

        GpuBuffer vertexBuffer;
        try (ByteBufferBuilder.Result result = bufferBuilder.getVertexBuffer()) {
            vertexBuffer = this.dynamicVertexBuffer.uploadImmediate(result.byteBuffer());
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, 64);
            builder.putMat4f(mvp);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), builder.get());
        }

        RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> Constants.MOD_ID + ": Render Pos3dColor",
                Objects.requireNonNull(mainRenderTarget.getColorTextureView()),
                Optional.empty(),
                mainRenderTarget.getDepthTextureView(),
                OptionalDouble.empty())
        ) {
            renderPass.setPipeline(pipeline);
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, ubo);
            renderPass.setVertexBuffer(0, vertexBuffer.slice());
            renderPass.draw(bufferBuilder.getVertexCount(), 1, 0, 0);
        }
    }

    public static class BufferBuilder {

        private static final int RECORD_SIZE = 4 * 4;

        private final ByteBufferBuilder vertexBuffer = new ByteBufferBuilder(0x1000);
        private int vertices;

        public void clear() {
            vertices = 0;
            vertexBuffer.clear();
        }

        public ByteBufferBuilder.Result getVertexBuffer() {
            return vertexBuffer.build();
        }

        public int getVertexCount() {
            return vertices;
        }

        public boolean isEmpty() {
            return vertices == 0;
        }

        public void vertex(float x, float y, float z, int color) {
            long pointer = vertexBuffer.reserve(RECORD_SIZE);
            MemoryUtil.memPutFloat(pointer + 0x00L, x);
            MemoryUtil.memPutFloat(pointer + 0x04L, y);
            MemoryUtil.memPutFloat(pointer + 0x08L, z);
            MemoryUtil.memPutInt(pointer + 0x0CL, ColorUtils.toShader(color));
            vertices++;
        }
    }

    private static final class Holder {
        public static final Position3dColorRenderer INSTANCE = new Position3dColorRenderer();
    }
}