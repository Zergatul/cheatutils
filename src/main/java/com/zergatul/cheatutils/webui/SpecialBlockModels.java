package com.zergatul.cheatutils.webui;

import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelBanner;
import net.minecraft.client.model.ModelBed;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.model.ModelShulker;
import net.minecraft.client.renderer.entity.RenderShulker;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;

public class SpecialBlockModels {

    public static final ResourceLocation BANNER_TEXTURE = new ResourceLocation("cheatutils:textures/preview/banner.png");

    public static List<BlockModelApi.Quad> getQuads(Block block) {
        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST) {
            ModelChest model = new ModelChest();
            String texture = block == Blocks.ENDER_CHEST ? "ender" :
                    block == Blocks.TRAPPED_CHEST ? "trapped" : "normal";
            // Closed single chest, facing north like the default block state.
            return ModelQuadExporter.export("minecraft:textures/entity/chest/" + texture + ".png",
                    v -> new Vec3d(0.5 - v.x, 0.5 - v.y, v.z - 0.5),
                    model.chestLid, model.chestBelow, model.chestKnob);
        }
        if (block instanceof BlockShulkerBox) {
            ModelShulker model = new ModelShulker();
            String texture = RenderShulker.SHULKER_ENDERGOLEM_TEXTURE[
                    ((BlockShulkerBox) block).getColor().getMetadata()].toString();
            // Closed, upward-facing box. The entity model's head is not part of the block.
            // Match the renderer's small inset about model-space y=1.
            return ModelQuadExporter.export(texture,
                    v -> new Vec3d(v.x * 0.9995, (1 - v.y) * 0.9995, -v.z * 0.9995),
                    model.base, model.lid);
        }
        if (block == Blocks.STANDING_SIGN || block == Blocks.WALL_SIGN) {
            ModelSign model = new ModelSign();
            boolean standing = block == Blocks.STANDING_SIGN;
            model.signStick.showModel = standing;
            return ModelQuadExporter.export("minecraft:textures/entity/sign.png",
                    v -> standing ? new Vec3d(v.x * 2 / 3, -v.y * 2 / 3, -v.z * 2 / 3) :
                            new Vec3d(-v.x * 2 / 3, -0.3125 - v.y * 2 / 3, 0.4375 + v.z * 2 / 3),
                    model.signBoard, model.signStick);
        }
        if (block == Blocks.SKULL) {
            // Skull type and rotation live in the tile entity, not the block ID.
            ModelSkeletonHead model = new ModelSkeletonHead(0, 0, 64, 32);
            return ModelQuadExporter.export("minecraft:textures/entity/skeleton/skeleton.png",
                    v -> new Vec3d(-v.x, -0.5 - v.y, v.z), model.skeletonHead);
        }
        if (block == Blocks.BED) {
            return getBedQuads();
        }
        if (block == Blocks.STANDING_BANNER || block == Blocks.WALL_BANNER) {
            ModelBanner model = new ModelBanner();
            boolean standing = block == Blocks.STANDING_BANNER;
            model.bannerStand.showModel = standing;
            model.bannerSlate.rotationPointY = -32;
            // Freeze the cloth at the renderer's time-zero pose.
            model.bannerSlate.rotateAngleX = -0.0025F * (float) Math.PI;
            return fitPreview(ModelQuadExporter.export(BANNER_TEXTURE.toString(),
                    v -> standing ? new Vec3d(v.x * 2 / 3, -v.y * 2 / 3, -v.z * 2 / 3) :
                            new Vec3d(-v.x * 2 / 3, -2.0 / 3 - 0.3125 - v.y * 2 / 3, 0.4375 + v.z * 2 / 3),
                    model.bannerSlate, model.bannerStand, model.bannerTop));
        }
        return Collections.emptyList();
    }

    private static List<BlockModelApi.Quad> getBedQuads() {
        ModelBed model = new ModelBed();
        String texture = "minecraft:textures/entity/bed/red.png";
        // Render both halves, using the north-facing renderer transform Rx(90).
        model.preparePiece(true);
        List<BlockModelApi.Quad> quads = ModelQuadExporter.export(texture,
                v -> new Vec3d(v.x - 0.5, 0.0625 - v.z, v.y - 1),
                model.headPiece, model.footPiece, model.legs[0], model.legs[1], model.legs[2], model.legs[3]);
        model.preparePiece(false);
        quads.addAll(ModelQuadExporter.export(texture,
                v -> new Vec3d(v.x - 0.5, 0.0625 - v.z, v.y),
                model.headPiece, model.footPiece, model.legs[0], model.legs[1], model.legs[2], model.legs[3]));
        return fitPreview(quads);
    }

    private static List<BlockModelApi.Quad> fitPreview(List<BlockModelApi.Quad> quads) {
        float minX = Float.POSITIVE_INFINITY, minY = minX, minZ = minX;
        float maxX = Float.NEGATIVE_INFINITY, maxY = maxX, maxZ = maxX;
        for (BlockModelApi.Quad quad : quads) {
            for (BlockModelApi.Vertex vertex : quad.vertices) {
                minX = Math.min(minX, vertex.x);
                minY = Math.min(minY, vertex.y);
                minZ = Math.min(minZ, vertex.z);
                maxX = Math.max(maxX, vertex.x);
                maxY = Math.max(maxY, vertex.y);
                maxZ = Math.max(maxZ, vertex.z);
            }
        }
        float scale = 1 / Math.max(1, Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ)));
        for (BlockModelApi.Quad quad : quads) {
            for (BlockModelApi.Vertex vertex : quad.vertices) {
                vertex.x = (vertex.x - (minX + maxX) / 2) * scale;
                vertex.y = (vertex.y - (minY + maxY) / 2) * scale;
                vertex.z = (vertex.z - (minZ + maxZ) / 2) * scale;
            }
        }
        return quads;
    }
}