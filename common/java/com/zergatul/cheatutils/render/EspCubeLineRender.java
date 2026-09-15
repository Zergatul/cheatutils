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
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;
import java.util.Optional;

public class EspCubeLineRender {

    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;
    private final BufferBuilder bufferBuilder;
    private final DynamicGpuBuffer dynamicVertexBuffer;

    private EspCubeLineRender() {
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/esp-cube-lines"))
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "esp-cube-lines"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "lines"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
                .withVertexBinding(0, VertexFormats.CUBE_LINES_INSTANCED)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .withCull(false)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(
                () -> Constants.MOD_ID + ": ESP Cube Lines Renderer UBO",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                72);
        bufferBuilder = new BufferBuilder();
        dynamicVertexBuffer = DynamicGpuBuffer.vertex();
    }

    public static EspCubeLineRender getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        bufferBuilder.clear();
    }

    public void cube(float x, float y, float z, int color, float width) {
        bufferBuilder.vertex(x, y, z, color, width);
    }

    public void end(Matrix4f mvp) {
        if (bufferBuilder.isEmpty()) {
            return;
        }

        GpuBuffer vertexBuffer;
        try (ByteBufferBuilder.Result result = bufferBuilder.getVertexBuffer()) {
            vertexBuffer = this.dynamicVertexBuffer.uploadImmediate(result.byteBuffer());
        }

        RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, 72);
            builder.putMat4f(mvp);
            builder.putVec2(mainRenderTarget.width, mainRenderTarget.height);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), builder.get());
        }

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> Constants.MOD_ID + ": Render ESP Cube Lines",
                Objects.requireNonNull(mainRenderTarget.getColorTextureView()),
                Optional.empty())
        ) {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, ubo);
            renderPass.setVertexBuffer(0, vertexBuffer.slice());
            renderPass.draw(12 * 6, bufferBuilder.getCubeCount(), 0, 0);
        }
    }

    private static class BufferBuilder {

        private static final int RECORD_SIZE = 4 * 5;

        private final ByteBufferBuilder vertexBuffer = new ByteBufferBuilder(0x1000);
        private int cubes;

        public void clear() {
            cubes = 0;
            vertexBuffer.clear();
        }

        public ByteBufferBuilder.Result getVertexBuffer() {
            return vertexBuffer.build();
        }

        public int getCubeCount() {
            return cubes;
        }

        public boolean isEmpty() {
            return cubes == 0;
        }

        public void vertex(float x, float y, float z, int color, float width) {
            long pointer = vertexBuffer.reserve(RECORD_SIZE);
            MemoryUtil.memPutFloat(pointer + 0x00L, x);
            MemoryUtil.memPutFloat(pointer + 0x04L, y);
            MemoryUtil.memPutFloat(pointer + 0x08L, z);
            MemoryUtil.memPutInt(pointer + 0x0CL, ColorUtils.toShader(color));
            MemoryUtil.memPutFloat(pointer + 0x10L, width);
            cubes++;
        }
    }

    private static final class Holder {
        public static final EspCubeLineRender INSTANCE = new EspCubeLineRender();
    }
}