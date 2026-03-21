package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.ModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.util.Optional;
import java.util.OptionalInt;

public class UniformColorLineRenderer {

    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;
    private final ByteBufferBuilder byteBufferBuilder;
    private BufferBuilder bufferBuilder;

    private UniformColorLineRenderer() {
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(ModMain.MODID, "pipeline/uniform-color-line"))
                .withUniform("Block", UniformType.UNIFORM_BUFFER)
                .withVertexShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "uniform-color-line"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "uniform-color-line"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_ALL))
                .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.DEBUG_LINES)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(() -> "Uniform Color Line Renderer UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 80);
        byteBufferBuilder = new ByteBufferBuilder(0x10000);
    }

    public static UniformColorLineRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        byteBufferBuilder.clear();
        bufferBuilder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
    }

    public void cuboid(float x1, float y1, float z1, float x2, float y2, float z2) {
        line(x1, y1, z1, x1, y1, z2);
        line(x1, y1, z2, x2, y1, z2);
        line(x2, y1, z2, x2, y1, z1);
        line(x2, y1, z1, x1, y1, z1);

        line(x1, y2, z1, x1, y2, z2);
        line(x1, y2, z2, x2, y2, z2);
        line(x2, y2, z2, x2, y2, z1);
        line(x2, y2, z1, x1, y2, z1);

        line(x1, y1, z1, x1, y2, z1);
        line(x1, y1, z2, x1, y2, z2);
        line(x2, y1, z2, x2, y2, z2);
        line(x2, y1, z1, x2, y2, z1);
    }

    public void line(float x1, float y1, float z1, float x2, float y2, float z2) {
        bufferBuilder.addVertex(x1, y1, z1);
        bufferBuilder.addVertex(x2, y2, z2);
    }

    public void end(Matrix4f mvp, Color color) {
        int vertexCount;
        GpuBuffer vertexBuffer;
        try (MeshData mesh = bufferBuilder.buildOrThrow()) {
            vertexBuffer = this.pipeline.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());
            vertexCount = mesh.drawState().vertexCount();
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, 80);
            builder.putMat4f(mvp);
            builder.putVec4(
                    color.getRed() / 255f,
                    color.getGreen() / 255f,
                    color.getBlue() / 255f,
                    color.getAlpha() / 255f);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), builder.get());
        }

        RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Render Lines", mainRenderTarget.getColorTextureView(), OptionalInt.empty())) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            renderPass.setPipeline(pipeline);
            renderPass.setUniform("Block", ubo);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.draw(0, vertexCount);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }
    }

    private static final class Holder {
        public static final UniformColorLineRenderer INSTANCE = new UniformColorLineRenderer();
    }
}