package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.mixins.common.accessors.RenderSetupAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.RenderTypeAccessor;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import com.zergatul.cheatutils.utils.ColorUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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

public class CustomizableVanillaFontRenderer {

    public static final CustomizableVanillaFontRenderer instance = new CustomizableVanillaFontRenderer();

    protected static final float SHADOW_FACTOR = 0.25f;

    private final FontBufferSource bufferSource;

    private CustomizableVanillaFontRenderer() {
        this.bufferSource = new FontBufferSource();
    }

    public void render(Font font, int scale, RenderBuffers buffers, String text, int x, int y) {
        bufferSource.clear();

        VanillaFontHelper.drawInBatch(
                font,
                text,
                0, 0,
                -1,
                false, // drawShadow
                new Matrix4f(),
                bufferSource,
                Font.DisplayMode.NORMAL,
                0, // backgroundColor
                15728880);

        Map<RenderType, FontVertexConsumer> consumers = bufferSource.getConsumers();
        for (var entry : consumers.entrySet()) {
            RenderType type = entry.getKey();
            FontVertexConsumer consumer = entry.getValue();

            if (consumer.buffer.isEmpty()) {
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

            AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(binding.location());
            for (int i = 0; i < consumer.buffer.size() / 20; i++) {
                Position2dTextureColorRenderer.BufferBuilder buffer = buffers.getTexColor2d(RenderBuffers.FONT, texture.getTextureView());
                buffer.quad(
                        x + consumer.getX(i * 4) * scale, // x1
                        y + consumer.getY(i * 4) * scale, // y1
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

            Position2dTextureColorRenderer.BufferBuilder buffer = buffers.getTexColor2d(RenderBuffers.FONT_SHADOW, texture.getTextureView());
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
    }

    private static class FontBufferSource implements MultiBufferSource {

        private final Map<RenderType, FontVertexConsumer> map = new HashMap<>();

        public void clear() {
            for (FontVertexConsumer consumer : map.values()) {
                consumer.clear();
            }
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