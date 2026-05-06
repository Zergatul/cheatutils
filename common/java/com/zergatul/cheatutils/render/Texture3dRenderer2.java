package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
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

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class Texture3dRenderer2 {

    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;
    private final ByteBufferBuilder byteBufferBuilder;
    private BufferBuilder bufferBuilder;
    private boolean isEmpty;

    private Texture3dRenderer2() {
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/position-3d-texture"))
                .withSampler("InSampler")
                .withUniform("Block", UniformType.UNIFORM_BUFFER)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "position-3d-texture"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "position-3d-texture"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_ALL))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLES)
                .withDepthStencilState(DepthStencilState.DEFAULT)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(() -> "Texture 3d Renderer UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 64);
        byteBufferBuilder = new ByteBufferBuilder(0x10000);
    }

    public static Texture3dRenderer2 getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        isEmpty = true;
        byteBufferBuilder.clear();
        bufferBuilder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
    }

    public void quad(
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            float x4, float y4, float z4, float u4, float v4
    ) {
        isEmpty = false;

        bufferBuilder.addVertex(x1, y1, z1).setUv(u1, v1);
        bufferBuilder.addVertex(x2, y2, z2).setUv(u2, v2);
        bufferBuilder.addVertex(x3, y3, z3).setUv(u3, v3);

        bufferBuilder.addVertex(x1, y1, z1).setUv(u1, v1);
        bufferBuilder.addVertex(x3, y3, z3).setUv(u3, v3);
        bufferBuilder.addVertex(x4, y4, z4).setUv(u4, v4);
    }

    public void end(Matrix4f mvp, GpuTextureView texture) {
        if (isEmpty) {
            return;
        }

        int vertexCount;
        GpuBuffer vertexBuffer;
        try (MeshData mesh = bufferBuilder.buildOrThrow()) {
            vertexBuffer = this.pipeline.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());
            vertexCount = mesh.drawState().vertexCount();
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, 64);
            builder.putMat4f(mvp);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), builder.get());
        }

        RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Render 3d Texture",
                mainRenderTarget.getColorTextureView(),
                OptionalInt.empty(),
                mainRenderTarget.getDepthTextureView(),
                OptionalDouble.empty())
        ) {
            renderPass.setPipeline(pipeline);
            renderPass.bindTexture("InSampler", texture, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            renderPass.setUniform("Block", ubo);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.draw(0, vertexCount);
        }
    }

    private static final class Holder {
        public static final Texture3dRenderer2 INSTANCE = new Texture3dRenderer2();
    }
}