package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.render.Position2dTextureColorRenderer;
import com.zergatul.cheatutils.render.VanillaFontHelper;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import com.zergatul.cheatutils.utils.ColorUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.TextRenderable;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class VanillaFontRenderer extends FontRenderer {

    private final Minecraft mc = Minecraft.getInstance();
    private final FontBufferSource bufferSource = new FontBufferSource();
    private final FontBackend backend;
    private final int scale;
    private final boolean dropShadow;

    protected VanillaFontRenderer(FontBackend backend, int scale, boolean dropShadow) {
        this.backend = backend;
        this.scale = scale;
        this.dropShadow = dropShadow;
    }

    @Override
    public boolean uses(FontBackend backend) {
        return this.backend == backend;
    }

    @Override
    public TextBounds getTextSize(StylizedText text) {
        backend.markUse();

        int width = 0;
        for (StylizedTextChunk chunk : text.chunks) {
            width += mc.font.width(chunk.text());
        }
        int scale = getScale();
        return new TextBounds(width * scale, -mc.font.lineHeight * scale, 0);
    }

    @Override
    public float getLineHeight() {
        return mc.font.lineHeight * getScale();
    }

    @Override
    public void drawText(RenderBuffers buffers, StylizedText text, float x, float y) {
        backend.markUse();

        float scale = getScale();

        y -= getLineHeight();

        for (StylizedTextChunk chunk : text.chunks) {
            if (chunk.text().isEmpty()) {
                continue;
            }

            VanillaFontHelper.visit(
                    mc.font,
                    chunk.text(),
                    0, 0,
                    chunk.color(),
                    false, // drawShadow
                    0, // backgroundColor
                    bufferSource::accept);

            if (bufferSource.hasData()) {
                Map<GpuTextureView, FontVertexConsumer> consumers = bufferSource.getConsumers();
                for (var entry : consumers.entrySet()) {
                    GpuTextureView textureView = entry.getKey();
                    FontVertexConsumer consumer = entry.getValue();

                    if (consumer.buffer.isEmpty()) {
                        continue;
                    }

                    if (dropShadow) {
                        Position2dTextureColorRenderer.BufferBuilder buffer = buffers.getTexColor2d(RenderBuffers.FONT_SHADOW, textureView);
                        for (int i = 0; i < consumer.buffer.size() / 20; i++) {
                            buffer.quad(
                                    x + scale + consumer.getX(i * 4) * scale, // x1
                                    y + scale + consumer.getY(i * 4) * scale, // y1
                                    consumer.getU(i * 4), // u1
                                    consumer.getV(i * 4), // v1
                                    x + scale + consumer.getX(i * 4 + 1) * scale, // x2
                                    y + scale + consumer.getY(i * 4 + 1) * scale, // y2
                                    consumer.getU(i * 4 + 1), // u2
                                    consumer.getV(i * 4 + 1), // v2
                                    x + scale + consumer.getX(i * 4 + 2) * scale, // x3
                                    y + scale + consumer.getY(i * 4 + 2) * scale, // y3
                                    consumer.getU(i * 4 + 2), // u3
                                    consumer.getV(i * 4 + 2), // v3
                                    x + scale + consumer.getX(i * 4 + 3) * scale, // x3
                                    y + scale + consumer.getY(i * 4 + 3) * scale, // y3
                                    consumer.getU(i * 4 + 3), // u3
                                    consumer.getV(i * 4 + 3), // v3
                                    ColorUtils.shadowed(consumer.getColor(i * 4), SHADOW_FACTOR));
                        }
                    }

                    Position2dTextureColorRenderer.BufferBuilder buffer = buffers.getTexColor2d(RenderBuffers.FONT, textureView);
                    for (int i = 0; i < consumer.buffer.size() / 20; i++) {
                        buffer.quad(
                                x + consumer.getX(i * 4) * scale, // x1
                                y +  consumer.getY(i * 4) * scale, // y1
                                consumer.getU(i * 4), // u1
                                consumer.getV(i * 4), // v1
                                x + consumer.getX(i * 4 + 1) * scale, // x2
                                y + consumer.getY(i * 4 + 1) * scale, // y2
                                consumer.getU(i * 4 + 1), // u2
                                consumer.getV(i * 4 + 1), // v2
                                x + consumer.getX(i * 4 + 2) * scale, // x3
                                y + consumer.getY(i * 4 + 2) * scale, // y3
                                consumer.getU(i * 4 + 2), // u3
                                consumer.getV(i * 4 + 2), // v3
                                x + consumer.getX(i * 4 + 3) * scale, // x3
                                y + consumer.getY(i * 4 + 3) * scale, // y3
                                consumer.getU(i * 4 + 3), // u3
                                consumer.getV(i * 4 + 3), // v3
                                consumer.getColor(i * 4));
                    }
                }
            }

            bufferSource.clear();

            x += mc.font.width(chunk.text()) * scale;
        }
    }

    private int getScale() {
        if (scale == 0) {
            return mc.getWindow().getGuiScale();
        } else {
            return scale;
        }
    }

    private static class FontBufferSource {

        private final Map<GpuTextureView, FontVertexConsumer> map = new HashMap<>();

        public void clear() {
            for (FontVertexConsumer consumer : map.values()) {
                consumer.clear();
            }
        }

        public boolean hasData() {
            return map.values().stream().anyMatch(c -> !c.buffer.isEmpty());
        }

        public Map<GpuTextureView, FontVertexConsumer> getConsumers() {
            return map;
        }

        public void accept(TextRenderable renderable) {
            GpuTextureView textureView = renderable.textureView();
            FontVertexConsumer consumer;
            if (map.containsKey(textureView)) {
                consumer = map.get(textureView);
            } else {
                consumer = new FontVertexConsumer();
                map.put(textureView, consumer);
            }
            renderable.render(new Matrix4f(), consumer, 15728880, false);
        }
    }

    private static class FontVertexConsumer implements VertexConsumer {

        public final IntList buffer;
        private boolean hasPos, hasColor, hasUv;
        private float x, y, u, v;
        private int color;

        private FontVertexConsumer() {
            this.buffer = new IntArrayList();
        }

        public void clear() {
            buffer.clear();
        }

        public float getX(int index) {
            return Float.intBitsToFloat(buffer.getInt(index * 5));
        }

        public float getY(int index) {
            return Float.intBitsToFloat(buffer.getInt(index * 5 + 1));
        }

        public float getU(int index) {
            return Float.intBitsToFloat(buffer.getInt(index * 5 + 2));
        }

        public float getV(int index) {
            return Float.intBitsToFloat(buffer.getInt(index * 5 + 3));
        }

        public int getColor(int index) {
            return buffer.getInt(index * 5 + 4);
        }

        @Override
        public @NotNull VertexConsumer addVertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.hasPos = true;
            this.store();
            return this;
        }

        @Override
        public @NotNull VertexConsumer setColor(int c) {
            this.color = c;
            this.hasColor = true;
            this.store();
            return this;
        }

        @Override
        public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
            throw new AssertionError();
        }

        @Override
        public @NotNull VertexConsumer setUv(float u, float v) {
            this.u = u;
            this.v = v;
            this.hasUv = true;
            this.store();
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv1(int i, int j) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv2(int i, int j) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setNormal(float f, float g, float h) {
            return this;
        }

        @Override
        public @NotNull VertexConsumer setLineWidth(float f) {
            return this;
        }

        private void store() {
            if (this.hasPos && this.hasColor && this.hasUv) {
                buffer.add(Float.floatToIntBits(this.x));
                buffer.add(Float.floatToIntBits(this.y));
                buffer.add(Float.floatToIntBits(this.u));
                buffer.add(Float.floatToIntBits(this.v));
                buffer.add(this.color);
                this.hasPos = this.hasColor = this.hasUv = false;
            }
        }
    }
}