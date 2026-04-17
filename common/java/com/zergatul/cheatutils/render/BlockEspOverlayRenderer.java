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
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.ModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalInt;

public class BlockEspOverlayRenderer {

    private final RenderTarget renderTarget;
    private final RenderPipeline drawPipeline;
    private final GpuBuffer drawUbo;
    private final RenderPipeline blitPipeline;
    private final GpuBuffer blitUbo;
    private final IndexBufferBuilder indexBufferBuilder;
    private final DynamicGpuBuffer dynamicVertexBuffer;
    private final DynamicGpuBuffer dynamicIndexBuffer;

    private BlockEspOverlayRenderer() {
        renderTarget = RenderTargets.getEsp();
        drawPipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(ModMain.MODID, "pipeline/block-esp-overlay-draw"))
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "block-overlay-buffer"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "block-overlay-buffer"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_ALL))
                .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.TRIANGLES)
                .build();
        drawUbo = RenderSystem.getDevice().createBuffer(() -> "Block ESP Overlay Buffer Draw UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 64);
        blitPipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(ModMain.MODID, "pipeline/block-esp-overlay-blit"))
                .withBindGroupLayout(BindGroupLayouts.TEXTURE0)
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "screen-quad"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "color-overlay"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_COLOR))
                .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
                .build();
        blitUbo = RenderSystem.getDevice().createBuffer(() -> "Block ESP Overlay Blit UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 16);
        indexBufferBuilder = new IndexBufferBuilder();
        dynamicVertexBuffer = DynamicGpuBuffer.vertex();
        dynamicIndexBuffer = DynamicGpuBuffer.index();
    }

    public static BlockEspOverlayRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        indexBufferBuilder.clear();
    }

    public void submitBlock(float x, float y, float z) {
        int v1 = indexBufferBuilder.vertex(x + 0, y + 0, z + 0);
        int v2 = indexBufferBuilder.vertex(x + 0, y + 0, z + 1);
        int v3 = indexBufferBuilder.vertex(x + 1, y + 0, z + 0);
        int v4 = indexBufferBuilder.vertex(x + 1, y + 0, z + 1);
        int v5 = indexBufferBuilder.vertex(x + 0, y + 1, z + 0);
        int v6 = indexBufferBuilder.vertex(x + 0, y + 1, z + 1);
        int v7 = indexBufferBuilder.vertex(x + 1, y + 1, z + 0);
        int v8 = indexBufferBuilder.vertex(x + 1, y + 1, z + 1);

        // bottom (-Y)
        indexBufferBuilder.triangle(v1, v4, v2);
        indexBufferBuilder.triangle(v1, v3, v4);

        // top (+Y)
        indexBufferBuilder.triangle(v5, v6, v8);
        indexBufferBuilder.triangle(v5, v8, v7);

        // side 1 (-Z)
        indexBufferBuilder.triangle(v1, v5, v7);
        indexBufferBuilder.triangle(v1, v7, v3);

        // side 2 (+Z)
        indexBufferBuilder.triangle(v2, v8, v6);
        indexBufferBuilder.triangle(v2, v4, v8);

        // side 3 (-X)
        indexBufferBuilder.triangle(v1, v2, v6);
        indexBufferBuilder.triangle(v1, v6, v5);

        // side 4 (+X)
        indexBufferBuilder.triangle(v3, v7, v8);
        indexBufferBuilder.triangle(v3, v8, v4);
    }

    public void end(Matrix4f mvp, Color color) {
        GpuBuffer vertexBuffer;
        try (ByteBufferBuilder.Result result = indexBufferBuilder.getVertexBuffer()) {
            vertexBuffer = this.dynamicVertexBuffer.uploadImmediate(result.byteBuffer());
        }
        GpuBuffer indexBuffer;
        try (ByteBufferBuilder.Result result = indexBufferBuilder.getIndexBuffer()) {
            indexBuffer = this.dynamicIndexBuffer.uploadImmediate(result.byteBuffer());
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = Std140Builder.onStack(stack, 64).putMat4f(mvp).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(drawUbo.slice(), byteBuffer);
        }

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Draw block overlay", renderTarget.getColorTextureView(), OptionalInt.of(0))) {
            renderPass.setPipeline(drawPipeline);
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, drawUbo);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.setIndexBuffer(indexBuffer, VertexFormat.IndexType.INT);
            renderPass.drawIndexed(0, 0, indexBufferBuilder.getIndexCount(), 1);
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
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Blit block overlay", mainRenderTarget.getColorTextureView(), OptionalInt.empty())) {
            renderPass.setPipeline(blitPipeline);
            renderPass.bindTexture(BindGroupLayouts.TEXTURE0_NAME, renderTarget.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, blitUbo);
            renderPass.draw(0, 3);
        }
    }

    private static final class Holder {
        public static final BlockEspOverlayRenderer INSTANCE = new BlockEspOverlayRenderer();
    }
}