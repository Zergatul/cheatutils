package com.zergatul.cheatutils.webui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NullMarked;

import java.util.*;

public class BlockModelApi extends ApiBase {

    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public String getRoute() {
        return "block-model";
    }

    @Override
    public String get(String id) throws ApiException {
        Identifier loc = Identifier.parse(id);
        Block block = Registries.BLOCKS.getValue(loc);
        if (block == null) {
            throw new ApiException("Cannot find block by id.", HttpResponseCodes.NOT_FOUND);
        }

        // TODO: not working!!!
//        List<Quad> quads = getFromBlockModel(block);
//        if (quads.isEmpty()) {
//            quads = getFromItemRenderer(block);
//        }

        return gson.toJson(List.of());
    }

    private List<Quad> getFromBlockModel(Block block) {
        BlockState blockState = block.defaultBlockState();
        BlockModel model = Minecraft.getInstance().getModelManager().getBlockModelSet().get(blockState);
        BlockModelRenderState renderState = new BlockModelRenderState();
        model.update(renderState, blockState, BlockDisplayContext.create(), 0);

        InMemoryNodeCollector collector = new InMemoryNodeCollector();
        renderState.submit(new PoseStack(), collector, 0, 0, 0);

        return extractQuads(collector);
    }

    private List<Quad> getFromItemRenderer(Block block) {
        ItemStack stack = new ItemStack(block.asItem());
        InMemoryNodeCollector collector = new InMemoryNodeCollector();

        ItemStackRenderState state = new ItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(state, stack, ItemDisplayContext.GROUND, null, null, 0);
        state.submit(new PoseStack(), collector, 15728880, OverlayTexture.NO_OVERLAY, 0);

        return extractQuads(collector);
    }

    private List<Quad> extractQuads(InMemoryNodeCollector collector) {
        List<Quad> result = new ArrayList<>();

        PoseStack poseStack = new PoseStack();

        for (SubmitNodeStorage.BlockModelSubmit submission : collector.blockModelSubmits) {
            for (BlockStateModelPart part : submission.modelParts()) {
                for (Direction direction : Direction.values()) {
                    for (BakedQuad quad : part.getQuads(direction)) {
                        result.add(new Quad(quad, submission.tintLayers()));
                    }
                }
                for (BakedQuad quad : part.getQuads(null)) {
                    result.add(new Quad(quad, submission.tintLayers()));
                }
            }
        }

        for (SubmitNodeStorage.ModelSubmit<?> submission : collector.modelSubmits) {
            if (submission.sprite() == null) {
                continue;
            }

            for (ModelPart part : submission.model().allParts()) {
                part.visit(poseStack, (pose, str, i, cube) -> {
                    for (ModelPart.Polygon polygon : cube.polygons) {
                        result.add(new Quad(
                                submission.sprite().atlasLocation().toString(),
                                new Vertex(submission.pose(), pose, submission.sprite(), polygon.vertices()[0], submission.tintedColor()),
                                new Vertex(submission.pose(), pose, submission.sprite(), polygon.vertices()[1], submission.tintedColor()),
                                new Vertex(submission.pose(), pose, submission.sprite(), polygon.vertices()[2], submission.tintedColor()),
                                new Vertex(submission.pose(), pose, submission.sprite(), polygon.vertices()[3], submission.tintedColor())));
                    }
                });
            }
        }

        /*for (SubmitNodeStorage.ModelPartSubmit submission : collector.modelPartSubmits) {
            if (submission.sprite() == null) {
                continue;
            }

            submission.modelPart().visit(poseStack, (pose, str, i, cube) -> {
                for (ModelPart.Polygon polygon : cube.polygons) {
                    result.add(new Quad(
                            submission.sprite().atlasLocation().toString(),
                            new Vertex(submission.pose(), pose, submission.sprite(), polygon.vertices()[0], submission.tintedColor()),
                            new Vertex(submission.pose(), pose, submission.sprite(), polygon.vertices()[1], submission.tintedColor()),
                            new Vertex(submission.pose(), pose, submission.sprite(), polygon.vertices()[2], submission.tintedColor()),
                            new Vertex(submission.pose(), pose, submission.sprite(), polygon.vertices()[3], submission.tintedColor())));
                }
            });
        }*/

        for (SubmitNodeStorage.ItemSubmit submission : collector.itemSubmits) {
            for (BakedQuad quad : submission.quads()) {
                result.add(new Quad(quad, submission.tintLayers()));
            }
        }

        return result;
    }

    @NullMarked
    private static class InMemoryNodeCollector implements SubmitNodeCollector {

        public final List<SubmitNodeStorage.BlockModelSubmit> blockModelSubmits = new ArrayList<>();
        public final List<SubmitNodeStorage.ModelSubmit<?>> modelSubmits = new ArrayList<>();
        public final List<SubmitNodeStorage.ItemSubmit> itemSubmits = new ArrayList<>();

        @Override
        public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {
            throw new AssertionError();
        }

        @Override
        public void submitNameTag(PoseStack poseStack, @org.jspecify.annotations.Nullable Vec3 nameTagAttachment, int offset, Component name, boolean seeThrough, int lightCoords, double distanceToCameraSq, CameraRenderState camera) {
            throw new AssertionError();
        }

        @Override
        public void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow, Font.DisplayMode displayMode, int lightCoords, int color, int backgroundColor, int outlineColor) {
            throw new AssertionError();
        }

        @Override
        public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {
            throw new AssertionError();
        }

        @Override
        public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
            throw new AssertionError();
        }

        @Override
        public <S> void submitModel(
                Model<? super S> model,
                S state,
                PoseStack poseStack,
                RenderType renderType,
                int lightCoords,
                int overlayCoords,
                int tintedColor,
                @Nullable TextureAtlasSprite sprite,
                int outlineColor,
                @Nullable final ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
        ) {
            this.modelSubmits.add(new SubmitNodeStorage.ModelSubmit<>(
                    poseStack.last().copy(),
                    model,
                    state,
                    lightCoords,
                    overlayCoords,
                    tintedColor,
                    sprite,
                    outlineColor,
                    crumblingOverlay));
        }

        @Override
        public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {
            throw new AssertionError();
        }

        @Override
        public void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> parts, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
            this.blockModelSubmits.add(new SubmitNodeStorage.BlockModelSubmit(
                    poseStack.last().copy(),
                    renderType,
                    parts,
                    tintLayers,
                    lightCoords,
                    overlayCoords,
                    outlineColor));
        }

        @Override
        public void submitBreakingBlockModel(PoseStack poseStack, BlockStateModel model, long seed, int progress) {
            throw new AssertionError();
        }

        @Override
        public void submitShapeOutline(PoseStack poseStack, VoxelShape shape, RenderType renderType, int color, float width, boolean afterTerrain) {
            throw new AssertionError();
        }

        @Override
        public void submitItem(
                PoseStack poseStack,
                ItemDisplayContext displayContext,
                int lightCoords,
                int overlayCoords,
                int outlineColor,
                int[] tintLayers,
                List<BakedQuad> quads,
                ItemStackRenderState.FoilType foilType
        ) {
            itemSubmits.add(new SubmitNodeStorage.ItemSubmit(poseStack.last().copy(), displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads, foilType));
        }

        @Override
        public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer customGeometryRenderer) {
            throw new AssertionError();
        }

        @Override
        public void submitParticleGroup(ParticleGroupRenderer particleGroupRenderer) {
            throw new AssertionError();
        }

        @Override
        public OrderedSubmitNodeCollector order(int order) {
            throw new AssertionError();
        }
    }

    public static class Quad {

        public final String location;
        public final Vertex[] vertices;

        public Quad(BakedQuad quad, BlockState state) {
            this.location = quad.materialInfo().sprite().atlasLocation().toString();

            this.vertices = new Vertex[4];
            for (int i = 0; i < 4; i++) {
                this.vertices[i] = new Vertex();
                this.vertices[i].x = quad.position(i).x() - 0.5f;
                this.vertices[i].y = quad.position(i).y() - 0.5f;
                this.vertices[i].z = quad.position(i).z() - 0.5f;

                int color = quad.materialInfo().isTinted() ?
                        Minecraft.getInstance().getBlockColors().getTintSource(state, 0).color(state) :
                        -1;
                this.vertices[i].r = color & 0xFF;
                this.vertices[i].g = (color >> 8) & 0xFF;
                this.vertices[i].b = (color >> 16) & 0xFF;
                this.vertices[i].a = quad.materialInfo().isTinted() ? 255 : (color >> 24) & 0xFF;

                this.vertices[i].u = UVPair.unpackU(quad.packedUV(i));
                this.vertices[i].v = UVPair.unpackV(quad.packedUV(i));
            }
        }

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

        public Vertex(PoseStack.Pose pose1, PoseStack.Pose pose2, TextureAtlasSprite sprite, ModelPart.Vertex vertex, int color) {
            Vector3f pos = pose1.pose().mul(pose2.pose(), new Matrix4f()).transformPosition(vertex.worldX(), vertex.worldY(), vertex.worldZ(), new Vector3f());
            this.x = pos.x() - 0.5f;
            this.y = pos.y() - 0.5f;
            this.z = pos.z() - 0.5f;
            this.r = ((color >>> 16) & 0xFF);
            this.g = ((color >>> 8) & 0xFF);
            this.b = ((color >>> 0) & 0xFF);
            this.a = ((color >>> 24) & 0xFF);
            this.u = sprite.getU(vertex.u());
            this.v = sprite.getV(vertex.v());
        }
    }
}