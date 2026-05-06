package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.util.Optional;
import java.util.OptionalInt;

public class VertexColorLineWidthRenderer {

    private static final float FEATHER = 1.25f;

    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;
    private final VertexBufferBuilder bufferBuilder;
    private final Vector4f v1 = new Vector4f();
    private final Vector4f v2 = new Vector4f();
    private final Vector4f rect1 = new Vector4f();
    private final Vector4f rect2 = new Vector4f();
    private final Vector4f rect3 = new Vector4f();
    private final Vector4f rect4 = new Vector4f();

    private Matrix4f mvp;
    private int viewportWidth;
    private int viewportHeight;

    private VertexColorLineWidthRenderer() {
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/vertex-color-line-width"))
                .withUniform("Block", UniformType.UNIFORM_BUFFER)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "vertex-color-line-width"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "vertex-color-line-width"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_ALL))
                .withVertexFormat(VertexFormats.POSITION_COLOR_GRADIENT_LINE_WIDTH, VertexFormat.Mode.TRIANGLES)
                .withCull(false)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(() -> "Vertex Color Line Width Renderer UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 4);
        bufferBuilder = new VertexBufferBuilder();
    }

    public static VertexColorLineWidthRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin(Matrix4f mvp) {
        this.bufferBuilder.clear();
        this.mvp = mvp;

        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        this.viewportWidth = mainTarget.width;
        this.viewportHeight = mainTarget.height;
    }

    public void cuboid(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a,
            float width
    ) {
        line(x1, y1, z1, x1, y1, z2, r, g, b, a, width);
        line(x1, y1, z2, x2, y1, z2, r, g, b, a, width);
        line(x2, y1, z2, x2, y1, z1, r, g, b, a, width);
        line(x2, y1, z1, x1, y1, z1, r, g, b, a, width);

        line(x1, y2, z1, x1, y2, z2, r, g, b, a, width);
        line(x1, y2, z2, x2, y2, z2, r, g, b, a, width);
        line(x2, y2, z2, x2, y2, z1, r, g, b, a, width);
        line(x2, y2, z1, x1, y2, z1, r, g, b, a, width);

        line(x1, y1, z1, x1, y2, z1, r, g, b, a, width);
        line(x1, y1, z2, x1, y2, z2, r, g, b, a, width);
        line(x2, y1, z2, x2, y2, z2, r, g, b, a, width);
        line(x2, y1, z1, x2, y2, z1, r, g, b, a, width);
    }

    public void line(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a,
            float width
    ) {
        v1.set(x1, y1, z1, 1);
        v2.set(x2, y2, z2, 1);
        v1.mul(mvp);
        v2.mul(mvp);

        if (v1.w < 0 && v2.w < 0) {
            return;
        }

        if (v1.w < 0 || v2.w < 0) {
            // clipping
            float t = (v1.w - 0.0001f) / (v1.w - v2.w);
            if (v1.w <= 0) {
                v1.lerp(v2, t);
            } else {
                v2.lerp(v1, 1 - t);
            }
        }

        if (createRect(width)) {
            point(rect1, r, g, b, a, 0, width);
            point(rect2, r, g, b, a, 1, width);
            point(rect4, r, g, b, a, 1, width);

            point(rect1, r, g, b, a, 0, width);
            point(rect4, r, g, b, a, 1, width);
            point(rect3, r, g, b, a, 0, width);
        }
    }

    public void end() {
        if (bufferBuilder.getVertexCount() == 0) {
            return;
        }

        GpuBuffer vertexBuffer;
        try (ByteBufferBuilder.Result result = bufferBuilder.getVertexBuffer()) {
            vertexBuffer = this.pipeline.getVertexFormat().uploadImmediateVertexBuffer(result.byteBuffer());
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder builder = Std140Builder.onStack(stack, 4);
            builder.putFloat(FEATHER);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), builder.get());
        }

        RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Render Width Lines", mainRenderTarget.getColorTextureView(), OptionalInt.empty())) {
            renderPass.setPipeline(pipeline);
            renderPass.setUniform("Block", ubo);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.draw(0, bufferBuilder.getVertexCount());
        }
    }

    private boolean createRect(float lineWidth) {
        // Step 1: Convert clip-space coordinates to NDC (Normalized Device Coordinates)
        float v1x = v1.x / v1.w;
        float v1y = v1.y / v1.w;
        float v1z = v1.z / v1.w;
        float v2x = v2.x / v2.w;
        float v2y = v2.y / v2.w;
        float v2z = v2.z / v2.w;

        // Step 2: Convert NDC to screen coordinates
        float x1_screen = (v1x + 1.0f) * 0.5f * viewportWidth;
        float y1_screen = (v1y + 1.0f) * 0.5f * viewportHeight;

        float x2_screen = (v2x + 1.0f) * 0.5f * viewportWidth;
        float y2_screen = (v2y + 1.0f) * 0.5f * viewportHeight;

        // Step 3: Calculate the direction vector of the line in screen space
        float dx = x2_screen - x1_screen;
        float dy = y2_screen - y1_screen;

        // Calculate the length to normalize the perpendicular vector
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0.0f) {
            return false;
        }

        // Step 4: Compute the normalized perpendicular vector
        float px = -dy / length;
        float py = dx / length;

        // Step 5: Scale the perpendicular vector by half the line width to get the offset
        float offsetX = px * ((lineWidth + FEATHER) / 2.0f);
        float offsetY = py * ((lineWidth + FEATHER) / 2.0f);

        // Step 6: Calculate the four corner points of the rectangle in screen space
        float x0a = x1_screen + offsetX;
        float y0a = y1_screen + offsetY;

        float x0b = x1_screen - offsetX;
        float y0b = y1_screen - offsetY;

        float x1a = x2_screen + offsetX;
        float y1a = y2_screen + offsetY;

        float x1b = x2_screen - offsetX;
        float y1b = y2_screen - offsetY;

        // Step 7: Convert the rectangle's corner points back to NDC coordinates
        float x0a_ndc = (x0a / (viewportWidth * 0.5f)) - 1.0f;
        float y0a_ndc = (y0a / (viewportHeight * 0.5f)) - 1.0f;

        float x0b_ndc = (x0b / (viewportWidth * 0.5f)) - 1.0f;
        float y0b_ndc = (y0b / (viewportHeight * 0.5f)) - 1.0f;

        float x1a_ndc = (x1a / (viewportWidth * 0.5f)) - 1.0f;
        float y1a_ndc = (y1a / (viewportHeight * 0.5f)) - 1.0f;

        float x1b_ndc = (x1b / (viewportWidth * 0.5f)) - 1.0f;
        float y1b_ndc = (y1b / (viewportHeight * 0.5f)) - 1.0f;

        rect1.set(x0a_ndc, y0a_ndc, v1z, 1);
        rect2.set(x0b_ndc, y0b_ndc, v1z, 1);
        rect3.set(x1a_ndc, y1a_ndc, v2z, 1);
        rect4.set(x1b_ndc, y1b_ndc, v2z, 1);

        return true;
    }

    private void point(Vector4f v, float r, float g, float b, float a, float gradient, float width) {
        bufferBuilder.vertexFloat9(v.x, v.y, v.z, r, g, b, a, gradient, width);
    }

    private static final class Holder {
        public static final VertexColorLineWidthRenderer INSTANCE = new VertexColorLineWidthRenderer();
    }
}