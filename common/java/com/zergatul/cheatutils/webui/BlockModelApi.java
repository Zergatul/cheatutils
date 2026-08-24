package com.zergatul.cheatutils.webui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.utils.JavaRandom;
import com.zergatul.cheatutils.wrappers.BakedModelWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlockModelApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-model";
    }

    @Override
    public String get(String id) throws ApiException {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new ApiException("Invalid block id: " + id, HttpResponseCodes.BAD_REQUEST);
        }

        Block block = Registries.BLOCKS.getValue(location);
        if (block == null || block == Blocks.AIR && !location.equals(Registries.BLOCKS.getKey(Blocks.AIR))) {
            throw new ApiException("Cannot find block by id: " + id, HttpResponseCodes.NOT_FOUND);
        }

        List<Quad> quads = getFromBlockModel(block);
        if (quads.isEmpty()) {
            quads = getFromItemRenderer(block);
        }

        return gson.toJson(quads);
    }

    private static List<Quad> getFromBlockModel(Block block) {
        List<Quad> result = new ArrayList<>();
        BlockState state = block.defaultBlockState();
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        JavaRandom random = new JavaRandom(0);

        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : BakedModelWrapper.getQuads(model, direction, random)) {
                result.add(new Quad(quad, state));
            }
        }
        for (BakedQuad quad : BakedModelWrapper.getQuads(model, null, random)) {
            result.add(new Quad(quad, state));
        }

        return result;
    }

    private static List<Quad> getFromItemRenderer(Block block) {
        List<Quad> result = new ArrayList<>();
        PoseStack pose = new PoseStack();
        ItemStack stack = new ItemStack(block);
        MemoryMultiBufferSource bufferSources = new MemoryMultiBufferSource();

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.GROUND,
                0,
                OverlayTexture.NO_OVERLAY,
                pose,
                bufferSources,
                null,
                0);

        for (Map.Entry<RenderType, MemoryVertexConsumer> entry : bufferSources.buffers.entrySet()) {
            RenderType renderType = entry.getKey();
            if (renderType.mode() != VertexFormat.Mode.QUADS) {
                continue;
            }

            ResourceLocation texture = getTexture(renderType);
            if (texture == null) {
                continue;
            }

            List<Vertex> vertices = entry.getValue().vertices;
            for (Vertex vertex : vertices) {
                vertex.x = (vertex.x + 0.125f) * 4;
                vertex.y = (vertex.y - 0.0625f) * 4;
                vertex.z = (vertex.z + 0.125f) * 4;
            }

            int index = 0;
            while (index <= vertices.size() - 4) {
                result.add(new Quad(
                        texture.toString(),
                        vertices.get(index++),
                        vertices.get(index++),
                        vertices.get(index++),
                        vertices.get(index++)));
            }
        }

        return result;
    }

    private static ResourceLocation getTexture(RenderType renderType) {
        try {
            for (Field field : renderType.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                field.setAccessible(true);
                Object candidate = field.get(renderType);
                if (candidate == null || candidate.getClass().getEnclosingClass() != RenderType.class) {
                    continue;
                }
                ResourceLocation location = getTextureFromCompositeState(candidate);
                if (location != null) {
                    return location;
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot inspect item render type.", e);
        }

        return null;
    }

    private static ResourceLocation getTextureFromCompositeState(Object state) throws IllegalAccessException {
        if (state == null) {
            return null;
        }

        for (Field field : state.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            Object shard = field.get(state);
            if (shard instanceof RenderStateShard) {
                ResourceLocation location = getTextureFromShard(shard);
                if (location != null) {
                    return location;
                }
            }
        }

        return null;
    }

    private static ResourceLocation getTextureFromShard(Object shard) throws IllegalAccessException {
        for (Class<?> type = shard.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getType() != Optional.class) {
                    continue;
                }

                field.setAccessible(true);
                Optional<?> optional = (Optional<?>) field.get(shard);
                if (optional.isPresent() && optional.get() instanceof ResourceLocation location) {
                    return location;
                }
            }
        }

        return null;
    }

    private static class MemoryMultiBufferSource implements MultiBufferSource {

        private final Map<RenderType, MemoryVertexConsumer> buffers = new HashMap<>();

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return buffers.computeIfAbsent(renderType, key -> new MemoryVertexConsumer());
        }
    }

    private static class MemoryVertexConsumer implements VertexConsumer {

        private final List<Vertex> vertices = new ArrayList<>();
        private Vertex current;
        private boolean useDefaultColor;
        private int defaultR;
        private int defaultG;
        private int defaultB;
        private int defaultA;

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            current = new Vertex();
            current.x = (float) x;
            current.y = (float) y;
            current.z = (float) z;
            if (useDefaultColor) {
                current.r = defaultR;
                current.g = defaultG;
                current.b = defaultB;
                current.a = defaultA;
            }
            vertices.add(current);
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            current.r = r;
            current.g = g;
            current.b = b;
            current.a = a;
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            current.u = u;
            current.v = v;
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            return this;
        }

        @Override
        public void endVertex() {
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            useDefaultColor = true;
            defaultR = r;
            defaultG = g;
            defaultB = b;
            defaultA = a;
        }

        @Override
        public void unsetDefaultColor() {
            useDefaultColor = false;
        }
    }

    private static class Quad {

        public final String location;
        public final Vertex[] vertices;

        public Quad(BakedQuad quad, BlockState state) {
            location = quad.getSprite().atlasLocation().toString();
            int[] values = quad.getVertices();
            vertices = new Vertex[4];

            for (int i = 0; i < vertices.length; i++) {
                int offset = i * 8;
                Vertex vertex = new Vertex();
                vertex.x = Float.intBitsToFloat(values[offset]);
                vertex.y = Float.intBitsToFloat(values[offset + 1]);
                vertex.z = Float.intBitsToFloat(values[offset + 2]);

                if (quad.isTinted()) {
                    int color = Minecraft.getInstance().getBlockColors().getColor(
                            state, null, null, quad.getTintIndex());
                    vertex.r = color >> 16 & 0xFF;
                    vertex.g = color >> 8 & 0xFF;
                    vertex.b = color & 0xFF;
                    vertex.a = 255;
                } else {
                    int color = values[offset + 3];
                    vertex.r = color & 0xFF;
                    vertex.g = color >> 8 & 0xFF;
                    vertex.b = color >> 16 & 0xFF;
                    vertex.a = color >> 24 & 0xFF;
                }

                vertex.u = Float.intBitsToFloat(values[offset + 4]);
                vertex.v = Float.intBitsToFloat(values[offset + 5]);
                vertices[i] = vertex;
            }
        }

        public Quad(String location, Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
            this.location = location;
            this.vertices = new Vertex[] { v1, v2, v3, v4 };
        }
    }

    private static class Vertex {
        public float x;
        public float y;
        public float z;
        public int r;
        public int g;
        public int b;
        public int a;
        public float u;
        public float v;
    }
}