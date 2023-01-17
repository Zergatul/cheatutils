package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SchematicaConfig;
import com.zergatul.cheatutils.schematics.PlacingConverter;
import com.zergatul.cheatutils.schematics.PlacingSettings;
import com.zergatul.cheatutils.schematics.SchemaFile;
import com.zergatul.cheatutils.utils.*;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.BlockUpdateEvent;
import com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SchematicaController {

    public static final SchematicaController instance = new SchematicaController();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Entry> entries = new ArrayList<>();
    private final RandomSource random = new JavaRandom(0);
    private final long[] lastSlotUsage = new long[9];

    private SchematicaController() {
        ModApiWrapper.ScannerChunkLoaded.add(this::onChunkLoaded);
        ModApiWrapper.ScannerBlockUpdated.add(this::onBlockUpdated);
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
        ModApiWrapper.RenderWorldLast.add(this::onRender);

        Arrays.fill(lastSlotUsage, Long.MIN_VALUE);
    }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized void place(SchemaFile file, PlacingSettings placing) {
        Entry entry = new Entry(file, placing);
        entries.add(entry);
        ChunkController.instance.getLoadedChunks().forEach(p -> entry.onChunkLoaded(p.getSecond()));
    }

    private synchronized void onClientTickEnd() {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled || !config.autoBuild) {
            return;
        }

        if (mc.level == null || mc.player == null) {
            return;
        }

        Vec3 pos = mc.player.getEyePosition();
        int xp = (int)Math.round(pos.x);
        int yp = (int)Math.round(pos.y);
        int zp = (int)Math.round(pos.z);
        int distance = (int)Math.round(config.autoBuildDistance) + 1;
        double maxDistanceSqr = config.autoBuildDistance * config.autoBuildDistance;
        ItemStack itemInHand = mc.player.getMainHandItem();

        Block blockInHand;
        if (itemInHand.getItem() instanceof BlockItem blockItem) {
            blockInHand = blockItem.getBlock();
        } else {
            if (config.autoSelectItems) {
                blockInHand = null;
            } else {
                return;
            }
        }

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int dx = -distance; dx <= distance; dx++) {
            for (int dy = -distance; dy <= distance; dy++) {
                for (int dz = -distance; dz <= distance; dz++) {
                    mpos.setX(xp + dx);
                    mpos.setY(yp + dy);
                    mpos.setZ(zp + dz);

                    double blockDx = mpos.getX() + 0.5 - xp;
                    double blockDy = mpos.getY() + 0.5 - yp;
                    double blockDz = mpos.getZ() + 0.5 - zp;
                    if (blockDx * blockDx + blockDy * blockDy + blockDz * blockDz > maxDistanceSqr) {
                        continue;
                    }

                    for (Entry entry : entries) {
                        BlockState state = entry.getBlockState(mpos.getX(), mpos.getY(), mpos.getZ());
                        if (state.isAir()) {
                            continue;
                        }

                        BlockUtils.PlaceBlockPlan plan = BlockUtils.getPlacingPlan(mpos);
                        if (plan == null) {
                            continue;
                        }

                        if (config.autoSelectItems) {
                            if (selectItem(config, state.getBlock()))  {
                                blockInHand = state.getBlock();
                            }
                        }
                        if (blockInHand == state.getBlock() && mc.level.getBlockState(mpos).isAir()) {
                            BlockUtils.applyPlacingPlan(plan);
                            return;
                        }
                    }
                }
            }
        }
    }

    private synchronized void onRender(RenderWorldLastEvent event) {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled) {
            return;
        }

        if (mc.level == null) {
            return;
        }

        Vec3 view = event.getCamera().getPosition();

        if (config.showMissingBlockTracers) {
            Vec3 tracerCenter = event.getTracerCenter();
            double tracerX = tracerCenter.x;
            double tracerY = tracerCenter.y;
            double tracerZ = tracerCenter.z;

            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShaderColor(0.2f, 1.0f, 0.2f, 0.8f);

            for (Entry entry : entries) {
                entry.forEachMissing(view, config.missingBlockTracersMaxDistance, pos -> {
                    bufferBuilder.vertex(tracerX - view.x, tracerY - view.y, tracerZ - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(pos.getX() + 0.5 - view.x, pos.getY() + 0.5 - view.y, pos.getZ() + 0.5 - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                });
            }

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            RenderSystem.disableDepthTest();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);

            SharedVertexBuffer.instance.bind();
            SharedVertexBuffer.instance.upload(bufferBuilder.end());
            SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
            VertexBuffer.unbind();

            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }

        if (config.showMissingBlockCubes) {
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            RenderSystem.setShaderColor(0.2f, 1.0f, 0.2f, 0.8f);

            for (Entry entry : entries) {
                entry.forEachMissing(view, config.missingBlockCubesMaxDistance, pos -> {
                    double x1 = pos.getX() + 0.25 - view.x;
                    double y1 = pos.getY() + 0.25 - view.y;
                    double z1 = pos.getZ() + 0.25 - view.z;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;

                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();

                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();

                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                });
            }

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            RenderSystem.disableDepthTest();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);

            SharedVertexBuffer.instance.bind();
            SharedVertexBuffer.instance.upload(bufferBuilder.end());
            SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
            VertexBuffer.unbind();

            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
            RenderSystem.enableDepthTest();
        }

        if (config.showMissingBlockGhosts) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, 0.6f);

            Map<BlockPos, BlockState> ghosts = new HashMap<>();
            for (Entry entry : entries) {
                entry.forEachMissingState(view, config.missingBlockGhostsMaxDistance, ghosts::put);
            }

            BlockPos.MutableBlockPos neighPos = new BlockPos.MutableBlockPos();
            for (var mapEntry : ghosts.entrySet()) {
                BlockPos pos = mapEntry.getKey();
                BlockState state = mapEntry.getValue();
                BakedModel model = mc.getBlockRenderer().getBlockModel(state);
                for (var direction : Direction.values()) {
                    neighPos.setX(pos.getX() + direction.getStepX());
                    neighPos.setY(pos.getY() + direction.getStepY());
                    neighPos.setZ(pos.getZ() + direction.getStepZ());
                    if (!ghosts.containsKey(neighPos)) {
                        List<BakedQuad> quads = model.getQuads(null, direction, random, ModelData.EMPTY, null);
                        if (quads.size() > 0) {
                            BakedQuad quad = quads.get(0);
                            TextureAtlasSprite sprite = quad.getSprite();
                            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                            RenderSystem.setShaderTexture(0, sprite.atlasLocation());

                            FaceInfo face = FaceInfo.fromFacing(direction);
                            FaceInfo.VertexInfo info;

                            info = face.getVertexInfo(0);
                            bufferBuilder.vertex(
                                            (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                            (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                            (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                    .uv(sprite.getU0(), sprite.getV0()).endVertex();

                            info = face.getVertexInfo(1);
                            bufferBuilder.vertex(
                                            (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                            (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                            (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                    .uv(sprite.getU0(), sprite.getV1()).endVertex();

                            info = face.getVertexInfo(2);
                            bufferBuilder.vertex(
                                            (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                            (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                            (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                    .uv(sprite.getU1(), sprite.getV1()).endVertex();

                            info = face.getVertexInfo(3);
                            bufferBuilder.vertex(
                                            (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                            (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                            (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                    .uv(sprite.getU1(), sprite.getV0()).endVertex();

                            SharedVertexBuffer.instance.bind();
                            SharedVertexBuffer.instance.upload(bufferBuilder.end());
                            SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionTexShader());
                            VertexBuffer.unbind();
                        }
                    }
                }
            }
        }

        /*if (config.showMissingBlockGhosts || config.showMissingBlockTracers || config.showMissingBlockCubes) {
            double missingBlockGhostsMaxDistanceSqr =
                    config.missingBlockGhostsMaxDistance * config.missingBlockGhostsMaxDistance;
            double missingBlockTracersMaxDistanceSqr =
                    config.missingBlockTracersMaxDistance * config.missingBlockTracersMaxDistance;
            double missingBlockCubesMaxDistanceSqr =
                    config.missingBlockCubesMaxDistance * config.missingBlockCubesMaxDistance;

            List<BlockPos> missingBlockTracers = new ArrayList<>();
            List<BlockPos> missingBlockCubes = new ArrayList<>();

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, 0.6f);

            BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
            for (Entry entry : entries) {
                for (var mapEntry : entry.blocks.entrySet()) {
                    BlockPos pos = mapEntry.getKey();
                    Block block = mapEntry.getValue();
                    BlockState state = mc.level.getBlockState(pos);

                    double dx = pos.getX() - view.x;
                    double dy = pos.getY() - view.y;
                    double dz = pos.getZ() - view.z;
                    double distanceSqr = dx * dx + dy * dy + dz * dz;

                    if (state.isAir()) {
                        if (config.showMissingBlockGhosts && distanceSqr < missingBlockGhostsMaxDistanceSqr) {
                            BakedModel model = mc.getBlockRenderer().getBlockModel(block.defaultBlockState());
                            for (var direction : Direction.values()) {
                                mpos.setX(pos.getX() + direction.getStepX());
                                mpos.setY(pos.getY() + direction.getStepY());
                                mpos.setZ(pos.getZ() + direction.getStepZ());
                                if (!entry.blocks.containsKey(mpos)) {
                                    List<BakedQuad> quads = model.getQuads(null, direction, random, ModelData.EMPTY, null);
                                    if (quads.size() > 0) {
                                        BakedQuad quad = quads.get(0);
                                        TextureAtlasSprite sprite = quad.getSprite();
                                        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                                        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                                        RenderSystem.setShaderTexture(0, sprite.atlasLocation());

                                        FaceInfo face = FaceInfo.fromFacing(direction);
                                        FaceInfo.VertexInfo info;

                                        info = face.getVertexInfo(0);
                                        bufferBuilder.vertex(
                                                        (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                                        (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                                        (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                                .uv(sprite.getU0(), sprite.getV0()).endVertex();

                                        info = face.getVertexInfo(1);
                                        bufferBuilder.vertex(
                                                        (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                                        (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                                        (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                                .uv(sprite.getU0(), sprite.getV1()).endVertex();

                                        info = face.getVertexInfo(2);
                                        bufferBuilder.vertex(
                                                        (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                                        (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                                        (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                                .uv(sprite.getU1(), sprite.getV1()).endVertex();

                                        info = face.getVertexInfo(3);
                                        bufferBuilder.vertex(
                                                        (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                                        (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                                        (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                                .uv(sprite.getU1(), sprite.getV0()).endVertex();

                                        SharedVertexBuffer.instance.bind();
                                        SharedVertexBuffer.instance.upload(bufferBuilder.end());
                                        SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionTexShader());
                                        VertexBuffer.unbind();
                                    }
                                }
                            }
                        }

                        if (config.showMissingBlockTracers && distanceSqr < missingBlockTracersMaxDistanceSqr) {
                            missingBlockTracers.add(pos);
                        }

                        if (config.showMissingBlockCubes && distanceSqr < missingBlockCubesMaxDistanceSqr) {
                            missingBlockCubes.add(pos);
                        }
                    }
                }
            }

            if (missingBlockTracers.size() > 0) {
                Vec3 tracerCenter = event.getTracerCenter();
                double tracerX = tracerCenter.x;
                double tracerY = tracerCenter.y;
                double tracerZ = tracerCenter.z;

                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                RenderSystem.setShaderColor(0.2f, 1.0f, 0.2f, 0.8f);

                for (BlockPos pos : missingBlockTracers) {
                    bufferBuilder.vertex(tracerX - view.x, tracerY - view.y, tracerZ - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(pos.getX() + 0.5 - view.x, pos.getY() + 0.5 - view.y, pos.getZ() + 0.5 - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                }

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                SharedVertexBuffer.instance.bind();
                SharedVertexBuffer.instance.upload(bufferBuilder.end());
                SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();

                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            if (missingBlockCubes.size() > 0) {
                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                RenderSystem.setShaderColor(0.2f, 1.0f, 0.2f, 0.8f);

                for (BlockPos pos : missingBlockCubes) {
                    double x1 = pos.getX() + 0.25 - view.x;
                    double y1 = pos.getY() + 0.25 - view.y;
                    double z1 = pos.getZ() + 0.25 - view.z;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;

                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();

                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();

                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                }

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                SharedVertexBuffer.instance.bind();
                SharedVertexBuffer.instance.upload(bufferBuilder.end());
                SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();

                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }

        if (config.showWrongBlockTracers || config.showWrongBlockCubes) {
            double wrongBlockTracersMaxDistanceSqr =
                    config.wrongBlockTracersMaxDistance * config.wrongBlockTracersMaxDistance;
            double wrongBlockCubesMaxDistanceSqr =
                    config.wrongBlockCubesMaxDistance * config.wrongBlockCubesMaxDistance;
            double maxCheck = Math.max(wrongBlockTracersMaxDistanceSqr, wrongBlockCubesMaxDistanceSqr);

            List<BlockPos> wrongBlockTracers = new ArrayList<>();
            List<BlockPos> wrongBlockCubes = new ArrayList<>();

            BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
            for (Entry entry: entries) {
                int x1 = entry.x1;
                int x2 = entry.x2;
                int y1 = entry.y1;
                int y2 = entry.y2;
                int z1 = entry.z1;
                int z2 = entry.z2;
                for (int x = x1; x < x2; x++) {
                    mpos.setX(x);
                    double dx = x - view.x;
                    double dxSqr = dx * dx;
                    for (int y = y1; y < y2; y++) {
                        mpos.setY(y);
                        double dy = y - view.y;
                        double dySqr = dy * dy;
                        for (int z = z1; z < z2; z++) {
                            mpos.setZ(z);
                            double dz = z - view.z;
                            double dzSqr = dz * dz;
                            double distanceSqr = dxSqr + dySqr + dzSqr;
                            if (distanceSqr > maxCheck) {
                                continue;
                            }

                            BlockState state = mc.level.getBlockState(mpos);
                            if (state.isAir()) {
                                continue;
                            }
                            Block block = entry.blocks.getOrDefault(mpos, Blocks.AIR);
                            if (state.getBlock() != block) {
                                if (distanceSqr < wrongBlockTracersMaxDistanceSqr) {
                                    wrongBlockTracers.add(mpos.immutable());
                                }
                                if (distanceSqr < wrongBlockCubesMaxDistanceSqr) {
                                    wrongBlockCubes.add(mpos.immutable());
                                }
                            }
                        }
                    }
                }
            }

            if (wrongBlockTracers.size() > 0) {
                Vec3 tracerCenter = event.getTracerCenter();
                double tracerX = tracerCenter.x;
                double tracerY = tracerCenter.y;
                double tracerZ = tracerCenter.z;

                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                RenderSystem.setShaderColor(1.0f, 0.2f, 0.2f, 0.8f);

                for (BlockPos pos : wrongBlockTracers) {
                    bufferBuilder.vertex(tracerX - view.x, tracerY - view.y, tracerZ - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(pos.getX() + 0.5 - view.x, pos.getY() + 0.5 - view.y, pos.getZ() + 0.5 - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                }

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                SharedVertexBuffer.instance.bind();
                SharedVertexBuffer.instance.upload(bufferBuilder.end());
                SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();

                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            if (wrongBlockCubes.size() > 0) {
                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                RenderSystem.setShaderColor(1.0f, 0.2f, 0.2f, 0.8f);

                for (BlockPos pos : wrongBlockCubes) {
                    double x1 = pos.getX() + 0.25 - view.x;
                    double y1 = pos.getY() + 0.25 - view.y;
                    double z1 = pos.getZ() + 0.25 - view.z;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;

                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();

                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();

                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                }

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                SharedVertexBuffer.instance.bind();
                SharedVertexBuffer.instance.upload(bufferBuilder.end());
                SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();

                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }*/
    }

    private synchronized void onChunkLoaded(LevelChunk chunk) {
        for (Entry entry : entries) {
            entry.onChunkLoaded(chunk);
        }
    }

    private synchronized void onBlockUpdated(BlockUpdateEvent event) {
        for (Entry entry : entries) {
            entry.onBlockUpdated(event);
        }
    }

    private boolean selectItem(SchematicaConfig config, Block block) {
        Inventory inventory = mc.player.getInventory();

        // search on hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() == block) {
                    lastSlotUsage[i] = System.nanoTime();
                    inventory.selected = i;
                    return true;
                }
            }
        }

        if (config.autoSelectSlots.length == 0) {
            return false;
        }

        // search in inventory
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() == block) {
                    long minTime = Long.MAX_VALUE;
                    int minSlot = config.autoSelectSlots[0];
                    for (int slot : config.autoSelectSlots) {
                        if (lastSlotUsage[slot - 1] < minTime) {
                            minTime = lastSlotUsage[slot - 1];
                            minSlot = slot - 1;
                        }
                    }

                    InventoryUtils.moveItemStack(new InventorySlot(i), new InventorySlot(minSlot));
                    lastSlotUsage[minSlot] = System.nanoTime();
                    inventory.selected = minSlot;
                    return true;
                }
            }
        }

        return false;
    }

    private static class Entry {

        public final int x1, x2, y1, y2, z1, z2;
        public final Map<Long, Chunk> chunks;

        public Entry(SchemaFile file, PlacingSettings placing) {
            PlacingConverter converter = new PlacingConverter(placing, file.getWidth(), file.getHeight(), file.getLength());

            x1 = placing.x;
            x2 = x1 + converter.getWidth();
            y1 = placing.y;
            y2 = y1 + converter.getHeight();
            z1 = placing.z;
            z2 = z1 + converter.getLength();

            chunks = new HashMap<>();
            for (int x = 0; x < file.getWidth(); x++) {
                for (int y = 0; y < file.getHeight(); y++) {
                    for (int z = 0; z < file.getLength(); z++) {
                        BlockState state = file.getBlockState(x, y, z);
                        if (!state.isAir()) {
                            PlacingConverter.Vec3iMutable vec = converter.convert(x, y, z);
                            int wx = x1 + vec.x;
                            int wy = y1 + vec.y;
                            int wz = z1 + vec.z;
                            long chunkIndex = blockToChunkIndex(wx, wz);
                            Chunk chunk = chunks.get(chunkIndex);
                            if (chunk == null) {
                                chunk = new Chunk(wx & 0xFFFFFFF0, wz & 0xFFFFFFF0);
                                chunks.put(chunkIndex, chunk);
                            }
                            chunk.setBlockState(wx & 0x0F, wy, wz & 0x0F, state);
                        }
                    }
                }
            }
        }

        public void forEachMissing(Vec3 view, double distance, Consumer<BlockPos> callback) {
            double chunkDistance2 = (distance + 23) * (distance + 23);
            double distance2 = distance * distance;
            for (Chunk chunk : chunks.values()) {
                if (chunk.getDistanceSqrTo(view) > chunkDistance2) {
                    continue;
                }

                for (ChunkSection section : chunk.sections) {
                    if (section == null) {
                        continue;
                    }
                    if (section.getDistanceSqrTo(view) > chunkDistance2) {
                        continue;
                    }

                    for (BlockPos pos : section.missing) {
                        if (pos.distToCenterSqr(view) < distance2) {
                            callback.accept(pos);
                        }
                    }
                }
            }
        }

        public void forEachMissingState(Vec3 view, double distance, BiConsumer<BlockPos, BlockState> callback) {
            double chunkDistance2 = (distance + 23) * (distance + 23);
            double distance2 = distance * distance;
            for (Chunk chunk : chunks.values()) {
                if (chunk.getDistanceSqrTo(view) > chunkDistance2) {
                    continue;
                }

                for (ChunkSection section : chunk.sections) {
                    if (section == null) {
                        continue;
                    }
                    if (section.getDistanceSqrTo(view) > chunkDistance2) {
                        continue;
                    }

                    for (BlockPos pos : section.missing) {
                        if (pos.distToCenterSqr(view) < distance2) {
                            callback.accept(pos, section.getBlockState(pos.getX() & 0x0F, pos.getY() & 0x0F, pos.getZ() & 0x0F));
                        }
                    }
                }
            }
        }

        public BlockState getBlockState(int x, int y, int z) {
            long chunkIndex = blockToChunkIndex(x, z);
            Chunk chunk = chunks.get(chunkIndex);
            if (chunk == null) {
                return Blocks.AIR.defaultBlockState();
            } else {
                return chunk.getBlockState(x & 0x0F, y, z & 0x0F);
            }
        }

        public void onChunkLoaded(LevelChunk levelChunk) {
            long chunkIndex = chunkToChunkIndex(levelChunk);
            Chunk chunk = chunks.get(chunkIndex);
            if (chunk != null) {
                chunk.onChunkLoaded(levelChunk);
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            long chunkIndex = blockToChunkIndex(event.pos().getX(), event.pos().getZ());
            Chunk chunk = chunks.get(chunkIndex);
            if (chunk != null) {
                chunk.onBlockUpdated(event);
            }
        }

        private long blockToChunkIndex(int x, int z) {
            x = SectionPos.blockToSectionCoord(x);
            z = SectionPos.blockToSectionCoord(z);
            return ChunkPos.asLong(x, z);
        }

        private long chunkToChunkIndex(LevelChunk chunk) {
            return ChunkPos.asLong(chunk.getPos().x, chunk.getPos().z);
        }
    }

    private static class Chunk {

        private static final int MIN_Y = -64;
        private static final int MAX_Y = 320;
        private static final int MIN_SECTION_Y = MIN_Y >> 4;
        private static final int MAX_SECTION_Y = MAX_Y >> 4;

        private final int minX;
        private final int minZ;
        public final ChunkSection[] sections;

        public Chunk(int x, int z) {
            minX = x;
            minZ = z;
            sections = new ChunkSection[MAX_SECTION_Y - MIN_SECTION_Y];
        }

        public BlockState getBlockState(int x, int y, int z) {
            int sectionIndex = (y - MIN_Y) >> 4;
            ChunkSection section = sections[sectionIndex];
            if (section == null) {
                return Blocks.AIR.defaultBlockState();
            } else {
                return section.getBlockState(x, y & 0x0F, z);
            }
        }

        public double getDistanceSqrTo(Vec3 point) {
            double dx = point.x - (minX + 8);
            double dz = point.z - (minZ + 8);
            return dx * dx + dz * dz;
        }

        public void onChunkLoaded(LevelChunk chunk) {
            for (int i = 0; i < sections.length; i++) {
                if (sections[i] != null) {
                    sections[i].onChunkLoaded(chunk);
                }
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            int sectionIndex = (event.pos().getY() - MIN_Y) >> 4;
            ChunkSection section = sections[sectionIndex];
            if (section != null) {
                section.onBlockUpdated(event);
            }
        }

        public void setBlockState(int x, int y, int z, BlockState state) {
            int sectionIndex = (y - MIN_Y) >> 4;
            if (sections[sectionIndex] == null) {
                sections[sectionIndex] = new ChunkSection(minX, MIN_Y + (sectionIndex << 4), minZ);
            }
            sections[sectionIndex].setBlockState(x, y & 0x0F, z, state);
        }
    }

    private static class ChunkSection {

        private final int minX;
        private final int minY;
        private final int minZ;
        private final PalettedContainer<BlockState> states;
        private final List<BlockPos> missing = new ArrayList<>();
        private final List<BlockPos> wrong = new ArrayList<>();

        public ChunkSection(int x, int y, int z) {
            minX = x;
            minY = y;
            minZ = z;
            states = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
        }

        public BlockState getBlockState(int x, int y, int z) {
            return states.get(x, y, z);
        }

        public double getDistanceSqrTo(Vec3 point) {
            double dx = point.x - (minX + 8);
            double dy = point.y - (minY + 8);
            double dz = point.z - (minZ + 8);
            return dx * dx + dy * dy + dz * dz;
        }

        public void onChunkLoaded(LevelChunk chunk) {
            missing.clear();
            wrong.clear();

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = 0; x < 16; x++) {
                pos.setX(x);
                for (int y = 0; y < 16; y++) {
                    pos.setY(minY | y);
                    for (int z = 0; z < 16; z++) {
                        pos.setZ(z);
                        BlockState chunkState = chunk.getBlockState(pos);
                        BlockState finalState = states.get(x, y, z);
                        if (chunkState.isAir() && !finalState.isAir()) {
                            missing.add(new BlockPos(minX | x, minY | y, minZ | z));
                        }
                        if (!chunkState.isAir() && chunkState != finalState) {
                            wrong.add(new BlockPos(minX | x, minY | y, minZ | z));
                        }
                    }
                }
            }
        }

        public void onBlockUpdated(BlockUpdateEvent event) {
            BlockPos pos = event.pos();
            missing.removeIf(p -> p.equals(pos));
            wrong.removeIf(p -> p.equals(pos));
            BlockState chunkState = event.state();
            BlockState finalState = states.get(pos.getX() & 0x0F, pos.getY() & 0x0F, pos.getZ() & 0x0F);
            if (chunkState.isAir() && !finalState.isAir()) {
                missing.add(pos.immutable());
            }
            if (!chunkState.isAir() && chunkState != finalState) {
                wrong.add(pos.immutable());
            }
        }

        public void setBlockState(int x, int y, int z, BlockState state) {
            states.set(x, y, z, state); // locking can be slow?
        }
    }
}