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
import net.minecraft.world.level.block.state.BlockState;
import org.apache.http.HttpException;
import org.apache.http.MethodNotSupportedException;

import java.util.ArrayList;
import java.util.List;

public class BlockModelApi extends ApiBase {

    private final JavaRandom random = new JavaRandom(0);

    @Override
    public String getRoute() {
        return "block-model";
    }

    @Override
    public String get(String id) throws HttpException {
        ResourceLocation loc = new ResourceLocation(id);
        Block block = Registries.BLOCKS.getValue(loc);
        if (block == null) {
            throw new MethodNotSupportedException("Cannot find block by id.");
        }

        List<Quad> result = new ArrayList<>();

        BlockState state = block.defaultBlockState();
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads = BakedModelWrapper.getQuads(model, direction, random);
            for (BakedQuad quad : quads) {
                result.add(new Quad(quad));
            }
        }

        return gson.toJson(result);
    }

    public static class Quad {

        public final String location;
        public final Vertex[] vertices;

        public Quad(BakedQuad quad) {
            this.location = quad.getSprite().atlasLocation().toString();

            int[] values = quad.getVertices();
            this.vertices = new Vertex[4];
            for (int i = 0; i < 4; i++) {
                int offset = i * 8;
                this.vertices[i] = new Vertex();
                this.vertices[i].x = Float.intBitsToFloat(values[offset]);
                this.vertices[i].y = Float.intBitsToFloat(values[offset + 1]);
                this.vertices[i].z = Float.intBitsToFloat(values[offset + 2]);
                this.vertices[i].r = values[offset + 3];
                this.vertices[i].g = values[offset + 3];
                this.vertices[i].b = values[offset + 3];
                this.vertices[i].u = Float.intBitsToFloat(values[offset + 4]);
                this.vertices[i].v = Float.intBitsToFloat(values[offset + 5]);
            }
        }
    }

    public static class Vertex {
        public float x;
        public float y;
        public float z;
        public int r;
        public int g;
        public int b;
        public float u;
        public float v;
    }
}
