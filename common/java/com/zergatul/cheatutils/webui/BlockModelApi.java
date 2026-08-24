package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.utils.JavaRandom;
import com.zergatul.cheatutils.wrappers.BakedModelWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

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

        return gson.toJson(getQuads(block));
    }

    private static List<Quad> getQuads(Block block) {
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