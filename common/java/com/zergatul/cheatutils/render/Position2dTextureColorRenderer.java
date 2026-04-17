package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public class Position2dTextureColorRenderer {

    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;
    private final DynamicGpuBuffer dynamicVertexBuffer;

    private Position2dTextureColorRenderer() {
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(ModMain.MODID, "pos2d-tex-color"))
                .withBindGroupLayout(BindGroupLayouts.TEXTURE0)
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "position-2d-texture-color"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "position-2d-texture-color"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_ALL))
                .withVertexFormat(VertexFormats.POSITION_2D_TEXTURE_COLOR, VertexFormat.Mode.TRIANGLES)
                .withCull(false)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(
                () -> ModMain.MODID + ": Pos2dTexCol Renderer UBO",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                64);
        dynamicVertexBuffer = DynamicGpuBuffer.vertex();
    }

    public static Position2dTextureColorRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void draw(RenderTarget renderTarget, GpuTextureView textureView, Matrix4f mvp, BufferBuilder buffer) {
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
                () -> ModMain.MODID + ": Pos2d Tex Color",
                Objects.requireNonNull(renderTarget.getColorTextureView()),
                OptionalInt.empty()
        )) {
            renderPass.setPipeline(pipeline);
            renderPass.bindTexture(BindGroupLayouts.TEXTURE0_NAME, textureView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, ubo);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.draw(0, buffer.getVertexCount());
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

        public void rect(float x, float y, float width, float height, int color) {
            quad(
                    x, y, 0, 0,
                    x, y + height, 0, 1,
                    x + width, y + height, 1, 1,
                    x + width, y, 1, 0,
                    color);
        }

        public void rect(
                float x, float y, float width, float height,
                AtlasTexture.Item item,
                int color
        ) {
            quad(
                    x, y, item.getU1(), item.getV1(),
                    x, y + height, item.getU1(), item.getV2(),
                    x + width, y + height, item.getU2(), item.getV2(),
                    x + width, y, item.getU2(), item.getV1(),
                    color);
        }

        public void quad(
                float x1, float y1, float u1, float v1,
                float x2, float y2, float u2, float v2,
                float x3, float y3, float u3, float v3,
                float x4, float y4, float u4, float v4,
                int color
        ) {
            vertex(x1, y1, u1, v1, color);
            vertex(x2, y2, u2, v2, color);
            vertex(x3, y3, u3, v3, color);

            vertex(x1, y1, u1, v1, color);
            vertex(x4, y4, u4, v4, color);
            vertex(x3, y3, u3, v3, color);
        }

        public void vertex(float x, float y, float u, float v, int color) {
            long pointer = vertexBuffer.reserve(20);
            MemoryUtil.memPutFloat(pointer + 0x00L, x);
            MemoryUtil.memPutFloat(pointer + 0x04L, y);
            MemoryUtil.memPutFloat(pointer + 0x08L, u);
            MemoryUtil.memPutFloat(pointer + 0x0CL, v);
            MemoryUtil.memPutInt(pointer + 0x10L, ColorUtils.toShader(color));
            vertices++;
        }
    }

    private static final class Holder {
        public static final Position2dTextureColorRenderer INSTANCE = new Position2dTextureColorRenderer();
    }
}