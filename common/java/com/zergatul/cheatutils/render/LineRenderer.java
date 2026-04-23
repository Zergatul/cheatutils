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
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.extensions.RenderPassExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class LineRenderer {

    private final RenderPipeline pipeline;
    private final RenderPipeline depthPipeline;
    private final GpuBuffer ubo;
    private final BufferBuilder bufferBuilder;
    private final DynamicGpuBuffer dynamicVertexBuffer;

    private LineRenderer() {
        pipeline = createPipeline("pipeline/lines", false);
        depthPipeline = createPipeline("pipeline/depth-lines", true);
        ubo = RenderSystem.getDevice().createBuffer(
                () -> ModMain.MODID + ": Lines Renderer UBO",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                72);
        bufferBuilder = new BufferBuilder();
        dynamicVertexBuffer = DynamicGpuBuffer.vertex();
    }

    private static RenderPipeline createPipeline(String location, boolean depth) {
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(ModMain.MODID, location))
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "lines"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "lines"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_ALL))
                .withVertexFormat(VertexFormats.LINES_INSTANCED, VertexFormat.Mode.TRIANGLES)
                .withCull(false);

        if (depth) {
            builder.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false));
        }

        return builder.build();
    }

    public static LineRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        bufferBuilder.clear();
    }

    public void cuboid(
            Vec3 cameraPos,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            int color, float width
    ) {
        cuboid(
                (float) (x1 - cameraPos.x), (float) (y1 - cameraPos.y), (float) (z1 - cameraPos.z),
                (float) (x2 - cameraPos.x), (float) (y2 - cameraPos.y), (float) (z2 - cameraPos.z),
                color, width);
    }

    public void cuboid(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            int color, float width
    ) {
        line(x1, y1, z1, x1, y1, z2, color, width);
        line(x1, y1, z2, x2, y1, z2, color, width);
        line(x2, y1, z2, x2, y1, z1, color, width);
        line(x2, y1, z1, x1, y1, z1, color, width);

        line(x1, y2, z1, x1, y2, z2, color, width);
        line(x1, y2, z2, x2, y2, z2, color, width);
        line(x2, y2, z2, x2, y2, z1, color, width);
        line(x2, y2, z1, x1, y2, z1, color, width);

        line(x1, y1, z1, x1, y2, z1, color, width);
        line(x1, y1, z2, x1, y2, z2, color, width);
        line(x2, y1, z2, x2, y2, z2, color, width);
        line(x2, y1, z1, x2, y2, z1, color, width);
    }

    public void line(
            Vec3 cameraPos,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            int color, float width
    ) {
        line(
                (float) (x1 - cameraPos.x), (float) (y1 - cameraPos.y), (float) (z1 - cameraPos.z),
                (float) (x2 - cameraPos.x), (float) (y2 - cameraPos.y), (float) (z2 - cameraPos.z),
                color, width);
    }

    public void line(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            int color, float width
    ) {
        bufferBuilder.vertex(x1, y1, z1, x2, y2, z2, color, width);
    }

    public void end(Matrix4f mvp) {
        end(mvp, false);
    }

    public void end(Matrix4f mvp, boolean depth) {
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

        try (RenderPass renderPass = createRenderPass(mainRenderTarget, depth)) {
            renderPass.setPipeline(depth ? depthPipeline : pipeline);
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, ubo);
            renderPass.setVertexBuffer(0, vertexBuffer);
            ((RenderPassExtension) renderPass).drawInstanced_CU(0, 6, bufferBuilder.getLineCount());
        }
    }

    private RenderPass createRenderPass(RenderTarget mainRenderTarget, boolean depth) {
        assert mainRenderTarget.getColorTextureView() != null;

        if (depth) {
            return RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> ModMain.MODID + ": Render Depth Lines",
                    mainRenderTarget.getColorTextureView(),
                    OptionalInt.empty(),
                    mainRenderTarget.getDepthTextureView(),
                    OptionalDouble.empty());
        } else {
            return RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> ModMain.MODID + ": Render Lines",
                    mainRenderTarget.getColorTextureView(),
                    OptionalInt.empty());
        }
    }

    private static class BufferBuilder {

        private static final int RECORD_SIZE = 4 * 8;

        private final ByteBufferBuilder vertexBuffer = new ByteBufferBuilder(0x1000);
        private int lines;

        public void clear() {
            lines = 0;
            vertexBuffer.clear();
        }

        public ByteBufferBuilder.Result getVertexBuffer() {
            return vertexBuffer.build();
        }

        public int getLineCount() {
            return lines;
        }

        public boolean isEmpty() {
            return lines == 0;
        }

        public void vertex(float x1, float y1, float z1, float x2, float y2, float z2, int color, float width) {
            long pointer = vertexBuffer.reserve(RECORD_SIZE);
            MemoryUtil.memPutFloat(pointer + 0x00L, x1);
            MemoryUtil.memPutFloat(pointer + 0x04L, y1);
            MemoryUtil.memPutFloat(pointer + 0x08L, z1);
            MemoryUtil.memPutFloat(pointer + 0x0CL, x2);
            MemoryUtil.memPutFloat(pointer + 0x10L, y2);
            MemoryUtil.memPutFloat(pointer + 0x14L, z2);
            MemoryUtil.memPutInt(pointer + 0x18L, color);
            MemoryUtil.memPutFloat(pointer + 0x1CL, width);
            lines++;
        }
    }

    private static final class Holder {
        public static final LineRenderer INSTANCE = new LineRenderer();
    }
}