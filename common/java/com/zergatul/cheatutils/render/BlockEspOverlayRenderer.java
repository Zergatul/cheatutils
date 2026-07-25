package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.zergatul.cheatutils.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

public class BlockEspOverlayRenderer {

    private final RenderTarget renderTarget;
    private final RenderPipeline drawPipeline;
    private final GpuBuffer drawUbo;
    private final RenderPipeline blitPipeline;
    private final GpuBuffer blitUbo;
    private final BufferBuilder bufferBuilder;
    private final DynamicGpuBuffer dynamicVertexBuffer;

    private BlockEspOverlayRenderer() {
        renderTarget = RenderTargets.getEsp();
        drawPipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/block-esp-overlay-draw"))
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block-overlay-buffer"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "block-overlay-buffer"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALL))
                .withVertexBinding(0, VertexFormats.BLOCK_OVERLAY_INSTANCED)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .build();
        drawUbo = RenderSystem.getDevice().createBuffer(() -> "Block ESP Overlay Buffer Draw UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 64);
        blitPipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/block-esp-overlay-blit"))
                .withBindGroupLayout(BindGroupLayouts.TEXTURE0)
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "screen-quad"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "color-overlay"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_COLOR))
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .build();
        blitUbo = RenderSystem.getDevice().createBuffer(() -> "Block ESP Overlay Blit UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 16);
        bufferBuilder = new BufferBuilder();
        dynamicVertexBuffer = DynamicGpuBuffer.vertex();
    }

    public static BlockEspOverlayRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        bufferBuilder.clear();
    }

    public void submitBlock(float x, float y, float z) {
        bufferBuilder.vertex(x, y, z);
    }

    public void end(Matrix4f mvp, Color color) {
        if (bufferBuilder.isEmpty()) {
            return;
        }

        GpuBuffer vertexBuffer;
        try (ByteBufferBuilder.Result result = bufferBuilder.getVertexBuffer()) {
            vertexBuffer = this.dynamicVertexBuffer.uploadImmediate(result.byteBuffer());
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = Std140Builder.onStack(stack, 64).putMat4f(mvp).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(drawUbo.slice(), byteBuffer);
        }

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> Constants.MOD_ID + ": Draw block overlay",
                Objects.requireNonNull(renderTarget.getColorTextureView()),
                Optional.of(GuiRenderer.CLEAR_COLOR))
        ) {
            renderPass.setPipeline(drawPipeline);
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, drawUbo);
            renderPass.setVertexBuffer(0, vertexBuffer.slice());
            renderPass.draw(36, bufferBuilder.getBlockCount(), 0, 0);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(16);
            buffer.putFloat(color.getRed() / 255f);
            buffer.putFloat(color.getGreen() / 255f);
            buffer.putFloat(color.getBlue() / 255f);
            buffer.putFloat(color.getAlpha() / 255f);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(blitUbo.slice(), buffer.flip());
        }

        RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> Constants.MOD_ID + ": Blit block overlay",
                Objects.requireNonNull(mainRenderTarget.getColorTextureView()),
                Optional.empty())
        ) {
            renderPass.setPipeline(blitPipeline);
            renderPass.bindTexture(BindGroupLayouts.TEXTURE0_NAME, renderTarget.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, blitUbo);
            renderPass.draw(3, 1, 0, 0);
        }
    }

    private static class BufferBuilder {

        private static final int RECORD_SIZE = 3 * 4;

        private final ByteBufferBuilder vertexBuffer = new ByteBufferBuilder(0x1000);
        private int blocks;

        public void clear() {
            blocks = 0;
            vertexBuffer.clear();
        }

        public ByteBufferBuilder.Result getVertexBuffer() {
            return vertexBuffer.build();
        }

        public int getBlockCount() {
            return blocks;
        }

        public boolean isEmpty() {
            return blocks == 0;
        }

        public void vertex(float x, float y, float z) {
            long pointer = vertexBuffer.reserve(RECORD_SIZE);
            MemoryUtil.memPutFloat(pointer + 0x00L, x);
            MemoryUtil.memPutFloat(pointer + 0x04L, y);
            MemoryUtil.memPutFloat(pointer + 0x08L, z);
            blocks++;
        }
    }

    private static final class Holder {
        public static final BlockEspOverlayRenderer INSTANCE = new BlockEspOverlayRenderer();
    }
}