package com.zergatul.cheatutils.webui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.mixins.common.accessors.ModelFeatureRendererStorageAccessor;
import com.zergatul.cheatutils.render.RenderTypeHelper;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class BlockModelApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-model";
    }

    @Override
    public String get(String id) throws ApiException, ExecutionException, InterruptedException {
        Identifier loc = Identifier.parse(id);
        // we need to run this in the main thread
        // because we call submission.model().setupAnim(..), and this mutates Model state
        // that can be used for rendering real stuff at the same time
        List<Quad> quads = Minecraft.getInstance().submit(() -> {
            Block block = Registries.BLOCKS.getValue(loc);
            return getFromBlockModel(block);
        }).get();
        return gson.toJson(quads);
    }

    private List<Quad> getFromBlockModel(Block block) {
        BlockState blockState = block.defaultBlockState();
        BlockModel model = Minecraft.getInstance().getModelManager().getBlockModelSet().get(blockState);
        BlockModelRenderState renderState = new BlockModelRenderState();
        model.update(renderState, blockState, BlockDisplayContext.create(), 0);

        SubmitNodeStorage storage = new SubmitNodeStorage();
        renderState.submit(new PoseStack(), storage, 0, 0, 0);

        return extractQuads(storage);
    }

    private List<Quad> extractQuads(SubmitNodeStorage storage) {
        List<Quad> result = new ArrayList<>();
        for (SubmitNodeCollection collection : storage.getSubmitsPerOrder().values()) {
            extractQuads(collection, result);
        }
        return result;
    }

    private void extractQuads(SubmitNodeCollection collection, List<Quad> output) {
        for (BlockModelFeatureRenderer.Submit submission : collection.getBlockModelSubmits()) {
            extractQuads(submission, output);
        }

        extractQuads(collection.getModelSubmits(), output);

        ModMain.BRIDGE.extractAdditionalQuads(collection, output);
    }

    private void extractQuads(BlockModelFeatureRenderer.Submit submission, List<Quad> output) {
        for (BlockStateModelPart part : submission.modelParts()) {
            for (Direction direction : Direction.values()) {
                extractQuads(part.getQuads(direction), submission.tintLayers(), output);
            }
            extractQuads(part.getQuads(null), submission.tintLayers(), output);
        }
    }

    private void extractQuads(ModelFeatureRenderer.Storage storage, List<Quad> output) {
        ModelFeatureRendererStorageAccessor accessor = (ModelFeatureRendererStorageAccessor) storage;
        // order is important for banners: first translucent, second solid
        // since quad with the same coordinates are submitted?
        for (ModelFeatureRenderer.Submit<?> submission : accessor.getTranslucentModelSubmits_CU()) {
            extractQuads(submission, output);
        }
        accessor.getSolidModelSubmits_CU().forEach((renderType, submissions) -> {
            for (ModelFeatureRenderer.Submit<?> submission : submissions) {
                extractQuads(renderType, submission, output);
            }
        });
    }

    private <S> void extractQuads(ModelFeatureRenderer.Submit<S> submission, List<Quad> output) {
        extractQuads(submission.renderType(), submission, output);
    }

    private <S> void extractQuads(RenderType renderType, ModelFeatureRenderer.Submit<S> submission, List<Quad> output) {
        TextureAtlasSprite sprite = submission.sprite();
        String textureLocation;
        if (sprite != null) {
            textureLocation = sprite.atlasLocation().toString();
        } else {
            Optional<Identifier> optional = RenderTypeHelper.getTextureLocation(renderType);
            if (optional.isEmpty()) {
                return;
            }
            textureLocation = optional.get().toString();
        }

        submission.model().setupAnim(submission.state());
        submission.model().root().visit(new PoseStack(), (pose, str, i, cube) -> {
            for (ModelPart.Polygon polygon : cube.polygons) {
                output.add(new Quad(
                        textureLocation,
                        new Vertex(submission.pose(), pose, sprite, polygon.vertices()[0], submission.tintedColor()),
                        new Vertex(submission.pose(), pose, sprite, polygon.vertices()[1], submission.tintedColor()),
                        new Vertex(submission.pose(), pose, sprite, polygon.vertices()[2], submission.tintedColor()),
                        new Vertex(submission.pose(), pose, sprite, polygon.vertices()[3], submission.tintedColor())));
            }
        });
    }

    private void extractQuads(List<BakedQuad> bakedQuads, int[] tintLayers, List<Quad> output) {
        for (BakedQuad quad : bakedQuads) {
            output.add(new Quad(quad, tintLayers));
        }
    }

    public static class Quad {

        public final String location;
        public final Vertex[] vertices;

        public Quad(BakedQuad quad, int[] tintLayers) {
            this.location = quad.materialInfo().sprite().atlasLocation().toString();

            this.vertices = new Vertex[4];
            for (int i = 0; i < 4; i++) {
                this.vertices[i] = new Vertex();
                this.vertices[i].x = quad.position(i).x() - 0.5f;
                this.vertices[i].y = quad.position(i).y() - 0.5f;
                this.vertices[i].z = quad.position(i).z() - 0.5f;

                if (quad.materialInfo().isTinted() && quad.materialInfo().tintIndex() < tintLayers.length) {
                    int color = tintLayers[quad.materialInfo().tintIndex()];
                    this.vertices[i].r = ColorUtils.Int.r(color);
                    this.vertices[i].g = ColorUtils.Int.g(color);
                    this.vertices[i].b = ColorUtils.Int.b(color);
                    this.vertices[i].a = ColorUtils.Int.a(color);
                } else {
                    this.vertices[i].r = 255;
                    this.vertices[i].g = 255;
                    this.vertices[i].b = 255;
                    this.vertices[i].a = 255;
                }

                this.vertices[i].u = UVPair.unpackU(quad.packedUV(i));
                this.vertices[i].v = UVPair.unpackV(quad.packedUV(i));
            }
        }

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

        public Vertex() {}

        public Vertex(PoseStack.Pose pose1, PoseStack.Pose pose2, @Nullable TextureAtlasSprite sprite, ModelPart.Vertex vertex, int color) {
            Vector3f pos = pose1.pose().mul(pose2.pose(), new Matrix4f()).transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
            this.x = pos.x() - 0.5f;
            this.y = pos.y() - 0.5f;
            this.z = pos.z() - 0.5f;
            this.r = ((color >>> 16) & 0xFF);
            this.g = ((color >>> 8) & 0xFF);
            this.b = ((color >>> 0) & 0xFF);
            this.a = ((color >>> 24) & 0xFF);
            if (sprite != null) {
                this.u = sprite.getU(vertex.u());
                this.v = sprite.getV(vertex.v());
            } else {
                this.u = vertex.u();
                this.v = vertex.v();
            }
        }
    }
}
