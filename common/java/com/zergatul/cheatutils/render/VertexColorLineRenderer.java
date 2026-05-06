package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class VertexColorLineRenderer {

    private final RenderPipeline pipeline;
    private final RenderPipeline depthPipeline;
    private final GpuBuffer ubo;
    private final ByteBufferBuilder byteBufferBuilder;
    private BufferBuilder bufferBuilder;
    private boolean isEmpty;

    private VertexColorLineRenderer() {
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/vertex-color-line"))
                .withUniform("Block", UniformType.UNIFORM_BUFFER)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "vertex-color-line"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "vertex-color-line"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_ALL))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES)
                .build();
        depthPipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/vertex-color-line"))
                .withUniform("Block", UniformType.UNIFORM_BUFFER)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "vertex-color-line"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "vertex-color-line"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_ALL))
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .build();
        ubo = RenderSystem.getDevice().createBuffer(() -> "Vertex Color Line Renderer UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 64);
        byteBufferBuilder = new ByteBufferBuilder(0x10000);
    }

    public static VertexColorLineRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        isEmpty = true;
        byteBufferBuilder.clear();
        bufferBuilder = new BufferBuilder(byteBufferBuilder, pipeline.getVertexFormatMode(), pipeline.getVertexFormat());
    }

    public void cuboid(
            Vec3 cameraPos,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float r, float g, float b, float a
    ) {
        cuboid(
                (float) (x1 - cameraPos.x), (float) (y1 - cameraPos.y), (float) (z1 - cameraPos.z),
                (float) (x2 - cameraPos.x), (float) (y2 - cameraPos.y), (float) (z2 - cameraPos.z),
                r, g, b, a);
    }

    public void cuboid(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a
    ) {
        line(x1, y1, z1, x1, y1, z2, r, g, b, a);
        line(x1, y1, z2, x2, y1, z2, r, g, b, a);
        line(x2, y1, z2, x2, y1, z1, r, g, b, a);
        line(x2, y1, z1, x1, y1, z1, r, g, b, a);

        line(x1, y2, z1, x1, y2, z2, r, g, b, a);
        line(x1, y2, z2, x2, y2, z2, r, g, b, a);
        line(x2, y2, z2, x2, y2, z1, r, g, b, a);
        line(x2, y2, z1, x1, y2, z1, r, g, b, a);

        line(x1, y1, z1, x1, y2, z1, r, g, b, a);
        line(x1, y1, z2, x1, y2, z2, r, g, b, a);
        line(x2, y1, z2, x2, y2, z2, r, g, b, a);
        line(x2, y1, z1, x2, y2, z1, r, g, b, a);
    }

    public void line(
            float x1, float y1, float z1,
            float r1, float g1, float b1, float a1,
            float x2, float y2, float z2,
            float r2, float g2, float b2, float a2
    ) {
        isEmpty = false;
        bufferBuilder.addVertex(x1, y1, z1).setColor(r1, g1, b1, a1);
        bufferBuilder.addVertex(x2, y2, z2).setColor(r2, g2, b2, a2);;
    }

    public void line(
            Vec3 cameraPos,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float r, float g, float b, float a
    ) {
        line(
                (float) (x1 - cameraPos.x), (float) (y1 - cameraPos.y), (float) (z1 - cameraPos.z),
                (float) (x2 - cameraPos.x), (float) (y2 - cameraPos.y), (float) (z2 - cameraPos.z),
                r, g, b, a);
    }

    public void line(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a
    ) {
        isEmpty = false;
        bufferBuilder.addVertex(x1, y1, z1).setColor(r, g, b, a);
        bufferBuilder.addVertex(x2, y2, z2).setColor(r, g, b, a);;
    }

    public void end(Matrix4f mvp) {
        end(mvp, false);
    }

    public void end(Matrix4f mvp, boolean depth) {
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
            Std140Builder builder = Std140Builder.onStack(stack, 80);
            builder.putMat4f(mvp);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), builder.get());
        }

        try (RenderPass renderPass = createRenderPass(depth)) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            renderPass.setPipeline(depth ? depthPipeline : pipeline);
            renderPass.setUniform("Block", ubo);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.draw(0, vertexCount);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }
    }

    private RenderPass createRenderPass(boolean depth) {
        RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        if (depth) {
            return RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Render Lines",
                    mainRenderTarget.getColorTextureView(),
                    OptionalInt.empty(),
                    mainRenderTarget.getDepthTextureView(),
                    OptionalDouble.empty());
        } else {
            return RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Render Lines",
                    mainRenderTarget.getColorTextureView(),
                    OptionalInt.empty());
        }
    }

    private static final class Holder {
        public static final VertexColorLineRenderer INSTANCE = new VertexColorLineRenderer();
    }
}