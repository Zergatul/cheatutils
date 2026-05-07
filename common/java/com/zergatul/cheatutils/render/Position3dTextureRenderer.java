package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

public class Position3dTextureRenderer {

    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;
    private final BufferBuilder bufferBuilder;
    private final DynamicGpuBuffer dynamicVertexBuffer;

    private Position3dTextureRenderer() {
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/position-3d-texture"))
                .withBindGroupLayout(BindGroupLayouts.TEXTURE0)
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "position-3d-texture"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "position-3d-texture"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
                .withVertexBinding(0, VertexFormats.POSITION_3D_TEXTURE)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withDepthStencilState(DepthStencilState.DEFAULT)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(
                () -> Constants.MOD_ID + ": Texture 3d Renderer UBO",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                64);
        bufferBuilder = new BufferBuilder();
        dynamicVertexBuffer = DynamicGpuBuffer.vertex();
    }

    public static Position3dTextureRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        bufferBuilder.clear();
    }

    public void quad(
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            float x4, float y4, float z4, float u4, float v4
    ) {
        bufferBuilder.vertex(x1, y1, z1, u1, v1);
        bufferBuilder.vertex(x2, y2, z2, u2, v2);
        bufferBuilder.vertex(x3, y3, z3, u3, v3);

        bufferBuilder.vertex(x1, y1, z1, u1, v1);
        bufferBuilder.vertex(x3, y3, z3, u3, v3);
        bufferBuilder.vertex(x4, y4, z4, u4, v4);
    }

    public void end(Matrix4f mvp, GpuTextureView texture) {
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
                () -> "Render 3d Texture",
                Objects.requireNonNull(mainRenderTarget.getColorTextureView()),
                Optional.empty(),
                mainRenderTarget.getDepthTextureView(),
                OptionalDouble.empty())
        ) {
            renderPass.setPipeline(pipeline);
            renderPass.bindTexture(BindGroupLayouts.TEXTURE0_NAME, texture, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, ubo);
            renderPass.setVertexBuffer(0, vertexBuffer.slice());
            renderPass.draw(0, bufferBuilder.getVertexCount());
        }
    }

    private static class BufferBuilder {

        private static final int RECORD_SIZE = 5 * 4;

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

        public void vertex(float x, float y, float z, float u, float v) {
            long pointer = vertexBuffer.reserve(RECORD_SIZE);
            MemoryUtil.memPutFloat(pointer + 0x00L, x);
            MemoryUtil.memPutFloat(pointer + 0x04L, y);
            MemoryUtil.memPutFloat(pointer + 0x08L, z);
            MemoryUtil.memPutFloat(pointer + 0x0CL, u);
            MemoryUtil.memPutFloat(pointer + 0x10L, v);
            vertices++;
        }
    }

    private static final class Holder {
        public static final Position3dTextureRenderer INSTANCE = new Position3dTextureRenderer();
    }
}