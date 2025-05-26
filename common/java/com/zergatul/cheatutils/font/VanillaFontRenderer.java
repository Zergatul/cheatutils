package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.mixins.common.accessors.CompositeRenderTypeAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.CompositeStateAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.TextureStateShardAccessor;
import com.zergatul.cheatutils.render.MainFrameBuffer;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VanillaFontRenderer extends FontRenderer {

    private final Minecraft mc = Minecraft.getInstance();
    private final int scale;

    protected VanillaFontRenderer(int scale) {
        super(null, null);
        this.scale = scale;
    }

    @Override
    public boolean uses(GlyphRenderer renderer) {
        return false;
    }

    @Override
    public TextBounds getTextSize(StylizedText text) {
        int width = 0;
        for (StylizedTextChunk chunk : text.chunks) {
            width += mc.font.width(chunk.text());
        }
        int scale = getScale();
        return new TextBounds(width * scale, 0, mc.font.lineHeight * scale);
    }

    @Override
    public float getLineHeight() {
        return mc.font.lineHeight * getScale();
    }

//    @Override
//    public void drawText(Matrix4f matrix, StylizedText text, float x, float y) {
//        float scale = getScale();
//
//        for (StylizedTextChunk chunk : text.chunks) {
//
//            Map<RenderType, MyVertexConsumer> map = new HashMap<>();
//            var source = new MultiBufferSource() {
//                @Override
//                public VertexConsumer getBuffer(RenderType renderType) {
//                    if (map.containsKey(renderType)) {
//                        return map.get(renderType);
//                    } else {
//                        var consumer = new MyVertexConsumer();
//                        map.put(renderType, consumer);
//                        return consumer;
//                    }
//                }
//            };
//
//            mc.font.drawInBatch(
//                    chunk.text(),
//                    0, 0, chunk.getColor(),
//                    false, // drawShadow
//                    new Matrix4f(),
//                    source,
//                    Font.DisplayMode.NORMAL,
//                    0, // backgroundColor
//                    15728880);
//
//            RenderType type = map.keySet().stream().findFirst().orElseThrow();
//            if (type instanceof CompositeRenderTypeAccessor accessor) {
//                RenderType.CompositeState state = accessor.getState_CU();
//                RenderStateShard.EmptyTextureStateShard shard = ((CompositeStateAccessor) (Object) state).getTextureState_CU();
//                if (shard instanceof RenderStateShard.TextureStateShard textureStateShard) {
//                    Optional<ResourceLocation> texture = ((TextureStateShardAccessor) textureStateShard).getTexture_CU();
//                    AbstractTexture t = mc.getTextureManager().getTexture(texture.get());
//                    int id = ((GlTexture) t.getTexture()).glId();
//
//                    if (type.mode() == VertexFormat.Mode.QUADS) {
//                        MyVertexConsumer consumer = map.get(type);
//
//                        MainFrameBuffer.enter();
//
//                        TextureColor2dRenderer renderer = RenderUtilities.instance.getTextureColor2dRenderer();
//                        renderer.begin();
//                        for (int i = 0; i < consumer.list.size() / 8 / 4; i++) {
//                            renderer.quad(
//                                    x + consumer.list.get(i * 8 * 4 + 0) * scale, // x1
//                                    y + consumer.list.get(i * 8 * 4 + 1) * scale, // y1
//                                    consumer.list.get(i * 8 * 4 + 2), // u1
//                                    consumer.list.get(i * 8 * 4 + 3), // v1
//                                    x + consumer.list.get(i * 8 * 4 + 8) * scale, // x2
//                                    y + consumer.list.get(i * 8 * 4 + 9) * scale, // y2
//                                    consumer.list.get(i * 8 * 4 + 10), // u2
//                                    consumer.list.get(i * 8 * 4 + 11), // v2
//                                    x + consumer.list.get(i * 8 * 4 + 16) * scale, // x3
//                                    y + consumer.list.get(i * 8 * 4 + 17) * scale, // y3
//                                    consumer.list.get(i * 8 * 4 + 18), // u3
//                                    consumer.list.get(i * 8 * 4 + 19), // v3
//                                    x + consumer.list.get(i * 8 * 4 + 24) * scale, // x3
//                                    y + consumer.list.get(i * 8 * 4 + 25) * scale, // y3
//                                    consumer.list.get(i * 8 * 4 + 26), // u3
//                                    consumer.list.get(i * 8 * 4 + 27), // v3
//                                    consumer.list.get(i * 8 * 4 + 4), // r
//                                    consumer.list.get(i * 8 * 4 + 5), // g
//                                    consumer.list.get(i * 8 * 4 + 6), // b
//                                    consumer.list.get(i * 8 * 4 + 7)); // a
//                        }
//                        renderer.end(matrix, id);
//                    }
//                }
//            }
//
//            x += mc.font.width(chunk.text()) * scale;
//        }
//    }

    @Override
    public void drawText(RenderBuffers buffers, StylizedText text, float x, float y) {
        float scale = getScale();

        for (StylizedTextChunk chunk : text.chunks) {

            Map<RenderType, MyVertexConsumer> map = new HashMap<>();
            var source = new MultiBufferSource() {
                @Override
                public VertexConsumer getBuffer(RenderType renderType) {
                    if (map.containsKey(renderType)) {
                        return map.get(renderType);
                    } else {
                        var consumer = new MyVertexConsumer();
                        map.put(renderType, consumer);
                        return consumer;
                    }
                }
            };

            mc.font.drawInBatch(
                    chunk.text(),
                    0, 0, chunk.getColor(),
                    false, // drawShadow
                    new Matrix4f(),
                    source,
                    Font.DisplayMode.NORMAL,
                    0, // backgroundColor
                    15728880);

            RenderType type = map.keySet().stream().findFirst().orElseThrow();
            if (type instanceof CompositeRenderTypeAccessor accessor) {
                RenderType.CompositeState state = accessor.getState_CU();
                RenderStateShard.EmptyTextureStateShard shard = ((CompositeStateAccessor) (Object) state).getTextureState_CU();
                if (shard instanceof RenderStateShard.TextureStateShard textureStateShard) {
                    Optional<ResourceLocation> texture = ((TextureStateShardAccessor) textureStateShard).getTexture_CU();
                    AbstractTexture t = mc.getTextureManager().getTexture(texture.get());
                    int id = ((GlTexture) t.getTexture()).glId();

                    if (type.mode() == VertexFormat.Mode.QUADS) {
                        MyVertexConsumer consumer = map.get(type);

                        MainFrameBuffer.enter();

                        var buffer = buffers.getTexColor2d(id);
                        for (int i = 0; i < consumer.list.size() / 8 / 4; i++) {
                            buffer.quad(
                                    x + consumer.list.get(i * 8 * 4 + 0) * scale, // x1
                                    y + consumer.list.get(i * 8 * 4 + 1) * scale, // y1
                                    consumer.list.get(i * 8 * 4 + 2), // u1
                                    consumer.list.get(i * 8 * 4 + 3), // v1
                                    x + consumer.list.get(i * 8 * 4 + 8) * scale, // x2
                                    y + consumer.list.get(i * 8 * 4 + 9) * scale, // y2
                                    consumer.list.get(i * 8 * 4 + 10), // u2
                                    consumer.list.get(i * 8 * 4 + 11), // v2
                                    x + consumer.list.get(i * 8 * 4 + 16) * scale, // x3
                                    y + consumer.list.get(i * 8 * 4 + 17) * scale, // y3
                                    consumer.list.get(i * 8 * 4 + 18), // u3
                                    consumer.list.get(i * 8 * 4 + 19), // v3
                                    x + consumer.list.get(i * 8 * 4 + 24) * scale, // x3
                                    y + consumer.list.get(i * 8 * 4 + 25) * scale, // y3
                                    consumer.list.get(i * 8 * 4 + 26), // u3
                                    consumer.list.get(i * 8 * 4 + 27), // v3
                                    consumer.list.get(i * 8 * 4 + 4), // r
                                    consumer.list.get(i * 8 * 4 + 5), // g
                                    consumer.list.get(i * 8 * 4 + 6), // b
                                    consumer.list.get(i * 8 * 4 + 7)); // a
                        }
                    }
                }
            }

            x += mc.font.width(chunk.text()) * scale;
        }
    }

    private int getScale() {
        if (scale == 0) {
            return (int) mc.getWindow().getGuiScale();
        } else {
            return scale;
        }
    }

    private static class MyVertexConsumer implements VertexConsumer {

        private final FloatList list;
        private int index;

        private MyVertexConsumer() {
            this.list = new FloatArrayList();
            /*
                list.add(x);
                list.add(y);
                list.add(u);
                list.add(v);
                list.add(r);
                list.add(g);
                list.add(b);
                list.add(a);
            */
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            index = list.size();
            for (int i = 0; i < 8; i++) {
                list.add(0);
            }
            list.set(index, x);
            list.set(index + 1, y);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            list.set(index + 4, r / 255f);
            list.set(index + 5, g / 255f);
            list.set(index + 6, b / 255f);
            list.set(index + 7, a / 255f);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            list.set(index + 2, u);
            list.set(index + 3, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int i, int j) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int i, int j) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float f, float g, float h) {
            return this;
        }
    }
}