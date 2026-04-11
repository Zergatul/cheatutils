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
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.ModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class EspLineRenderer {

    private final RenderPipeline pipeline;
    private final RenderPipeline depthPipeline;
    private final GpuBuffer ubo;
    private final VertexBufferBuilder bufferBuilder;

    private EspLineRenderer() {
        pipeline = createPipeline("pipeline/esp-lines", false);
        depthPipeline = createPipeline("pipeline/esp-lines-depth", true);
        ubo = RenderSystem.getDevice().createBuffer(() -> "ESP Lines Renderer UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 72);
        bufferBuilder = new VertexBufferBuilder();
    }

    private static RenderPipeline createPipeline(String location, boolean depth) {
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(ModMain.MODID, location))
                .withUniform("Block", UniformType.UNIFORM_BUFFER)
                .withVertexShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "esp-lines"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "esp-lines"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_ALL))
                .withVertexFormat(VertexFormats.LINES, VertexFormat.Mode.TRIANGLES)
                .withCull(false);

        if (depth) {
            builder.withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false));
        }

        return builder.build();
    }

    public static EspLineRenderer getInstance() {
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
        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color, 0, -1, width);
        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color, 0, +1, width);
        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color, 1, -1, width);

        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color, 0, +1, width);
        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color, 1, -1, width);
        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color, 1, +1, width);
    }

    public void line(
            float x1, float y1, float z1, int color1,
            float x2, float y2, float z2, int color2,
            float width
    ) {
        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color1, 0, -1, width);
        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color1, 0, +1, width);
        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color2, 1, -1, width);

        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color1, 0, +1, width);
        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color2, 1, -1, width);
        bufferBuilder.vertexLine(x1, y1, z1, x2, y2, z2, color2, 1, +1, width);
    }

    public void end(Matrix4f mvp) {
        end(mvp, false);
    }

    public void end(Matrix4f mvp, boolean depth) {
        if (bufferBuilder.getVertexCount() == 0) {
            return;
        }

        GpuBuffer vertexBuffer;
        try (ByteBufferBuilder.Result result = bufferBuilder.getVertexBuffer()) {
            vertexBuffer = this.pipeline.getVertexFormat().uploadImmediateVertexBuffer(result.byteBuffer());
        }

        RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, 72);
            builder.putMat4f(mvp);
            builder.putVec2(mainRenderTarget.width, mainRenderTarget.height);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), builder.get());
        }

        try (RenderPass renderPass = createRenderPass(mainRenderTarget, depth)) {
            renderPass.setPipeline(depth ? depthPipeline : pipeline);
            renderPass.setUniform("Block", ubo);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.draw(0, bufferBuilder.getVertexCount());
        }
    }

    private RenderPass createRenderPass(RenderTarget mainRenderTarget, boolean depth) {
        if (depth) {
            return RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Render Depth-Tested ESP Lines",
                    mainRenderTarget.getColorTextureView(),
                    OptionalInt.empty(),
                    mainRenderTarget.getDepthTextureView(),
                    OptionalDouble.empty());
        } else {
            return RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Render ESP Lines",
                    mainRenderTarget.getColorTextureView(),
                    OptionalInt.empty());
        }
    }

    private static final class Holder {
        public static final EspLineRenderer INSTANCE = new EspLineRenderer();
    }
}
