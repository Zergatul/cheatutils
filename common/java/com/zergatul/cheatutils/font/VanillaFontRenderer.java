package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.mixins.common.accessors.RenderSetupAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.RenderTypeAccessor;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import com.zergatul.cheatutils.render.buffers.TextureColor2dRenderBuffer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

import static com.zergatul.cheatutils.render.GlHelper.getGlTexture;

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

            mc.font.drawInBatch(
                    chunk.text(),
                    0, 0,
                    chunk.color(),
                    false, // drawShadow
                    new Matrix4f(),
                    bufferSource,
                    Font.DisplayMode.NORMAL,
                    0, // backgroundColor
                    15728880);

            if (bufferSource.hasData()) {
                Map<RenderType, FontVertexConsumer> consumers = bufferSource.getConsumers();
                for (var entry : consumers.entrySet()) {
                    RenderType type = entry.getKey();
                    FontVertexConsumer consumer = entry.getValue();

                    if (consumer.list.isEmpty()) {
                        continue;
                    }

                    RenderSetup setup = ((RenderTypeAccessor) type).getState_CU();
                    Map<String, RenderSetup.TextureBinding> textures = ((RenderSetupAccessor) (Object) setup).getTextures_CU();
                    RenderSetup.TextureBinding binding = textures.get("Sampler0");
                    if (binding == null) {
                        continue;
                    }

                    if (entry.getKey().mode() != VertexFormat.Mode.QUADS) {
                        continue;
                    }

                    AbstractTexture texture = mc.getTextureManager().getTexture(binding.location());
                    int id = getGlTexture(texture.getTexture()).glId();

                    TextureColor2dRenderBuffer buffer = buffers.getTexColor2d(id);
                    if (dropShadow) {
                        for (int i = 0; i < consumer.list.size() / 8 / 4; i++) {
                            float r = consumer.list.getFloat(i * 8 * 4 + 4) * SHADOW_FACTOR;
                            float g = consumer.list.getFloat(i * 8 * 4 + 5) * SHADOW_FACTOR;
                            float b = consumer.list.getFloat(i * 8 * 4 + 6) * SHADOW_FACTOR;
                            float a = consumer.list.getFloat(i * 8 * 4 + 7);
                            buffer.quad(
                                    x + scale + consumer.list.getFloat(i * 8 * 4 + 0) * scale, // x1
                                    y + scale + consumer.list.getFloat(i * 8 * 4 + 1) * scale, // y1
                                    consumer.list.getFloat(i * 8 * 4 + 2), // u1
                                    consumer.list.getFloat(i * 8 * 4 + 3), // v1
                                    x + scale + consumer.list.getFloat(i * 8 * 4 + 8) * scale, // x2
                                    y + scale + consumer.list.getFloat(i * 8 * 4 + 9) * scale, // y2
                                    consumer.list.getFloat(i * 8 * 4 + 10), // u2
                                    consumer.list.getFloat(i * 8 * 4 + 11), // v2
                                    x + scale + consumer.list.getFloat(i * 8 * 4 + 16) * scale, // x3
                                    y + scale + consumer.list.getFloat(i * 8 * 4 + 17) * scale, // y3
                                    consumer.list.getFloat(i * 8 * 4 + 18), // u3
                                    consumer.list.getFloat(i * 8 * 4 + 19), // v3
                                    x + scale + consumer.list.getFloat(i * 8 * 4 + 24) * scale, // x3
                                    y + scale + consumer.list.getFloat(i * 8 * 4 + 25) * scale, // y3
                                    consumer.list.getFloat(i * 8 * 4 + 26), // u3
                                    consumer.list.getFloat(i * 8 * 4 + 27), // v3
                                    r, g, b, a);
                        }
                    }

                    for (int i = 0; i < consumer.list.size() / 8 / 4; i++) {
                        buffer.quad(
                                x + consumer.list.getFloat(i * 8 * 4 + 0) * scale, // x1
                                y + consumer.list.getFloat(i * 8 * 4 + 1) * scale, // y1
                                consumer.list.getFloat(i * 8 * 4 + 2), // u1
                                consumer.list.getFloat(i * 8 * 4 + 3), // v1
                                x + consumer.list.getFloat(i * 8 * 4 + 8) * scale, // x2
                                y + consumer.list.getFloat(i * 8 * 4 + 9) * scale, // y2
                                consumer.list.getFloat(i * 8 * 4 + 10), // u2
                                consumer.list.getFloat(i * 8 * 4 + 11), // v2
                                x + consumer.list.getFloat(i * 8 * 4 + 16) * scale, // x3
                                y + consumer.list.getFloat(i * 8 * 4 + 17) * scale, // y3
                                consumer.list.getFloat(i * 8 * 4 + 18), // u3
                                consumer.list.getFloat(i * 8 * 4 + 19), // v3
                                x + consumer.list.getFloat(i * 8 * 4 + 24) * scale, // x3
                                y + consumer.list.getFloat(i * 8 * 4 + 25) * scale, // y3
                                consumer.list.getFloat(i * 8 * 4 + 26), // u3
                                consumer.list.getFloat(i * 8 * 4 + 27), // v3
                                consumer.list.getFloat(i * 8 * 4 + 4), // r
                                consumer.list.getFloat(i * 8 * 4 + 5), // g
                                consumer.list.getFloat(i * 8 * 4 + 6), // b
                                consumer.list.getFloat(i * 8 * 4 + 7)); // a
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

    private static class FontBufferSource implements MultiBufferSource {

        private final Map<RenderType, FontVertexConsumer> map = new HashMap<>();

        public void clear() {
            for (FontVertexConsumer consumer : map.values()) {
                consumer.clear();
            }
        }

        public boolean hasData() {
            return map.values().stream().anyMatch(c -> !c.list.isEmpty());
        }

        public Map<RenderType, FontVertexConsumer> getConsumers() {
            return map;
        }

        @Override
        public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
            if (map.containsKey(renderType)) {
                return map.get(renderType);
            } else {
                var consumer = new FontVertexConsumer();
                map.put(renderType, consumer);
                return consumer;
            }
        }
    }

    private static class FontVertexConsumer implements VertexConsumer {

        private final FloatList list;
        private int index;

        private FontVertexConsumer() {
            this.list = new FloatArrayList();
        }

        public void clear() {
            list.clear();
            index = 0;
        }

        @Override
        public @NotNull VertexConsumer addVertex(float x, float y, float z) {
            index = list.size();
            for (int i = 0; i < 8; i++) {
                list.add(0);
            }
            list.set(index, x);
            list.set(index + 1, y);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setColor(int c) {
            return setColor((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, (c >> 24) & 0xFF);
        }

        @Override
        public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
            list.set(index + 4, r / 255f);
            list.set(index + 5, g / 255f);
            list.set(index + 6, b / 255f);
            list.set(index + 7, a / 255f);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv(float u, float v) {
            list.set(index + 2, u);
            list.set(index + 3, v);
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
    }
}