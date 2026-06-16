package com.zergatul.cheatutils.webui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.mixins.common.accessors.SimpleFeatureRenderPhaseAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.SimpleFeatureRenderPhaseFeatureSubmitsAccessor;
import com.zergatul.cheatutils.mixins.common.accessors.TranslucentFeatureRenderPhaseAccessor;
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
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.feature.phase.TranslucentFeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
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
        extractQuads(collection.translucentBlocksAndItems, output);
        extractQuads(collection.translucentModels, output);
        extractQuads(collection.solid, output);

        // these are not used for block models
        //extractQuads(collection.breakingOverlay, output);
        //extractQuads(collection.waterMask, output);
        //extractQuads(collection.outline, output);
    }

    private void extractQuads(BlockModelFeatureRenderer.Submit submission, List<Quad> output) {
        extractBlockModelQuads(submission.modelParts(), submission.tintLayers(), submission.tintColor(), output);
    }

    private void extractQuads(SimpleFeatureRenderPhase phase, List<Quad> output) {
        for (SimpleFeatureRenderPhase.FeatureSubmits<SubmitNode> submits : ((SimpleFeatureRenderPhaseAccessor) phase).getSubmitsByFeature_CU()) {
            if (submits == null) {
                continue;
            }

            SimpleFeatureRenderPhaseFeatureSubmitsAccessor accessor = (SimpleFeatureRenderPhaseFeatureSubmitsAccessor) submits;
            for (SubmitNode submission : accessor.getUnbatched_CU()) {
                extractQuads(submission, output);
            }
            for (List<SubmitNode> batch : accessor.getBatches_CU().values()) {
                for (SubmitNode submission : batch) {
                    extractQuads(submission, output);
                }
            }
        }
    }

    private void extractQuads(TranslucentFeatureRenderPhase phase, List<Quad> output) {
        for (TranslucentSubmit submission : ((TranslucentFeatureRenderPhaseAccessor) phase).getSubmits_CU()) {
            extractQuads(submission, output);
        }
    }

    private void extractQuads(SubmitNode submission, List<Quad> output) {
        if (submission instanceof BlockModelFeatureRenderer.Submit blockModel) {
            extractQuads(blockModel, output);
        } else if (submission instanceof ModelFeatureRenderer.Submit<?> model) {
            extractQuads(model, output);
        } else {
            ModLoaderBridgeInstance.get().extractQuads(submission, output);
        }
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

    public static void extractBlockModelQuads(List<BlockStateModelPart> parts, int[] tintLayers, int baseTintColor, List<Quad> output) {
        for (BlockStateModelPart part : parts) {
            for (Direction direction : Direction.values()) {
                extractBakedQuads(part.getQuads(direction), tintLayers, baseTintColor, output);
            }
            extractBakedQuads(part.getQuads(null), tintLayers, baseTintColor, output);
        }
    }

    public static void extractBakedQuads(List<BakedQuad> bakedQuads, int[] tintLayers, int baseTintColor, List<Quad> output) {
        for (BakedQuad quad : bakedQuads) {
            output.add(new Quad(quad, tintLayers, baseTintColor));
        }
    }

    public static int getBlockModelQuadColor(int tintIndex, int[] tintLayers, int baseTintColor) {
        if (tintIndex >= 0 && tintIndex < tintLayers.length) {
            return ColorUtils.Int.multiply(baseTintColor, tintLayers[tintIndex]);
        } else {
            return baseTintColor;
        }
    }

    public static class Quad {

        public final String location;
        public final Vertex[] vertices;

        public Quad(BakedQuad quad, int[] tintLayers, int baseTintColor) {
            this.location = quad.materialInfo().sprite().atlasLocation().toString();

            this.vertices = new Vertex[4];
            for (int i = 0; i < 4; i++) {
                this.vertices[i] = new Vertex();
                this.vertices[i].x = quad.position(i).x() - 0.5f;
                this.vertices[i].y = quad.position(i).y() - 0.5f;
                this.vertices[i].z = quad.position(i).z() - 0.5f;

                int color = quad.materialInfo().isTinted() ?
                        getBlockModelQuadColor(quad.materialInfo().tintIndex(), tintLayers, baseTintColor) :
                        baseTintColor;
                this.vertices[i].setColor(color);

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

        public Vertex(float x, float y, float z, int color, float u, float v) {
            this.x = x - 0.5f;
            this.y = y - 0.5f;
            this.z = z - 0.5f;
            setColor(color);
            this.u = u;
            this.v = v;
        }

        public Vertex(PoseStack.Pose pose1, PoseStack.Pose pose2, @Nullable TextureAtlasSprite sprite, ModelPart.Vertex vertex, int color) {
            Vector3f pos = pose1.pose().mul(pose2.pose(), new Matrix4f()).transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
            this.x = pos.x() - 0.5f;
            this.y = pos.y() - 0.5f;
            this.z = pos.z() - 0.5f;
            setColor(color);
            if (sprite != null) {
                this.u = sprite.getU(vertex.u());
                this.v = sprite.getV(vertex.v());
            } else {
                this.u = vertex.u();
                this.v = vertex.v();
            }
        }

        private void setColor(int color) {
            this.r = ColorUtils.Int.r(color);
            this.g = ColorUtils.Int.g(color);
            this.b = ColorUtils.Int.b(color);
            this.a = ColorUtils.Int.a(color);
        }
    }
}