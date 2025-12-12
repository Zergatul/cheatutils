package com.zergatul.cheatutils.webui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.utils.JavaRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class BlockModelApi extends ApiBase {

    private final Minecraft mc = Minecraft.getInstance();
    private final JavaRandom random = new JavaRandom(0);

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

        List<Quad> quads = getFromBlockModel(block);
        if (quads.isEmpty()) {
            quads = getFromItemRenderer(block);
        }

        return gson.toJson(quads);
    }

    private List<Quad> getFromBlockModel(Block block) {
        List<Quad> result = new ArrayList<>();

        BlockState state = block.defaultBlockState();
        BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        List<BlockModelPart> parts = model.collectParts(random);
        for (BlockModelPart part : parts) {
            for (Direction direction : Direction.values()) {
                List<BakedQuad> quads = part.getQuads(direction);
                for (BakedQuad quad : quads) {
                    result.add(new Quad(quad, state));
                }
            }

            List<BakedQuad> quads = part.getQuads(null);
            for (BakedQuad quad : quads) {
                result.add(new Quad(quad, state));
            }
        }

        return result;
    }

    private List<Quad> getFromItemRenderer(Block block) {
        List<Quad> result = new ArrayList<>();

        PoseStack poseStack = new PoseStack();
        ItemStack stack = new ItemStack(block);
        InMemoryNodeCollector collector = new InMemoryNodeCollector();

        ItemStackRenderState state = new ItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(state, stack, ItemDisplayContext.GROUND, null, null, 0);
        state.submit(poseStack, collector, 15728880, OverlayTexture.NO_OVERLAY, 0);

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

        for (SubmitNodeStorage.ModelPartSubmit submission : collector.modelPartSubmits) {
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
        }

        for (SubmitNodeStorage.ItemSubmit submission : collector.itemSubmits) {
            for (BakedQuad quad : submission.quads()) {
                result.add(new Quad(quad));
            }
        }

        /*Vector3f min = new Vector3f(1000, 1000, 1000);
        Vector3f max = new Vector3f(-1000, -1000, -1000);
        result.forEach(quad -> {
            Arrays.stream(quad.vertices).forEach(vertex -> {
                if (vertex.x < min.x) {
                    min.x = vertex.x;
                }
                if (vertex.y < min.y) {
                    min.y = vertex.y;
                }
                if (vertex.z < min.z) {
                    min.z = vertex.z;
                }
                if (vertex.x > max.x) {
                    max.x = vertex.x;
                }
                if (vertex.y > max.y) {
                    max.y = vertex.y;
                }
                if (vertex.z > max.z) {
                    max.z = vertex.z;
                }
            });
        });

        System.out.println("MIN =" + min.toString(new DecimalFormat("0.000")));
        System.out.println("MAX =" + max.toString(new DecimalFormat("0.000")));*/

        return result;
    }

    private static class InMemoryNodeCollector implements SubmitNodeCollector {

        public final List<SubmitNodeStorage.ModelSubmit<?>> modelSubmits = new ArrayList<>();
        public final List<SubmitNodeStorage.ModelPartSubmit> modelPartSubmits = new ArrayList<>();
        public final List<SubmitNodeStorage.ItemSubmit> itemSubmits = new ArrayList<>();

        @Override
        public OrderedSubmitNodeCollector order(int i) {
            throw new AssertionError();
        }

        @Override
        public void submitShadow(PoseStack poseStack, float f, List<EntityRenderState.ShadowPiece> list) {
            throw new AssertionError();
        }

        @Override
        public void submitNameTag(PoseStack poseStack, @Nullable Vec3 vec3, int i, Component component, boolean bl, int j, double d, CameraRenderState cameraRenderState) {
            throw new AssertionError();
        }

        @Override
        public void submitText(PoseStack poseStack, float f, float g, FormattedCharSequence formattedCharSequence, boolean bl, Font.DisplayMode displayMode, int i, int j, int k, int l) {
            throw new AssertionError();
        }

        @Override
        public void submitFlame(PoseStack poseStack, EntityRenderState entityRenderState, Quaternionf quaternionf) {
            throw new AssertionError();
        }

        @Override
        public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
            throw new AssertionError();
        }

        @Override
        public <S> void submitModel(Model<? super S> model, S object, PoseStack poseStack, RenderType renderType, int i, int j, int k, @Nullable TextureAtlasSprite textureAtlasSprite, int l, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
            this.modelSubmits.add(new SubmitNodeStorage.ModelSubmit<>(
                    poseStack.last().copy(),
                    model,
                    object,
                    i,
                    j,
                    k,
                    textureAtlasSprite,
                    l,
                    crumblingOverlay));
        }

        @Override
        public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite, boolean bl, boolean bl2, int k, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int l) {
            this.modelPartSubmits.add(new SubmitNodeStorage.ModelPartSubmit(
                    poseStack.last().copy(),
                    modelPart,
                    i,
                    j,
                    textureAtlasSprite,
                    bl,
                    bl2,
                    k,
                    crumblingOverlay,
                    l));
        }

        @Override
        public void submitBlock(PoseStack poseStack, BlockState blockState, int i, int j, int k) {
            throw new AssertionError();
        }

        @Override
        public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {
            throw new AssertionError();
        }

        @Override
        public void submitBlockModel(PoseStack poseStack, RenderType renderType, BlockStateModel blockStateModel, float f, float g, float h, int i, int j, int k) {
            throw new AssertionError();
        }

        @Override
        public void submitItem(PoseStack poseStack, ItemDisplayContext itemDisplayContext, int i, int j, int k, int[] is, List<BakedQuad> list, RenderType renderType, ItemStackRenderState.FoilType foilType) {
            itemSubmits.add(new SubmitNodeStorage.ItemSubmit(poseStack.last().copy(), itemDisplayContext, i, j, k, is, list, renderType, foilType));
        }

        @Override
        public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, CustomGeometryRenderer customGeometryRenderer) {
            throw new AssertionError();
        }

        @Override
        public void submitParticleGroup(ParticleGroupRenderer particleGroupRenderer) {
            throw new AssertionError();
        }
    }

    public static class Quad {

        public final String location;
        public final Vertex[] vertices;

        public Quad(BakedQuad quad, BlockState state) {
            this.location = quad.sprite().atlasLocation().toString();

            this.vertices = new Vertex[4];
            for (int i = 0; i < 4; i++) {
                this.vertices[i] = new Vertex();
                this.vertices[i].x = quad.position(i).x() - 0.5f;
                this.vertices[i].y = quad.position(i).y() - 0.5f;
                this.vertices[i].z = quad.position(i).z() - 0.5f;

                int color = quad.isTinted() ?
                        Minecraft.getInstance().getBlockColors().getColor(state, null, null, 0) :
                        -1;
                this.vertices[i].r = color & 0xFF;
                this.vertices[i].g = (color >> 8) & 0xFF;
                this.vertices[i].b = (color >> 16) & 0xFF;
                this.vertices[i].a = quad.isTinted() ? 255 : (color >> 24) & 0xFF;

                this.vertices[i].u = UVPair.unpackU(quad.packedUV(i));
                this.vertices[i].v = UVPair.unpackV(quad.packedUV(i));
            }
        }

        public Quad(BakedQuad quad) {
            this.location = quad.sprite().atlasLocation().toString();

            this.vertices = new Vertex[4];
            for (int i = 0; i < 4; i++) {
                this.vertices[i] = new Vertex();
                this.vertices[i].x = quad.position(i).x() - 0.5f;
                this.vertices[i].y = quad.position(i).y() - 0.5f;
                this.vertices[i].z = quad.position(i).z() - 0.5f;

                this.vertices[i].r = 255;
                this.vertices[i].g = 255;
                this.vertices[i].b = 255;
                this.vertices[i].a = 255;

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
            this.x = pos.x() * 4;
            this.y = (pos.y() - 0.1875f) * 4;
            this.z = pos.z() * 4;
            this.r = ((color >>> 16) & 0xFF);
            this.g = ((color >>> 8) & 0xFF);
            this.b = ((color >>> 0) & 0xFF);
            this.a = ((color >>> 24) & 0xFF);
            this.u = sprite.getU(vertex.u());
            this.v = sprite.getV(vertex.v());
        }
    }
}