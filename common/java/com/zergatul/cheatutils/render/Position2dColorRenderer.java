package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;
import java.util.Optional;

public class Position2dColorRenderer {

    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;
    private final DynamicGpuBuffer dynamicVertexBuffer;

    private Position2dColorRenderer() {
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pos2d-color"))
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "position-2d-color"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "position-2d-color"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
                .withVertexBinding(0, VertexFormats.POSITION_2D_COLOR)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withCull(false)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(
                () -> Constants.MOD_ID +  ": Pos2dCol Renderer UBO",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                64);
        dynamicVertexBuffer = DynamicGpuBuffer.vertex();
    }

    public static Position2dColorRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void draw(RenderTarget renderTarget, Matrix4f mvp, BufferBuilder buffer) {
        if (buffer.isEmpty()) {
            return;
        }

        GpuBuffer vertexBuffer;
        try (ByteBufferBuilder.Result result = buffer.getVertexBuffer()) {
            vertexBuffer = this.dynamicVertexBuffer.uploadImmediate(result.byteBuffer());
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, 64);
            builder.putMat4f(mvp);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), builder.get());
        }

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> Constants.MOD_ID + ": Pos2d Color",
                Objects.requireNonNull(renderTarget.getColorTextureView()),
                Optional.empty()
        )) {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, ubo);
            renderPass.setVertexBuffer(0, vertexBuffer.slice());
            renderPass.draw(buffer.getVertexCount(), 1, 0, 0);
        }
    }

    public static class BufferBuilder {

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

        public void rect(
                float x, float y, float width, float height,
                int color
        ) {
            quad(
                    x, y,
                    x, y + height,
                    x + width, y + height,
                    x + width, y,
                    color);
        }

        public void quad(
                float x1, float y1,
                float x2, float y2,
                float x3, float y3,
                float x4, float y4,
                int color
        ) {
            vertex(x1, y1, color);
            vertex(x2, y2, color);
            vertex(x3, y3, color);

            vertex(x1, y1, color);
            vertex(x4, y4, color);
            vertex(x3, y3, color);
        }

        public void vertex(float x, float y, int color) {
            long pointer = vertexBuffer.reserve(12);
            MemoryUtil.memPutFloat(pointer + 0x00L, x);
            MemoryUtil.memPutFloat(pointer + 0x04L, y);
            MemoryUtil.memPutInt(pointer + 0x08L, ColorUtils.toShader(color));
            vertices++;
        }
    }

    private static final class Holder {
        public static final Position2dColorRenderer INSTANCE = new Position2dColorRenderer();
    }
}