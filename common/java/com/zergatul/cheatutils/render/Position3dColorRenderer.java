package com.zergatul.cheatutils.render;

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
import com.zergatul.cheatutils.ModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class Position3dColorRenderer {

    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;
    private final ByteBufferBuilder byteBufferBuilder;
    private final DynamicGpuBuffer dynamicVertexBuffer;
    private BufferBuilder bufferBuilder;
    private boolean isEmpty;

    private Position3dColorRenderer() {
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(ModMain.MODID, "pipeline/position-3d-color"))
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "position-3d-color"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "position-3d-color"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_ALL))
                .withVertexFormat(VertexFormats.POSITION_3D_COLOR, VertexFormat.Mode.TRIANGLES)
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                .withCull(false)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(
                () -> ModMain.MODID + ": Pos3d Color Renderer UBO",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                64);
        byteBufferBuilder = new ByteBufferBuilder(0x1000);
        dynamicVertexBuffer = DynamicGpuBuffer.vertex();
    }

    public static Position3dColorRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        isEmpty = true;
        byteBufferBuilder.clear();
        bufferBuilder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
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
        isEmpty = false;

        bufferBuilder.addVertex(x1, y1, z1).setColor(color);
        bufferBuilder.addVertex(x2, y2, z2).setColor(color);
        bufferBuilder.addVertex(x3, y3, z3).setColor(color);

        bufferBuilder.addVertex(x1, y1, z1).setColor(color);
        bufferBuilder.addVertex(x3, y3, z3).setColor(color);
        bufferBuilder.addVertex(x4, y4, z4).setColor(color);
    }

    public void end(Matrix4f mvp) {
        if (isEmpty) {
            return;
        }

        int vertexCount;
        GpuBuffer vertexBuffer;
        try (MeshData mesh = bufferBuilder.buildOrThrow()) {
            vertexBuffer = this.dynamicVertexBuffer.uploadImmediate(mesh.vertexBuffer());
            vertexCount = mesh.drawState().vertexCount();
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, 64);
            builder.putMat4f(mvp);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), builder.get());
        }

        RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> ModMain.MODID + ": Render Pos3dColor",
                Objects.requireNonNull(mainRenderTarget.getColorTextureView()),
                OptionalInt.empty(),
                mainRenderTarget.getDepthTextureView(),
                OptionalDouble.empty())
        ) {
            renderPass.setPipeline(pipeline);
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, ubo);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.draw(0, vertexCount);
        }
    }

    private static final class Holder {
        public static final Position3dColorRenderer INSTANCE = new Position3dColorRenderer();
    }
}