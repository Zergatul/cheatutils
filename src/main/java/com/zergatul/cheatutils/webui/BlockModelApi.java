package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.utils.ResourceLocationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class BlockModelApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-model";
    }

    @Override
    public String get(String id) throws Throwable {
        ResourceLocation location = ResourceLocationHelper.parseSafe(id);
        if (location == null) {
            return gson.toJson(null);
        }

        Block block = ForgeRegistries.BLOCKS.getValue(location);
        if (block == null) {
            return gson.toJson(null);
        }

        return gson.toJson(getBlockModel(block));
    }

    private List<Quad> getBlockModel(Block block) {
        IBlockState state = block.getDefaultState();
        IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);

        List<Quad> quads = new ArrayList<>();
        for (EnumFacing side : EnumFacing.values()) {
            for (BakedQuad baked : model.getQuads(state, side, 0L)) {
                quads.add(fromBakedQuad(state, baked));
            }
        }
        for (BakedQuad baked : model.getQuads(state, null, 0L)) {
            quads.add(fromBakedQuad(state, baked));
        }

        return quads;
    }

    private Quad fromBakedQuad(IBlockState state, BakedQuad quad) {
        int tint = quad.hasTintIndex() ?
                Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, null, null, quad.getTintIndex()) :
                0xFFFFFF;
        float r = ColorUtils.r(tint);
        float g = ColorUtils.g(tint);
        float b = ColorUtils.b(tint);

        Vertex[] vertices = new Vertex[4];
        float[] values = new float[4];
        for (int i = 0; i < 4; i++) {
            Vertex vertex = vertices[i] = new Vertex();
            vertex.r = vertex.g = vertex.b = vertex.a = 255;

            for (int elementIndex = 0; elementIndex < quad.getFormat().getElementCount(); elementIndex++) {
                VertexFormatElement element = quad.getFormat().getElement(elementIndex);
                switch (element.getUsage()) {
                    case POSITION:
                        LightUtil.unpack(quad.getVertexData(), values, quad.getFormat(), i, elementIndex);
                        vertex.x = values[0] - 0.5f;
                        vertex.y = values[1] - 0.5f;
                        vertex.z = values[2] - 0.5f;
                        break;
                    case COLOR:
                        LightUtil.unpack(quad.getVertexData(), values, quad.getFormat(), i, elementIndex);
                        vertex.r = Math.round(255 * values[0]);
                        vertex.g = Math.round(255 * values[1]);
                        vertex.b = Math.round(255 * values[2]);
                        vertex.a = Math.round(255 * values[3]);
                        break;
                    case UV:
                        if (element.getIndex() == 0) {
                            LightUtil.unpack(quad.getVertexData(), values, quad.getFormat(), i, elementIndex);
                            vertex.u = values[0];
                            vertex.v = values[1];
                        }
                        break;
                    default:
                        break;
                }
            }

            vertex.r = Math.round(vertex.r * r);
            vertex.g = Math.round(vertex.g * g);
            vertex.b = Math.round(vertex.b * b);
        }

        return new Quad(TextureMap.LOCATION_BLOCKS_TEXTURE.toString(), vertices[0], vertices[1], vertices[2], vertices[3]);
    }

    public static class Quad {

        public final String location;
        public final Vertex[] vertices;

        public Quad(String location, Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
            this.location = location;
            this.vertices = new Vertex[] { v1, v2, v3, v4 };
        }
    }

    public static class Vertex {
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